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

package io.fairyproject;

import io.fairyproject.mysql.Session;
import io.fairyproject.mysql.config.AbstractSqlRepositoryProvider;
import io.fairyproject.mysql.connection.AbstractConnectionFactory;
import io.fairyproject.mysql.pojo.Transaction;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class SQLRepository<T, I extends Serializable> extends AbstractRepository<T, I, AbstractSqlRepositoryProvider> {

    public SQLRepository(AbstractSqlRepositoryProvider repositoryProvider, Class<T> type, String repoId) {
        super(repositoryProvider, type, repoId);
    }

    private AbstractConnectionFactory getFactory() {
        return this.repositoryProvider.getFactory();
    }

    public void init() {
        this.repositoryProvider.getIOLock().lock();
        this.getFactory().createTable(this.type());
        this.repositoryProvider.getIOLock().unlock();
    }

    public <R> R supplySession(Function<Session, R> sessionConsumer) {
        R result = null;

        Transaction transaction = null;

        this.repositoryProvider.getIOLock().lock();
        try {
            transaction = this.getFactory().startTransaction();

            Session session = this.getFactory().session(transaction);
            result = sessionConsumer.apply(session);

            transaction.commit();
        } catch (Throwable throwable) {
            if (transaction != null) {
                transaction.rollback();
            }
            throwable.printStackTrace();
        } finally {
            this.repositoryProvider.getIOLock().unlock();
        }

        return result;
    }

    public void runSession(Consumer<Session> sessionConsumer) {
        Transaction transaction = null;

        this.repositoryProvider.getIOLock().lock();
        try {
            transaction = this.getFactory().startTransaction();

            Session session = this.getFactory().session(transaction);
            sessionConsumer.accept(session);

            transaction.commit();
        } catch (Throwable throwable) {
            if (transaction != null) {
                transaction.rollback();
            }
            throwable.printStackTrace();
        } finally {
            this.repositoryProvider.getIOLock().unlock();
        }
    }

    @Override
    public <S extends T> S save(S pojo) {
        this.runSession(session -> session.upsert(pojo));
        return pojo;
    }

    @Override
    public Optional<T> findById(I id) {
        return Optional.ofNullable(this.supplySession(session -> session.find(this.type(), id)));
    }

    @Override
    public <Q> Optional<T> findByQuery(String queryName, Q value) {
        return Optional.ofNullable(this.supplySession(session -> session.findByQuery(this.type(), queryName, value)));
    }

    @Override
    public boolean existsById(I id) {
        return this.supplySession(session -> session.find(this.type(), id) != null);
    }

    @Override
    public Iterable<T> findAll() {
        return this.supplySession(session -> session.results(this.type()));
    }

    @Override
    public Iterable<T> findAllById(List<I> ids) {
        return this.supplySession(session -> session.query().byMultipleIds(this.type(), ids).results(this.type()));
    }

    @Override
    public long count() {
        return this.supplySession(session -> session.query().count(this.type()).first(Long.class));
    }

    @Override
    public void deleteById(I id) {
        this.runSession(session -> session.delete(id));
    }

    @Override
    public <Q> void deleteByQuery(String queryName, Q value) {
        this.runSession(session -> session.query()
                .whereQuery(queryName, value)
                .delete());
    }

    @Override
    public void deleteAll() {
        this.runSession(session -> session.query().delete());
    }


}
