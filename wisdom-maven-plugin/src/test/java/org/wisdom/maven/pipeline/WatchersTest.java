package org.wisdom.maven.pipeline;

import org.apache.maven.execution.MavenSession;
import org.junit.Test;
import org.wisdom.maven.Watcher;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check that the watchers behaves correctly.
 */
public class WatchersTest {


    @Test
    public void testWatchers() {
        Properties properties = new Properties();
        MavenSession session = mock(MavenSession.class);
        when(session.getExecutionProperties()).thenReturn(properties);

        // Nothing added.
        assertThat(Watchers.all(session)).isEmpty();

        // Added watchers.
        Watcher watcher1 = mock(Watcher.class);
        Watchers.add(session, watcher1);
        assertThat(Watchers.all(session)).hasSize(1);

        Watcher watcher2 = mock(Watcher.class);
        Watchers.add(session, watcher2);
        assertThat(Watchers.all(session)).hasSize(2);

        // Remove watchers
        assertThat(Watchers.remove(session, watcher1)).isTrue();
        assertThat(Watchers.remove(session, null)).isFalse();
        assertThat(Watchers.all(session)).hasSize(1);
    }

}
