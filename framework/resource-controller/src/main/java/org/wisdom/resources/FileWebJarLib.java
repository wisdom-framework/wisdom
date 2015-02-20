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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents and serves a web jar library exploded on the file system.
 */
class FileWebJarLib extends WebJarLib {

    /**
     * The root directory of the webjar.
     */
    public final File root;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileWebJarLib.class);

    /**
     * The (optional) source, indicating from where come the library.
     */
    public final String source;

    private Map<String, File> index = new TreeMap<>();

    FileWebJarLib(String name, String version, File root) {
        super(name, version);
        this.root = root;
        this.source = null;
        index();
    }

    FileWebJarLib(String name, String version, File root, String source) {
        super(name, version);
        this.root = root;
        this.source = source;
        index();
    }

    public Result get(String path, Context context, ApplicationConfiguration configuration, Crypto crypto) {
        File file = new File(root, path);
        if (!file.isFile()) {
            return Results.notFound();
        }
        return CacheUtils.fromFile(file, context, configuration, crypto);
    }

    @Override
    public Object get(String path) {
        File file = new File(root, path);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    @Override
    public long lastModified() {
        return root.lastModified();
    }

    public Collection<String> names() {
        return index.keySet();
    }

    private void index() {
        File base=root;
        if(source!=null){
            // If source is defined as component-version.jar (eg: polymer-0.5.4.jar)
            // we are looking for an inner folder of the same name ( "polymer" in the example)
            String module = source.substring(0, source.lastIndexOf('-'));
            File file = new File(root, module);
            if(file.isDirectory()) {
                // If root/component is a directory,
                // then we will only index this directory.
                LOGGER.debug("Indexing files for WebJar library {}-{} / {}", name, version, source);
                base= file;
            }else{
                LOGGER.debug("Indexing files for WebJar library {}-{}", name, version);
            }
        }else{
            LOGGER.debug("Indexing files for WebJar library {}-{}", name, version);
        }
        for (File file : FileUtils.listFiles(base, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            if (!file.isDirectory()) {
                // We compute the relative path of the file from the webjar root directory.
                String path = file.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);
                // On windows we need to replace \ by /
                path = path.replace("\\", "/");
                index.put(path, file);
            }
        }
    }

    @Override
    public String toString() {
        if(source != null)
            return  super.toString() + '-' + source;
        return super.toString();
    }
}
