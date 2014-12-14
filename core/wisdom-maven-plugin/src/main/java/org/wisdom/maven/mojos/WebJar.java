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
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The webjar configuration. Let you configure the file set included in the webjar, the classifier, the name and
 * version of the webjar.
 */
public class WebJar {

    FileSet fileset;

    String name;

    String version;

    String classifier;

    /**
     * Creates an instance of {@link org.wisdom.maven.mojos.WebJar}.
     */
    public WebJar() {
        // Default constructor.
    }

    /**
     * Creates an instance of {@link org.wisdom.maven.mojos.WebJar}.
     *
     * @param name       the name
     * @param version    the version
     * @param classifier the classifier
     * @param fileset    the file set
     */
    public WebJar(String name, String version, String classifier, FileSet fileset) {
        this();
        setName(name);
        setVersion(version);
        setClassifier(classifier);
        setFileset(fileset);
    }

    /**
     * @return the file set.
     */
    public FileSet getFileset() {
        return fileset;
    }

    /**
     * Sets the file set to include in the webjar.
     *
     * @param fileset the file set
     */
    public void setFileset(FileSet fileset) {
        this.fileset = fileset;
    }

    /**
     * @return the name of the webjar.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the webjar.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the version of the webjar.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of the webjar.
     *
     * @param version the version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the artifact's classifier.
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * Sets the classifier.
     *
     * @param classifier the classifier
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    /**
     * @return the artifact's file name.
     */
    public String getOutputFileName() {
        return getName() + "-" + getVersion() + "-" + getClassifier() + ".jar";
    }

    /**
     * @return the selected set of files.
     */
    public Collection<File> getSelectedFiles() {
        // Because of a symlink issue on OpenJDK, we cannot use the FileSetManager directory, because we need to
        // override a method from the DirectoryScanner.
        // The exception is: java.lang.ClassNotFoundException: sun/nio/fs/AixFileSystemProvider

        final FileSet set = getFileset();
        String[] names = getIncludedFiles(set);
        List<File> files = new ArrayList<>();
        File base = new File(set.getDirectory());
        for (String n : names) {
            files.add(new File(base, n));
        }
        return files;
    }

    /**
     * Get all the filenames which have been included by the rules in this fileset.
     *
     * @param fileSet The fileset defining rules for inclusion/exclusion, and base directory.
     * @return the array of matching filenames, relative to the basedir of the file-set.
     */
    public String[] getIncludedFiles(FileSet fileSet) {
        DirectoryScanner scanner = scan(fileSet);
        if (scanner != null) {
            return scanner.getIncludedFiles();
        } else {
            return new String[0];
        }
    }

    private DirectoryScanner scan(FileSet fileSet) {
        File basedir = new File(fileSet.getDirectory());
        if (!basedir.exists() || !basedir.isDirectory()) {
            return null;
        }

        DirectoryScanner scanner = new DirectoryScanner() {
            /**
             * Checks whether a given file is a symbolic link.
             * <p/>
             * <p>It doesn't really test for symbolic links but whether the
             * canonical and absolute paths of the file are identical - this
             * may lead to false positives on some platforms.</p>
             * <p>
             * We override this method to avoid using the Files API, leading to
             * java.lang.ClassNotFoundException: sun/nio/fs/AixFileSystemProvider
             *</p>
             * @param parent the parent directory of the file to test
             * @param name   the name of the file to test.
             * @return true if it's a symbolic link
             * @throws java.io.IOException .
             */
            @Override
            public boolean isSymbolicLink(File parent, String name) throws IOException {
                File resolvedParent = new File(parent.getCanonicalPath());
                File toTest = new File(resolvedParent, name);
                return !toTest.getAbsolutePath().equals(toTest.getCanonicalPath());
            }
        };

        String[] includesArray = fileSet.getIncludesArray();
        String[] excludesArray = fileSet.getExcludesArray();

        if (includesArray.length > 0) {
            scanner.setIncludes(includesArray);
        }

        if (excludesArray.length > 0) {
            scanner.setExcludes(excludesArray);
        }

        if (fileSet.isUseDefaultExcludes()) {
            scanner.addDefaultExcludes();
        }

        scanner.setBasedir(basedir);
        scanner.setFollowSymlinks(fileSet.isFollowSymlinks());

        scanner.scan();

        return scanner;
    }
}
