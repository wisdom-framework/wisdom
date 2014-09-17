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
package org.wisdom.template.thymeleaf;

import ognl.NumericTypes;
import ognl.OgnlOps;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Basic OGNL tests, especially the patched part.  Unlike {@link org.wisdom.template.thymeleaf.OgnlOpsTest}, this class defines the OGNLOps class so Jacoco can computes the coverage.
 */
public class OgnlOpsByReflectionTest {

    private static Class clazz;

    @BeforeClass
    public static void prepare() throws ClassNotFoundException {
        ClassLoader classLoader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals(OgnlOps.class.getName())) {
                    byte[] clazz;
                    try {
                        clazz = FileUtils.readFileToByteArray(new File("target/classes/ognl/OgnlOps.class"));
                    } catch (IOException e) {
                        throw new ClassNotFoundException("Cannot define the class");
                    }
                    return defineClass(OgnlOps.class.getName(), clazz, 0, clazz.length);
                } else {
                    return OgnlOpsByReflectionTest.class.getClassLoader().loadClass(name);
                }
            }
        };
        clazz = classLoader.loadClass(OgnlOps.class.getName());
    }

    private Object invoke(String method, Object... args)
            throws Exception {
        Class[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            classes[i] = Object.class;
        }
        return clazz.getMethod(method, classes).invoke(null, args);
    }

    private Object invoke(String method, Class[] classes, Object... args)
            throws Exception {
        return clazz.getMethod(method, classes).invoke(null, args);
    }


    @Test
    public void testPatchedBooleanValue() throws Exception {
        Assertions.assertThat((boolean) invoke("booleanValue", "true")).isTrue();
        assertThat((boolean) invoke("booleanValue", "false")).isFalse();

        assertThat((boolean) invoke("booleanValue", "yes")).isTrue();
        assertThat((boolean) invoke("booleanValue", "no")).isFalse();

        assertThat((boolean) invoke("booleanValue", "on")).isTrue();
        assertThat((boolean) invoke("booleanValue", "off")).isFalse();
    }

    @Test
    public void testNegate() throws Exception {
        assertThat(invoke("negate", -1)).isEqualTo(1);
        assertThat(invoke("negate", -1.0)).isEqualTo(1.0);
        assertThat(invoke("negate", new BigInteger("1"))).isEqualTo(new BigInteger("-1"));
        assertThat(invoke("negate", new BigDecimal(1.5))).isEqualTo(new BigDecimal(-1.5));
    }

    @Test
    public void testRemainder() throws Exception {
        assertThat(invoke("remainder", 4, 2)).isEqualTo(0);
        assertThat(invoke("remainder", 4.0, 2)).isEqualTo(0.0);
        assertThat(invoke("remainder", new BigInteger("4"), 2)).isEqualTo(new BigInteger("0"));
        assertThat(invoke("remainder", new BigDecimal("4.0"), 2)).isEqualTo(new BigInteger("0"));
    }

    @Test
    public void testDivide() throws Exception {
        assertThat(invoke("divide", 4, 2)).isEqualTo(2);
        assertThat(invoke("divide", 4.0, 2)).isEqualTo(2.0);
        assertThat(invoke("divide", new BigInteger("4"), 2)).isEqualTo(new BigInteger("2"));
        assertThat(invoke("divide", new BigDecimal("4.0"), 2)).isEqualTo(new BigDecimal("2.0"));
    }

    @Test
    public void testMultiply() throws Exception {
        assertThat(invoke("multiply", 4, 2)).isEqualTo(8);
        assertThat(invoke("multiply", 4.0, 2)).isEqualTo(8.0);
        assertThat(invoke("multiply", new BigInteger("4"), 2)).isEqualTo(new BigInteger("8"));
        assertThat(invoke("multiply", new BigDecimal("4.0"), 2)).isEqualTo(new BigDecimal("8.0"));
    }

    @Test
    public void testSubtract() throws Exception {
        assertThat(invoke("subtract", 4, 2)).isEqualTo(2);
        assertThat(invoke("subtract", 4.0, 2)).isEqualTo(2.0);
        assertThat(invoke("subtract", new BigInteger("4"), 2)).isEqualTo(new BigInteger("2"));
        assertThat(invoke("subtract", new BigDecimal("4.0"), 2)).isEqualTo(new BigDecimal("2.0"));
    }

    @Test
    public void testAdd() throws Exception {
        assertThat(invoke("add", 4, 2)).isEqualTo(6);
        assertThat(invoke("add", 4.0, 2)).isEqualTo(6.0);
        assertThat(invoke("add", new BigInteger("4"), 2)).isEqualTo(new BigInteger("6"));
        assertThat(invoke("add", new BigDecimal("4.0"), 2)).isEqualTo(new BigDecimal("6.0"));
        // The add method can also be used on String resulting in a concatenation
        assertThat(invoke("add", "4", "2")).isEqualTo("42");
        assertThat(invoke("add", "foo", "bar")).isEqualTo("foobar");
    }

    @Test
    public void testShifts() throws Exception {
        assertThat(invoke("shiftLeft", 30, 2)).isEqualTo(120);
        assertThat(invoke("shiftLeft", new BigInteger("30"), 2)).isEqualTo(new BigInteger("120"));
        assertThat(invoke("shiftRight", 30, 2)).isEqualTo(7);
        assertThat(invoke("shiftRight", new BigInteger("30"), 2)).isEqualTo(new BigInteger("7"));
        assertThat(invoke("unsignedShiftRight", 30, 2)).isEqualTo(7);
        assertThat(invoke("unsignedShiftRight", 30l, 2)).isEqualTo(7l);
        assertThat(invoke("unsignedShiftRight", new BigInteger("30"), 2)).isEqualTo(new BigInteger("7"));
    }

    @Test
    public void testIn() throws Exception {
        assertThat((boolean) invoke("in", "b", Arrays.asList("a", "b", "c"))).isTrue();
        assertThat((boolean) invoke("in", "d", Arrays.asList("a", "b", "c"))).isFalse();
        assertThat((boolean) invoke("in", "b", new String[]{"a", "b", "c"})).isTrue();
        assertThat((boolean) invoke("in", "d", new String[]{"a", "b", "c"})).isFalse();
        assertThat((boolean) invoke("in", "d", null)).isFalse();
    }

    @Test
    public void testLessAndGreater() throws Exception {
        assertThat((boolean) invoke("less", 1, 2)).isTrue();
        assertThat((boolean) invoke("less", "1", "2")).isTrue();
        assertThat((boolean) invoke("less", 2, 1)).isFalse();
        assertThat((boolean) invoke("greater", 2, 1)).isTrue();
        assertThat((boolean) invoke("greater", "2", "1")).isTrue();
    }

    @Test
    public void testBinaryOperator() throws Exception {
        assertThat(invoke("binaryAnd", "42", "8")).isEqualTo(new BigInteger("8"));
        assertThat(invoke("binaryAnd", new BigInteger("42"), "8")).isEqualTo(new BigInteger("8"));
        assertThat(invoke("binaryOr", "42", "8")).isEqualTo(new BigInteger("42"));
        assertThat(invoke("binaryOr", new BigInteger("42"), "8")).isEqualTo(new BigInteger("42"));
        assertThat(invoke("binaryXor", "42", "8")).isEqualTo(new BigInteger("34"));
        assertThat(invoke("binaryXor", new BigInteger("42"), "8")).isEqualTo(new BigInteger("34"));
    }

    @Test
    public void testNewReal() throws Exception {
        Class[] classes = new Class[]{Integer.TYPE, Double.TYPE};
        assertThat(invoke("newReal", classes, NumericTypes.FLOAT, 42.0)).isEqualTo(42f);
        assertThat(invoke("newReal", classes, NumericTypes.DOUBLE, 42.0)).isEqualTo(42d);
    }

    @Test
    public void testNewInteger() throws Exception {
        Class[] classes = new Class[]{Integer.TYPE, Long.TYPE};
        assertThat(invoke("newInteger", classes, NumericTypes.INT, 42l)).isEqualTo(42);
        assertThat(invoke("newInteger", classes, NumericTypes.LONG, 42l)).isEqualTo(42l);
        assertThat(invoke("newInteger", classes, NumericTypes.BYTE, 8)).isEqualTo(new Byte("8"));
        assertThat(invoke("newInteger", classes, NumericTypes.SHORT, 42)).isEqualTo(new Short("42"));

        // Also works with float and double, but only if there are no decimal part
        assertThat(invoke("newInteger", classes, NumericTypes.FLOAT, 42)).isEqualTo(42f);
        assertThat(invoke("newInteger", classes, NumericTypes.DOUBLE, 42)).isEqualTo(42d);
    }

    @Test
    public void testConvertValue() throws Exception {
        Class[] classes = new Class[]{Object.class, Class.class};
        // Null value
        assertThat(invoke("convertValue", classes, null, Long.TYPE)).isEqualTo(0l);
        assertThat(invoke("convertValue", classes, null, String.class)).isNull();

        // Primitive
        assertThat(invoke("convertValue", classes, "42", Integer.class)).isEqualTo(42);
        assertThat(invoke("convertValue", classes, "42", Integer.TYPE)).isEqualTo(42);
        assertThat(invoke("convertValue", classes, "42", Byte.class)).isEqualTo(Byte.valueOf("42"));
        assertThat(invoke("convertValue", classes, "42", Byte.TYPE)).isEqualTo(Byte.valueOf("42"));
        assertThat(invoke("convertValue", classes, "42", Short.class)).isEqualTo(Short.valueOf("42"));
        assertThat(invoke("convertValue", classes, "42", Short.TYPE)).isEqualTo(Short.valueOf("42"));
        assertThat(invoke("convertValue", classes, String.valueOf((int) 'c'), Character.class)).isEqualTo('c');
        assertThat(invoke("convertValue", classes, String.valueOf((int) 'c'), Character.TYPE)).isEqualTo('c');
        assertThat(invoke("convertValue", classes, "42", Long.class)).isEqualTo(42l);
        assertThat(invoke("convertValue", classes, "42", Long.TYPE)).isEqualTo(42l);
        assertThat(invoke("convertValue", classes, "true", Boolean.class)).isEqualTo(true);
        assertThat(invoke("convertValue", classes, "true", Boolean.TYPE)).isEqualTo(true);
        assertThat(invoke("convertValue", classes, "42", Double.class)).isEqualTo(42.0);
        assertThat(invoke("convertValue", classes, "42", Double.TYPE)).isEqualTo(42.0);
        assertThat(invoke("convertValue", classes, "42", Float.class)).isEqualTo(42.0f);
        assertThat(invoke("convertValue", classes, "42", Float.TYPE)).isEqualTo(42.0f);

        // BigInteger, BigDecimal and String
        assertThat(invoke("convertValue", classes, "42", BigDecimal.class)).isEqualTo(new BigDecimal("42"));
        assertThat(invoke("convertValue", classes, "42", BigInteger.class)).isEqualTo(new BigInteger("42"));
        assertThat(invoke("convertValue", classes, "42", String.class)).isEqualTo("42");

        //Array
        assertThat(invoke("convertValue", classes, new Object[]{1, 2, 3}, (new int[0]).getClass())).isNotNull();
    }

    @Test
    public void testBitNegate() throws Exception {
        assertThat(invoke("bitNegate", "42")).isEqualTo(new BigInteger("-43"));
        assertThat(invoke("bitNegate", new BigInteger("42"))).isEqualTo(new BigInteger("-43"));
        assertThat(invoke("bitNegate", new BigDecimal("42"))).isEqualTo(new BigInteger("-43"));
    }


}
