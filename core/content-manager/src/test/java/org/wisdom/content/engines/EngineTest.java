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
package org.wisdom.content.engines;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.content.serializers.JSONSerializer;
import org.wisdom.content.serializers.XMLSerializer;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


public class EngineTest {

    Engine engine = new Engine();

    JSONSerializer json = new JSONSerializer();
    XMLSerializer xml = new XMLSerializer();

    @Before
    public void setUp() {
        engine.serializers = ImmutableList.of(
                json, xml
        );
    }

    @Test
    public void testGetContentSerializerForContentType() throws Exception {
        assertThat(engine.getContentSerializerForContentType(null)).isNull();
        assertThat(engine.getContentSerializerForContentType(MimeTypes.JSON)).isEqualTo(json);
        assertThat(engine.getContentSerializerForContentType(MimeTypes.XML)).isEqualTo(xml);
    }

    @Test
    public void testGetBestSerializer() throws Exception {
        // Match */* (use first form serializer list)
        Collection<MediaType> types = mediaTypes("text/*;q=0.3, text/html;q=0.7, text/html;level=1, " +
                "text/html;level=2;q=0.4, */*;q=0.5");
        assertThat(engine.getBestSerializer(types)).isEqualTo(json);

        // Match application/xml
        types = mediaTypes("text/*;q=0.3, application/xml;q=0.7, text/html;level=1, " +
                "text/html;level=2;q=0.4, */*;q=0.5");
        assertThat(engine.getBestSerializer(types)).isEqualTo(xml);

        // Match application/json
        types = mediaTypes("text/*;q=0.3, application/json;q=0.7, text/html;level=1, " +
                "text/html;level=2;q=0.4, */*;q=0.5");
        assertThat(engine.getBestSerializer(types)).isEqualTo(json);

        // Match application/json
        types = mediaTypes(MimeTypes.JSON);
        assertThat(engine.getBestSerializer(types)).isEqualTo(json);

        // Match application/* (use first form serializer list)
        types = mediaTypes("application/*");
        assertThat(engine.getBestSerializer(types)).isEqualTo(json);

        // Match application/json
        types = mediaTypes("text/*;q=0.3, application/json;q=0.7, application/xml;level=1;q=0.6, " +
                "text/html;level=2;q=0.4, */*;q=0.5");
        assertThat(engine.getBestSerializer(types)).isEqualTo(json);

        // Match application/xml
        types = mediaTypes("text/*;q=0.3, application/json;q=0.6, application/xml;level=1, " +
                "text/html;level=2;q=0.4, */*;q=0.5");
        assertThat(engine.getBestSerializer(types)).isEqualTo(xml);

        assertThat(engine.getBestSerializer(null)).isNull();
        assertThat(engine.getBestSerializer(Collections.<MediaType>emptyList())).isNull();

    }

    @Test
    public void testMediaType() throws Exception {
        String accept = "text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5";
        assertThat(mediaTypes(accept)).containsExactly(
                MediaType.parse("text/html").withParameter("level", "1"),
                MediaType.parse("text/html").withParameter("q", "0.7"),
                MediaType.parse("*/*").withParameter("q", "0.5"),
                MediaType.parse("text/html").withParameter("level", "2").withParameter("q", "0.4"),
                MediaType.parse("text/*").withParameter("q", "0.3")
        );
    }


    Collection<MediaType> mediaTypes(String accept) {
        if (accept == null) {
            // Any text by default.
            return ImmutableList.of(MediaType.ANY_TEXT_TYPE);
        }

        TreeSet<MediaType> set = new TreeSet<>(new Comparator<MediaType>() {
            @Override
            public int compare(MediaType o1, MediaType o2) {
                double q1 = 1.0, q2 = 1.0;
                List<String> ql1 = o1.parameters().get("q");
                List<String> ql2 = o2.parameters().get("q");

                if (ql1 != null && !ql1.isEmpty()) {
                    q1 = Double.parseDouble(ql1.get(0));
                }

                if (ql2 != null && !ql2.isEmpty()) {
                    q2 = Double.parseDouble(ql2.get(0));
                }

                return new Double(q2).compareTo(q1);
            }
        });

        // Split and sort.
        for (String segment : Splitter.on(",").split(accept)) {
            MediaType type = MediaType.parse(segment.trim());
            set.add(type);
        }

        return set;
    }
}