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
package org.wisdom.source.ast.model;

import com.google.common.base.Strings;

import java.util.Comparator;

/**
 * Created by clement on 09/05/15.
 */
public class PathComparator implements Comparator<String> {

    /**
     * /foo < /foo/ < /foo/bar < /fooa
     *
     * @param o1
     * @param o2
     * @return
     */
    @Override
    public int compare(String o1, String o2) {

        if (o1.equals(o2)) {
            return 0;
        }

        String prefix = Strings.commonPrefix(o1, o2);
        if (prefix.length() == 0) {
            // No common prefix
            return o1.compareTo(o2);
        }

        if (o1.length() == prefix.length()) {
            return -1;
        }

        return o1.compareTo(o2);

    }
}
