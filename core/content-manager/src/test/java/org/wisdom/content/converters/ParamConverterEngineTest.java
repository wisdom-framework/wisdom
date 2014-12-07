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
package org.wisdom.content.converters;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.wisdom.api.content.ParameterConverter;
import org.wisdom.api.content.ParameterFactory;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.test.parents.FakeContext;

import java.lang.reflect.Type;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


public class ParamConverterEngineTest {


    @Test
    public void testCreatingSingleValueUsingValueOfOnEnumeration() {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.converters = Collections.emptyList();

        assertThat(engine.convertValue("GET", HttpMethod.class, null, null)).isEqualTo(HttpMethod.GET);
        assertThat(engine.convertValue("GET", HttpMethod.class, null, "POST")).isEqualTo(HttpMethod.GET);
        assertThat(engine.convertValue(null, HttpMethod.class, null, "POST")).isEqualTo(HttpMethod.POST);

        assertThat(engine.convertValues(ImmutableList.of("GET"), HttpMethod.class, null, "POST")).isEqualTo(HttpMethod.GET);
        assertThat(engine.convertValues(ImmutableList.of("GET", "POST"), HttpMethod.class, null,
                null)).isEqualTo(HttpMethod.GET);
        assertThat(engine.convertValues(null, HttpMethod.class, null,
                "GET")).isEqualTo(HttpMethod.GET);
        assertThat(engine.convertValues(ImmutableList.<String>of(), HttpMethod.class, null,
                "GET")).isEqualTo(HttpMethod.GET);

        assertThat(engine.convertValues(null, HttpMethod[].class, null,
                "GET, POST,")).containsExactly(HttpMethod.GET, HttpMethod.POST);

        try {
            assertThat(engine.convertValue(null, HttpMethod.class, null, null)).isNull();
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            // OK.
        }

        // Invalid value
        try {
            assertThat(engine.convertValue("FOO", HttpMethod.class, null, null)).isNull();
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            // OK.
        }
    }

    @Test
    public void testCreatingCollectionsUsingValueOfOnEnumeration() throws NoSuchMethodException {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.converters = Collections.emptyList();

        Type listOfMethods = this.getClass().getMethod("listOfMethods", List.class).getGenericParameterTypes()[0];
        Type setOfMethods = this.getClass().getMethod("setOfMethods", Set.class).getGenericParameterTypes()[0];

        assertThat(engine.convertValue("GET, POST, DELETE", List.class, listOfMethods, null)).containsExactly(HttpMethod.GET,
                HttpMethod.POST, HttpMethod.DELETE).isInstanceOf(List.class);
        assertThat(engine.convertValue("GET, POST, DELETE", Set.class, setOfMethods, null)).containsExactly(HttpMethod.GET,
                HttpMethod.POST, HttpMethod.DELETE).isInstanceOf(Set.class);

        assertThat(engine.convertValues(ImmutableList.of("GET"), List.class, listOfMethods,
                "POST")).containsExactly(HttpMethod.GET);
        assertThat(engine.convertValues(ImmutableList.of("GET"), Set.class, listOfMethods,
                "POST")).containsExactly(HttpMethod.GET);

        assertThat(engine.convertValues(ImmutableList.of("GET", "POST"), List.class, listOfMethods,
                "POST")).containsExactly(HttpMethod.GET, HttpMethod.POST);

        assertThat(engine.convertValue("", List.class, listOfMethods, null)).isEmpty();
        assertThat(engine.convertValue("", Set.class, setOfMethods, null)).isEmpty();
        assertThat(engine.convertValue(null, Set.class, setOfMethods, "")).isEmpty();

        assertThat(engine.convertValue(null, List.class, listOfMethods, "GET, PATCH, DELETE")).containsExactly(HttpMethod
                .GET, HttpMethod.PATCH, HttpMethod.DELETE);
        assertThat(engine.convertValue(null, Set.class, setOfMethods, "GET, PATCH, " +
                "DELETE")).containsExactly(HttpMethod
                .GET, HttpMethod.PATCH, HttpMethod.DELETE);
    }

    @Test
    public void testCreatingArrayUsingValueOfOnEnumeration() throws NoSuchMethodException {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.converters = Collections.emptyList();

        assertThat(engine.convertValue("GET, POST, DELETE", HttpMethod[].class, null,
                null)).containsExactly(HttpMethod.GET,
                HttpMethod.POST, HttpMethod.DELETE);
        assertThat(engine.convertValue("GET, POST, DELETE", HttpMethod[].class, null, null)).containsExactly(HttpMethod.GET,
                HttpMethod.POST, HttpMethod.DELETE);

        assertThat(engine.convertValues(ImmutableList.of("GET"), HttpMethod[].class, null,
                "POST")).containsExactly(HttpMethod.GET);
        assertThat(engine.convertValues(ImmutableList.of("GET"), HttpMethod[].class, null,
                "POST")).containsExactly(HttpMethod.GET);

        assertThat(engine.convertValues(ImmutableList.of("GET", "POST"), HttpMethod[].class, null,
                "POST")).containsExactly(HttpMethod.GET, HttpMethod.POST);

        assertThat(engine.convertValue("", HttpMethod[].class, null, null)).isEmpty();
        assertThat(engine.convertValue(null, HttpMethod[].class, null, "")).isEmpty();

        assertThat(engine.convertValue(null, HttpMethod[].class, null, "GET, PATCH, " +
                "DELETE")).containsExactly(HttpMethod.GET, HttpMethod.PATCH, HttpMethod.DELETE);
        assertThat(engine.convertValue(null, HttpMethod[].class, null, "GET, PATCH, " +
                "DELETE")).containsExactly(HttpMethod
                .GET, HttpMethod.PATCH, HttpMethod.DELETE);
    }

    @Test
    public void testWithString() throws NoSuchMethodException {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.converters = Collections.emptyList();

        assertThat(engine.convertValue("Hello", String.class, null, null)).isEqualTo("Hello");
        assertThat(engine.convertValue("", String.class, null, null)).isEqualTo("");
        assertThat(engine.convertValue(null, String.class, null, "Hello2")).isEqualTo("Hello2");
        assertThat(engine.convertValues(ImmutableList.of("Hello", "World"), String[].class, null,
                null)).containsExactly("Hello", "World");
        assertThat(engine.convertValue(null, String[].class, null,
                "Hello, World")).containsExactly("Hello", "World");
    }

    @Test
    public void testWithPrimitives() throws NoSuchMethodException {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.converters = Collections.emptyList();

        assertThat(engine.convertValue("1", Integer.class, null, null)).isEqualTo(1);
        assertThat(engine.convertValue("1", Integer.TYPE, null, null)).isEqualTo(1);
        assertThat(engine.convertValue(null, Integer.class, null, "1")).isEqualTo(1);

        assertThat(engine.convertValue("2", Long.class, null, null)).isEqualTo(2l);
        assertThat(engine.convertValue("2", Long.TYPE, null, null)).isEqualTo(2l);
        assertThat(engine.convertValue(null, Long.class, null, "2")).isEqualTo(2l);

        assertThat(engine.convertValue("3", Short.class, null, null)).isEqualTo((short) 3);
        assertThat(engine.convertValue("3", Short.TYPE, null, null)).isEqualTo((short) 3);
        assertThat(engine.convertValue(null, Short.class, null, "3")).isEqualTo((short) 3);

        assertThat(engine.convertValue("4", Byte.class, null, null)).isEqualTo((byte) 4);
        assertThat(engine.convertValue("4", Byte.TYPE, null, null)).isEqualTo((byte) 4);
        assertThat(engine.convertValue(null, Byte.class, null, "4")).isEqualTo((byte) 4);

        assertThat(engine.convertValue("5.5", Float.class, null, null)).isEqualTo(5.5f);
        assertThat(engine.convertValue("5.5", Float.TYPE, null, null)).isEqualTo(5.5f);
        assertThat(engine.convertValue(null, Float.class, null, "5.5")).isEqualTo(5.5f);

        assertThat(engine.convertValue("5.5", Double.class, null, null)).isEqualTo(5.5d);
        assertThat(engine.convertValue("5.5", Double.TYPE, null, null)).isEqualTo(5.5d);
        assertThat(engine.convertValue(null, Double.class, null, "5.5")).isEqualTo(5.5d);

        assertThat(engine.convertValue("a", Character.class, null, null)).isEqualTo('a');
        assertThat(engine.convertValue("a", Character.TYPE, null, null)).isEqualTo('a');
        assertThat(engine.convertValue(null, Character.class, null, "1")).isEqualTo('1');

        assertThat(engine.convertValue("true", Boolean.class, null, null)).isTrue();
        assertThat(engine.convertValue("on", Boolean.TYPE, null, null)).isTrue();
        assertThat(engine.convertValue(null, Boolean.class, null, "yes")).isTrue();
        assertThat(engine.convertValue("", Boolean.class, null, null)).isFalse();
        assertThat(engine.convertValue(null, Boolean.TYPE, null, null)).isFalse();
        assertThat(engine.convertValue(null, Boolean.class, null, "no")).isFalse();
    }

    @Test
    public void testWithArrayOfPrimitives() throws NoSuchMethodException {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.converters = Collections.emptyList();

        assertThat(engine.convertValue("1, 2", Integer[].class, null, null)).containsExactly(1, 2);
        assertThat(engine.convertValue("1, 2", int[].class, null, null)).containsExactly(1, 2);
        assertThat(engine.convertValue(null, Integer[].class, null, "1,2")).containsExactly(1, 2);

        assertThat(engine.convertValue("1, 2", Long[].class, null, null)).containsExactly(1l, 2l);
        assertThat(engine.convertValue("1, 2", long[].class, null, null)).containsExactly(1l, 2l);
        assertThat(engine.convertValue(null, Long[].class, null, "1,2")).containsExactly(1l, 2l);

        assertThat(engine.convertValue("1.1, 2.2", Float[].class, null, null)).containsExactly(1.1f, 2.2f);
        assertThat(engine.convertValue("1.1, 2.2", float[].class, null, null)).containsExactly(1.1f, 2.2f);
        assertThat(engine.convertValue(null, Float[].class, null, "1,2")).containsExactly(1.0f, 2.0f);

        assertThat(engine.convertValue("1.1, 2.2", Double[].class, null, null)).containsExactly(1.1d, 2.2d);
        assertThat(engine.convertValue("1.1, 2.2", double[].class, null, null)).containsExactly(1.1d, 2.2d);
        assertThat(engine.convertValue(null, Double[].class, null, "1,2")).containsExactly(1.0d, 2.0d);

        assertThat(engine.convertValue("true, yes, no, false, off", Boolean[].class, null,
                null)).containsExactly(true, true, false, false, false);
        assertThat(engine.convertValue("true, yes, no, false, off", boolean[].class, null,
                null)).containsExactly(true, true, false, false, false);
        assertThat(engine.convertValue(null, boolean[].class, null,
                "true, yes, no, false, off")).containsExactly(true, true, false, false, false);
    }

    @Test
    public void testWithCollectionOfPrimitives() throws NoSuchMethodException {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.converters = Collections.emptyList();

        Type type = this.getClass().getMethod("listOfInt", List.class).getGenericParameterTypes()[0];

        assertThat(engine.convertValue("1, 2", List.class, type, null)).containsExactly(1, 2);
        assertThat(engine.convertValue(null, List.class, type, "1,2")).containsExactly(1, 2);
    }

    @Test
    public void testUsingConstructor() throws NoSuchMethodException {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.converters = Collections.emptyList();

        assertThat(engine.convertValue("wisdom", Person.class, null, null).name).isEqualTo("wisdom");
        assertThat(engine.convertValue(null, Person.class, null, "wisdom").name).isEqualTo("wisdom");
        assertThat(engine.convertValue("wisdom, welcome", Person[].class, null, null)).hasSize(2);
        assertThat(engine.convertValue("wisdom, welcome", Person[].class, null, null)[1].name).isEqualTo("welcome");
        assertThat(engine.convertValues(ImmutableList.of("wisdom", "welcome"), Person[].class, null, null)).hasSize(2);
        assertThat(engine.convertValues(ImmutableList.of("wisdom", "welcome"), Person[].class, null,
                null)[1].name).isEqualTo("welcome");

        Type type = this.getClass().getMethod("listOfPersons", Set.class).getGenericParameterTypes()[0];

        assertThat(engine.convertValues(ImmutableList.of("wisdom", "welcome"), Set.class, type, null)).hasSize(2);
        assertThat(engine.convertValues(ImmutableList.of("wisdom", "welcome"), List.class, type, null)).hasSize(2);
    }

    @Test
    public void testUsingFrom() throws NoSuchMethodException {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.converters = Collections.emptyList();

        assertThat(engine.convertValue("wisdom", Person2.class, null, null).name).isEqualTo("wisdom");
        assertThat(engine.convertValue(null, Person2.class, null, "wisdom").name).isEqualTo("wisdom");
        assertThat(engine.convertValue("wisdom, welcome", Person2[].class, null, null)).hasSize(2);
        assertThat(engine.convertValue("wisdom, welcome", Person2[].class, null, null)[1].name).isEqualTo("welcome");
        assertThat(engine.convertValues(ImmutableList.of("wisdom", "welcome"), Person2[].class, null, null)).hasSize(2);
        assertThat(engine.convertValues(ImmutableList.of("wisdom", "welcome"), Person2[].class, null,
                null)[1].name).isEqualTo("welcome");

        Type type = this.getClass().getMethod("listOfPersons2", Set.class).getGenericParameterTypes()[0];

        assertThat(engine.convertValues(ImmutableList.of("wisdom", "welcome"), Set.class, type, null)).hasSize(2);
        assertThat(engine.convertValues(ImmutableList.of("wisdom", "welcome"), List.class, type, null)).hasSize(2);
    }

    @Test
    public void testUsingFromString() throws NoSuchMethodException {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.converters = Collections.emptyList();

        assertThat(engine.convertValue("wisdom", Person3.class, null, null).name).isEqualTo("wisdom");
        assertThat(engine.convertValue(null, Person3.class, null, "wisdom").name).isEqualTo("wisdom");
        assertThat(engine.convertValue("wisdom, welcome", Person3[].class, null, null)).hasSize(2);
        assertThat(engine.convertValue("wisdom, welcome", Person3[].class, null, null)[1].name).isEqualTo("welcome");
        assertThat(engine.convertValues(ImmutableList.of("wisdom", "welcome"), Person3[].class, null, null)).hasSize(2);
        assertThat(engine.convertValues(ImmutableList.of("wisdom", "welcome"), Person3[].class, null,
                null)[1].name).isEqualTo("welcome");

        Type type = this.getClass().getMethod("listOfPersons3", Set.class).getGenericParameterTypes()[0];

        assertThat(engine.convertValues(ImmutableList.of("wisdom", "welcome"), Set.class, type, null)).hasSize(2);
        assertThat(engine.convertValues(ImmutableList.of("wisdom", "welcome"), List.class, type, null)).hasSize(2);
    }

    @Test
    public void testUsingConverter() throws NoSuchMethodException {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.converters = ImmutableList.<ParameterConverter>of(new MemberConverter());

        assertThat(engine.convertValue("wisdom-0", Member.class, null, null)).isEqualTo(new Member("wisdom", 0));
        assertThat(engine.convertValue(null, Member.class, null, "wisdom-1")).isEqualTo(new Member("wisdom", 1));
        assertThat(engine.convertValue("wisdom-0, welcome-1", Member[].class, null, null)).hasSize(2);
        assertThat(engine.convertValue("wisdom-0, welcome-1", Member[].class, null,
                null)[1].name).isEqualTo("welcome");
        assertThat(engine.convertValues(ImmutableList.of("wisdom-0", "welcome-1"), Member[].class, null,
                null)).hasSize(2);
        assertThat(engine.convertValues(ImmutableList.of("wisdom-0", "welcome-1"), Member[].class, null,
                null)[1].name).isEqualTo("welcome");

        Type type = this.getClass().getMethod("listOfMembers", Set.class).getGenericParameterTypes()[0];

        assertThat(engine.convertValues(ImmutableList.of("wisdom-0", "welcome-1"), Set.class, type, null)).hasSize(2);
        assertThat(engine.convertValues(ImmutableList.of("wisdom-0", "welcome-1"), List.class, type, null)).hasSize(2);

        //Invalid
        try {
            engine.convertValue(null, Member.class, null, null);
            fail("exception expected");
        } catch (NullPointerException e) {
            // OK
        }

        try {
            engine.convertValue("illegal", Member.class, null, null);
            fail("exception expected");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testMissingConverter() throws NoSuchMethodException {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.converters = ImmutableList.<ParameterConverter>of(new MemberConverter());

        engine.convertValue("hello", Object.class, null, null);
    }

    @Test
    public void testEmptyCollectionsAndArrays() throws NoSuchMethodException {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.converters = ImmutableList.<ParameterConverter>of(new MemberConverter());

        Type type = this.getClass().getMethod("listOfInt", List.class).getGenericParameterTypes()[0];

        assertThat(engine.convertValues(null, List.class, type, null)).isEmpty();
        assertThat(engine.convertValues(null, Set.class, type, null)).isEmpty();
        assertThat(engine.convertValues(null, Collection.class, type, null)).isEmpty();

        final List<String> empty = Collections.emptyList();
        assertThat(engine.convertValues(empty, List.class, type, "")).isEmpty();
        assertThat(engine.convertValues(empty, Set.class, type, "")).isEmpty();
        assertThat(engine.convertValues(empty, Collection.class, type, "")).isEmpty();

        assertThat(engine.convertValues(null, int[].class, null, null)).isEmpty();
        assertThat(engine.convertValues(empty, int[].class, null, "")).isEmpty();
    }

    /**
     * When no generic metadata, default to String.
     * @throws NoSuchMethodException
     */
    @Test
    public void testListWithoutGenerics() throws NoSuchMethodException {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.converters = ImmutableList.<ParameterConverter>of(new MemberConverter());

        Type type = this.getClass().getMethod("list", List.class).getGenericParameterTypes()[0];

        assertThat(engine.convertValue("a", List.class, type, null)).containsExactly("a");
        assertThat(engine.convertValues(null, Set.class, type, "b")).containsExactly("b");
    }

    @Test
    public void testFactory() {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.factories = ImmutableList.<ParameterFactory>of(new StuffFactory());

        assertThat(engine.getTypesHandledByFactories()).hasSize(1).contains(Stuff.class);
        FakeContext context = new FakeContext().setHeader("X-Stuff", "bar");
        Stuff stuff = engine.newInstance(context, Stuff.class);
        assertThat(stuff.context).isEqualTo(context);
        assertThat(stuff.name).isEqualTo("bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingFactory() {
        ParamConverterEngine engine = new ParamConverterEngine();
        engine.factories = ImmutableList.of();
        assertThat(engine.getTypesHandledByFactories()).hasSize(0);
        engine.factories = ImmutableList.<ParameterFactory>of(new StuffFactory());
        assertThat(engine.getTypesHandledByFactories()).hasSize(1).contains(Stuff.class);
        FakeContext context = new FakeContext().setHeader("X-Stuff", "bar");
        // Going to throw an exception.
        engine.newInstance(context, List.class);

    }

    public void listOfMethods(List<HttpMethod> methods) {
        // ...
    }

    public void setOfMethods(Set<HttpMethod> methods) {
        // ...
    }

    public void listOfInt(List<Integer> integers) {
        // ...
    }

    public void listOfPersons(Set<Person> persons) {
        // ...
    }

    public void listOfPersons2(Set<Person2> persons) {
        // ...
    }

    public void listOfPersons3(Set<Person3> persons) {
        // ...
    }

    public void listOfMembers(Set<Member> persons) {
        // ...
    }

    public void list(List persons) {
        // ...
    }


}