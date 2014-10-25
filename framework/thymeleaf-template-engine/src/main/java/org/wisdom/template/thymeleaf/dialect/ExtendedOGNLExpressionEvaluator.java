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

import ognl.ClassResolver;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Configuration;
import org.thymeleaf.cache.ICache;
import org.thymeleaf.cache.ICacheManager;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.expression.*;
import org.wisdom.api.http.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * An extended version of the standard evaluator injecting the `routes` expression object.
 */
public class ExtendedOGNLExpressionEvaluator implements IStandardVariableExpressionEvaluator {

    public static final ExtendedOGNLExpressionEvaluator INSTANCE = new ExtendedOGNLExpressionEvaluator();
    private static final Logger LOGGER = LoggerFactory.getLogger(OgnlVariableExpressionEvaluator.class);
    private static final String OGNL_CACHE_PREFIX = "{ognl}";

    public static final String BUNDLE_VAR_KEY = "__bundle__";

    protected Map<String, Object> computeAdditionalContextVariables(IProcessingContext processingContext) {
        Map<String, Object> var = new HashMap<>();
        var.put(Routes.OBJECT_NAME, processingContext.getContext().getVariables().get(Routes.ROUTES_VAR));
        final Context context = Context.CONTEXT.get();
        if (context != null) {
            var.put("http", context);
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

    public final Object evaluate(final Configuration configuration,
                                 final IProcessingContext processingContext, final String expression,
                                 final StandardExpressionExecutionContext expContext, final boolean useSelectionAsRoot) {

        try {

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("OGNL expression: evaluating expression \"{}\" on target", expression);
            }

            Object expressionTree = null;
            ICache<String, Object> cache = null;

            if (configuration != null) {
                final ICacheManager cacheManager = configuration.getCacheManager();
                if (cacheManager != null) {
                    cache = cacheManager.getExpressionCache();
                }
            }

            if (cache != null) {
                expressionTree = cache.get(OGNL_CACHE_PREFIX + expression);
            }


            if (expressionTree == null) {
                expressionTree = ognl.Ognl.parseExpression(expression);
                if (cache != null && null != expressionTree) {
                    cache.put(OGNL_CACHE_PREFIX + expression, expressionTree);
                }
            }

            final OgnlContext ctxt = new OgnlContext();
            ctxt.putAll(processingContext.getExpressionObjects());

            final Map<String, Object> additionalContextVariables = computeAdditionalContextVariables(processingContext);
            if (additionalContextVariables != null) {
                ctxt.putAll(additionalContextVariables);
            }

            final Object evaluationRoot =
                    (useSelectionAsRoot ?
                            processingContext.getExpressionSelectionEvaluationRoot() :
                            processingContext.getExpressionEvaluationRoot());

            // If we have a bundle set, customize the class loading.
            if ((ctxt.get("vars") instanceof Map) && ((Map) ctxt.get("vars")).containsKey(BUNDLE_VAR_KEY)) {
                final Bundle bundle = (Bundle) ((Map) ctxt.get("vars")).get(BUNDLE_VAR_KEY);
                Ognl.setClassResolver(ctxt, new ClassResolver() {
                    /**
                     * Loads a class. This method is called when the processing of a template requires a class. The
                     * class loading defines in this method uses the {@link org.osgi.framework.Bundle} object passed
                     * in the context. If the class cannot be found, it falls back to the system bundle.
                     * @param className the class name
                     * @param context the context.
                     * @return the class object
                     * @throws ClassNotFoundException if the class cannot be found
                     */
                    @Override
                    public Class classForName(String className, Map context) throws ClassNotFoundException {
                        try {
                            return bundle.loadClass(className);
                        } catch (ClassNotFoundException e) { //NOSONAR
                            // Ignore it.
                        }

                        // Try with the system bundle, if the bundle is not the system bundle
                        if (bundle.getBundleId() != 0) {
                            try {
                                return bundle.getBundleContext().getBundle(0).loadClass(className);
                            } catch (ClassNotFoundException e) { //NOSONAR
                                // Ignore it.
                            }
                        }
                        // Nothing we can do.
                        LOGGER.warn("A template tried to load the '" + className + "' class, " +
                                "but this class is not available. Try to import it in the bundle containing the " +
                                "template.");

                        throw new ClassNotFoundException(className);
                    }
                });
            }

            final Object result = Ognl.getValue(expressionTree, ctxt, evaluationRoot);

            if (!expContext.getPerformTypeConversion()) {
                return result;
            }

            final IStandardConversionService conversionService =
                    StandardExpressions.getConversionService(configuration);

            return conversionService.convert(configuration, processingContext, result, String.class);

        } catch (final OgnlException e) {
            throw new TemplateProcessingException(
                    "Exception evaluating OGNL expression: \"" + expression + "\"", e);
        }

    }

}
