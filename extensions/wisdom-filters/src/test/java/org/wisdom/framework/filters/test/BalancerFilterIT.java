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
package org.wisdom.framework.filters.test;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.wisdom.api.http.Status;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;


public class BalancerFilterIT extends WisdomBlackBoxTest {

    /**
     * Deploy the test bundle as we need the messages.
     */
    @BeforeClass
    public static void init() throws BundleException {
        installTestBundle();
    }

    @AfterClass
    public static void cleanup() throws BundleException {
        removeTestBundle();
    }

    @Test
    public void checkRoundRobin() throws Exception {
        HttpResponse<String> response = get("/balancer").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        assertThat(response.body()).contains("Perdu").contains("Internet");

        boolean perdus = false;
        if (response.body().contains("Perdus sur Internet")) {
            perdus = true;
        }

        response = get("/balancer").asString();
        assertThat(response.code()).isEqualTo(Status.OK);
        if (perdus) {
            // Should be perdu now
            assertThat(response.body()).contains("Perdu sur l'Internet ?");
        } else {
            // Should be perdus
            assertThat(response.body()).contains("Perdus sur Internet ?");
        }
    }
}
