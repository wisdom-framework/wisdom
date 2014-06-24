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
package org.wisdom.api.crypto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the common hash algorithms.
 */
public class HashTest {

    @Test
    public void testToString() {
        assertThat(Hash.MD5.toString()).isEqualTo("MD5");
        assertThat(Hash.SHA1.toString()).isEqualTo("SHA-1");
        assertThat(Hash.SHA256.toString()).isEqualTo("SHA-256");
        assertThat(Hash.SHA512.toString()).isEqualTo("SHA-512");

    }
}
