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

package io.fairyproject.mysql.connection.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.fairyproject.log.Log;
import lombok.Getter;
import io.fairyproject.Fairy;
import io.fairyproject.mysql.connection.AbstractConnectionFactory;
import io.fairyproject.mysql.pojo.statement.SqlStatementBuilder;
import io.fairyproject.mysql.pojo.statement.StandardSqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class HikariConnectionFactory extends AbstractConnectionFactory {

    @Getter
    protected HikariConfig config;
    protected HikariDataSource dataSource;

    public abstract String defaultPort();

    public abstract void configureDatabase(String address, String port, String databaseName, String username, String password);

    protected void postInitialize() {

    }

    @Override
    public void init() {
        try {
            this.config = new HikariConfig();
        } catch (LinkageError ex) {
            handleLinkageError(ex);
            throw ex;
        }

        this.config.setPoolName("imanity-hikari");
        this.config.setInitializationFailTimeout(-1);
        this.config.addDataSourceProperty("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)));
    }

    @Override
    public SqlStatementBuilder builder() {
        return new StandardSqlStatementBuilder();
    }

    @Override
    public void connect() {
        this.dataSource = new HikariDataSource(this.config);

        this.postInitialize();
    }

    @Override
    public void shutdown() {
        if (this.dataSource != null) {
            this.dataSource.close();
        }
    }

    @Override
    public Connection connection() throws SQLException {
        if (this.dataSource == null) {
            throw new SQLException("Unable to get a connection from the pool. (dataSource is null)");
        }

        Connection connection = this.dataSource.getConnection();
        if (connection == null) {
            throw new SQLException("Unable to get a connection from the pool. (dataSource.getConnection() returned null)");
        }

        return connection;
    }

    private static void handleLinkageError(LinkageError linkageError) {
        List<String> noteworthyClasses = Arrays.asList(
                "org.slf4j.LoggerFactory",
                "org.slf4j.ILoggerFactory",
                "org.apache.logging.slf4j.Log4jLoggerFactory",
                "org.apache.logging.log4j.spi.LoggerContext",
                "org.apache.logging.log4j.spi.AbstractLoggerAdapter",
                "org.slf4j.impl.StaticLoggerBinder"
        );

        Log.warn("A " + linkageError.getClass().getSimpleName() + " has occurred whilst initialising Hikari. This is likely due to classloading conflicts between other plugins.");
        Log.warn("Please check for other plugins below (and try loading LuckPerms without them installed) before reporting the issue.");

        for (String className : noteworthyClasses) {
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (Exception e) {
                continue;
            }

            ClassLoader loader = clazz.getClassLoader();
            String loaderName;
            try {
                loaderName = Fairy.getPlatform().getClassLoaderName(loader) + " (" + loader.toString() + ")";
            } catch (Throwable e) {
                loaderName = loader.toString();
            }

            Log.warn("Class " + className + " has been loaded by: " + loaderName);
        }
    }
}