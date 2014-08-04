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
package org.wisdom.samples.it;

import org.junit.Test;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.router.Router;
import org.wisdom.samples.hello.MyForm;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.Invocation;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.test.parents.Action.action;

/**
 * Check router.
 */
public class RouterIT extends WisdomTest {

    @Inject
    public Router router;

    @Test
    public void testSampleRoutes() throws Throwable {
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                try {
                    System.out.println("route : " + router.getRouteFor(HttpMethod.GET, "/samples/hello/"));
                    return router.getRouteFor(HttpMethod.GET, "/samples/hello/").invoke();
                } catch(Throwable e) {
                    e.printStackTrace();
                    return Results.internalServerError(e);
                }
            }
        }).invoke();
        System.out.println(toString(result));
        assertThat(status(result)).isEqualTo(OK);

    }

    @Test
    public void testHelloRoute() throws Throwable {
        MyForm myform = new MyForm();
        myform.name = "--wisdom--";
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return router.getRouteFor(HttpMethod.POST, "/samples/hello/result").invoke();
            }
        }).with().body(myform).invoke();
        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).contains("Hello", "--wisdom--");
    }

}
