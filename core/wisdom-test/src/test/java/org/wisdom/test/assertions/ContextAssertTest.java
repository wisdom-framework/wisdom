/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.test.assertions;

import org.junit.Test;
import org.wisdom.test.parents.FakeContext;

import java.io.File;

/**
 * Checks the {@link ContextAssert}.
 */
public class ContextAssertTest {

    @Test
    public void testHasParameter() throws Exception {
        FakeContext context = new FakeContext().setParameter("key", "value");
        ContextAssert.assertThat(context).hasParameter("key", "value");
    }


    @Test
    public void testHasInSession() throws Exception {
        FakeContext context = new FakeContext().addToSession("key", "value");
        ContextAssert.assertThat(context).hasInSession("key", "value");
    }

    @Test
    public void testBodyManagement() throws Exception {
        FakeContext context = new FakeContext().setBody(new Form("wisdom"));
        ContextAssert.assertThat(context)
                .hasBody(Form.class, new Form("wisdom"))
                .hasBody("wisdom")
                .hasBodyMatch("wis(.*)")
                .hasInBody("wis");
    }

    @Test
    public void testMultipart() throws Exception {
        FakeContext context = new FakeContext().setFormField("test", new File("src/test/resources/foo.txt"));
        ContextAssert.assertThat(context).isMultipart();

        context = new FakeContext().setParameter("foo", "bar");
        ContextAssert.assertThat(context).isNotMultipart();
    }

    private class Form {
        public final String name;

        private Form(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Form form = (Form) o;
            return name.equals(form.name);

        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

}