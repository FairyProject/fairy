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


import io.fairyproject.mysql.pojo.Query;
import io.fairyproject.mysql.pojo.info.StandardPojoInfo;

public class MySqlStatementBuilder extends StandardSqlStatementBuilder {

	@Override
	public String getUpsertSql(Query query, Object row) {
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
		return pojoInfo.getUpsertSql();
	}

	@Override
	public Object[] getUpsertArgs(Query query, Object row) {
		
		// same args as insert, but we need to duplicate the values
		Object [] args = super.getInsertArgs(query, row);
		
		int count = args.length;
		
		Object [] upsertArgs = new Object[count * 2];
		System.arraycopy(args, 0, upsertArgs, 0, count);
		System.arraycopy(args, 0, upsertArgs, count, count);
		
		return upsertArgs;
	}
	

	@Override
	public void makeUpsertSql(StandardPojoInfo pojoInfo) {

		// INSERT INTO table (a,b,c) VALUES (1,2,3) ON DUPLICATE KEY UPDATE c=c+1;
		
		// mostly the same as the makeInsertSql code
		// it uses the same column names and argcount

		StringBuilder buf = new StringBuilder();
		buf.append(pojoInfo.getInsertSql());
		buf.append(" on duplicate key update ");
		
		boolean first = true;
		for (String colName: pojoInfo.getInsertColumnNames()) {
			if (first) {
				first = false;
			} else {
				buf.append(',');
			}
			buf.append(colName);
			buf.append("=?");
		}
		
		pojoInfo.setUpsertSql(buf.toString());
	}

	@Override
	protected String getColType(Class<?> dataType, int length, int precision, int scale) {
		String colType;

		if (dataType.equals(Boolean.class) || dataType.equals(boolean.class)) {
			colType = "tinyint";
		} else {
			colType = super.getColType(dataType, length, precision, scale);
		}
		return colType;
	}

	@Override
	public Object convertValue(Object value, String columnTypeName) {
		Object retVal;
		if ("TINYINT".equalsIgnoreCase(columnTypeName)) {
			if (value instanceof Byte) {
				retVal = (byte) value == 1;
			} else {
				retVal = (int) value == 1;
			}
		} else {
			retVal = value;
		}

		return retVal;
	}

}
