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
import lombok.Getter;
import lombok.SneakyThrows;
import org.fairy.RepositoryType;
import org.fairy.mysql.config.AbstractSqlRepositoryProvider;
import org.fairy.mysql.connection.AbstractConnectionFactory;
import org.fairy.mysql.connection.hikari.HikariConnectionFactory;
import org.fairy.mysql.connection.hikari.MariaConnectionFactory;
import org.fairy.mysql.connection.hikari.MySqlConnectionFactory;
import org.fairy.mysql.connection.hikari.PostgreConnectionFactory;

public abstract class AbstractHikariRepositoryProvider extends AbstractSqlRepositoryProvider {

    private final RepositoryType repositoryType;
    private final HikariFactory hikariFactory;

    public AbstractHikariRepositoryProvider(String id, RepositoryType repositoryType) {
        super(id);
        this.repositoryType = repositoryType;
        this.hikariFactory = HikariFactory.getByRepositoryType(repositoryType);
    }

    @Override
    @SneakyThrows
    public AbstractConnectionFactory createFactory() {
        final HikariConnectionFactory hikariConnectionFactory = this.hikariFactory.getType().newInstance();
        this.hikariFactory.setupFactory(this, hikariConnectionFactory);
        return hikariConnectionFactory;
    }

    public abstract String address();

    public abstract String port();

    public abstract String databaseName();

    public abstract String username();

    public abstract String password();

    @Override
    public Class<? extends AbstractConnectionFactory> factoryClass() {
        return this.hikariFactory.getType();
    }

    @Override
    public RepositoryType type() {
        return this.repositoryType;
    }

    @Getter
    private enum HikariFactory {

        MYSQL(RepositoryType.MYSQL, MySqlConnectionFactory.class) {
            @Override
            public void setupFactory(AbstractHikariRepositoryProvider repositoryProvider, HikariConnectionFactory factory) {
                super.setupFactory(repositoryProvider, factory);
                HikariConfig config = factory.getConfig();

                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("useServerPrepStmts", "true");
                config.addDataSourceProperty("useLocalSessionState", "true");
                config.addDataSourceProperty("useLocalTransactionState", "true");
                config.addDataSourceProperty("rewriteBatchedStatements", "true");
                config.addDataSourceProperty("cacheResultSetMetadata", "true");
                config.addDataSourceProperty("cacheServerConfiguration", "true");
                config.addDataSourceProperty("elideSetAutoCommits", "true");
                config.addDataSourceProperty("maintainTimeStats", "false");
            }
        },
        MARIA(RepositoryType.MARIADB, MariaConnectionFactory.class),
        POSTGRE(RepositoryType.POSTGRE, PostgreConnectionFactory.class);

        private final RepositoryType repositoryType;
        private final Class<? extends HikariConnectionFactory> type;

        HikariFactory(RepositoryType repositoryType, Class<? extends HikariConnectionFactory> type) {
            this.repositoryType = repositoryType;
            this.type = type;
        }

        public static HikariFactory getByRepositoryType(RepositoryType type) {
            for (HikariFactory factory : HikariFactory.values()) {
                if (factory.getRepositoryType() == type) {
                    return factory;
                }
            }

            throw new UnsupportedOperationException(type.name() + " does not have HikariFactory type.");
        }

        public void setupFactory(AbstractHikariRepositoryProvider repositoryProvider, HikariConnectionFactory factory) {
            factory.configureDatabase(
                    repositoryProvider.address(),
                    repositoryProvider.port(),
                    repositoryProvider.databaseName(),
                    repositoryProvider.username(),
                    repositoryProvider.password()
            );
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

    }

}
