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
package org.wisdom.resources;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks the Asset Controller behavior
 */
public class AssetControllerTest {

    @Test
    public void testExternalAssets() {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBaseDir()).thenReturn(new File("target/test-classes"));
        Crypto crypto = mock(Crypto.class);
        BundleContext context = mock(BundleContext.class);
        when(context.getBundles()).thenReturn(new Bundle[0]);
        AssetController controller = new AssetController(configuration, crypto, context, "/public", false, null,
                "/public");

        assertThat(controller.assets()).isNotEmpty().hasSize(1);
        assertThat(controller.assetAt("my-asset.js")).isNotNull();
        assertThat(controller.assetAt("does_not_exist.js")).isNull();

    }

    @Test
    public void testWithoutExternalAssets() {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBaseDir()).thenReturn(new File("target/test-classes"));
        Crypto crypto = mock(Crypto.class);
        BundleContext context = mock(BundleContext.class);
        when(context.getBundles()).thenReturn(new Bundle[0]);
        AssetController controller = new AssetController(configuration, crypto, context, "/assets", false, null,
                "/public");

        assertThat(controller.assets()).isEmpty();
        assertThat(controller.assetAt("does_not_exist.js")).isNull();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEmptyUrlRoot() {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBaseDir()).thenReturn(new File("target/test-classes"));
        Crypto crypto = mock(Crypto.class);
        BundleContext context = mock(BundleContext.class);
        when(context.getBundles()).thenReturn(new Bundle[0]);
        new AssetController(configuration, crypto, context, "/assets", false, null,
                "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithUrlRootNotStartingBySlash() {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBaseDir()).thenReturn(new File("target/test-classes"));
        Crypto crypto = mock(Crypto.class);
        BundleContext context = mock(BundleContext.class);
        when(context.getBundles()).thenReturn(new Bundle[0]);
        new AssetController(configuration, crypto, context, "/assets", false, null,
                "foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWitInBundlePathNotStartingBySlash() {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBaseDir()).thenReturn(new File("target/test-classes"));
        Crypto crypto = mock(Crypto.class);
        BundleContext context = mock(BundleContext.class);
        when(context.getBundles()).thenReturn(new Bundle[0]);
        new AssetController(configuration, crypto, context, "/assets", true, "internal/",
                "/foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWitInBundlePathNotEndingBySlash() {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBaseDir()).thenReturn(new File("target/test-classes"));
        Crypto crypto = mock(Crypto.class);
        BundleContext context = mock(BundleContext.class);
        when(context.getBundles()).thenReturn(new Bundle[0]);
        new AssetController(configuration, crypto, context, "/assets", true, "/internal",
                "/foo");
    }

}