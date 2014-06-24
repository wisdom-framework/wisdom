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
package org.wisdom.router;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.wisdom.api.Controller;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.router.parameters.ActionParameter;
import org.wisdom.api.router.parameters.Source;

/**
 * the on receive callback is a bit different as we need to handle the wrapping of the received message.
 */
public class OnMessageWebSocketCallback extends DefaultWebSocketCallback {

    public OnMessageWebSocketCallback(Controller controller, Method method, String uri, WebSocketRouter router) {
        super(controller, method, uri, router);
    }

    @Override
    public List<ActionParameter> buildArguments(Method method) {
        List<ActionParameter> arguments = new ArrayList<>();
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] typesOfParameters = method.getParameterTypes();
        Type[] genericTypeOfParameters = method.getGenericParameterTypes();
        for (int i = 0; i < annotations.length; i++) {
            boolean sourceDetected = false;
            for (int j = 0; !sourceDetected && j < annotations[i].length; j++) {
                Annotation annotation = annotations[i][j];
                if (annotation instanceof Parameter) {
                    Parameter parameter = (Parameter) annotation;
                    arguments.add(new ActionParameter(parameter.value(),
                            Source.PARAMETER, typesOfParameters[i], genericTypeOfParameters[i]));
                    sourceDetected = true;
                }
                if (annotation instanceof Body) {
                    arguments.add(new ActionParameter(null,
                            Source.BODY, typesOfParameters[i], genericTypeOfParameters[i]));
                    sourceDetected = true;
                }
            }
            if (!sourceDetected) {
                // All parameters must have been annotated.
                WebSocketRouter.getLogger().error("The method {} has a parameter without annotations indicating " +
                        " the injected data. Only @Parameter and @Body annotations are supported in web sockets " +
                        "callbacks receiving events",
                        method.getName());
                return null;
            }
        }
        return arguments;
    }
}
