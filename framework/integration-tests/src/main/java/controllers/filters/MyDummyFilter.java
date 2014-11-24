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
package controllers.filters;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.http.Result;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;

import java.util.regex.Pattern;

@Service
public class MyDummyFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyDummyFilter.class);

    @Override
    public Result call(Route route, RequestContext context) throws Exception {
        LOGGER.info("Request intercepted by {}", this);
        if (context.request().parameterAsBoolean("insertValue")) {
            context.data().put("key", "value");
        }
        if (context.request().parameterAsBoolean("modifyValue")) {
            LOGGER.info("Modifying parameters");
            context.context().headers().put("X-Foo", ImmutableList.of("bar"));
            LOGGER.info("Headers : {}", context.context().headers());
            context.context().form().put("field", ImmutableList.of("value"));
            LOGGER.info("Form : {}", context.context().form());
        }
        Result result = context.proceed();
        result.getHeaders().put("X-Filtered", "true");
        return result;
    }

    @Override
    public Pattern uri() {
        return Pattern.compile("/filter/dummy");
    }

    @Override
    public int priority() {
        return 1000;
    }
}
