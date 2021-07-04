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

package org.fairy.reflect;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 *
 * @see Builder
 */
@Getter
public class ReflectQuery {

    private String name;
    private int index = -1; // -1 == none, -2 == last
    private Class<?> returnType;
    private Class<?>[] types;

    private int modifier = -1;

	public ReflectQuery(Class<?> returnType, String name, int index, Class<?>... types) {
		this.returnType = returnType;
		this.index = index;
		this.name = name;
		this.types = types;
	}

	public ReflectQuery(Class<?> returnType, int index, Class<?>... types) {
		this.returnType = returnType;
		this.index = index;
		this.types = types;
	}

	public ReflectQuery(int index, Class<?>... types) {
		this.index = index;
		this.types = types;
	}

	public ReflectQuery(Class<?> returnType, int index) {
		this.returnType = returnType;
		this.index = index;
		this.types = new Class[0];
	}

	public ReflectQuery(Class<?> returnType, String name, Class<?>... types) {
		this.returnType = returnType;
		this.name = name;
		this.types = types;
	}

    public ReflectQuery(String name, Class<?>... types) {
        this.name = name;
        this.types = types;
    }

    public ReflectQuery(String name) {
        this.name = name;
        this.types = new Class[0];
    }

    public ReflectQuery(Class<?>... types) {
        this.types = types;
    }

    public ReflectQuery modifier(int modifier) {
		this.modifier |= modifier;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ReflectQuery that = (ReflectQuery) o;

		if (index != that.index) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (returnType != null ? !returnType.equals(that.returnType) : that.returnType != null) return false;
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(types, that.types)) return false;
		return modifier == that.getModifier();
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + index;
		result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
		result = 31 * result + Arrays.hashCode(types);
		result = 31 * result + (modifier);
		return result;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", ReflectQuery.class.getSimpleName() + "[", "]")
				.add("name='" + name + "'")
				.add("index=" + index)
				.add("returnType=" + returnType)
				.add("types=" + Arrays.toString(types))
				.add("modifier=" + modifier)
				.toString();
	}

	public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for {@link ReflectQuery} Access using {@link ReflectQuery#builder()}
     */
    public static class Builder {

        private final List<ReflectQuery> queryList = new ArrayList<ReflectQuery>();

        private Builder() {
        }

		public Builder with(Class<?> returnType, String name, int index, Class<?>[] types) {
			queryList.add(new ReflectQuery(returnType, name, index, types));
			return this;
		}

		public Builder with(Class<?> returnType, int index, Class<?>[] types) {
			queryList.add(new ReflectQuery(returnType, index, types));
			return this;
		}

		public Builder with(int index, Class<?>[] types) {
			queryList.add(new ReflectQuery(index, types));
			return this;
		}

		public Builder with(Class<?> returnType, int index) {
			queryList.add(new ReflectQuery(returnType, index));
			return this;
		}

        public Builder with(Class<?> returnType, String name, Class<?>[] types) {
        	queryList.add(new ReflectQuery(returnType, name, types));
        	return this;
		}

        public Builder with(String name, Class<?>[] types) {
            queryList.add(new ReflectQuery(name, types));
            return this;
        }

        public Builder with(String name) {
            queryList.add(new ReflectQuery(name));
            return this;
        }

        public Builder with(Class<?>[] types) {
            queryList.add(new ReflectQuery(types));
            return this;
        }

        public ReflectQuery[] build() {
            return queryList.toArray(new ReflectQuery[0]);
        }

    }
}
