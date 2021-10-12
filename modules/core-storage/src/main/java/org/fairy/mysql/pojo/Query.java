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

import lombok.Getter;
import org.fairy.mysql.ImanitySqlException;
import org.fairy.mysql.connection.AbstractConnectionFactory;
import org.fairy.mysql.pojo.info.PojoInfo;
import org.fairy.mysql.pojo.statement.SqlStatementBuilder;
import org.fairy.mysql.util.SQLUtil;
import org.intellij.lang.annotations.Language;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Query {

	private Object generatedKeyReceiver;
	private String[] generatedKeyNames;

	private String sql;
	private String table;
	private String orderBy;

	private String where;
	private Object[] args;

	private List<Where> wheres;

	private int rowsAffected;

	private ResultSetMetaData metaData;

	private final AbstractConnectionFactory factory;
	private final SqlStatementBuilder sqlStatementBuilder;

	private Transaction transaction;

	public Query(AbstractConnectionFactory factory) {
		this.factory = factory;
		this.sqlStatementBuilder = factory.builder();
		this.wheres = new ArrayList<>();
	}

	/**
	 * Add a where clause and some parameters to a query. Has no effect if the
	 * .sql() method is used.
	 * 
	 * @param where Example: "name=?"
	 * @param args  The parameter values to use in the where, example: "Bob"
	 */
	public Query where(String where, Object... args) {
		this.where = where;
		this.args = args;
		return this;
	}

	public Query whereQuery(String key, Object value) {
		this.wheres.add(new Where(key, value));
		return this;
	}

	public Query count(Class<?> type) {
		PojoInfo pojoInfo = this.sqlStatementBuilder.getPojoInfo(type);
		if (pojoInfo == null) {
			throw new IllegalArgumentException("The POJO info for type " + type.getName() + " does not exists!");
		}
		this.sql = "select count(*) as count from " + pojoInfo.getTable();
		if (this.where != null) {
			this.sql += " where " + where;
		}
		return this;
	}

	public Query byMultipleIds(Class<?> type, List ids) {
		PojoInfo info = this.sqlStatementBuilder.getPojoInfo(type);
		if (info != null) {
			this.where = info.getPrimaryKeyName() + " in ";

			Object[] idArray = new Object[ids.size()];
			for (int i = 0; i < idArray.length; i++) {
				idArray[i] = info.toReadableValue(info.getProperty(info.getPrimaryKeyName()), ids.get(i));
			}

			this.args = new Object[] {ids};
		} else {
			throw new IllegalArgumentException("The POJO info for type " + type.getName() + " does not exists!");
		}
		return this;
	}

	public Query byId(Class<?> type, Object id) {
		PojoInfo info = this.sqlStatementBuilder.getPojoInfo(type);
		if (info != null) {
			this.whereQuery(info.getPrimaryKeyName(), id);
		} else {
			throw new IllegalArgumentException("The POJO info for type " + type.getName() + " does not exists!");
		}

		return this;
	}

	/**
	 * Create a query using straight SQL. Overrides any other methods like .where(),
	 * .orderBy(), etc.
	 * 
	 * @param sql  The SQL string to use, may include ? parameters.
	 * @param args The parameter values to use in the query.
	 */
	public Query sql(@Language("SQL") String sql, Object... args) {
		this.sql = sql;
		this.args = args;
		return this;
	}

	/**
	 * Create a query using straight SQL. Overrides any other methods like .where(),
	 * .orderBy(), etc.
	 * 
	 * @param sql  The SQL string to use, may include ? parameters.
	 * @param args The parameter values to use in the query.
	 */
	public Query sql(@Language("SQL") String sql, List<?> args) {
		this.sql = sql;
		this.args = args.toArray();
		return this;
	}

	/**
	 * Add an "orderBy" clause to a query.
	 */
	public Query orderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	/**
	 * Returns the first row in a query in a pojo, or null if the query returns no
	 * results. Will return it in a Map if a class that implements Map is specified.
	 */
	public <T> T first(Class<T> clazz) {
		List<T> list = results(clazz);
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Provides the results as a list of Map objects instead of a list of pojos.
	 */
	private List<Map<String, Object>> resultsMap(Class<Map<String, Object>> clazz) {

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Connection con = null;
		PreparedStatement state = null;

		try {
			if (sql == null) {
				sql = sqlStatementBuilder.getSelectSql(this, clazz);
			}

			Connection localCon;
			if (transaction == null) {
				localCon = factory.connection();
				con = localCon; // con gets closed below if non-null
			} else {
				localCon = transaction.getConnection();
			}

			state = localCon.prepareStatement(sql);
			loadArgs(state);

			ResultSet resultSet = state.executeQuery();

			metaData = resultSet.getMetaData();
			int colCount = metaData.getColumnCount();

			while (resultSet.next()) {
				Map<String, Object> map;
				if (clazz.equals(Map.class)) {
					map = new HashMap<>();
				} else {
					map = clazz.getDeclaredConstructor().newInstance();
				}

				for (int i = 1; i <= colCount; i++) {
					String columnName = metaData.getColumnLabel(i);
					map.put(columnName, resultSet.getObject(i));
				}
				result.add(map);
			}

		} catch (InstantiationException | IllegalAccessException | SQLException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new ImanitySqlException(e);
		} finally {
			close(state);
			close(con);
		}

		return result;
	}

	/**
	 * Execute a "select" query and return a list of results where each row is an
	 * instance of clazz. Returns an empty list if there are no results.
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> results(Class<T> clazz) {
		if (Map.class.isAssignableFrom(clazz)) {
			return (List<T>) resultsMap((Class<Map<String, Object>>) clazz);
		}

		List<T> out = new ArrayList<T>();
		Connection con = null;
		PreparedStatement state = null;

		try {
			if (sql == null) {
				sql = sqlStatementBuilder.getSelectSql(this, clazz);
			}

			Connection localCon;
			if (transaction == null) {
				localCon = factory.connection();
				con = localCon; // con gets closed below if non-null
			} else {
				localCon = transaction.getConnection();
			}

			state = localCon.prepareStatement(sql);
			loadArgs(state);

			ResultSet rs = state.executeQuery();

			metaData = rs.getMetaData();
			int colCount = metaData.getColumnCount();

			if (SQLUtil.isPrimitiveOrString(clazz) || clazz.getPackage().getName().startsWith("java.sql")) {
				// if the receiver class is a primitive or jdbc type just grab the first column
				// and assign it
				while (rs.next()) {
					Object colValue = rs.getObject(1);
					out.add((T) colValue);
				}

			} else {
				PojoInfo pojoInfo = sqlStatementBuilder.getPojoInfo(clazz);
				while (rs.next()) {
					T row = clazz.getDeclaredConstructor().newInstance();

					for (int i = 1; i <= colCount; i++) {
						String colName = metaData.getColumnLabel(i);
						Object colValue = sqlStatementBuilder.convertValue(rs.getObject(i), metaData.getColumnTypeName(i));

						pojoInfo.putValue(row, colName, colValue, true);
					}
					out.add(row);
				}
			}

		} catch (InstantiationException | IllegalAccessException | SQLException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			ImanitySqlException dbe = new ImanitySqlException(e);
			dbe.setSql(sql);
			throw dbe;
		} finally {
			close(state);
			close(con);
		}

		return out;
	}

	private void loadArgs(PreparedStatement state) throws SQLException {
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				state.setObject(i + 1, args[i]);
			}
		}
	}

	private void close(AutoCloseable ac) {
		if (ac == null) {
			return;
		}
		try {
			ac.close();
		} catch (Exception e) {
			// bury it
		}
	}

	/**
	 * Insert a row into a table. The row pojo can have a @Table annotation to
	 * specify the table, or you can specify the table with the .table() method.
	 */
	public Query insert(Object row) {

		if (this.generatedKeyReceiver == null) {
			PojoInfo pojoInfo = sqlStatementBuilder.getPojoInfo(row.getClass());
			Property prop = pojoInfo.getGeneratedColumnProperty();
			if (prop != null) {
				this.generatedKeyReceiver = row;
				this.generatedKeyNames = new String[] {prop.getName()};
			}
		}

		sql = sqlStatementBuilder.getInsertSql(this, row);
		args = sqlStatementBuilder.getInsertArgs(this, row);

		execute();

		return this;
	}

	/**
	 * Upsert a row into a table. See http://en.wikipedia.org/wiki/Merge_%28SQL%29
	 */
	public Query upsert(Object row) {

		sql = sqlStatementBuilder.getUpsertSql(this, row);
		args = sqlStatementBuilder.getUpsertArgs(this, row);

		execute();

		return this;
	}

	/**
	 * Update a row in a table. It will match an existing row based on the primary
	 * key.
	 */
	public Query update(Object row) {

		sql = sqlStatementBuilder.getUpdateSql(this, row);
		args = sqlStatementBuilder.getUpdateArgs(this, row);

		if (execute().getRowsAffected() <= 0) {
			throw new ImanitySqlException("Row not updated because the primary key was not found");
		}
		return this;
	}

	/**
	 * Execute a sql command that does not return a result set. The sql should
	 * previously have been set with the sql(String) method. Returns this Query
	 * object. To see how the command did, call .rowsAffected().
	 */
	public Query execute() {

		Connection con = null;
		PreparedStatement state = null;

		try {

			Connection localCon;
			if (transaction == null) {
				localCon = factory.connection();
				con = localCon; // con gets closed below if non-null
			} else {
				localCon = transaction.getConnection();
			}

			// see notes on generatedKeyReceiver()
			if (generatedKeyReceiver != null) {
				state = localCon.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			} else {
				state = localCon.prepareStatement(sql);
			}

			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					Object arg = args[i];

					/*
					 * The purpose of this is to allow List args to be inserted into JDBC array
					 * fields. Postgres JDBC drivers do not handle this correctly.
					 */
					if (arg != null && List.class.isAssignableFrom(arg.getClass())) {
						arg = ((List<?>) arg).toArray();
					}

					state.setObject(i + 1, arg);
				}
			}

			rowsAffected = state.executeUpdate();

			if (generatedKeyReceiver != null) {
				populateGeneratedKeys(state, generatedKeyReceiver, generatedKeyNames);
			}

		} catch (SQLException | IllegalArgumentException e) {
			ImanitySqlException dbe = new ImanitySqlException(e);
			dbe.setSql(sql);
			throw dbe;
		} finally {
			close(state);
			close(con);
		}

		return this;
	}

	@SuppressWarnings("unchecked")
	private void populateGeneratedKeys(PreparedStatement state, Object generatedKeyReceiver,
			String[] generatedKeyNames) {

		ResultSet resultSet = null;

		try {
			boolean isMap = Map.class.isAssignableFrom(generatedKeyReceiver.getClass());

			PojoInfo pojoInfo = null;
			if (!isMap) {
				pojoInfo = sqlStatementBuilder.getPojoInfo(generatedKeyReceiver.getClass());
			}

			/*-
			 * JDBC drivers are inconsistent in the way they handle generated keys.
			 * MySQL returns a single column named "GENERATED_KEY". The column has the incorrect name, obviously.
			 * Postgres returns a row of keys with the right names, but it returns more than just the generated ones.
			 * So we do a hack: it it's just one column, assume it's the right one, else fetch the value
			 * by column name.
			 */

			resultSet = state.getGeneratedKeys();

			ResultSetMetaData meta = resultSet.getMetaData();
			int colCount = meta.getColumnCount();

			if (resultSet.next()) {
				if (isMap) {
					Map<String, Object> map = (Map<String, Object>) generatedKeyReceiver;
					if (colCount == 1) {
						map.put(generatedKeyNames[0], resultSet.getObject(1));
					} else {
						for (String generatedKeyName : generatedKeyNames) {
							map.put(generatedKeyName, resultSet.getObject(generatedKeyName));
						}
					}

				} else {

					for (String generatedKeyName : generatedKeyNames) {
						Property prop = pojoInfo.getProperty(generatedKeyName);
						if (prop == null) {
							throw new ImanitySqlException("Generated key name not found: " + generatedKeyName);
						}

						/*
						 * getObject() below doesn't handle primitives correctly. Must convert to object
						 * equivalent.
						 */

						Class<?> type = SQLUtil.wrap(prop.getDataType());

						Object newKey;
						if (colCount == 1) {
							newKey = resultSet.getObject(1, type);
						} else {
							newKey = resultSet.getObject(prop.getName(), type);
						}

						pojoInfo.putValue(generatedKeyReceiver, prop.getName(), newKey);
					}
				}
			}

		} catch (SQLException | SecurityException | IllegalArgumentException e) {
			throw new ImanitySqlException(e);

		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException ignored) {
				}
			}
		}

	}

	// similar to Guava's Primitives.wrap

	/**
	 * Specify the object and its fields that should receive any column values that
	 * the database server generates during an insert or update. If a column is
	 * marked as "AUTO INCREMENT" (MySql) or has datatype "SERIAL" (Postgres) then
	 * the database will generate a value for that column. Databases can also
	 * generate a last-updated timestamp for a column, or just fill in a column with
	 * a default value. To get those values, you can specify generatedKeyReceiver as
	 * a Map or as a pojo. It can be the pojo you just inserted or updated. Specify
	 * also the field/column names that should be filled in.
	 */
	public Query generatedKeyReceiver(Object generatedKeyReceiver, String... generatedKeyNames) {
		this.generatedKeyReceiver = generatedKeyReceiver;
		this.generatedKeyNames = generatedKeyNames;
		return this;
	}

	/**
	 * Simple, primitive method for creating a table based on a pojo.
	 */
	public Query createTable(Class<?> clazz) {
		sql = sqlStatementBuilder.getCreateTableSql(clazz);
		execute();
		return this;
	}

	/**
	 * Delete a row in a table. This method looks for an @Id annotation to find the
	 * row to delete by primary key, and looks for a @Table annotation to figure out
	 * which table to hit.
	 */
	public Query delete(Object row) {

		sql = sqlStatementBuilder.getDeleteSql(this, row);
		args = sqlStatementBuilder.getDeleteArgs(this, row);

		execute();
		return this;
	}

	/**
	 * Delete multiple rows in a table. Be sure to specify the table with the
	 * .table() method and limit the rows to delete using the .where() method.
	 */
	public Query delete() {
		String table = getTable();
		if (table == null) {
			throw new ImanitySqlException("You must specify a table name with the table() method.");
		}
		sql = "delete from " + table;
		if (where != null) {
			sql += " where " + where;
		}
		execute();
		return this;
	}

	/**
	 * Specify the table to operate on.
	 */
	public Query table(String table) {
		this.table = table;
		return this;
	}

	/**
	 * For queries that affect the database in some way, this method returns the
	 * number of rows affected. Call it after you call .execute(), .update(),
	 * .delete(), etc.: .table("foo").where("bar=bah").delete().rowsAffected();
	 */
	public int getRowsAffected() {
		return rowsAffected;
	}

	/**
	 * Specify that this query should be a part of the specified transaction.
	 */
	public Query transaction(Transaction trans) {
		this.transaction = trans;
		return this;
	}

}
