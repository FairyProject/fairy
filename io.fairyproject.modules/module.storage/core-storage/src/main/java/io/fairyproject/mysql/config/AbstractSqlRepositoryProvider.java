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

package io.fairyproject.mysql.config;

import io.fairyproject.AbstractRepositoryProvider;
import io.fairyproject.Repository;
import io.fairyproject.RepositoryType;
import io.fairyproject.SQLRepository;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import lombok.Getter;
import io.fairyproject.mysql.connection.AbstractConnectionFactory;
import io.fairyproject.util.Utility;

import java.io.Serializable;
import java.sql.SQLException;

public abstract class AbstractSqlRepositoryProvider extends AbstractRepositoryProvider {

    @Getter
    private AbstractConnectionFactory factory;

    public AbstractSqlRepositoryProvider(String id) {
        super(id);
    }

    @Override
    public void build0() {
        if (this.factory != null) {
            ThrowingRunnable.sneaky(this.factory::shutdown).run();
        }
        this.factory = this.createFactory();
        this.factory.init();
        try {
            this.factory.connect();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() throws Exception {
        this.factory.shutdown();
    }

    @Override
    public <E, ID extends Serializable> Repository<E, ID> createRepository(Class<E> entityType, String repoId) {
        return new SQLRepository<>(this, entityType, repoId);
    }

    public abstract AbstractConnectionFactory createFactory();

    public abstract Class<? extends AbstractConnectionFactory> factoryClass();

    public abstract RepositoryType type();

}
