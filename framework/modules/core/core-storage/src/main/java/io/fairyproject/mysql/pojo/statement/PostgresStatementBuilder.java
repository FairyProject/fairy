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

import io.fairyproject.mysql.pojo.Property;
import io.fairyproject.mysql.pojo.info.StandardPojoInfo;

import javax.persistence.Column;

public class PostgresStatementBuilder extends StandardSqlStatementBuilder {

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
				if (prop.isGenerated()) {
					buf.append(" serial");
				} else {
					buf.append(getColType(prop.getDataType(), 255, 10, 2));
				}
				
			} else {
				if (columnAnnot.columnDefinition() == null) {
					
					// let the column def override everything
					buf.append(columnAnnot.columnDefinition());
					
				} else {

					buf.append(prop.getName());
					buf.append(" ");
					if (prop.isGenerated()) {
						buf.append(" serial");
					} else {
						buf.append(getColType(prop.getDataType(), columnAnnot.length(), columnAnnot.precision(), columnAnnot.scale()));
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


}
