/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2015 Wisdom Framework
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
package monitor.raml.console;

import org.wisdom.monitor.service.MonitorExtension;

/**
 * @author barjo
 */
public class RamlMonitorConsole implements MonitorExtension {
    private final String label;

    public RamlMonitorConsole(String fileName) {
        label = fileName;
    }

    // The menu label
    @Override
    public String label() {
        return label;
    }

    // The url of the page
    @Override
    public String url() {
        return "/monitor/raml/"+ label;
    }

    // The category (to structure the menu)
    @Override
    public String category() {
        return "RAML";
    }
}
