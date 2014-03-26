package org.wisdom.error;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of the default page error handler.
 */
public class DefaultPageErrorHandlerTest {
    @Test
    public void testCleanup() throws Exception {
        StackTraceElement[] stack = ImmutableList.of(
                new StackTraceElement("org.wisdom.samples.error.ErroneousController", "__M_doSomethingWrong",
                        "ErroneousController.java", 36),
                new StackTraceElement("org.wisdom.samples.error.ErroneousController", "doSomethingWrong",
                        "ErroneousController.java", -1)
        ).toArray(new StackTraceElement[2]);

        List<StackTraceElement> cleanup = DefaultPageErrorHandler.cleanup(stack);
        assertThat(cleanup).hasSize(1);
        assertThat(cleanup.get(0).getMethodName()).isEqualTo(stack[1].getMethodName());
    }

    @Test
    public void testUri() throws Exception {
        DefaultPageErrorHandler handler = new DefaultPageErrorHandler();
        assertThat(handler.uri().matcher("/").matches()).isTrue();
        assertThat(handler.uri().matcher("/foo").matches()).isTrue();
    }

    @Test
    public void testPriority() throws Exception {
        DefaultPageErrorHandler handler = new DefaultPageErrorHandler();
        assertThat(handler.priority()).isGreaterThan(0);
    }
}
