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

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.SilentLog;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks the defensive thread factory behavior.
 */
public class DefensiveThreadFactoryTest {

    DefensiveThreadFactory factory;
    private CollectorLog log;

    @Before
    public void setUp() {
        log = new CollectorLog();
        Mojo mojo = mock(Mojo.class);
        when(mojo.getLog()).thenReturn(log);
        factory = new DefensiveThreadFactory("test", mojo);
    }

    @Test
    public void testOnOkThread() {
        Thread thread = factory.newThread(new Runnable() {
            @Override
            public void run() {
                // Ok.
            }
        });

        // Use run and not start to execute the wrapped runnable synchronously.
        thread.run();
        assertThat(log.error).isNull();
    }

    @Test
    public void testOnBadThread() {
        Thread thread = factory.newThread(new Runnable() {
            @Override
            public void run() {
                throw new NullPointerException();
            }
        });

        // Use run and not start to execute the wrapped runnable synchronously.
        thread.run();
        assertThat(log.error).isNotNull();
        assertThat(log.throwable).isExactlyInstanceOf(NullPointerException.class);
    }

    private class CollectorLog extends SilentLog {

        CharSequence error;
        Throwable throwable;

        /**
         * Collect the error message.
         *
         * @param content the message
         * @param error the error
         * @see org.apache.maven.plugin.logging.Log#error(CharSequence, Throwable)
         */
        @Override
        public void error(CharSequence content, Throwable error) {
            this.error = content;
            this.throwable = error;
        }
    }
}
