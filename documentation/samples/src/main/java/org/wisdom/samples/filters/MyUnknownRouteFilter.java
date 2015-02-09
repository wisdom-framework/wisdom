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
package org.wisdom.samples.filters;

import org.slf4j.LoggerFactory;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;

import java.util.regex.Pattern;

/**
 * A filter redirecting to a custom 404 page.
 */
@Service
public class MyUnknownRouteFilter implements Filter {

    @Override
    public Result call(Route route, RequestContext context) throws Exception {
        System.out.println("Filter called...");
        Result result = context.proceed();
        System.out.println("==>" + result);
        if (result.getStatusCode() == 404) {
            LoggerFactory.getLogger(MyUnknownRouteFilter.class).info("Route " + route.getUrl() + " not found");

            return Results.notFound("<h1>Sorry guy, nobody here...</h1>").html();
        }
        return result;
    }

    @Override
    public Pattern uri() {
        return Pattern.compile("/samples/.*");
    }

    @Override
    public int priority() {
        return 1000;
    }
}
