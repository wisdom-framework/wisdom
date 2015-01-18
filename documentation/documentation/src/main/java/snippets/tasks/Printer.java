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
package snippets.tasks;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.wisdom.api.annotations.scheduler.Every;
import org.wisdom.api.scheduler.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * A component with scheduled tasks.
 */
// tag::scheduled[]
@Component
@Provides
@Instantiate
public class Printer implements Scheduled {

    @Every("1h")
    public void print() {
        System.out.println(new SimpleDateFormat().format(new Date()) + " - Hello ...");
    }

    @Every(period = 1, unit = TimeUnit.DAYS)
    public void cleanup() {
        System.out.println("Cleanup !");
    }
}
// end::scheduled[]
