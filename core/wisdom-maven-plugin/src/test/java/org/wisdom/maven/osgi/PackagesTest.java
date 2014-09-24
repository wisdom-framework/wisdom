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
package org.wisdom.maven.osgi;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class PackagesTest {

    @Test
    public void testToClause() throws Exception {
        assertThat(Packages.toClause(Collections.<String>emptyList())).isEmpty();
        assertThat(Packages.toClause(ImmutableList.of("org.acme"))).contains("org.acme");
        assertThat(Packages.toClause(ImmutableList.of("org.acme", "com.foo"))).contains("org.acme, com.foo");
    }

    @Test
    public void testPackageNameComputation() {
        assertThat(Packages.getPackageName("foo/bar/Baz.class")).isEqualTo("foo.bar");
        assertThat(Packages.getPackageName("foo/bar/Baz.java")).isEqualTo("foo.bar");
        assertThat(Packages.getPackageName("Baz.class")).isEqualTo(".");
    }

    @Test
    public void testExportPackageHeuristicsForServiceAndAPI() {
        assertThat(Packages.shouldBeExported("")).isFalse();
        assertThat(Packages.shouldBeExported("org.apache.felix")).isFalse();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo")).isFalse();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.service")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.service.data")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.services")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.services.misc.exception")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.api")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.api.svc")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.apis")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.apis.svc")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.apiculteur.svc")).isFalse();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.service4.svc")).isFalse();
    }

    @Test
    public void testExportPackageHeuristicsForModelAndEntity() {
        assertThat(Packages.shouldBeExported("")).isFalse();
        assertThat(Packages.shouldBeExported("org.apache.felix")).isFalse();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo")).isFalse();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.model")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.model.data")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.models")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.models.misc.exception")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.entity")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.entity.svc")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.entities")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.entities.svc")).isTrue();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.modelization.svc")).isFalse();
        assertThat(Packages.shouldBeExported("org.apache.felix.ipojo.entitification.svc")).isFalse();
    }
}