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
package controllers.interceptor;

import org.wisdom.api.annotations.Service;
import org.wisdom.api.http.Result;
import org.wisdom.api.interception.Interceptor;
import org.wisdom.api.interception.RequestContext;

import java.net.URL;

/**
 * An interceptor populating values in the request scope and in the session.
 */
@Service(Interceptor.class)
public class MyCustomInterceptor extends Interceptor<MyCustomAnnotation> {

    @Override
    public Result call(MyCustomAnnotation configuration, RequestContext context) throws Exception {
        context.data().put("url", new URL("http://perdu.com"));
        context.context().session().put("data", configuration.value());
        return context.proceed();
    }

    @Override
    public Class<MyCustomAnnotation> annotation() {
        return MyCustomAnnotation.class;
    }
}
