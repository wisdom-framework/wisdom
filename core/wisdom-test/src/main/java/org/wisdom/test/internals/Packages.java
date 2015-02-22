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
package org.wisdom.test.internals;

/**
 * Sets of methods appending packages to export to the given list.
 */
public final class Packages {

    private Packages() {
        //Hide implicit constructor
    }

    /**
     * Adds the junit packages.
     *
     * @param builder the builder
     */
    public static void junit(StringBuilder builder) {
        checkEmpty(builder);
        // org.junit
        builder.append("org.junit,");
        builder.append("org.junit.runners,");
        builder.append("org.junit.runners.model,");
        builder.append("org.junit.runner,");
        builder.append("org.junit.runner.manipulation,");
        builder.append("org.junit.runner.notification,");
        builder.append("org.junit.matchers,");
        builder.append("org.junit.rules,");

        // junit.framework
        builder.append("junit.framework");
    }

    /**
     * Adds the wisdom test packages.
     *
     * @param builder the builder
     */
    public static void wisdomtest(StringBuilder builder) {
        checkEmpty(builder);
        builder.append("org.wisdom.test,");
        builder.append("org.wisdom.test.shared");
    }

    /**
     * Adds the javax.inject package.
     *
     * @param builder the builder
     */
    public static void javaxinject(StringBuilder builder) {
        checkEmpty(builder);
        builder.append("javax.inject;version=1.0.0");
    }

    /**
     * Adds the AssertJ packages.
     *
     * @param builder the builder
     */
    public static void assertj(StringBuilder builder) {
        checkEmpty(builder);
        builder.append("org.assertj.core.api,");
        builder.append("org.assertj.core.api.filter,");
        builder.append("org.assertj.core.condition");
    }

    private static void checkEmpty(StringBuilder builder) {
        if (builder.length() != 0) {
            builder.append(',');
        }
    }

}
