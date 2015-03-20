/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.api.utils;

import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the loading of the mime types.
 */
public class KnownMimeTypesTest {


    @Test
    public void testLoading() {
        assertThat(KnownMimeTypes.getMimeTypeByExtension("zip")).isEqualTo("application/zip");
        assertThat(KnownMimeTypes.getMimeTypeByExtension("jpg")).isEqualTo("image/jpeg");
        assertThat(KnownMimeTypes.getMimeTypeByExtension("json")).isEqualTo("application/json");
    }

    @Test
    public void testThatWeSupportTheSameSetOfExtensionsAsBefore() {
        for(Map.Entry<String, String> entry : OldKnownMimeTypes.EXTENSIONS.entrySet()) {
            // Check that the extension is not already used somewhere
            if (KnownMimeTypes.getMimeTypeByExtension(entry.getKey()) == null) {
                assertThat(entry.getValue()).isEqualTo(KnownMimeTypes.EXTENSIONS.get(entry.getKey()));
            }
        }
    }

}