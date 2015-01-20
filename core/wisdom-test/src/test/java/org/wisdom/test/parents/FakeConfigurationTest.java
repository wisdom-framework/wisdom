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
package org.wisdom.test.parents;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.wisdom.api.http.Context;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;


public class FakeConfigurationTest {

    @Test
    public void testGetConfiguration() throws Exception {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("key", "value"));
        assertThat(configuration.getConfiguration("prefix")).isNull();

        configuration = new FakeConfiguration(ImmutableMap.of("k", "v", "prefix", configuration));
        assertThat(configuration.getConfiguration("prefix")).isNotNull();
        assertThat(configuration.getConfiguration("prefix").get("key")).isEqualTo("value");
    }

    @Test
    public void testHas() throws Exception {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("key", "value"));
        assertThat(configuration.has("key")).isTrue();
        assertThat(configuration.has("missing")).isFalse();
    }

    @Test
    public void testGet() throws Exception {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("key", "value"));
        assertThat(configuration.get("key")).isEqualToIgnoringCase("value");
        assertThat(configuration.get("missing")).isNull();
        assertThat(configuration.getWithDefault("missing", "default")).isEqualTo("default");
        assertThat(configuration.getWithDefault("key", "default")).isEqualTo("value");

        assertThat(configuration.getOrDie("key")).isEqualToIgnoringCase("value");
        try {
            configuration.getOrDie("missing");
            fail("Exception expected");
        } catch (RuntimeException e) {
            // OK
        }
    }

    @Test
    public void testGetWithClass() throws Exception {
        FakeConfiguration configuration = new FakeConfiguration(
                ImmutableMap.of("key", "value", "stuff", new FakeContext()));
        assertThat(configuration.get("stuff", FakeContext.class)).isNotNull();
        assertThat(configuration.get("stuff", Context.class)).isNotNull();
        assertThat(configuration.get("missing", FakeContext.class)).isNull();
        assertThat(configuration.get("missing", FakeContext.class, new FakeContext())).isNotNull();
        assertThat(configuration.get("stuff", FakeContext.class, (FakeContext) null)).isNotNull();
        assertThat(configuration.get("stuff", FakeContext.class, "broken")).isNotNull();
    }

    @Test
    public void testGetOrDieWithData() throws Exception {
        FakeConfiguration configuration = new FakeConfiguration(
                ImmutableMap.of("key", "value", "stuff", new FakeContext()));
        assertThat(configuration.getOrDie("stuff", FakeContext.class)).isNotNull();
    }

    @Test(expected = RuntimeException.class)
    public void testGetOrDieWithoutData() throws Exception {
        FakeConfiguration configuration = new FakeConfiguration(
                ImmutableMap.of("key", "value", "stuff", new FakeContext()));
        configuration.getOrDie("missing", FakeContext.class);
    }

    @Test
    public void testIntegers() {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("key", "value",
                "integer", 1, "integer2", Integer.valueOf(2)));

        assertThat(configuration.getInteger("integer")).isEqualTo(1);
        assertThat(configuration.getInteger("integer2")).isEqualTo(2);
        assertThat(configuration.getIntegerOrDie("integer")).isEqualTo(1);
        assertThat(configuration.getIntegerWithDefault("integer", 3)).isEqualTo(1);
        assertThat(configuration.getIntegerWithDefault("missing", 3)).isEqualTo(3);

        try {
            configuration.getIntegerOrDie("missing");
            fail("Exception expected");
        } catch (RuntimeException e) {
            // OK
        }
    }

    @Test
    public void testDoubles() {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("key", "value",
                "num", 1.0));

        assertThat(configuration.getDouble("num")).isEqualTo(1.0);
        assertThat(configuration.getDoubleOrDie("num")).isEqualTo(1.0);
        assertThat(configuration.getDoubleWithDefault("num", 3.0)).isEqualTo(1.0);
        assertThat(configuration.getDoubleWithDefault("missing", 3.0)).isEqualTo(3.0);

        try {
            configuration.getIntegerOrDie("missing");
            fail("Exception expected");
        } catch (RuntimeException e) {
            // OK
        }
    }

    @Test
    public void testLong() {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("key", "value",
                "num", 1l));

        assertThat(configuration.getLong("num")).isEqualTo(1l);
        assertThat(configuration.getLong("missing")).isNull();
        assertThat(configuration.getLongOrDie("num")).isEqualTo(1l);
        assertThat(configuration.getLongWithDefault("num", 3l)).isEqualTo(1l);
        assertThat(configuration.getLongWithDefault("missing", 3l)).isEqualTo(3l);

        try {
            configuration.getLongOrDie("missing");
            fail("Exception expected");
        } catch (RuntimeException e) {
            // OK
        }
    }

    @Test
    public void testGetBoolean() throws Exception {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("key", "value",
                "num", true));

        assertThat(configuration.getBoolean("num")).isTrue();
        assertThat(configuration.getBoolean("missing")).isNull();
        assertThat(configuration.getBooleanOrDie("num")).isTrue();
        assertThat(configuration.getBooleanWithDefault("num", false)).isTrue();
        assertThat(configuration.getBooleanWithDefault("missing", false)).isFalse();

        try {
            configuration.getBooleanOrDie("missing");
            fail("Exception expected");
        } catch (RuntimeException e) {
            // OK
        }
    }


    @Test
    public void testGetDuration() throws Exception {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("key", "value",
                "duration", 1000l));

        assertThat(configuration.getDuration("duration", TimeUnit.SECONDS)).isEqualTo(1000);
        assertThat(configuration.getDuration("missing", TimeUnit.HOURS)).isNull();
        assertThat(configuration.getDuration("missing", TimeUnit.HOURS, 2)).isEqualTo(2);
    }

    @Test
    public void testGetBytes() throws Exception {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("key", "value",
                "size", 1024l));

        assertThat(configuration.getBytes("size")).isEqualTo(1024);
        assertThat(configuration.getBytes("missing")).isNull();
        assertThat(configuration.getBytes("missing", 2)).isEqualTo(2);
    }

    @Test
    public void testGetStringArray() throws Exception {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("key", "value",
                "array", new String[] {"a", "b"}));

        assertThat(configuration.getStringArray("array")).hasSize(2);
        assertThat(configuration.getStringArray("missing")).hasSize(0);
    }

    @Test
    public void testGetList() throws Exception {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("key", "value",
                "array", ImmutableList.of("a", "b")));

        assertThat(configuration.getList("array")).hasSize(2);
        assertThat(configuration.getList("missing")).hasSize(0);
    }

    @Test
    public void testAsProperties() throws Exception {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("key", "value",
                "num", true));
        assertThat(configuration.asProperties()).hasSize(2);
    }

    @Test
    public void testAsMap() throws Exception {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("key", "value",
                "num", true));
        assertThat(configuration.asMap()).hasSize(2);
    }
}
