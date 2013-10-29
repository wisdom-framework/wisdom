package org.ow2.chameleon.wisdom.template.thymeleaf.dialect;

import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
import org.thymeleaf.standard.expression.OgnlVariableExpressionEvaluator;
import org.thymeleaf.standard.expression.StandardExpressionParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Wisdom Themeleaf dialect.
 *
 * Wisdom does not use the standard Thymeleaf dialect directly. First, injecting 'expression' object is not possible,
 * while we need to inject `routes`. In addition, the OGNL boolean fix use a method not compatible with OSGi.
 */
public class WisdomStandardDialect extends StandardDialect {



    @Override
    public Map<String, Object> getExecutionAttributes() {
        Map<String, Object> attributes = super.getExecutionAttributes();
        final IStandardVariableExpressionEvaluator expressionEvaluator = ExtendedVariableExpressionEvaluator.INSTANCE;

        attributes.put(
                "StandardVariableExpressionEvaluator", expressionEvaluator);
        return attributes;
    }


}
