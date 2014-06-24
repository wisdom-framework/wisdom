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
package org.wisdom.monitor.extensions.ipojo;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.metadata.Element;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstanceModelTest {

    @Test
    public void testInstanceModel() {
        InstanceDescription description = mock(InstanceDescription.class);
        when(description.getName()).thenReturn("my-instance");
        when(description.getDescription()).thenReturn(new Element("instance", null));
        when(description.getState()).thenReturn(ComponentInstance.VALID);
        Architecture arch = mock(Architecture.class);
        when(arch.getInstanceDescription()).thenReturn(description);

        InstanceModel model = new InstanceModel(arch);

        assertThat(model.getName()).isEqualTo("my-instance");
        assertThat(model.getState()).isEqualTo("VALID");
        assertThat(model.getArchitecture()).contains("instance");

        // Check the other state.
        when(description.getState()).thenReturn(ComponentInstance.INVALID);
        assertThat(model.getState()).isEqualTo("INVALID");

        when(description.getState()).thenReturn(ComponentInstance.STOPPED);
        assertThat(model.getState()).isEqualTo("STOPPED");

        when(description.getState()).thenReturn(28);
        assertThat(model.getState()).isEqualTo("UNKNOWN");
    }

}