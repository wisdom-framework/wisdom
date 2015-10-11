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
package org.wisdom.cache.ehcache;

import org.junit.Test;
import org.wisdom.api.cache.Cache;
import org.wisdom.api.http.Result;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.Invocation;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.test.parents.Action.action;

/**
 * Test the Cache service.
 */
public class EhCacheServiceIT extends WisdomTest {

    @Inject
    Cache service;

    @Inject
    MyCachedController controller;

    @Test
    public void testInsertionAndExpiration() throws InterruptedException {
        service.set("key", "value", 1);
        assertThat((String) service.get("key")).isEqualTo("value");
        Thread.sleep(2000);
        assertThat((String) service.get("key")).isNull();
    }

    @Test
    public void testCacheFromController() throws InterruptedException {
        Action.ActionResult result = action(new Invocation() {

            @Override
            public Result invoke() throws Throwable {
                return controller.retrieve();
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).isEqualTo("100");

        result = action(new Invocation() {

            @Override
            public Result invoke() throws Throwable {
                return controller.retrieve();
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).isEqualTo("101");

        Thread.sleep(3000);

        result = action(new Invocation() {

            @Override
            public Result invoke() throws Throwable {
                return controller.retrieve();
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).isEqualTo("100");
    }
}
