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
package org.wisdom.samples.bean;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.BeanParameter;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import javax.validation.Valid;

/**
 * Shows how to use the {@link org.wisdom.api.annotations.BeanParameter} annotation.
 */
@Controller
public class BeanExample extends DefaultController {

    /**
     * The {@link org.wisdom.api.annotations.BeanParameter} is used to inject the parameter. It can be combined with
     * the {@link javax.validation.Valid} annotation to ensure the validity of the resulting object.
     *
     * @param bean the instantiated bean
     * @return the json form of the bean
     */
    @Route(method = HttpMethod.GET, uri = "/bean")
    public Result get(@Valid @BeanParameter Bean bean) {
        return ok(bean).json();
    }
}
