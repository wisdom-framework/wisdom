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
package org.wisdom.template.thymeleaf.dialect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IContextVariableRestriction;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.context.VariablesMap;
import org.thymeleaf.expression.ExpressionEvaluatorObjects;
import org.thymeleaf.standard.expression.OgnlVariableExpressionEvaluator;
import org.thymeleaf.standard.expression.StandardExpressionExecutionContext;
import org.thymeleaf.standard.expression.StandardVariableRestrictions;
import org.wisdom.api.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An extended version of the standard evaluator injecting the `routes` expression object.
 */
public class ExtendedOGNLExpressionEvaluator extends OgnlVariableExpressionEvaluator {

    public static final ExtendedOGNLExpressionEvaluator INSTANCE = new ExtendedOGNLExpressionEvaluator();

    protected Map<String, Object> computeAdditionalContextVariables(IProcessingContext processingContext) {
        Map<String, Object> var = new HashMap<>();
        var.put(Routes.OBJECT_NAME, processingContext.getContext().getVariables().get(Routes.ROUTES_VAR));
        final Context context = Context.CONTEXT.get();
        if (context != null) {
            var.put("context", context);
            var.put("session", context.session());
            var.put("flash", context.flash());
            var.put("request", context.request());
            var.put("parameters", context.parameters());
        }
        return var;
    }

    @Override
    public String toString() {
        return "OGNL extended by Wisdom";
    }

    @Override
    protected boolean shouldApplyOgnlBooleanFix() {
        return false;
    }
}
