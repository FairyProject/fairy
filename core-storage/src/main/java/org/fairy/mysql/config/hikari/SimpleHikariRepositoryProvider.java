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

package org.fairy.mysql.config.hikari;

import com.zaxxer.hikari.HikariConfig;
import lombok.SneakyThrows;
import org.fairy.mysql.config.AbstractSqlRepositoryProvider;
import org.fairy.mysql.config.CustomSqlRepositoryProvider;
import org.fairy.mysql.connection.hikari.HikariConnectionFactory;

public abstract class SimpleHikariRepositoryProvider<T extends HikariConnectionFactory> extends CustomSqlRepositoryProvider<T> {

    public SimpleHikariRepositoryProvider(String id) {
        super(id);
    }

    @Override
    @SneakyThrows
    public T createFactory() {
        T factory = this.factoryClass().newInstance();
        factory.init();
        this.setupFactory(factory);
        return factory;
    }

    public void setupFactory(T factory) {
        factory.configureDatabase(this.address(), this.port(), this.databaseName(), this.username(), this.password());
        HikariConfig config = factory.getConfig();

        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(true);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(10);
        config.setValidationTimeout(3000);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(60000);
    }

    public abstract String address();

    public abstract String port();

    public abstract String databaseName();

    public abstract String username();

    public abstract String password();

}
