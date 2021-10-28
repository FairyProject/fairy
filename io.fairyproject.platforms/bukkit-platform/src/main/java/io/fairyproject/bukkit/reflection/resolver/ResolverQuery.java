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

package io.fairyproject.bukkit.reflection.resolver;

import lombok.Getter;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Container class for resolver-queries Used by {@link MethodResolver}
 *
 * @see ResolverQuery.Builder
 */
@Getter
public class ResolverQuery {

    private String name;
    private int index = -1; // -1 == none, -2 == last
    private Class<?> returnType;
    private Class<?>[] types;

    private ModifierOptions modifierOptions;

	public ResolverQuery(Class<?> returnType, String name, int index, Class<?>... types) {
		this.returnType = returnType;
		this.index = index;
		this.name = name;
		this.types = types;
	}

	public ResolverQuery(Class<?> returnType, int index, Class<?>... types) {
		this.returnType = returnType;
		this.index = index;
		this.types = types;
	}

	public ResolverQuery(int index, Class<?>... types) {
		this.index = index;
		this.types = types;
	}

	public ResolverQuery(Class<?> returnType, int index) {
		this.returnType = returnType;
		this.index = index;
		this.types = new Class[0];
	}

	public ResolverQuery(Class<?> returnType, String name, Class<?>... types) {
		this.returnType = returnType;
		this.name = name;
		this.types = types;
	}

    public ResolverQuery(String name, Class<?>... types) {
        this.name = name;
        this.types = types;
    }

    public ResolverQuery(String name) {
        this.name = name;
        this.types = new Class[0];
    }

    public ResolverQuery(Class<?>... types) {
        this.types = types;
    }

    public ResolverQuery withModifierOptions(ModifierOptions modifierOptions) {
		this.modifierOptions = modifierOptions;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ResolverQuery that = (ResolverQuery) o;

		if (index != that.index) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (returnType != null ? !returnType.equals(that.returnType) : that.returnType != null) return false;
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(types, that.types)) return false;
		return modifierOptions != null ? modifierOptions.equals(that.modifierOptions) : that.modifierOptions == null;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + index;
		result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
		result = 31 * result + Arrays.hashCode(types);
		result = 31 * result + (modifierOptions != null ? modifierOptions.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", ResolverQuery.class.getSimpleName() + "[", "]")
				.add("name='" + name + "'")
				.add("index=" + index)
				.add("returnType=" + returnType)
				.add("types=" + Arrays.toString(types))
				.add("modifierOptions=" + modifierOptions)
				.toString();
	}

	public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for {@link ResolverQuery} Access using {@link ResolverQuery#builder()}
     */
    public static class Builder {

        private final List<ResolverQuery> queryList = new ArrayList<ResolverQuery>();

        private Builder() {
        }

		public Builder with(Class<?> returnType, String name, int index, Class<?>[] types) {
			queryList.add(new ResolverQuery(returnType, name, index, types));
			return this;
		}

		public Builder with(Class<?> returnType, int index, Class<?>[] types) {
			queryList.add(new ResolverQuery(returnType, index, types));
			return this;
		}

		public Builder with(int index, Class<?>[] types) {
			queryList.add(new ResolverQuery(index, types));
			return this;
		}

		public Builder with(Class<?> returnType, int index) {
			queryList.add(new ResolverQuery(returnType, index));
			return this;
		}

        public Builder with(Class<?> returnType, String name, Class<?>[] types) {
        	queryList.add(new ResolverQuery(returnType, name, types));
        	return this;
		}

        public Builder with(String name, Class<?>[] types) {
            queryList.add(new ResolverQuery(name, types));
            return this;
        }

        public Builder with(String name) {
            queryList.add(new ResolverQuery(name));
            return this;
        }

        public Builder with(Class<?>[] types) {
            queryList.add(new ResolverQuery(types));
            return this;
        }

        public ResolverQuery[] build() {
            return queryList.toArray(new ResolverQuery[queryList.size()]);
        }

    }

    @Getter
    @lombok.Builder
    public static class ModifierOptions {

    	private boolean onlyDynamic;
    	private boolean onlyStatic;
    	private boolean onlyNonFinal;
    	private boolean onlyFinal;

    	public boolean matches(int modifier) {
    		if (onlyDynamic) {
    			if (Modifier.isStatic(modifier)) {
    				return false;
				}
			} else if (onlyStatic) {
				if (!Modifier.isStatic(modifier)) {
					return false;
				}
			}

			if (onlyNonFinal) {
				return !Modifier.isFinal(modifier);
			} else if (onlyFinal) {
				return Modifier.isFinal(modifier);
			}

			return true;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ModifierOptions that = (ModifierOptions) o;

			if (onlyDynamic != that.onlyDynamic) return false;
			if (onlyStatic != that.onlyStatic) return false;
			if (onlyNonFinal != that.onlyNonFinal) return false;
			return onlyFinal == that.onlyFinal;
		}

		@Override
		public int hashCode() {
			int result = (onlyDynamic ? 1 : 0);
			result = 31 * result + (onlyStatic ? 1 : 0);
			result = 31 * result + (onlyNonFinal ? 1 : 0);
			result = 31 * result + (onlyFinal ? 1 : 0);
			return result;
		}
	}
}
