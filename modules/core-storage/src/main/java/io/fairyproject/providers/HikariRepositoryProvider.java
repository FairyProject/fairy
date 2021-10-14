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

package io.fairyproject.providers;

import com.google.common.collect.ImmutableMap;
import io.fairyproject.RepositoryType;
import io.fairyproject.mysql.config.hikari.AbstractHikariRepositoryProvider;

import java.util.Map;

public class HikariRepositoryProvider extends AbstractHikariRepositoryProvider {

    private String address, port, database, username, password;

    public HikariRepositoryProvider(String id, RepositoryType repositoryType) {
        super(id, repositoryType);
    }

    @Override
    public String address() {
        return this.address;
    }

    @Override
    public String port() {
        return this.port;
    }

    @Override
    public String databaseName() {
        return this.database;
    }

    @Override
    public String username() {
        return this.username;
    }

    @Override
    public String password() {
        return this.password;
    }

    @Override
    public Map<String, String> getDefaultOptions() {
        return ImmutableMap.of(
                "address", "localhost",
                "port", "3306",
                "database", "database",
                "username", "user",
                "password", "password"
        );
    }

    @Override
    public void registerOptions(Map<String, String> map) {
        this.address = map.get("address");
        this.port = map.get("port");
        this.database = map.get("database");
        this.username = map.get("username");
        this.password = map.get("password");
    }

}
