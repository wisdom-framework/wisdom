package org.ow2.chameleon.wisdom.maven.processors;

import java.io.File;

/**
* Allows processor to notify a warning.
*/
public class ProcessorWarning {
    public final File file;
    public final int line;
    public final int character;
    public final String evidence;
    public final String reason;

    public ProcessorWarning(File file, int line, int character, String evidence, String reason) {
        this.file = file;
        this.line = line;
        this.character = character;
        this.evidence = evidence;
        this.reason = reason;
    }
}
