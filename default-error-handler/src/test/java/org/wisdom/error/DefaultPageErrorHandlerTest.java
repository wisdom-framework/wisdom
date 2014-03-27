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
