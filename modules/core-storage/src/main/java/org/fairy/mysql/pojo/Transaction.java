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

package org.fairy.mysql.pojo;

import org.fairy.mysql.ImanitySqlException;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;

/**
 * Represents a database transaction. Create it using Transaction trans =
 * Database.startTransation(), pass it to the query object using
 * .transaction(trans), and then call trans.commit() or trans.rollback().
 * <p>
 * Some things to note: commit() and rollback() also call close() on the
 * connection, so this class cannot be reused after the transaction is committed
 * or rolled back.
 * </p>
 * <p>
 * This is just a convenience class. If the implementation is too restrictive,
 * then you can manage your own transactions by calling Database.getConnection()
 * and operate on the Connection directly.
 * </p>
 */
public class Transaction implements Closeable {
	private Connection connection;

	public void setConnection(Connection con) {
		this.connection = con;
		try {
			con.setAutoCommit(false);
		} catch (Throwable t) {
			throw new ImanitySqlException(t);
		}
	}

	public void commit() {
		try {
			connection.commit();
		} catch (Throwable t) {
			throw new ImanitySqlException(t);
		} finally {
			try {
				connection.close();
			} catch (Throwable t) {
				throw new ImanitySqlException(t);
			}
		}
	}

	public void rollback() {
		try {
			connection.rollback();
		} catch (Throwable t) {
			throw new ImanitySqlException(t);
		} finally {
			try {
				connection.close();
			} catch (Throwable t) {
				throw new ImanitySqlException(t);
			}
		}
	}

	public Connection getConnection() {
		return connection;
	}

	/**
	 * This simply calls .commit();
	 */
	@Override
	public void close() throws IOException {
		commit();
	}

}
