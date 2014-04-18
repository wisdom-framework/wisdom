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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            }
        }

        LOGGER.info("{} libraries embedded within web jars detected", libs.size());
        LOGGER.info("WebJar index built - {} files indexed", indexSize());
    }

    int indexSize() {
        int count = 0;
        for (WebJarLib lib : libs) {
            count += lib.resources().size();
        }
        return count;
    }

    private List<WebJarLib> findLibsContaining(String path) {
        List<WebJarLib> list = new ArrayList<>();
        for (WebJarLib lib : libs) {
            if (lib.contains(path)) {
                list.add(lib);
            }
        }
        return list;
    }

    private List<WebJarLib> find(String name) {
        List<WebJarLib> list = new ArrayList<>();
        for (WebJarLib lib : libs) {
            if (lib.name.equals(name)) {
                list.add(lib);
            }
        }
        return list;
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

    Pattern PATTERN = Pattern.compile("([^/]+)(/([^/]+))?/(.*)");

    public Result serve() {
        String path = context().parameterFromPath("path");
        if (path == null) {
            LOGGER.error("Cannot server Web Jar resource : no path");
            return badRequest();
        }

        List<WebJarLib> candidates = findLibsContaining(path);

        if (candidates.size() == 1) {
            // Perfect ! only one match
            return candidates.get(0).get(path, context(), configuration, crypto);
        } else if (candidates.size() > 1) {
            // Several candidates
            LOGGER.warn("{} WebJars provide '{}' - returning the one from {}-{}", candidates.size(), path,
                    candidates.get(0).name, candidates.get(0).version);
            return candidates.get(0).get(path, context(), configuration, crypto);
        } else {
            Matcher matcher = PATTERN.matcher(path);
            if (!matcher.matches()) {
                // It should have been handled by the path match.
                return notFound();
            }

            final String name = matcher.group(1);
            final String version = matcher.group(3);
            if (version != null) {
                String rel = matcher.group(4);
                // We have a name and a version
                // Try to find the matching library
                WebJarLib lib = find(name, version);
                if (lib != null) {
                    return lib.get(rel, context(), configuration, crypto);
                }
                // If not found, it may be because the version is not really the version but a segment of the path.
            }
            // If we reach this point it means that the name/version lookup has failed, try without the version
            String rel = matcher.group(4);
            if (version != null) {
                // We have a group 3
                rel = version + "/" + rel;
            }

            List<WebJarLib> libs = find(name);
            if (libs.size() == 1) {
                // Only on library has the given name
                if (libs.get(0).contains(rel)) {
                    return libs.get(0).get(rel, context(), configuration, crypto);
                }
            } else if (libs.size() > 1) {
                // Several candidates
                for (WebJarLib lib : libs) {
                    if (lib.contains(rel)) {
                        LOGGER.warn("{} WebJars match the request '{}' - returning the resource from {}-{}",
                                libs.size(), path, lib.name, lib.version);
                        return lib.get(rel, context(), configuration, crypto);
                    }
                }
            }

            return notFound();
        }
    }

    private WebJarLib find(String name, String version) {
        for (WebJarLib lib : libs) {
            if (lib.name.equals(name) && lib.version.equals(version)) {
                return lib;
            }
        }
        return null;
    }

}
