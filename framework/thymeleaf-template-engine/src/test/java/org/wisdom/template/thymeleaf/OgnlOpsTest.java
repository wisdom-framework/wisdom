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
import ognl.OgnlException;
import ognl.OgnlOps;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Basic OGNL tests, especially the patched part.
 */
public class OgnlOpsTest {

    @Test
    public void testPatchedBooleanValue() {
        Assertions.assertThat(OgnlOps.booleanValue("true")).isTrue();
        assertThat(OgnlOps.booleanValue("false")).isFalse();

        assertThat(OgnlOps.booleanValue("yes")).isTrue();
        assertThat(OgnlOps.booleanValue("no")).isFalse();

        assertThat(OgnlOps.booleanValue("on")).isTrue();
        assertThat(OgnlOps.booleanValue("off")).isFalse();
    }

    @Test
    public void testNegate() {
        assertThat(OgnlOps.negate(-1)).isEqualTo(1);
        assertThat(OgnlOps.negate(-1.0)).isEqualTo(1.0);
        assertThat(OgnlOps.negate(new BigInteger("1"))).isEqualTo(new BigInteger("-1"));
        assertThat(OgnlOps.negate(new BigDecimal(1.5))).isEqualTo(new BigDecimal(-1.5));
    }

    @Test
    public void testRemainder() {
        assertThat(OgnlOps.remainder(4, 2)).isEqualTo(0);
        assertThat(OgnlOps.remainder(4.0, 2)).isEqualTo(0.0);
        assertThat(OgnlOps.remainder(new BigInteger("4"), 2)).isEqualTo(new BigInteger("0"));
        assertThat(OgnlOps.remainder(new BigDecimal("4.0"), 2)).isEqualTo(new BigInteger("0"));
    }

    @Test
    public void testDivide() {
        assertThat(OgnlOps.divide(4, 2)).isEqualTo(2);
        assertThat(OgnlOps.divide(4.0, 2)).isEqualTo(2.0);
        assertThat(OgnlOps.divide(new BigInteger("4"), 2)).isEqualTo(new BigInteger("2"));
        assertThat(OgnlOps.divide(new BigDecimal("4.0"), 2)).isEqualTo(new BigDecimal("2.0"));
    }

    @Test
    public void testMultiply() {
        assertThat(OgnlOps.multiply(4, 2)).isEqualTo(8);
        assertThat(OgnlOps.multiply(4.0, 2)).isEqualTo(8.0);
        assertThat(OgnlOps.multiply(new BigInteger("4"), 2)).isEqualTo(new BigInteger("8"));
        assertThat(OgnlOps.multiply(new BigDecimal("4.0"), 2)).isEqualTo(new BigDecimal("8.0"));
    }

    @Test
    public void testSubtract() {
        assertThat(OgnlOps.subtract(4, 2)).isEqualTo(2);
        assertThat(OgnlOps.subtract(4.0, 2)).isEqualTo(2.0);
        assertThat(OgnlOps.subtract(new BigInteger("4"), 2)).isEqualTo(new BigInteger("2"));
        assertThat(OgnlOps.subtract(new BigDecimal("4.0"), 2)).isEqualTo(new BigDecimal("2.0"));
    }

    @Test
    public void testAdd() {
        assertThat(OgnlOps.add(4, 2)).isEqualTo(6);
        assertThat(OgnlOps.add(4.0, 2)).isEqualTo(6.0);
        assertThat(OgnlOps.add(new BigInteger("4"), 2)).isEqualTo(new BigInteger("6"));
        assertThat(OgnlOps.add(new BigDecimal("4.0"), 2)).isEqualTo(new BigDecimal("6.0"));
        // The add method can also be used on String resulting in a concatenation
        assertThat(OgnlOps.add("4", "2")).isEqualTo("42");
        assertThat(OgnlOps.add("foo", "bar")).isEqualTo("foobar");
    }

    @Test
    public void testShifts() {
        assertThat(OgnlOps.shiftLeft(30, 2)).isEqualTo(120);
        assertThat(OgnlOps.shiftLeft(new BigInteger("30"), 2)).isEqualTo(new BigInteger("120"));
        assertThat(OgnlOps.shiftRight(30, 2)).isEqualTo(7);
        assertThat(OgnlOps.shiftRight(new BigInteger("30"), 2)).isEqualTo(new BigInteger("7"));
        assertThat(OgnlOps.unsignedShiftRight(30, 2)).isEqualTo(7);
        assertThat(OgnlOps.unsignedShiftRight(30l, 2)).isEqualTo(7l);
        assertThat(OgnlOps.unsignedShiftRight(new BigInteger("30"), 2)).isEqualTo(new BigInteger("7"));
    }

    @Test
    public void testIn() throws OgnlException {
        assertThat(OgnlOps.in("b", Arrays.asList("a", "b", "c"))).isTrue();
        assertThat(OgnlOps.in("d", Arrays.asList("a", "b", "c"))).isFalse();
        assertThat(OgnlOps.in("b", new String[] {"a", "b", "c"})).isTrue();
        assertThat(OgnlOps.in("d", new String[] {"a", "b", "c"})).isFalse();
        assertThat(OgnlOps.in("d", null)).isFalse();
    }

    @Test
    public void testLessAndGreater() throws OgnlException {
        assertThat(OgnlOps.less(1, 2)).isTrue();
        assertThat(OgnlOps.less("1", "2")).isTrue();
        assertThat(OgnlOps.less(2, 1)).isFalse();
        assertThat(OgnlOps.greater(2, 1)).isTrue();
        assertThat(OgnlOps.greater("2", "1")).isTrue();
    }

    @Test
    public void testBinaryOperator() {
        assertThat(OgnlOps.binaryAnd("42", "8")).isEqualTo(new BigInteger("8"));
        assertThat(OgnlOps.binaryAnd(new BigInteger("42"), "8")).isEqualTo(new BigInteger("8"));
        assertThat(OgnlOps.binaryOr("42", "8")).isEqualTo(new BigInteger("42"));
        assertThat(OgnlOps.binaryOr(new BigInteger("42"), "8")).isEqualTo(new BigInteger("42"));
        assertThat(OgnlOps.binaryXor("42", "8")).isEqualTo(new BigInteger("34"));
        assertThat(OgnlOps.binaryXor(new BigInteger("42"), "8")).isEqualTo(new BigInteger("34"));
    }

    @Test
    public void testNewReal() {
        assertThat(OgnlOps.newReal(NumericTypes.FLOAT, 42.0)).isEqualTo(42f);
        assertThat(OgnlOps.newReal(NumericTypes.DOUBLE, 42.0)).isEqualTo(42d);
    }

    @Test
    public void testNewInteger() {
        assertThat(OgnlOps.newInteger(NumericTypes.INT, 42l)).isEqualTo(42);
        assertThat(OgnlOps.newInteger(NumericTypes.LONG, 42l)).isEqualTo(42l);
        assertThat(OgnlOps.newInteger(NumericTypes.BYTE, 8)).isEqualTo(new Byte("8"));
        assertThat(OgnlOps.newInteger(NumericTypes.SHORT, 42)).isEqualTo(new Short("42"));

        // Also works with float and double, but only if there are no decimal part
        assertThat(OgnlOps.newInteger(NumericTypes.FLOAT, 42)).isEqualTo(42f);
        assertThat(OgnlOps.newInteger(NumericTypes.DOUBLE, 42)).isEqualTo(42d);
    }

    @Test
    public void testConvertValue() {
        // Null value
        assertThat(OgnlOps.convertValue(null, Long.TYPE)).isEqualTo(0l);
        assertThat(OgnlOps.convertValue(null, String.class)).isNull();

        // Primitive
        assertThat(OgnlOps.convertValue("42", Integer.class)).isEqualTo(42);
        assertThat(OgnlOps.convertValue("42", Integer.TYPE)).isEqualTo(42);
        assertThat(OgnlOps.convertValue("42", Byte.class)).isEqualTo(Byte.valueOf("42"));
        assertThat(OgnlOps.convertValue("42", Byte.TYPE)).isEqualTo(Byte.valueOf("42"));
        assertThat(OgnlOps.convertValue("42", Short.class)).isEqualTo(Short.valueOf("42"));
        assertThat(OgnlOps.convertValue("42", Short.TYPE)).isEqualTo(Short.valueOf("42"));
        assertThat(OgnlOps.convertValue(String.valueOf((int) 'c'), Character.class)).isEqualTo('c');
        assertThat(OgnlOps.convertValue(String.valueOf((int) 'c'), Character.TYPE)).isEqualTo('c');
        assertThat(OgnlOps.convertValue("42", Long.class)).isEqualTo(42l);
        assertThat(OgnlOps.convertValue("42", Long.TYPE)).isEqualTo(42l);
        assertThat(OgnlOps.convertValue("true", Boolean.class)).isEqualTo(true);
        assertThat(OgnlOps.convertValue("true", Boolean.TYPE)).isEqualTo(true);
        assertThat(OgnlOps.convertValue("42", Double.class)).isEqualTo(42.0);
        assertThat(OgnlOps.convertValue("42", Double.TYPE)).isEqualTo(42.0);
        assertThat(OgnlOps.convertValue("42", Float.class)).isEqualTo(42.0f);
        assertThat(OgnlOps.convertValue("42", Float.TYPE)).isEqualTo(42.0f);

        // BigInteger, BigDecimal and String
        assertThat(OgnlOps.convertValue("42", BigDecimal.class)).isEqualTo(new BigDecimal("42"));
        assertThat(OgnlOps.convertValue("42", BigInteger.class)).isEqualTo(new BigInteger("42"));
        assertThat(OgnlOps.convertValue("42", String.class)).isEqualTo("42");

        //Array
        assertThat(OgnlOps.convertValue(new Object[] {1, 2,3}, (new int[0]).getClass())).isNotNull();
    }

    @Test
    public void testBitNegate() {
        assertThat(OgnlOps.bitNegate("42")).isEqualTo(new BigInteger("-43"));
        assertThat(OgnlOps.bitNegate(new BigInteger("42"))).isEqualTo(new BigInteger("-43"));
        assertThat(OgnlOps.bitNegate(new BigDecimal("42"))).isEqualTo(new BigInteger("-43"));
    }


}
