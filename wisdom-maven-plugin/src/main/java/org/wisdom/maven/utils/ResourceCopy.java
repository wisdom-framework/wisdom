package org.wisdom.maven.utils;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.wisdom.maven.Constants;
import org.wisdom.maven.mojos.AbstractWisdomMojo;
import org.wisdom.maven.mojos.AbstractWisdomWatcherMojo;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Resource copy utilities.
 */
public class ResourceCopy {

    private static final Collection<? extends String> NON_FILTERED_EXTENSIONS = Arrays.asList(
            "pdf",
            "png",
            "bmp",
            "jpeg",
            "jpg",
            "tiff",
            "jar",
            "zip",
            "tar.gz",
            "gz"
    );

    /**
     * Copies the file <tt>file</tt> to the directory <tt>dir</tt>, keeping the structure relative to <tt>rel</tt>
     *
     * @throws IOException
     */
    public static void copyFileToDir(File file, File rel, File dir, AbstractWisdomMojo mojo, MavenResourcesFiltering
                                     filtering) throws
            IOException {
        if (filtering == null) {
            File out = computeRelativeFile(file, rel, dir);
            if (out.getParentFile() != null) {
                out.getParentFile().mkdirs();
                FileUtils.copyFileToDirectory(file, out.getParentFile());
            } else {
                throw new IOException("Cannot copy file - parent directory not accessible for "
                        + file.getAbsolutePath());
            }
        } else {
            Resource resource = new Resource();
            resource.setDirectory(rel.getAbsolutePath());
            resource.setFiltering(true);
            resource.setTargetPath(dir.getAbsolutePath());
            resource.setIncludes(ImmutableList.of("**/" + file.getName()));

            List<String> excludedExtensions = new ArrayList<>();
            excludedExtensions.addAll(filtering.getDefaultNonFilteredFileExtensions());
            excludedExtensions.addAll(NON_FILTERED_EXTENSIONS);

            MavenResourcesExecution exec = new MavenResourcesExecution(ImmutableList.of(resource), dir, mojo.project,
                    "UTF-8", Collections.emptyList(), excludedExtensions, mojo.session);

            try {
                filtering.filterResources(exec);
            } catch (MavenFilteringException e) {
                throw new IOException("Error while copying resources", e);
            }
        }
    }

    /**
     * Gets a File object representing a File in the directory <tt>dir</tt> which has the same path as the file
     * <tt>file</tt> from the directory <tt>rel</tt>.
     *
     * @param file
     * @param rel
     * @param dir
     * @return
     */
    public static File computeRelativeFile(File file, File rel, File dir) {
        String path = file.getAbsolutePath();
        String relativePath = path.substring(rel.getAbsolutePath().length());
        return new File(dir, relativePath);
    }

    public static void copyConfiguration(AbstractWisdomWatcherMojo mojo, MavenResourcesFiltering filtering) throws
            IOException {
        File in = new File(mojo.basedir, Constants.CONFIGURATION_SRC_DIR);
        File out = new File(mojo.getWisdomRootDirectory(), Constants.CONFIGURATION_DIR);
        if (!in.isDirectory()) {
            throw new IOException("The configuration directory (" + in.getAbsolutePath() + ") must exist");
        }
        filterAndCopy(mojo, filtering, in, out);
    }

    public static void copyExternalAssets(AbstractWisdomMojo mojo, MavenResourcesFiltering filtering) throws IOException {
        File in = new File(mojo.basedir, Constants.ASSETS_SRC_DIR);
        if (!in.exists()) {
            return;
        }
        File out = new File(mojo.getWisdomRootDirectory(), Constants.ASSETS_DIR);
        filterAndCopy(mojo, filtering, in, out);
    }

    public static void copyTemplates(AbstractWisdomMojo mojo, MavenResourcesFiltering filtering) throws IOException {
        File in = new File(mojo.basedir, Constants.TEMPLATES_SRC_DIR);
        if (!in.exists()) {
            return;
        }
        File out = new File(mojo.getWisdomRootDirectory(), Constants.TEMPLATES_DIR);

        filterAndCopy(mojo, filtering, in, out);
    }

    /**
     * Copy `src/main/resources` to `target/classes`
     *
     * @param mojo the mojo
     */
    public static void copyInternalResources(AbstractWisdomMojo mojo, MavenResourcesFiltering filtering) throws
            IOException {
        File in = new File(mojo.basedir, Constants.MAIN_RESOURCES_DIR);
        if (!in.exists()) {
            return;
        }
        File out = new File(mojo.buildDirectory, "classes");

        filterAndCopy(mojo, filtering, in, out);
    }

    private static void filterAndCopy(AbstractWisdomMojo mojo, MavenResourcesFiltering filtering, File in, File out) throws IOException {
        Resource resource = new Resource();
        resource.setDirectory(in.getAbsolutePath());
        resource.setFiltering(true);
        resource.setTargetPath(out.getAbsolutePath());

        List<String> excludedExtensions = new ArrayList<>();
        excludedExtensions.addAll(filtering.getDefaultNonFilteredFileExtensions());
        excludedExtensions.addAll(NON_FILTERED_EXTENSIONS);

        MavenResourcesExecution exec = new MavenResourcesExecution(ImmutableList.of(resource), out, mojo.project,
                "UTF-8", Collections.emptyList(), excludedExtensions, mojo.session);

        try {
            filtering.filterResources(exec);
        } catch (MavenFilteringException e) {
            throw new IOException("Error while copying resources", e);
        }
    }
}
