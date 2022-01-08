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

package io.fairyproject.mysql;

import io.fairyproject.mysql.connection.AbstractConnectionFactory;
import io.fairyproject.mysql.pojo.Query;
import io.fairyproject.mysql.pojo.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class Session implements AutoCloseable {

    private final AbstractConnectionFactory factory;
    private final Transaction transaction;

    @Override
    public void close() throws Exception {
        transaction.commit();
    }

    public Query query() {
        return this.factory.transaction(transaction);
    }

    public <T> T find(Class<T> clazz, Object id) {
        return this.byId(clazz, id).first(clazz);
    }

    public <T> T findByQuery(Class<T> clazz, String query, Object value) {
        return this.query().whereQuery(query, value).first(clazz);
    }

    public <T> List<T> findAllByQuery(Class<T> clazz, String query, Object value) {
        return this.query().whereQuery(query, value).results(clazz);
    }

    public Query byId(Class<?> clazz, Object id) {
        return this.query().byId(clazz, id);
    }

    /**
     * Simple, primitive method for creating a table based on a pojo. Does not add
     * indexes or implement complex data types. Probably not suitable for production
     * use.
     */
    public Query createTable(Class<?> clazz) {
        return this.query().createTable(clazz);
    }

    /**
     * Insert a row into a table. The row pojo can have a @Table annotation to
     * specify the table, or you can specify the table with the .table() method.
     */
    public Query insert(Object row) {
        return this.query().insert(row);
    }

    /**
     * See {@link Query#generatedKeyReceiver(Object, String...)
     * generateKeyReceiver} method.
     */
    public Query generatedKeyReceiver(Object generatedKeyReceiver, String... generatedKeyNames) {
        return this.query().generatedKeyReceiver(generatedKeyReceiver, generatedKeyNames);
    }

    /**
     * Delete a row in a table. This method looks for an @Id annotation to find the
     * row to delete by primary key, and looks for a @Table annotation to figure out
     * which table to hit.
     */
    public Query delete(Object row) {
        return this.query().delete(row);
    }

    /**
     * Execute a "select" query and get some results. The system will create a new
     * object of type "clazz" for each row in the result set and add it to a List.
     * It will also try to extract the table name from a @Table annotation in the
     * clazz.
     */
    public <T> List<T> results(Class<T> clazz) {
        return this.query().results(clazz);
    }

    /**
     * Returns the first row in a query in a pojo. Will return it in a Map if a
     * class that implements Map is specified.
     */
    public <T> T first(Class<T> clazz) {
        return this.query().first(clazz);
    }

    /**
     * Update a row in a table. It will match an existing row based on the primary
     * key.
     */
    public Query update(Object row) {
        return this.query().update(row);
    }

    /**
     * Upsert a row in a table. It will insert, and if that fails, do an update with
     * a match on a primary key.
     */
    public Query upsert(Object row) {
        return this.query().upsert(row);
    }

    /**
     * Create a query and specify which table it operates on.
     */
    public Query table(String table) {
        return this.query().table(table);
    }

}
