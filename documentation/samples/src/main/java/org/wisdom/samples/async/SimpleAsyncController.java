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
package org.wisdom.samples.async;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.scheduler.Async;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import java.util.concurrent.Callable;

/**
 * A simple controller demonstrating async result.
 */
@Controller
public class SimpleAsyncController extends DefaultController {

    /**
     * Waits ten second before sending the hello message.
     */
    @Route(method = HttpMethod.GET, uri = "/async/hello/{name}")
    public Result heavyComputation(@Parameter("name") final String name) {
        return async(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                System.out.println(System.currentTimeMillis() + " - Heavy computation executed from " + Thread
                        .currentThread().getName());
                Thread.sleep(10000);
                System.out.println(System.currentTimeMillis() + " -  Heavy computation done " + Thread.currentThread
                        ().getName());
                return ok("Hello " + name);
            }
        });
    }

    @Route(method = HttpMethod.GET, uri = "/async/hello2/{name}")
    @Async
    public Result heavyComputation2(@Parameter("name") final String name) throws InterruptedException {
        System.out.println(System.currentTimeMillis() + " - Heavy computation executed from " + Thread
                .currentThread().getName());
        Thread.sleep(10000);
        System.out.println(System.currentTimeMillis() + " -  Heavy computation done " + Thread.currentThread
                ().getName());
        return ok("Hello " + name);
    }

}

