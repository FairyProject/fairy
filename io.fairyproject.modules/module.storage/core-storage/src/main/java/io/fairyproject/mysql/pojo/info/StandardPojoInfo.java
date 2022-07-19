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

package io.fairyproject.mysql.pojo.info;

import io.fairyproject.ObjectSerializer;
import io.fairyproject.container.Autowired;
import io.fairyproject.container.SerializerFactory;
import io.fairyproject.mysql.ColumnOrder;
import io.fairyproject.mysql.ImanitySqlException;
import io.fairyproject.mysql.pojo.CustomSerialize;
import io.fairyproject.mysql.pojo.Property;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.util.ConditionUtils;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.beans.IntrospectionException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
@Getter
@Setter
public class StandardPojoInfo implements PojoInfo {

	@Autowired
	private static SerializerFactory SERIALIZER_FACTORY;

	private Map<String, Property> propertyMap = new LinkedHashMap<>();
	private String table;

	private String primaryKeyName;
	private String generatedColumnName;

	private String insertSql;
	private int insertSqlArgCount;
	private String[] insertColumnNames;

	private String upsertSql;
	private int upsertSqlArgCount;
	private String[] upsertColumnNames;

	private String updateSql;
	private String[] updateColumnNames;
	private int updateSqlArgCount;

	private String selectColumns;

	public StandardPojoInfo(Class<?> type) {
		try {

			if (Map.class.isAssignableFrom(type)) {
				// leave properties empty
			} else {
				List<Property> properties = populateProperties(type);

				ColumnOrder columnOrder = type.getAnnotation(ColumnOrder.class);
				if (columnOrder != null) {
					String[] columns = columnOrder.value();
					List<Property> reordered = new ArrayList<>();
					for (int i = 0; i < columns.length; i++) {
						for (Property property : properties) {
							if (property.getName().equals(columns[i])) {
								reordered.add(property);
								break;
							}
						}
					}
					properties = reordered;
				}

				for (Property property : properties) {
					if (this.getPropertyMap().containsKey(property.getName().toUpperCase())) {
						throw new IllegalArgumentException("The field property " + property.getName() + " already exists! (text case different?)");
					}
					this.getPropertyMap().put(property.getName().toUpperCase(), property);
				}

//				ConditionUtils.notNull(this.primaryKeyName, String.format("Primary key for %s cannot be found! (Any field annotate with @Id?)", type));
			}

			Table table = type.getAnnotation(Table.class);
			if (table != null) {
				if (!table.schema().isEmpty()) {
					this.setTable(table.schema() + "." + table.name());
				} else {
					this.setTable(table.name());
				}
			} else {
				this.setTable(type.getSimpleName());
			}

		} catch (Throwable t) {
			throw new ImanitySqlException(t);
		}
	}

	private List<Property> populateProperties(Class<?> clazz)
			throws ReflectiveOperationException {
		List<Property> properties = new ArrayList<>();

		for (Field field : clazz.getDeclaredFields()) {
			int modifiers = field.getModifiers();

			if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers)) {
				continue;
			}

			if (field.getAnnotation(Transient.class) != null) {
				continue;
			}

			AccessUtil.setAccessible(field);

			Property property = new Property();
			property.setName(field.getName());
			property.setField(field);

			CustomSerialize deserializerAnnotation = field.getAnnotation(CustomSerialize.class);
			if (deserializerAnnotation != null) {
				property.setSerializer(deserializerAnnotation.value().newInstance());
			}

			if (SERIALIZER_FACTORY != null) {

				ObjectSerializer<?, ?> serializer = property.getSerializer();
				if (serializer == null) {
					serializer = SERIALIZER_FACTORY.findSerializer(field.getType());
				}

				if (serializer != null) {
					property.setDataType(serializer.outputClass());
					property.setSerializer(serializer);
				} else {
					property.setDataType(field.getType());
				}
			} else {
				property.setDataType(field.getType());
			}

			applyAnnotations(property, field);

			properties.add(property);
		}

		return properties;
	}

	/**
	 * Apply the annotations on the field or getter method to the property.
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private void applyAnnotations(Property property, AnnotatedElement annotatedElement)
			throws InstantiationException, IllegalAccessException {
		Column column = annotatedElement.getAnnotation(Column.class);
		if (column != null) {
			String name = column.name().trim();
			if (name.length() > 0) {
				property.setName(name);
			}
			property.setColumnAnnotation(column);
		}

		if (annotatedElement.getAnnotation(Id.class) != null) {
			property.setPrimaryKey(true);
			setPrimaryKeyName(property.getName());
		}

		if (annotatedElement.getAnnotation(GeneratedValue.class) != null) {
			setGeneratedColumnName(property.getName());
			property.setGenerated(true);
		}

		if (property.getDataType().isEnum()) {
			property.setEnumField(true);
			property.setEnumClass((Class<Enum>) property.getDataType());
			/*
			 * We default to STRING enum type. Can be overriden with @Enumerated annotation
			 */
			property.setEnumType(EnumType.STRING);
			if (annotatedElement.getAnnotation(Enumerated.class) != null) {
				property.setEnumType(annotatedElement.getAnnotation(Enumerated.class).value());
			}
		}

		CustomSerialize deserializerAnnotation = annotatedElement.getAnnotation(CustomSerialize.class);
		if (deserializerAnnotation != null) {
			property.setSerializer(deserializerAnnotation.value().newInstance());
		} else if (SERIALIZER_FACTORY != null) {
			ObjectSerializer serializer = SERIALIZER_FACTORY.findSerializer(property.getDataType());
			if (serializer != null) {
				property.setSerializer(serializer);
			}
		}

	}

	public Object toReadableValue(Property prop, Object value) {
		if (prop.getSerializer() != null) {
			value = prop.getSerializer().serialize(value);

		} else if (prop.isEnumField()) {
			// handle enums according to selected enum type
			if (prop.getEnumType() == EnumType.ORDINAL) {
				value = ((Enum) value).ordinal();
			}
			// EnumType.STRING and others (if present in the future)
			else {
				value = value.toString();
			}
		}

		return value;
	}

	public Object getValue(Object pojo, String name) {

		try {

			Property prop = getPropertyMap().get(name.toUpperCase());
			if (prop == null) {
				throw new ImanitySqlException("No such field: " + name);
			}

			Object value = null;

			if (prop.getReadMethod() != null) {
				value = prop.getReadMethod().invoke(pojo);

			} else if (prop.getField() != null) {
				value = prop.getField().get(pojo);
			}

			if (value != null) {
				value = this.toReadableValue(prop, value);
			}

			return value;

		} catch (Throwable t) {
			throw new ImanitySqlException(t);
		}
	}

	public void putValue(Object pojo, String name, Object value) {
		putValue(pojo, name, value, false);
	}

	public void putValue(Object pojo, String name, Object value, boolean ignoreIfMissing) {

		Property prop = getPropertyMap().get(name.toUpperCase());
		if (prop == null) {
			if (ignoreIfMissing) {
				return;
			}
			throw new ImanitySqlException("No such field: " + name);
		}

		if (value != null) {
			if (prop.getSerializer() != null) {
				value = prop.getSerializer().deserialize(value);
				
			} else if (prop.isEnumField()) {
				value = getEnumConst(prop.getEnumClass(), prop.getEnumType(), value);
			}
		}

		if (prop.getWriteMethod() != null) {
			try {
				prop.getWriteMethod().invoke(pojo, value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new ImanitySqlException("Could not write value into pojo. Property: " + prop.getName() + " method: "
						+ prop.getWriteMethod().toString() + " value: " + value + " value class: "
						+ value.getClass().toString(), e);
			}
			return;
		}

		if (prop.getField() != null) {
			try {
				prop.getField().set(pojo, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new ImanitySqlException(
						"Could not set value into pojo. Field: " + prop.getField().toString() + " value: " + value, e);
			}
			return;
		}

	}

	/**
	 * Convert a string to an enum const of the appropriate class.
	 */
	private <T extends Enum<T>> Object getEnumConst(Class<T> enumType, EnumType type, Object value) {
		String str = value.toString();
		if (type == EnumType.ORDINAL) {
			Integer ordinalValue = (Integer) value;
			if (ordinalValue < 0 || ordinalValue >= enumType.getEnumConstants().length) {
				throw new ImanitySqlException(
						"Invalid ordinal number " + ordinalValue + " for enum class " + enumType.getCanonicalName());
			}
			return enumType.getEnumConstants()[ordinalValue];
		} else {
			for (T e : enumType.getEnumConstants()) {
				if (str.equals(e.toString())) {
					return e;
				}
			}
			throw new ImanitySqlException("Enum value does not exist. value:" + str);
		}
	}

	@Override
	public Property getGeneratedColumnProperty() {
		if (this.getGeneratedColumnName() == null) {
			return null;
		}
		return getPropertyMap().get(this.getGeneratedColumnName().toUpperCase());
	}

	@Override
	public Property getProperty(String name) {

		if (getPropertyMap().containsKey(name.toUpperCase())) {
			return getPropertyMap().get(name.toUpperCase());
		}

		throw new IllegalArgumentException("Couldn't find property by " + name + "!");
	}

	public void setPrimaryKeyName(String name) {
		this.primaryKeyName = name;
	}
}
