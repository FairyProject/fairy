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

package org.fairy.bukkit.reflection.wrapper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodWrapper<R> extends WrapperAbstract {

	private final Method method;

	public MethodWrapper(Method method) {
		this.method = method;
	}

	@Override
	public boolean exists() {
		return this.method != null;
	}

	public String getName() {
		return this.method.getName();
	}

	public R invoke(Object object, Object... args) {
		try {
			return (R) this.method.invoke(object, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public R invokeSilent(Object object, Object... args) {
		try {
			return (R) this.method.invoke(object, args);
		} catch (Exception e) {
		}
		return null;
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) { return true; }
		if (object == null || getClass() != object.getClass()) { return false; }

		MethodWrapper<?> that = (MethodWrapper<?>) object;

		return method != null ? method.equals(that.method) : that.method == null;

	}

	@Override
	public int hashCode() {
		return method != null ? method.hashCode() : 0;
	}

	/**
	 * Generates a method's signature.
	 *
	 * @param method         the method to get the signature for
	 * @param fullClassNames whether to use the full class name
	 * @return the method's signature
	 */
	public static String getMethodSignature(Method method, boolean fullClassNames) {
		//		StringBuilder stringBuilder = new StringBuilder();
		//
		//		Class<?> returnType = method.getReturnType();
		//		if (returnType.isPrimitive()) {
		//			stringBuilder.append(returnType);
		//		} else {
		//			stringBuilder.append(fullClassNames ? returnType.getName() : returnType.getSimpleName());
		//		}
		//		stringBuilder.append(" ");
		//		stringBuilder.append(method.getName());
		//
		//		stringBuilder.append("(");
		//
		//		boolean first = true;
		//		for (Class clazz : method.getParameterTypes()) {
		//			if (!first) { stringBuilder.append(","); }
		//			stringBuilder.append(fullClassNames ? clazz.getName() : clazz.getSimpleName());
		//			first = false;
		//		}
		//		return stringBuilder.append(")").toString();

		return MethodSignature.of(method, fullClassNames).getSignature();
	}

	/**
	 * @param method Method to get the signature for
	 * @return the signature
	 * @see #getMethodSignature(Method, boolean)
	 */
	public static String getMethodSignature(Method method) {
		return getMethodSignature(method, false);
	}

	public static class MethodSignature {
		static final Pattern SIGNATURE_STRING_PATTERN = Pattern.compile("(.+) (.*)\\((.*)\\)");

		private final String   returnType;
		private final Pattern  returnTypePattern;
		private final String   name;
		private final Pattern  namePattern;
		private final String[] parameterTypes;
		private final String   signature;

		public MethodSignature(String returnType, String name, String[] parameterTypes) {
			this.returnType = returnType;
			this.returnTypePattern = Pattern.compile(returnType
					.replace("?", "\\w")
					.replace("*", "\\w*")
					.replace("[", "\\[")
					.replace("]", "\\]"));
			this.name = name;
			this.namePattern = Pattern.compile(name.replace("?", "\\w").replace("*", "\\w*"));
			this.parameterTypes = parameterTypes;

			StringBuilder builder = new StringBuilder();
			builder.append(returnType).append(" ").append(name).append("(");
			boolean first = true;
			for (String parameterType : parameterTypes) {
				if (!first) {
					builder.append(",");
				}
				builder.append(parameterType);
				first = false;
			}
			this.signature = builder.append(")").toString();
		}

		public static MethodSignature of(Method method, boolean fullClassNames) {
			Class<?> returnType = method.getReturnType();
			Class<?>[] parameterTypes = method.getParameterTypes();

			String returnTypeString;
			if (returnType.isPrimitive()) {
				returnTypeString = returnType.toString();
			} else {
				returnTypeString = fullClassNames ? returnType.getName() : returnType.getSimpleName();
			}
			String methodName = method.getName();
			String[] parameterTypeStrings = new String[parameterTypes.length];
			for (int i = 0; i < parameterTypeStrings.length; i++) {
				if (parameterTypes[i].isPrimitive()) {
					parameterTypeStrings[i] = parameterTypes[i].toString();
				} else {
					parameterTypeStrings[i] = fullClassNames ? parameterTypes[i].getName() : parameterTypes[i].getSimpleName();
				}
			}

			return new MethodSignature(returnTypeString, methodName, parameterTypeStrings);
		}

		public static MethodSignature fromString(String signatureString) {
			if (signatureString == null) { return null; }
			Matcher matcher = SIGNATURE_STRING_PATTERN.matcher(signatureString);
			if (matcher.find()) {
				if (matcher.groupCount() != 3) {
					throw new IllegalArgumentException("invalid signature");
				}
				return new MethodSignature(matcher.group(1), matcher.group(2), matcher.group(3).split(","));
			} else {
				throw new IllegalArgumentException("invalid signature");
			}
		}

		public String getReturnType() {
			return returnType;
		}

		public boolean isReturnTypeWildcard() {
			return "?".equals(returnType) || "*".equals(returnType);
		}

		public String getName() {
			return name;
		}

		public boolean isNameWildcard() {
			return "?".equals(name) || "*".equals(name);
		}

		public String[] getParameterTypes() {
			return parameterTypes;
		}

		public String getParameterType(int index) throws IndexOutOfBoundsException {
			return parameterTypes[index];
		}

		public boolean isParameterWildcard(int index) throws IndexOutOfBoundsException {
			return "?".equals(getParameterType(index)) || "*".equals(getParameterType(index));
		}

		public String getSignature() {
			return signature;
		}

		/**
		 * Checks whether this signature matches another signature. Wildcards are checked in this signature, but not the other signature.
		 *
		 * @param other signature to check
		 * @return whether the signatures match
		 */
		public boolean matches(MethodSignature other) {
			if (other == null) { return false; }

			//			if (!returnType.equals(other.returnType)) {
			//				if (!isReturnTypeWildcard()) { return false; }
			//			}
			//			if (!name.equals(other.name)) {
			//				if (!isNameWildcard()) { return false; }
			//			}
			//			if (parameterTypes.length != other.parameterTypes.length) { return false; }
			//			for (int i = 0; i < parameterTypes.length; i++) {
			//				if (!getParameterType(i).equals(other.getParameterType(i))) {
			//					if (!isParameterWildcard(i)) { return false; }
			//				}
			//			}

			if (!returnTypePattern.matcher(other.returnType).matches()) {
				return false;
			}
			if (!namePattern.matcher(other.name).matches()) {
				return false;
			}
			if (parameterTypes.length != other.parameterTypes.length) { return false; }
			for (int i = 0; i < parameterTypes.length; i++) {
				if (!Pattern.compile(getParameterType(i).replace("?", "\\w").replace("*", "\\w*")).matcher(other.getParameterType(i)).matches()) {
					return false;
				}
			}

			return true;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) { return true; }
			if (o == null || getClass() != o.getClass()) { return false; }

			MethodSignature signature1 = (MethodSignature) o;

			if (!returnType.equals(signature1.returnType)) { return false; }
			if (!name.equals(signature1.name)) { return false; }
			// Probably incorrect - comparing Object[] arrays with Arrays.equals
			if (!Arrays.equals(parameterTypes, signature1.parameterTypes)) { return false; }
			return signature.equals(signature1.signature);

		}

		@Override
		public int hashCode() {
			int result = returnType.hashCode();
			result = 31 * result + name.hashCode();
			result = 31 * result + Arrays.hashCode(parameterTypes);
			result = 31 * result + signature.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return getSignature();
		}
	}

}
