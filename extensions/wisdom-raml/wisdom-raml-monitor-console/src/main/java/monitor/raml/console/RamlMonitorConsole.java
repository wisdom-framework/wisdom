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
 * <p>
 * Provides a MonitorExtension service for the raml files available in the assets.
 * Each raml spec extension is published under the RAML catefory. 
 *</p>
 *
 * @author barjo
 */
public class RamlMonitorConsole implements MonitorExtension {
    private final String label;

    /**
     * Create a new RamlMonitorConsole linked to the raml spec of given name.
     *
     * @param fileName The raml file name. Usually the name of a Controller.
     */
    public RamlMonitorConsole(String fileName) {
        label = fileName;
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public String label() {
        return label;
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public String url() {
        return "/monitor/raml/"+ label;
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public String category() {
        return "RAML";
    }
}
