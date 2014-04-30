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
package sample;

import org.junit.Test;
import org.wisdom.test.parents.WisdomFluentLeniumTest;

import static org.fluentlenium.assertj.FluentLeniumAssertions.assertThat;

/**
 * A UI Test checking that the page contains the correct element.
 * This test is executed in a browser.
 */
public class FluentLeniumIT extends WisdomFluentLeniumTest {

    @Test
    public void testThatTheWelcomePageContentIsCorrect() {
        goTo("/");
        assertThat(find(".lead")).hasText("Wisdom is knowing the right " +
                "path to take. Integrity is taking it.");
    }

}
