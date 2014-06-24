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
package org.wisdom.monitor.extensions.osgi;

import org.junit.Test;
import org.osgi.framework.Bundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the bundle states.
 */
public class BundleStatesTest {
    @Test
    public void testFromStateAsInteger() throws Exception {
        assertThat(BundleStates.from(Bundle.ACTIVE)).isEqualToIgnoringCase("active");
        assertThat(BundleStates.from(Bundle.INSTALLED)).isEqualToIgnoringCase("installed");
        assertThat(BundleStates.from(Bundle.RESOLVED)).isEqualToIgnoringCase("resolved");
        assertThat(BundleStates.from(Bundle.STARTING)).isEqualToIgnoringCase("starting");
        assertThat(BundleStates.from(Bundle.STOPPING)).isEqualToIgnoringCase("stopping");
        assertThat(BundleStates.from(-1)).isEqualToIgnoringCase("UNKNOWN (-1)");
    }

    @Test
    public void testFromBundle() throws Exception {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getState()).thenReturn(Bundle.ACTIVE);
        assertThat(BundleStates.from(bundle)).isEqualToIgnoringCase("active");
        when(bundle.getState()).thenReturn(Bundle.INSTALLED);
        assertThat(BundleStates.from(bundle)).isEqualToIgnoringCase("installed");
    }

    @Test
    public void testFromString() throws Exception {
        assertThat(BundleStates.from("active")).isEqualTo(Bundle.ACTIVE);
        assertThat(BundleStates.from("installed")).isEqualTo(Bundle.INSTALLED);
        assertThat(BundleStates.from("resolved")).isEqualTo(Bundle.RESOLVED);
        assertThat(BundleStates.from("starting")).isEqualTo(Bundle.STARTING);
        assertThat(BundleStates.from("stopping")).isEqualTo(Bundle.STOPPING);
        assertThat(BundleStates.from("unknown")).isEqualTo(-1);
        assertThat(BundleStates.from("unknown (-2)")).isEqualTo(-1);
    }
}
