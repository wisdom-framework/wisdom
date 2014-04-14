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
package org.wisdom.monitor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.JacksonModuleRepository;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.monitor.service.MonitorExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the central part of the monitor application.
 * <p/>
 * It lists the different extensions and provides the adequate data to build monitor UI menu.
 */
@Controller
public class MonitorCenter extends DefaultController {

    @Requires(specification = MonitorExtension.class)
    List<MonitorExtension> extensions = new ArrayList<>();

    @Requires
    JacksonModuleRepository repository;

    private final static Logger LOGGER = LoggerFactory.getLogger(MonitorCenter.class);

    private SimpleModule module;

    @Requires
    ApplicationConfiguration configuration;


    @Validate
    public void start() {
        module = new SimpleModule(MonitorExtension.class.getName());
        module.addSerializer(MonitorExtension.class, new JsonSerializer<MonitorExtension>() {
            @Override
            public void serialize(MonitorExtension monitorExtension, JsonGenerator jsonGenerator,
                                  SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("label", monitorExtension.label());
                jsonGenerator.writeStringField("url", monitorExtension.url());
                jsonGenerator.writeStringField("category", monitorExtension.category());
                jsonGenerator.writeEndObject();
            }
        });
        repository.register(module);
    }

    @Invalidate
    public void stop() {
        if (module != null) {
            repository.unregister(module);
        }
    }

    @Route(method = HttpMethod.GET, uri = "/monitor")
    public Result index() {
        String extension = configuration.getWithDefault("monitor.default", "dashboard");
        return redirect(getExtensionByName(extension).url());
    }

    private MonitorExtension getExtensionByName(String name) {
        for (MonitorExtension extension : extensions) {
            if (extension.label().equals(name)) {
                return extension;
            }
        }
        return null;
    }

    @Route(method = HttpMethod.GET, uri = "/monitor/extensions")
    public Result getExtensions() {
        return ok(extensions).json();
    }

}
