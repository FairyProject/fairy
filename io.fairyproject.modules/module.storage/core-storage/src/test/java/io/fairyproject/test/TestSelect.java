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

package io.fairyproject.test;

import io.fairyproject.mysql.connection.file.H2ConnectionFactory;
import org.junit.jupiter.api.Test;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

public class TestSelect {
	
	@Test
	public void test() {
		
		H2ConnectionFactory db = new H2ConnectionFactory(new File("./h2test").toPath().toAbsolutePath(), true);
		
		db.query().sql("drop table if exists selecttest").execute();
		
		db.createTable(Row.class);
		
		Row row = new Row();
		row.id = 99;
		row.name = "bob";
		db.insert(row);
		
		// primitive
		Long myId = db.query().sql("select id from selecttest").first(Long.class);
		if (myId != 99) {
			fail();
		}
		
		// map
		Map myMap = db.table("selecttest").first(LinkedHashMap.class);
		String str = myMap.toString();
		if (!str.equalsIgnoreCase("{id=99, name=bob}")) {
			System.out.println(str);
			fail();
		}
		
		// pojo
		Row myRow = db.first(Row.class);
		String myRowStr = myRow.toString();
		if (!myRowStr.equals("99bob")) {
			System.out.println(myRowStr);
			fail();
		}
		
	}
	
	@Table(name="selecttest")
	public static class Row {
		@Column(unique=true)
		public long id;
		public String name; 
		public String toString() {
			return id + name;
		}
	}

}
