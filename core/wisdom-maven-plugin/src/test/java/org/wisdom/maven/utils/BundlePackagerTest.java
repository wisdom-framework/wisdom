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
package org.wisdom.maven.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check bundle packager.
 */
public class BundlePackagerTest {

    @Test
    public void testExportPackageHeuristicsForServiceAndAPI() {
        assertThat(BundlePackager.shouldBeExported("")).isFalse();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix")).isFalse();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo")).isFalse();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.service")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.service.data")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.services")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.services.misc.exception")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.api")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.api.svc")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.apis")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.apis.svc")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.apiculteur.svc")).isFalse();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.service4.svc")).isFalse();
    }

    @Test
    public void testExportPackageHeuristicsForModelAndEntity() {
        assertThat(BundlePackager.shouldBeExported("")).isFalse();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix")).isFalse();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo")).isFalse();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.model")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.model.data")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.models")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.models.misc.exception")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.entity")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.entity.svc")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.entities")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.entities.svc")).isTrue();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.modelization.svc")).isFalse();
        assertThat(BundlePackager.shouldBeExported("org.apache.felix.ipojo.entitification.svc")).isFalse();
    }


}
