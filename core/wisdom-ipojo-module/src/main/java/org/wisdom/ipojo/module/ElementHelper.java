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
package org.wisdom.ipojo.module;

import org.apache.felix.ipojo.manipulator.metadata.annotation.ComponentWorkbench;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

/**
 * A couple of utilities method to generate Element - Attribute structure.
 */
public class ElementHelper {

    /**
     * Component attribute.
     */
    public static final String COMPONENT = "component";


    /**
     * Declares an instance.
     *
     * @param workbench the workbench
     * @return the Instance element
     */
    public static Element declareInstance(ComponentWorkbench workbench) {
        Element instance = new Element("instance", "");
        instance.addAttribute(new Attribute(COMPONENT, workbench.getType().getClassName()));
        return instance;
    }

    /**
     * Gets the 'provides' element.
     *
     * @return the provides element
     */
    public static Element getProvidesElement(String specifications) {
        Element provides = new Element("provides", "");
        if (specifications == null) {
            return provides;
        } else {
            Attribute attribute = new Attribute("specifications", specifications);
            provides.addAttribute(attribute);
            return provides;
        }
    }

    /**
     * @return the Component element.
     */
    public static Element getComponentElement() {
        return new Element(COMPONENT, "");
    }
}
