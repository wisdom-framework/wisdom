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

import org.junit.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class ReflectionHelperTest {

    @Test
    public void testGetTypeArgumentAndClassUsingSimpleType() throws Exception {
        Method method1 = this.getClass().getMethod("method1", String.class);
        Method method2 = this.getClass().getMethod("method2", List.class);
        Method method3 = this.getClass().getMethod("method3", List.class);
        List<ClassTypePair> ctp1 = ReflectionHelper.getTypeArgumentAndClass(method1.getGenericParameterTypes()[0]);
        List<ClassTypePair> ctp2 = ReflectionHelper.getTypeArgumentAndClass(method2.getGenericParameterTypes()[0]);
        List<ClassTypePair> ctp3 = ReflectionHelper.getTypeArgumentAndClass(method3.getGenericParameterTypes()[0]);
        assertThat(ctp1).isEmpty();
        assertThat(ctp2.get(0).rawClass()).isEqualTo(String.class);
        assertThat(ctp2.get(0).type().toString()).contains(String.class.getName());
        assertThat(ctp3.get(0).rawClass()).isEqualTo(List.class);
        assertThat(ctp3.get(0).type().toString()).contains("List").contains("String");
    }

    public void method1(String string) {
        // ...
    }

    public void method2(List<String> list) {
        // ...
    }

    public void method3(List<List<String>> listOfList) {
        // ...
    }


    @Test
    public void testGetTypeArgumentAndClassUsingWildcard() throws Exception {
        Method method1 = this.getClass().getMethod("wildcard", List.class);

        List<ClassTypePair> ctp1 = ReflectionHelper.getTypeArgumentAndClass(method1.getGenericParameterTypes()[0]);
        assertThat(ctp1.get(0).rawClass()).isEqualTo(String.class);
        assertThat(ctp1.get(0).type()).isInstanceOf(WildcardType.class);
    }

    public void wildcard(List<? extends String> list) {
        // ...
    }

    @Test
    public void testGetTypeArgumentAndClassUsingVariable() throws Exception {
        Method method1 = this.getClass().getMethod("var", List.class);

        List<ClassTypePair> ctp1 = ReflectionHelper.getTypeArgumentAndClass(method1.getGenericParameterTypes()[0]);
        assertThat(ctp1.get(0).rawClass()).isEqualTo(String.class);
        assertThat(ctp1.get(0).type()).isInstanceOf(TypeVariable.class);
    }

    public <A extends String> void var(List<A> list) {
        // ...
    }

    @Test
    public void testGetTypeArgumentAndClassUsingGenericArray() throws Exception {
        Method method1 = getMethod("array");

        List<ClassTypePair> ctp1 = ReflectionHelper.getTypeArgumentAndClass(method1.getGenericParameterTypes()[0]);
        assertThat(ctp1.get(0).rawClass()).isEqualTo(String[].class);
        assertThat(ctp1.get(0).type()).isInstanceOf(GenericArrayType.class);
    }

    public <A extends String> void array(List<A[]> array) {
        // ...
    }

    public Method getMethod(String name) {
        for (Method method : this.getClass().getMethods()) {
            if (name.equals(method.getName())) {
                return method;
            }
        }
        return null;
    }


}