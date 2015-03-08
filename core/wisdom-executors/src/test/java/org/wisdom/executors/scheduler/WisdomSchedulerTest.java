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
package org.wisdom.executors.scheduler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.wisdom.api.annotations.scheduler.Every;
import org.wisdom.api.scheduler.Scheduled;
import org.wisdom.executors.ManagedScheduledExecutorServiceImpl;
import org.wisdom.test.parents.FakeConfiguration;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Checks the Wisdom Scheduler.
 */
public class WisdomSchedulerTest {

    WisdomTaskScheduler scheduler = new WisdomTaskScheduler();

    @Before
    public void setUp() throws ClassNotFoundException {
        BundleContext context = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);
        when(bundle.getBundleId()).thenReturn(1L);
        when(context.getBundle()).thenReturn(bundle);
        doAnswer(
                new Answer<Class>() {
                    @Override
                    public Class answer(InvocationOnMock invocation) throws Throwable {
                        return WisdomSchedulerTest.class.getClassLoader().loadClass((String) invocation.getArguments()[0]);
                    }
                }
        ).when(bundle).loadClass(anyString());
        scheduler.scheduler = new ManagedScheduledExecutorServiceImpl("test",
                new FakeConfiguration(Collections.<String, Object>emptyMap()), null);
    }

    @After
    public void tearDown() throws InterruptedException {
        scheduler.scheduler.shutdown();
        scheduler.scheduler.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void testScheduled() throws InterruptedException {
        MyScheduled scheduled = new MyScheduled();
        scheduler.bindScheduled(scheduled);
        assertThat(scheduler.jobs).hasSize(1);
        Thread.sleep(2000);
        assertThat(scheduled.counter.get()).isGreaterThan(0);
        scheduler.unbindScheduled(scheduled);
        assertThat(scheduler.jobs).hasSize(0);
    }

    @Test
    public void testScheduledWithAmountAndUnit() throws InterruptedException {
        MySecondScheduled scheduled = new MySecondScheduled();
        scheduler.bindScheduled(scheduled);
        assertThat(scheduler.jobs).hasSize(1);
        Thread.sleep(2000);
        assertThat(scheduled.counter.get()).isGreaterThan(0);
        scheduler.unbindScheduled(scheduled);
        assertThat(scheduler.jobs).hasSize(0);
    }

    private class MyScheduled implements Scheduled {

        AtomicInteger counter = new AtomicInteger();


        @Every("1s")
        public void operation() {
            counter.incrementAndGet();
            System.out.println("Operate...");
        }
    }

    private class MySecondScheduled implements Scheduled {

        AtomicInteger counter = new AtomicInteger();


        @Every(period = 1, unit = TimeUnit.SECONDS)
        public void operation() {
            counter.incrementAndGet();
            System.out.println("Operate...");
        }
    }

}
