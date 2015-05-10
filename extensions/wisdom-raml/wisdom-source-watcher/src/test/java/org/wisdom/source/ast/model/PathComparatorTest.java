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

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Check the path comparator.
 */
public class PathComparatorTest {

    PathComparator comparator = new PathComparator();

    @Test
    public void testComparison() {

        assertThat(comparator.compare("/foo", "/foo/")).isNotEqualTo(0);
        assertThat(comparator.compare("/root/foo", "/root/foo/")).isNotEqualTo(0);

        ImmutableList<String> list = ImmutableList.of("/foo", "/bar");
        List<String> copy = new ArrayList<>(list);
        Collections.sort(copy, comparator);
        assertThat(copy).containsExactly("/bar", "/foo");

        list = ImmutableList.of("/foo", "/bar", "/baz");
        copy = new ArrayList<>(list);
        Collections.sort(copy, comparator);
        assertThat(copy).containsExactly("/bar", "/baz", "/foo");

        list = ImmutableList.of("/foo/bar", "/foo/baz", "/baz");
        copy = new ArrayList<>(list);
        Collections.sort(copy, comparator);
        assertThat(copy).containsExactly("/baz", "/foo/bar", "/foo/baz");

        list = ImmutableList.of("/", "/foo/bar", "/foo/baz", "/baz");
        copy = new ArrayList<>(list);
        Collections.sort(copy, comparator);
        assertThat(copy).containsExactly("/", "/baz", "/foo/bar", "/foo/baz");

        list = ImmutableList.of("/", "/foo", "/foo/", "/foo/bar", "/fooa");
        copy = new ArrayList<>(list);
        Collections.shuffle(copy);
        Collections.sort(copy, comparator);
        assertThat(copy).containsExactly("/", "/foo", "/foo/", "/foo/bar", "/fooa");
    }

}