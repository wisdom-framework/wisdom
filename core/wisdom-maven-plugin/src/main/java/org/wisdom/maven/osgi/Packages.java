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
package org.wisdom.maven.osgi;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Utility methods to handle packages item in packaging instructions.
 */
public final class Packages {

    private Packages() {
        // Avoid direct instantiation
    }

    /**
     * Computes the BND clause from the given set of packages.
     *
     * @param packages the packages
     * @return the clause
     */
    public static String toClause(List<String> packages) {
        return Joiner.on(", ").skipNulls().join(packages);
    }

    /**
     * Computes the package name from the given file path.
     * The path is a relative path from the class/source root.
     * For example {@code foo/bar/Baz.class} generates the {@code foo.bar} package.
     *
     * @param filePath the file's path
     * @return the package name
     */
    public static String getPackageName(String filePath) {
        char sep = File.separatorChar;
        int n = filePath.lastIndexOf(sep);
        if (n == -1  && sep != '/') {
            // Try with linux separator if the system separator is not found.
            sep = '/';
            n = filePath.lastIndexOf(sep);
        }

        return n < 0 ? "." : filePath.substring(0, n).replace(sep, '.');
    }

    /**
     * Checks whether the given package must be exported. The decision is made from heuristics.
     *
     * @param packageName the package name
     * @return {@literal true} if the package has to be exported, {@literal false} otherwise.
     */
    public static boolean shouldBeExported(String packageName) {
        boolean service = packageName.endsWith(".service");
        service = service
                || packageName.contains(".service.")
                || packageName.endsWith(".services")
                || packageName.contains(".services.");

        boolean api = packageName.endsWith(".api");
        api = api
                || packageName.contains(".api.")
                || packageName.endsWith(".apis")
                || packageName.contains(".apis.");

        boolean model = packageName.endsWith(".model");
        model = model
                || packageName.contains(".model.")
                || packageName.endsWith(".models")
                || packageName.contains(".models.");

        boolean entity = packageName.endsWith(".entity");
        entity = entity
                || packageName.contains(".entity.")
                || packageName.endsWith(".entities")
                || packageName.contains(".entities.");

        return !packageName.isEmpty() && !packageName.equals(".") && (service || api || model || entity);
    }

}
