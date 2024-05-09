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
import io.fairyproject.RepositoryType;
import io.fairyproject.mysql.pojo.statement.MySqlStatementBuilder;
import io.fairyproject.mysql.pojo.statement.SqlStatementBuilder;

public class MariaConnectionFactory extends HikariConnectionFactory {
    @Override
    public String defaultPort() {
        return "3306";
    }

    @Override
    public RepositoryType type() {
        return RepositoryType.MARIADB;
    }

    @Override
    public void configureDatabase(String address, String port, String databaseName, String username, String password) {
        config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        config.addDataSourceProperty("serverName", address);
        config.addDataSourceProperty("port", port);
        config.addDataSourceProperty("databaseName", databaseName);
        config.setUsername(username);
        config.setPassword(password);
    }

    @Override
    public SqlStatementBuilder builder() {
        return new MySqlStatementBuilder();
    }

}