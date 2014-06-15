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