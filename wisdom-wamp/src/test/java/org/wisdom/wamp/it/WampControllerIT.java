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
package org.wisdom.wamp.it;

import org.junit.Test;
import org.wisdom.test.parents.ControllerTest;
import org.wisdom.wamp.WampController;
import org.wisdom.wamp.services.Wamp;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Tests checking the WAMP support behavior.
 */
public class WampControllerIT extends ControllerTest {

    @Inject
    WampController controller;

    @Inject
    Wamp wamp;

    @Test
    public void testExposition() throws Exception {
        assertThat(wamp).isNotNull();
        assertThat(wamp.getWampBaseUrl()).contains("http://localhost:", "/wamp");
    }
}
