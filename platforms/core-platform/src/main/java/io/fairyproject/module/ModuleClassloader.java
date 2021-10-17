package io.fairyproject.module;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class ModuleClassloader extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }

    public ModuleClassloader(Path path) throws MalformedURLException {
        super(new URL[] { path.toUri().toURL() });
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
}
