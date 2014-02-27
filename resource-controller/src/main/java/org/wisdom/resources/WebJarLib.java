package org.wisdom.resources;

import java.io.File;

/**
* A simple class representing a library contained in a Web Jar.
*/
class WebJarLib {

    public final File root;
    public final String name;
    public final String version;

    WebJarLib(String name, String version, File root) {
        this.root = root;
        this.name = name;
        this.version = version;
    }

    public boolean contains(String path) {
        return new File(root, path).isFile();
    }

    public File get(String path) {
        return new File(root, path);
    }
}
