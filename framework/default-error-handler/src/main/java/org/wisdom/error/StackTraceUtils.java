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
package org.wisdom.error;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility method to handle stack traces.
 */
public final class StackTraceUtils {

    private StackTraceUtils() {
        // Avoid direct instantiation
    }

    /**
     * Removes the '__M_' iPOJO trace from the stack trace.
     *
     * @param stack the original stack trace
     * @return the cleaned stack trace
     */
    public static List<StackTraceElement> cleanup(StackTraceElement[] stack) {
        List<StackTraceElement> elements = new ArrayList<>();
        if (stack == null) {
            return elements;
        } else {
            for (StackTraceElement element : stack) {
                // Remove all iPOJO calls.
                if (element.getMethodName().startsWith("__M_")) {
                    String newMethodName = element.getMethodName().substring("__M_".length());
                    elements.add(new StackTraceElement(element.getClassName(), newMethodName, element.getFileName(),
                            element.getLineNumber()));
                } else if (element.getLineNumber() >= 0) {
                    elements.add(element);
                }
            }
        }
        return elements;
    }
}
