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
package org.wisdom.resources;

import org.junit.Test;
/**
 * Test the behaviour of the ComparableVersion class
 */
public class ComparableVersionTest {
    @Test
    public void testVersionClassicComparator() {
        ComparableVersion greatest = new ComparableVersion("2.1.0");
        ComparableVersion lesser = new ComparableVersion("2.0.9");
        ComparableVersion eqVersion = new ComparableVersion("2.0.9");
        assert(greatest.compareTo(lesser) > 0);
        assert(lesser.compareTo(greatest) < 0);
        assert(lesser.compareTo(eqVersion) == 0);
    }

    @Test
    public void testVersionLesserSnapshotComparator() {
        ComparableVersion release = new ComparableVersion("2.1.0");
        ComparableVersion snapshot = new ComparableVersion("2.1.0-SNAPSHOT");
        assert(release.compareTo(snapshot) > 0);
    }

    @Test
    public void testVersionGreatestSnapshotComparator() {
        ComparableVersion release = new ComparableVersion("2.0.9");
        ComparableVersion snapshot = new ComparableVersion("2.1.0-SNAPSHOT");
        assert(release.compareTo(snapshot) < 0);
    }

    @Test
    public void testVersionComparator() {
        ComparableVersion release = new ComparableVersion("2.0.9");
        ComparableVersion releaseWithQuickFix = new ComparableVersion("2.0.9-1");
        assert(release.compareTo(releaseWithQuickFix) < 0);
    }

}
