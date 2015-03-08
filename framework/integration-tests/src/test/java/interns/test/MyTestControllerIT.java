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
package interns.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.test.assertions.WisdomAssertions.assertThat;

/**
 * Test a controller from src/test/java.
 */
public class MyTestControllerIT extends WisdomBlackBoxTest {

    @BeforeClass
    public static void init() throws BundleException {
        WisdomBlackBoxTest.installTestBundle();
    }

    @AfterClass
    public static void cleanup() throws BundleException {
        WisdomBlackBoxTest.removeTestBundle();
    }

    @Test
    public void test() throws Exception {
        System.out.println(get("/intern/test").asString().body());
        assertThat(get("/intern/test").asJson()).hasStatus(200);
    }

    @Test
    public void test2() throws Exception {
        System.out.println(get("/intern/test").asString().body());
        assertThat(get("/intern/test").asJson()).hasStatus(200);
    }
}
