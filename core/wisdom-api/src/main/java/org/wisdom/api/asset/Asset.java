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
 * A structure representing assets.
 *
 * @param <T> the type of asset (file or url).
 */
public interface Asset<T> {

    /**
     * @return the path to retrieve the asset. The result of this method can be use in HREF or Routes to retrieve the
     * asset itself.
     */
    String getPath();

    /**
     * @return the asset content. Can be an url or a file.
     */
    T getContent();

    /**
     * @return an identifier of the source.
     */
    String getSource();

    /**
     * @return the last modified date.
     */
    long getLastModified();

    /**
     * @return the asset ETAG.
     */
    String getEtag();

}
