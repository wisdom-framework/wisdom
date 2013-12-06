package org.wisdom.asciidoc;

import org.asciidoctor.AbstractDirectoryWalker;

import java.io.File;
import java.util.List;

/**
 * A directory walker supporting several extensions.
 */
public class CustomExtensionDirectoryWalker extends AbstractDirectoryWalker {
    private final List<String> extensions;

    public CustomExtensionDirectoryWalker(final String root, final List<String> extensions) {
        super(root);
        this.extensions = extensions;
    }

    @Override
    protected boolean isAcceptedFile(final File filename) {
        final String name = filename.getName();
        for (final String extension : extensions) {
            if (name.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}
