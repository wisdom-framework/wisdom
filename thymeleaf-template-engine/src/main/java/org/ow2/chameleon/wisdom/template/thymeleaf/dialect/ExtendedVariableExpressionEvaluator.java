package org.ow2.chameleon.wisdom.template.thymeleaf.dialect;

import ognl.Ognl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.cache.ICache;
import org.thymeleaf.cache.ICacheManager;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IContextVariableRestriction;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.context.VariablesMap;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.expression.ExpressionEvaluatorObjects;
import org.thymeleaf.standard.expression.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An extended version of the standard evaluator injecting the `routes` expression object.
 */
public class ExtendedVariableExpressionEvaluator implements IStandardVariableExpressionEvaluator {

    public static final ExtendedVariableExpressionEvaluator INSTANCE = new ExtendedVariableExpressionEvaluator();
    private static final Logger logger = LoggerFactory.getLogger(ExtendedVariableExpressionEvaluator.class);
    private static final String OGNL_CACHE_PREFIX = "{ognl}";

    protected Map<String, Object> computeAdditionalContextVariables(IProcessingContext processingContext) {
        // Meant to be overridden.
        return Collections.emptyMap();
    }

    @Override
    public String toString() {
        return "OGNL extended by Wisdom";
    }

    public Object evaluate(final Configuration configuration,
                           final IProcessingContext processingContext, final String expression,
                           final StandardExpressionExecutionContext expContext, final boolean useSelectionAsRoot) {

        try {

            if (logger.isTraceEnabled()) {
                logger.trace("[THYMELEAF][{}] OGNL expression: evaluating expression \"{}\" on target",
                        TemplateEngine.threadIndex(), expression);
            }


            Object expressionTree = null;
            ICache<String, Object> cache = null;

            if (configuration != null) {
                final ICacheManager cacheManager = configuration.getCacheManager();
                if (cacheManager != null) {
                    cache = cacheManager.getExpressionCache();
                    if (cache != null) {
                        expressionTree = cache.get(OGNL_CACHE_PREFIX + expression);
                    }
                }
            }

            if (expressionTree == null) {
                expressionTree = ognl.Ognl.parseExpression(expression);
                if (cache != null && null != expressionTree) {
                    cache.put(OGNL_CACHE_PREFIX + expression, expressionTree);
                }
            }

            final Map<String, Object> contextVariables = processingContext.getExpressionObjects();
            if (!contextVariables.containsKey(Routes.ROUTES_VAR)) {
                contextVariables.put(Routes.OBJECT_NAME, processingContext.getContext().getVariables().get
                        (Routes.ROUTES_VAR));
            }

            final Map<String, Object> additionalContextVariables = computeAdditionalContextVariables(processingContext);
            if (additionalContextVariables != null) {
                contextVariables.putAll(additionalContextVariables);
            }

            final Object evaluationRoot =
                    (useSelectionAsRoot ?
                            processingContext.getExpressionSelectionEvaluationRoot() :
                            processingContext.getExpressionEvaluationRoot());

            setVariableRestrictions(expContext, evaluationRoot, contextVariables);

            final Object result = Ognl.getValue(expressionTree, contextVariables, evaluationRoot);

            if (!expContext.getPerformTypeConversion()) {
                return result;
            }

            final IStandardConversionService conversionService =
                    StandardExpressions.getConversionService(configuration);

            return conversionService.convert(configuration, processingContext, result, String.class);

        } catch (Exception e) {
            throw new TemplateProcessingException(
                    "Exception evaluating OGNL expression: \"" + expression + "\"", e);
        }

    }

    protected void setVariableRestrictions(final StandardExpressionExecutionContext expContext,
                                           final Object evaluationRoot, final Map<String, Object> contextVariables) {

        final List<IContextVariableRestriction> restrictions =
                (expContext.getForbidRequestParameters() ?
                        StandardVariableRestrictions.REQUEST_PARAMETERS_FORBIDDEN : null);

        final Object context = contextVariables.get(ExpressionEvaluatorObjects.CONTEXT_VARIABLE_NAME);
        if (context != null && context instanceof IContext) {
            final VariablesMap<?, ?> variablesMap = ((IContext) context).getVariables();
            variablesMap.setRestrictions(restrictions);
        }
        if (evaluationRoot != null && evaluationRoot instanceof VariablesMap<?, ?>) {
            ((VariablesMap<?, ?>) evaluationRoot).setRestrictions(restrictions);
        }

    }




}
