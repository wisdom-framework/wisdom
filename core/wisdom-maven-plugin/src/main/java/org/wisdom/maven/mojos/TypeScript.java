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
package org.wisdom.maven.mojos;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class TypeScript {

    protected String version = "1.6.2";

    protected boolean enabled = true;

    protected boolean generateDeclaration = true;

    protected List<String> otherArguments = new ArrayList<>();

    protected File mapRoot;

    protected String module;

    protected boolean removeComments = false;

    protected boolean generateMap = true;

    protected String target;

    protected boolean noImplicitAny = false;

    protected File project;

    public boolean isEnabled() {
        return enabled;
    }

    public TypeScript setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isGenerateDeclaration() {
        return generateDeclaration;
    }

    public TypeScript setGenerateDeclaration(boolean generateDeclaration) {
        this.generateDeclaration = generateDeclaration;
        return this;
    }

    public boolean isGenerateMap() {
        return generateMap;
    }

    public TypeScript setGenerateMap(boolean generateMap) {
        this.generateMap = generateMap;
        return this;
    }

    public File getMapRoot() {
        return mapRoot;
    }

    public TypeScript setMapRoot(File mapRoot) {
        this.mapRoot = mapRoot;
        return this;
    }

    public String getModule() {
        return module;
    }

    public TypeScript setModule(String module) {
        this.module = module;
        return this;
    }

    public List<String> getOtherArguments() {
        return otherArguments;
    }

    public TypeScript setOtherArguments(List<String> otherArguments) {
        this.otherArguments = otherArguments;
        return this;
    }

    public boolean isRemoveComments() {
        return removeComments;
    }

    public TypeScript setRemoveComments(boolean removeComments) {
        this.removeComments = removeComments;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public TypeScript setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getTarget() {
        return target;
    }

    public TypeScript setTarget(String target) {
        this.target = target;
        return this;
    }

    public boolean isNoImplicitAny() {
        return noImplicitAny;
    }

    public TypeScript setNoImplicitAny(boolean noImplicitAny) {
        this.noImplicitAny = noImplicitAny;
        return this;
    }

    public List<String> createTypeScriptCompilerArgumentList(File input, File destination, Collection<File> files) {
        List<String> arguments = new ArrayList<>();

        if (project == null) {

            if (removeComments) {
                arguments.add("--removeComments");
            }

            if (generateDeclaration) {
                arguments.add("--declaration");
            }

            if (mapRoot != null) {
                arguments.add("--mapRoot");
                arguments.add(mapRoot.getAbsolutePath());
            }

            if (module != null) {
                arguments.add("--module");
                arguments.add(module);
            }

            if (generateMap) {
                arguments.add("--sourceMap");
            }

            if (noImplicitAny) {
                arguments.add("--noImplicitAny");
            }

            if (target != null) {
                arguments.add("--target");
                arguments.add(target);
            }

            if (!otherArguments.isEmpty()) {
                arguments.addAll(otherArguments);
            }

            arguments.add("--outDir");
            arguments.add(destination.getAbsolutePath());

            arguments.add("--rootDir");
            arguments.add(input.getAbsolutePath());

            // Now we need to list all .ts files
            files.stream().forEach(f -> arguments.add(f.getAbsolutePath()));
        } else {
            arguments.add("--project");
            arguments.add(project.getAbsolutePath());
        }
        return arguments;
    }

    public File getProject() {
        return project;
    }

    public void setProject(File project) {
        this.project = project;
    }
}
