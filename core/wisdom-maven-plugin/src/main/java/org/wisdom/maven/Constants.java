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

     String WISDOM_DIRECTORY_NAME = "wisdom";

     String WISDOM_RUNTIME_ARTIFACT_ID = "wisdom-runtime";
     String WISDOM_BASE_RUNTIME_ARTIFACT_ID = "wisdom-base-runtime";

     String CONFIGURATION_SRC_DIR = "src/main/configuration";
     String INSTANCES_SRC_DIR = "src/main/instances";
     String INSTANCES_TEST_DIR = "src/main/instances";
     String TEMPLATES_SRC_DIR = "src/main/templates";
     String MAIN_RESOURCES_DIR = "src/main/resources";
     String ASSETS_SRC_DIR = "src/main/assets";
     String MAIN_SRC_DIR = "src/main/java";

    /**
     * The name of the application directory.
     */
     String APPLICATION_DIR = "application";

     String CONFIGURATION_DIR = "conf";
     String TEMPLATES_DIR = "templates";
     String ASSETS_DIR = "assets";

     String TEST_SRC_DIR = "src/test/java";
     String TEST_RESOURCES_DIR = "src/test/resources";

     String NODE_DIST_ROOT_URL = "https://nodejs.org/dist/";
     String NPM_REGISTRY_ROOT_URL = "http://registry.npmjs.org";
     String NODE_VERSION = "v4.1.2";
     String NPM_VERSION = "2.13.0";

     String INSTRUCTIONS_FILE = "src/main/osgi/osgi.bnd";

     String DEPENDENCIES_FILE = "target/osgi/dependencies.json";
     String EXTRA_HEADERS_FILE = "target/osgi/headers.properties";
     String OSGI_PROPERTIES = "target/osgi/osgi.properties";

}
