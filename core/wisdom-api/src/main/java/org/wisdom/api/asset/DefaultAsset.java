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
package org.wisdom.api.asset;

/**
 * A default implementation of asset.
 */
public class DefaultAsset<T> implements Asset<T> {

    private final String path;

    private final T content;

    private final String source;

    private final String etag;

    private final long lastModified;

    /**
     * Creates a new default asset.
     *
     * @param path    the path
     * @param content the content
     * @param source  the source
     */
    public DefaultAsset(String path, T content, String source) {
        this(path, content, source, -1, null);
    }

    /**
     * Creates a new default asset.
     *
     * @param path         the path
     * @param content      the content
     * @param source       the source
     * @param lastModified the last modification date
     * @param etag         the etag if computed (may be {@code null})
     */
    public DefaultAsset(String path, T content, String source, long lastModified, String etag) {
        this.path = path;
        this.content = content;
        this.source = source;
        this.etag = etag;
        this.lastModified = lastModified;
    }

    /**
     * Gets the path of the asset. Emitting a request on this path allows to retrieve the asset.
     *
     * @return the url
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the asset backend (file, url...).
     *
     * @return the content
     */
    public T getContent() {
        return content;
    }

    /**
     * Gets the asset's source.
     *
     * @return an identifier of the source
     */
    public String getSource() {
        return source;
    }

    /**
     * Gets the ETAG of the asset if computed.
     *
     * @return the ETAG, {@literal null} if not computed.
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Gets the last modification date.
     *
     * @return the last modification date of the asset
     */
    public long getLastModified() {
        return lastModified;
    }
}
