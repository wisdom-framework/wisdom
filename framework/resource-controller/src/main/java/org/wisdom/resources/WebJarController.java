/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A controller serving WebJars.
 * WebJars (http://www.webjars.org) are jar files embedding web resources.
 * <p/>
 * The Wisdom Maven plugin copies these files to 'assets/libs', so this controller just load the requested
 * resources from this place. contained resources are served from:
 * <ol>
 * <li>/libs/libraryname-version/path</li>
 * <li>/libs/libraryname/path</li>
 * <li>/libs/path</li>
 * </ol>
 */
@Component(immediate = true)
@Provides(specifications = Controller.class)
@Instantiate(name = "WebJarResourceController")
public class WebJarController extends DefaultController {

    /**
     * The default instance handle the `assets/libs` folder.
     */
    private final File directory;

    private static final Logger LOGGER = LoggerFactory.getLogger(WebJarController.class);

    TreeMultimap<String, File> index = TreeMultimap.create();
    List<WebJarLib> libs = new ArrayList<>();

    @Requires
    Crypto crypto;

    @Requires
    ApplicationConfiguration configuration;

    /**
     * Constructor used for testing purpose only.
     *
     * @param crypto        the crypto service
     * @param configuration the configuration
     * @param path          the path (relative to the configuration's base dir)
     */
    WebJarController(Crypto crypto, ApplicationConfiguration configuration, String path) {
        this.crypto = crypto;
        this.configuration = configuration;
        directory = new File(configuration.getBaseDir(), path);
        if (directory.isDirectory()) {
            buildFileIndex();
        }
    }

    public WebJarController(@Property(value = "assets/libs", name = "path") String path) {
        directory = new File(configuration.getBaseDir(), path);
        if (directory.isDirectory()) {
            buildFileIndex();
        }
    }

    private void buildFileIndex() {
        if (directory.listFiles() == null) {
            // Empty.
            return;
        }

        FileFilter isDirectory = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };
        File[] names = directory.listFiles(isDirectory);

        // Build index from files
        for (File dir : names) {
            String library = dir.getName();
            File[] versions = dir.listFiles(isDirectory);
            for (File ver : versions) {
                String version = ver.getName();
                WebJarLib lib = new WebJarLib(library, version, ver);
                libs.add(lib);
                populateIndexForLibrary(lib);
            }
        }

        LOGGER.info("{} libraries embedded within web jars detected", libs.size());
        LOGGER.info("WebJar index built - {} files indexed", index.size());
    }

    private void populateIndexForLibrary(WebJarLib lib) {
        LOGGER.debug("Indexing files for WebJar library {}-{}", lib.name, lib.version);
        for (File file : FileUtils.listFiles(lib.root, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            if (!file.isDirectory()) {
                String path = file.getAbsolutePath().substring(lib.root.getAbsolutePath().length() + 1);
                // On windows we need to replace \ by /
                path = path.replace("\\", "/");
                index.put(path, file);
            }
        }
    }

    @Override
    public List<Route> routes() {
        return ImmutableList.of(
                new RouteBuilder()
                        .route(HttpMethod.GET)
                        .on("/" + "libs" + "/{path+}")
                        .to(this, "serve")
        );
    }


    public Result serve() {
        String path = context().parameterFromPath("path");
        if (path == null) {
            LOGGER.error("Cannot server Web Jar resource : no path");
            return badRequest();
        }

        Collection<File> files = index.get(path);

        if (files.size() == 1) {
            // Perfect ! only one match
            return CacheUtils.fromFile(files.iterator().next(), context(), configuration, crypto);
        } else if (files.size() > 1) {
            // Several candidates
            LOGGER.warn("Several candidates to match '{}' : {} - returning the first match", path, files);
            return CacheUtils.fromFile(files.iterator().next(), context(), configuration, crypto);
        } else {
            // No direct match, try complete path
            File full = new File(directory, path);
            if (full.exists()) {
                // We have a full path (name/version/path)
                return CacheUtils.fromFile(full, context(), configuration, crypto);
            } else {
                // The version may have been omitted.
                // Try to extract the library name
                if (path.contains("/")) {
                    String library = path.substring(0, path.indexOf("/"));
                    String stripped = path.substring(path.indexOf("/") + 1);
                    File file = getFileFromLibrary(library, stripped);
                    if (file == null) {
                        return notFound();
                    } else {
                        return CacheUtils.fromFile(file, context(), configuration, crypto);
                    }
                } else {
                    return notFound();
                }
            }
        }
    }

    private File getFileFromLibrary(String library, String stripped) {
        for (WebJarLib lib : libs) {
            // We are sure that stripped does not contains the version, because it would have been catch by the full
            // path check, so stripped is the path within the module.
            if (lib.name.equals(library) && lib.contains(stripped)) {
                return lib.get(stripped);
            }
        }
        return null;
    }

}
