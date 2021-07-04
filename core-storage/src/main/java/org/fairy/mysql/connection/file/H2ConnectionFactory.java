/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.fairy.mysql.connection.file;

import org.fairy.Fairy;
import org.fairy.RepositoryType;
import org.fairy.library.Library;
import org.fairy.library.classloader.IsolatedClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class H2ConnectionFactory extends FileConnectionFactory {

    private static boolean LIBRARY_LOADED = false;
    private static IsolatedClassLoader CLASS_LOADER;

    private final Driver driver;
    private NonClosableConnection connection;

    public H2ConnectionFactory(Path path) {
        this(path, false);
    }

    public H2ConnectionFactory(Path path, boolean test) {
        super(path);

        try {
            if (test) {
                Class<?> driverClass = Class.forName("org.h2.Driver");
                Method loadMethod = driverClass.getMethod("load");
                this.driver = (Driver) loadMethod.invoke(null);
                return;
            }
            Class<?> driverClass = Fairy.getLibraryHandler()
                    .obtainClassLoaderWith(Library.H2_DRIVER)
                    .loadClass("org.h2.Driver");
            Method loadMethod = driverClass.getMethod("load");
            this.driver = (Driver) loadMethod.invoke(null);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public RepositoryType type() {
        return RepositoryType.H2;
    }

    @Override
    public void connect() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            return;
        }

        Connection connection = this.driver.connect(this.url(), new Properties());
        if (connection != null) {
            this.connection = NonClosableConnection.wrap(connection);
        } else {
            throw new SQLException("Unable to get a connection.");
        }
    }

    public String url() {
        return "jdbc:h2:" + this.path.toString() + ";mode=MySQL";
    }

    @Override
    public void shutdown() throws Exception {
        if (this.connection != null) {
            this.connection.shutdown();
        }
    }

    @Override
    public Connection connection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            this.connect();
        }

        return this.connection;
    }
}