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

package io.fairyproject.mysql.pojo.statement;

import io.fairyproject.mysql.ImanitySqlException;
import io.fairyproject.mysql.pojo.Property;
import io.fairyproject.mysql.pojo.Query;
import io.fairyproject.mysql.pojo.Where;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.fairyproject.mysql.pojo.info.StandardPojoInfo;
import io.fairyproject.mysql.util.SQLUtil;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Produces ANSI-standard SQL. Extend this class to handle different flavors of sql.
 */
public class StandardSqlStatementBuilder implements SqlStatementBuilder {

	private static final Logger LOGGER = LogManager.getLogger(StandardSqlStatementBuilder.class);
	private static final ConcurrentHashMap<Class<?>, StandardPojoInfo> POJOS = new ConcurrentHashMap<>();

	public StandardPojoInfo getPojoInfo(Class<?> rowClass) {
		StandardPojoInfo pi = POJOS.get(rowClass);
		if (pi == null) {
			pi = new StandardPojoInfo(rowClass);
			POJOS.put(rowClass, pi);
			
			makeInsertSql(pi);
			makeUpsertSql(pi);
			makeUpdateSql(pi);
			makeSelectColumns(pi);
		}
		return pi;
	}
	
	
	@Override
	public String getInsertSql(Query query, Object row) {
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
		return pojoInfo.getInsertSql();
	}
	
	@Override
	public Object[] getInsertArgs(Query query, Object row) {
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
		Object [] args = new Object[pojoInfo.getInsertSqlArgCount()];
		for (int i = 0; i < pojoInfo.getInsertSqlArgCount(); i++) {
			args[i] = pojoInfo.getValue(row, pojoInfo.getInsertColumnNames()[i]);
		}
		return args;
	}
	
	@Override
	public String getUpdateSql(Query query, Object row) {
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
		if (pojoInfo.getPrimaryKeyName() == null) {
			throw new ImanitySqlException("No primary key specified in the row. Use the @Id annotation.");
		}
		return pojoInfo.getUpdateSql();
	}

	@Override
	public Object[] getUpdateArgs(Query query, Object row) {
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
		
		Object [] args = new Object[pojoInfo.getUpdateSqlArgCount()];
		for (int i = 0; i < pojoInfo.getUpdateSqlArgCount() - 1; i++) {
			args[i] = pojoInfo.getValue(row, pojoInfo.getUpdateColumnNames()[i]);
		}
		// add the value for the where clause to the end
		Object pk = pojoInfo.getValue(row, pojoInfo.getPrimaryKeyName());
		args[pojoInfo.getUpdateSqlArgCount() - 1] = pk;
		return args;
	}




	public void makeUpdateSql(StandardPojoInfo pojoInfo) {
		
		ArrayList<String> cols = new ArrayList<String>();
		for (Property prop: pojoInfo.getPropertyMap().values()) {
			
			if (prop.isPrimaryKey()) {
				continue;
			}
			
			if (prop.isGenerated()) {
				continue;
			}
			
			cols.add(prop.getName());
		}
		pojoInfo.setUpdateColumnNames(cols.toArray(new String [cols.size()]));
		pojoInfo.setUpdateSqlArgCount(pojoInfo.getUpdateColumnNames().length + 1); // + 1 for the where arg
		
		StringBuilder buf = new StringBuilder();
		buf.append("update ");
		buf.append(pojoInfo.getTable());
		buf.append(" set ");

		for (int i = 0; i < cols.size(); i++) {
			if (i > 0) {
				buf.append(',');
			}
			buf.append(cols.get(i) + "=?");
		}
		buf.append(" where " + pojoInfo.getPrimaryKeyName() + "=?");
		
		pojoInfo.setUpdateSql(buf.toString());
	}
	

	
	public void makeInsertSql(StandardPojoInfo pojoInfo) {
		ArrayList<String> cols = new ArrayList<String>();
		for (Property prop: pojoInfo.getPropertyMap().values()) {
			if (prop.isGenerated()) {
				continue;
			}
			cols.add(prop.getName());
		}
		pojoInfo.setInsertColumnNames(cols.toArray(new String [cols.size()]));
		pojoInfo.setInsertSqlArgCount(pojoInfo.getInsertColumnNames().length);
		
		StringBuilder buf = new StringBuilder();
		buf.append("insert into ");
		buf.append(pojoInfo.getTable());
		buf.append(" (");
		buf.append(SQLUtil.join(pojoInfo.getInsertColumnNames())); // comma sep list?
		buf.append(") values (");
		buf.append(SQLUtil.getQuestionMarks(pojoInfo.getInsertSqlArgCount()));
		buf.append(")");
		
		pojoInfo.setInsertSql(buf.toString());
	}
	
	public void makeUpsertSql(StandardPojoInfo pojoInfo) {
	}

	private void makeSelectColumns(StandardPojoInfo pojoInfo) {
		if (pojoInfo.getPropertyMap().isEmpty()) {
			// this applies if the rowClass is a Map
			pojoInfo.setSelectColumns("*");
		} else {
			ArrayList<String> cols = new ArrayList<String>();
			for (Property prop: pojoInfo.getPropertyMap().values()) {
				cols.add(prop.getName());
			}
			pojoInfo.setSelectColumns(SQLUtil.join(cols));
		}
	}


	@Override
	public String getSelectSql(Query query, Class<?> rowClass) {

		// unlike insert and update, this needs to be done dynamically
		// and can't be precalculated because of the where and order by
		
		StandardPojoInfo pojoInfo = getPojoInfo(rowClass);
		String columns = pojoInfo.getSelectColumns();
		
		String where = query.getWhere();
		if (where == null) {
			if (query.getWheres().size() > 0) {
				where = "";
				Iterator<Where> iterator = query.getWheres().iterator();

				while (iterator.hasNext()) {

					Where whereObj = iterator.next();

					Object value;
					Property property = pojoInfo.getProperty(whereObj.getProperty());
					if (property != null) {
						value = pojoInfo.toReadableValue(property, whereObj.getValue());
					} else {
						value = whereObj.getValue().toString();
					}

					where += whereObj.getProperty() + "=\'" + value.toString() + "\'";
					if (iterator.hasNext()) {
						where += ",";
					}
				}
			}
		} else if (query.getWheres().size() > 0) {
			LOGGER.error(new IllegalArgumentException("There is where statement specified but Where list also not empty!"));
		}

		String table = query.getTable();
		if (table == null) {
			table = pojoInfo.getTable();
		}
		String orderBy = query.getOrderBy();
		
		StringBuilder out = new StringBuilder();
		out.append("select ");
		out.append(columns);
		out.append(" from ");
		out.append(table);
		if (where != null) {
			out.append(" where ");
			out.append(where);
		}
		if (orderBy != null) {
			out.append(" order by ");
			out.append(orderBy);
		}
		return out.toString();
	}


	@Override
	public String getCreateTableSql(Class<?> clazz) {
		
		StringBuilder buf = new StringBuilder();

		StandardPojoInfo pojoInfo = getPojoInfo(clazz);
		buf.append("create table if not exists ");
		buf.append(pojoInfo.getTable());
		buf.append(" (");
		
		boolean needsComma = false;
		for (Property prop : pojoInfo.getPropertyMap().values()) {
			
			if (needsComma) {
				buf.append(',');
			}
			needsComma = true;

			Column columnAnnot = prop.getColumnAnnotation();
			if (columnAnnot == null) {
	
				buf.append(prop.getName());
				buf.append(" ");
				buf.append(getColType(prop.getDataType(), 255, 10, 2));
				if (prop.isGenerated()) {
					buf.append(" auto_increment");
				}
				
			} else {
				if (columnAnnot.columnDefinition() == null) {
					
					// let the column def override everything
					buf.append(columnAnnot.columnDefinition());
					
				} else {

					buf.append(prop.getName());
					buf.append(" ");
					buf.append(getColType(prop.getDataType(), columnAnnot.length(), columnAnnot.precision(), columnAnnot.scale()));
					if (prop.isGenerated()) {
						buf.append(" auto_increment");
					}
					
					if (columnAnnot.unique()) {
						buf.append(" unique");
					}
					
					if (!columnAnnot.nullable()) {
						buf.append(" not null");
					}
				}
			}
		}
		
		if (pojoInfo.getPrimaryKeyName() != null) {
			buf.append(", primary key (");
			buf.append(pojoInfo.getPrimaryKeyName());
			buf.append(")");
		}
		
		buf.append(")");
		
		return buf.toString();
	}


	protected String getColType(Class<?> dataType, int length, int precision, int scale) {
		String colType;
		
		if (dataType.equals(Integer.class) || dataType.equals(int.class)) {
			colType = "integer";
			
		} else if (dataType.equals(Long.class) || dataType.equals(long.class)) {
			colType = "bigint";
			
		} else if (dataType.equals(Double.class) || dataType.equals(double.class)) {
			colType = "double";
			
		} else if (dataType.equals(Float.class) || dataType.equals(float.class)) {
			colType = "float";
			
		} else if (dataType.equals(BigDecimal.class)) {
			colType = "decimal(" + precision + "," + scale + ")";

		} else if (dataType.equals(java.util.Date.class)) {
			colType = "datetime";
			
		} else {
			colType = "varchar(" + length + ")";
		}
		return colType;
	}

	public Object convertValue(Object value, String columnTypeName) {
		return value;
	}

	@Override
	public String getDeleteSql(Query query, Object row) {
		
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
		
		String table = query.getTable();  
		if (table == null) {
			table = pojoInfo.getTable();
			if (table == null) {
				throw new ImanitySqlException("You must specify a table name");
			}
		}
		
		String primaryKeyName = pojoInfo.getPrimaryKeyName();
		
		return "delete from " + table + " where " + primaryKeyName + "=?";
	}


	@Override
	public Object[] getDeleteArgs(Query query, Object row) {
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
		Object primaryKeyValue = pojoInfo.getValue(row, pojoInfo.getPrimaryKeyName());
		Object [] args = new Object[1];
		args[0] = primaryKeyValue;
		return args;
	}


	@Override
	public String getUpsertSql(Query query, Object row) {
		String msg =
				"There's no standard upsert implemention. There is one in the MySql driver, though,"
				+ "so if you're using MySql, call Database.setSqlMaker(new MySqlMaker()); Or roll your own.";
		throw new UnsupportedOperationException(msg);
	}


	@Override
	public Object[] getUpsertArgs(Query query, Object row) {
		throw new UnsupportedOperationException();
	}



}
