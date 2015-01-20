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
package org.wisdom.monitor.extensions.executors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.concurrent.ManagedExecutorService;

import java.io.IOException;

@Service(Module.class)
public class ManagedExecutorModule extends SimpleModule {

    public ManagedExecutorModule() {
        super("ManagedExecutorService Module");
        addSerializer(ManagedExecutorService.class, new JsonSerializer<ManagedExecutorService>() {
            @Override
            public void serialize(ManagedExecutorService executor, JsonGenerator jsonGenerator,
                                  SerializerProvider serializerProvider)
                    throws IOException {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("name", executor.name());
                jsonGenerator.writeNumberField("core", executor.getCorePoolSize());
                jsonGenerator.writeNumberField("max", executor.getMaximumPoolSize());
                jsonGenerator.writeNumberField("largest", executor.getLargestPoolSize());
                jsonGenerator.writeNumberField("size", executor.getPoolSize());
                jsonGenerator.writeNumberField("active", executor.getActiveCount());
                jsonGenerator.writeNumberField("queue", executor.getQueue().size());
                jsonGenerator.writeNumberField("hung", executor.getHungTasks().size());
                jsonGenerator.writeNumberField("completed", executor.getCompletedTaskCount());
                final ManagedExecutorService.ExecutionStatistics statistics = executor.getExecutionTimeStatistics();
                jsonGenerator.writeNumberField("avg", statistics.getAverageExecutionTime());
                jsonGenerator.writeNumberField("max_exec", statistics.getMaximumExecutionTime());
                jsonGenerator.writeNumberField("min_exec", statistics.getMinimumExecutionTime());
                jsonGenerator.writeNumberField("total", statistics.getTotalExecutionTime());
                jsonGenerator.writeEndObject();
            }
        });
    }
}
