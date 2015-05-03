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
package org.wisdom.maven.mojos;

import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.utils.io.DirectoryScanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Configure aggregations.
 */
public class Aggregation {

    private String output;

    private boolean minification = true;

    private List<String> files = new ArrayList<>();

    private List<FileSet> fileSets;

    private boolean removeIncludedFiles;

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public boolean isMinification() {
        return minification;
    }

    public void setMinification(boolean minification) {
        this.minification = minification;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    /**
     * @return the file sets.
     */
    public List<FileSet> getFileSets() {
        return fileSets;
    }

    /**
     * Sets the file sets to include in the aggregation.
     *
     * @param fileSets the file sets
     */
    public void setFileSets(List<FileSet> fileSets) {
        this.fileSets = fileSets;
    }

    /**
     * @return the selected set of files.
     */
    public Collection<File> getSelectedFiles(File defaultBaseDirectory) {
        List<File> result = new ArrayList<>();
        final List<FileSet> sets = getFileSets();

        if (sets != null  && ! sets.isEmpty()) {
            for (FileSet set : sets) {
                File base;
                if (set.getDirectory() == null) {
                    // Set the directory if not set
                    set.setDirectory(defaultBaseDirectory.getAbsolutePath());
                    base = defaultBaseDirectory;
                } else {
                    base = new File(set.getDirectory());
                }

                for (String include : set.getIncludesArray()) {
                    // We don't extract the selected file set directly because the it does not enforce the include order.
                    // So we iterate over the set of include clause one by one, and include files if not already included.
                    addInto(base, set, include, result);
                }
            }
        } else {
            for (String f : getFiles()) {
                File file = new File(f);
                if (!file.isAbsolute()) {
                    file = new File(defaultBaseDirectory, f);
                }
                result.add(file);
            }
        }
        return result;
    }

    public boolean isRemoveIncludedFiles() {
        return removeIncludedFiles;
    }

    public void setRemoveIncludedFiles(boolean removeIncludedFiles) {
        this.removeIncludedFiles = removeIncludedFiles;
    }


    private void addInto(File base, FileSet fileSet, String include, List<File> includedFiles) {
        if (include.indexOf('*') > -1) {
            DirectoryScanner scanner = newScanner(base, fileSet);
            scanner.setIncludes(include);
            scanner.scan();
            String[] paths = scanner.getIncludedFiles();
            Arrays.sort(paths);
            for (String path : paths) {
                File file = new File(scanner.getBasedir(), path);
                if (!includedFiles.contains(file)) {
                    includedFiles.add(file);
                }
            }
        } else {
            File file = new File(include);
            if (!file.isAbsolute()) {
                file = new File(base, include);
            }
            if (!includedFiles.contains(file)) {
                includedFiles.add(file);
            }
        }
    }

    private DirectoryScanner newScanner(File base, FileSet fileSet) {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(base);
        if ((fileSet.getExcludes() != null) && (fileSet.getExcludes().size() != 0)) {
            scanner.setExcludes(fileSet.getExcludesArray());
        }
        scanner.addDefaultExcludes();
        return scanner;
    }
}
