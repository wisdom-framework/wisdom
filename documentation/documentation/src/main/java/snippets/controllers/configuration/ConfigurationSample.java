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
package snippets.controllers.configuration;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;
import snippets.controllers.MyData;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Component
public class ConfigurationSample {
    private static final MyData DEFAULT_DATA = new MyData();
    // tag::retrieve[]
    @Requires
    ApplicationConfiguration configuration;

    public void readConfiguration() {
        System.out.println(configuration.get("my-application-configuration.my-key"));
        // or
        Configuration conf =
                configuration.getConfiguration("my-application-configuration");
        System.out.println(conf.get("my value"));
    }
    // end::retrieve[]

    public void methods() {
        // tag::methods[]
        String v = configuration.get("key");
        v = configuration.getWithDefault("key", "default");
        v = configuration.getOrDie("key");

        boolean b = configuration.getBoolean("key");
        b = configuration.getBooleanWithDefault("key", true);
        b = configuration.getBooleanOrDie("key");

        int i = configuration.getInteger("key");
        i = configuration.getIntegerWithDefault("key", 5);
        i = configuration.getIntegerOrDie("key");

        long l = configuration.getLong("key");
        l = configuration.getLongWithDefault("key", 5l);
        l = configuration.getLongOrDie("key");

        // The application base directory
        File baseDir = configuration.getBaseDir();

        // Convert the value to a MyData object, using 'converters'
        MyData data = configuration.get("key", MyData.class);
        data = configuration.get("key", MyData.class, "data1,data2,data3");
        data = configuration.get("key", MyData.class, DEFAULT_DATA);

        // Durations
        // Durations are converted to the given unit, for instance for:
        // key = 1 minute
        // key = 2 hours
        long duration = configuration.getDuration("key", TimeUnit.SECONDS);
        duration = configuration.getDuration("key", TimeUnit.SECONDS, 2);


        // Sizes in bytes to avoid the confusion between powers of 1000 and powers of 1024
        // For instance for
        // key = 1 kB => 1000 bytes
        // key = 1 K => 1024 bytes
        long size = configuration.getBytes("key");
        size = configuration.getBytes("key", 2048);

        //end::methods[]
    }


}
