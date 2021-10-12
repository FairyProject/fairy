package org.fairy.bootstrap.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class FairyClassLoader extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }

    public FairyClassLoader(Path path) throws MalformedURLException {
        super(new URL[] {path.toUri().toURL()});
    }
}
