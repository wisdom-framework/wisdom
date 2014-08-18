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
package org.wisdom.maven;

/**
 * Wisdom Constant Definitions.
 */
public interface Constants {

    public static final String WISDOM_DIRECTORY_NAME = "wisdom";

    public static final String WISDOM_RUNTIME_ARTIFACT_ID = "wisdom-runtime";
    public static final String WISDOM_BASE_RUNTIME_ARTIFACT_ID = "wisdom-base-runtime";

    public static final String CONFIGURATION_SRC_DIR = "src/main/configuration";
    public static final String TEMPLATES_SRC_DIR = "src/main/templates";
    public static final String MAIN_RESOURCES_DIR = "src/main/resources";
    public static final String ASSETS_SRC_DIR = "src/main/assets";
    public static final String MAIN_SRC_DIR = "src/main/java";

    public static final String CONFIGURATION_DIR = "conf";
    public static final String TEMPLATES_DIR = "templates";
    public static final String ASSETS_DIR = "assets";

    public static final String TEST_SRC_DIR = "src/test/java";
    public static final String TEST_RESOURCES_DIR = "src/test/resources";

    public static final String NODE_VERSION = "0.10.30";
    public static final String NODE_VERSION_ARM = "0.10.26";
    public static final String NPM_VERSION = "1.4.12";

    public static final String INSTRUCTIONS_FILE = "src/main/osgi/osgi.bnd";

    public static final String DEPENDENCIES_FILE = "target/osgi/dependencies.json";
    public static final String EXTRA_HEADERS_FILE = "target/osgi/headers.properties";
    public static final String OSGI_PROPERTIES = "target/osgi/osgi.properties";

}
