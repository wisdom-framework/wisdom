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
import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ImageMinification {

    private boolean interlaced;

    private boolean progressive;

    private int optimizationLevel = -1;

    private boolean enabled = true;
    private String version = "2.0.0";


    public boolean isEnabled() {
        return enabled;
    }

    public String getVersion() {
        return version;
    }

    public ImageMinification setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public ImageMinification setInterlaced(boolean interlaced) {
        this.interlaced = interlaced;
        return this;
    }

    public ImageMinification setOptimizationLevel(int optimizationLevel) {
        this.optimizationLevel = optimizationLevel;
        return this;
    }

    public ImageMinification setProgressive(boolean progressive) {
        this.progressive = progressive;
        return this;
    }

    public ImageMinification setVersion(String version) {
        this.version = version;
        return this;
    }

    public String[] getArguments(File directory) {
        if (optimizationLevel < -1 || optimizationLevel > 7) {
            throw new IllegalStateException("The optimization must be in [0,7]");
        }

        List<String> args = new ArrayList<>();

        if (interlaced) {
            args.add("--interlaced");
        }

        if (progressive) {
            args.add("--progressive");
        }

        if (optimizationLevel != -1) {
            args.add("--optimizationLevel");
            args.add(Integer.toString(optimizationLevel));
        }

        args.add(directory.getAbsolutePath());
        args.add(directory.getAbsolutePath());

        return args.toArray(new String[args.size()]);
    }
}
