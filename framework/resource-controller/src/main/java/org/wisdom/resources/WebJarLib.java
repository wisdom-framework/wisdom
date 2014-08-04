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

import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;

import java.util.Collection;

/**
 * Represents Web Jar Libraries.
 */
public abstract class WebJarLib {

    /**
     * WebJar's name.
     */
    public final String name;

    /**
     * WebJar's version.
     */
    public final String version;

    /**
     * Creates a new WebJarLib instance.
     *
     * @param name    the name
     * @param version the version
     */
    public WebJarLib(String name, String version) {
        this.name = name;
        this.version = version;
    }

    /**
     * Method overridden by implementations.
     *
     * @return the list of files (path) contained in the web jar. The path are relative to the webjar's root.
     */
    public abstract Collection<String> names();

    /**
     * Does the current WebJar contains a resource with the given path?
     *
     * @param path the path
     * @return {@literal true} if the webjar contains the resource, {@literal false} otherwise.
     */
    public boolean contains(String path) {
        return names().contains(path);
    }

    /**
     * Creates the result to be returned to server a resource from the current webjar.
     * Method overridden by implementation.
     *
     * @param path          the resource's path
     * @param context       the Http Context
     * @param configuration the application configuration
     * @param crypto        the crypto service
     * @return the result
     */
    public abstract Result get(String path, Context context, ApplicationConfiguration configuration, Crypto crypto);

    /**
     * Gets the resources.
     *
     * @param path the resource's path
     * @return the resource.
     */
    public abstract Object get(String path);

    /**
     * Gets the last modification date of the webjar modules.
     *
     * @return the last modification date.
     */
    public abstract long lastModified();

    /**
     * @return name - version.
     */
    @Override
    public String toString() {
        return name + "-" + version;
    }


}
