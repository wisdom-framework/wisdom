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
package controller;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

/**
 * A controller referencing uri through a constant
 */
class Constants_For_ControllerReferencingConstant {
    public static final String URI = "/constant";
}
public class ControllerReferencingConstant extends DefaultController {
	public static interface Constants {
	    public static final String URI = "/constant";
	}
    public static final String URI = "/constant";

    @Route(method= HttpMethod.GET, uri=URI)
    public Result get() {
        return ok();
    }

    @Route(method= HttpMethod.POST, uri=Constants.URI)
    public Result post() {
        return ok();
    }

    @Route(method= HttpMethod.PUT, uri=Constants_For_ControllerReferencingConstant.URI)
    public Result put() {
        return ok();
    }
}
