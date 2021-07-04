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

package org.fairy.bukkit.reflection.minecraft;

import org.fairy.bukkit.reflection.MinecraftReflection;
import org.fairy.bukkit.reflection.resolver.ConstructorResolver;
import org.fairy.bukkit.reflection.resolver.FieldResolver;
import org.fairy.bukkit.reflection.resolver.MethodResolver;
import org.fairy.bukkit.reflection.resolver.ResolverQuery;
import org.fairy.bukkit.reflection.resolver.minecraft.NMSClassResolver;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

public class DataWatcher {

	static NMSClassResolver nmsClassResolver = new NMSClassResolver();

	public static Class<?> TYPE = nmsClassResolver.resolveSilent("DataWatcher");

	static Class<?> ItemStack        = nmsClassResolver.resolveSilent("ItemStack");
	static Class<?> ChunkCoordinates = nmsClassResolver.resolveSilent("ChunkCoordinates");
	static Class<?> BlockPosition    = nmsClassResolver.resolveSilent("BlockPosition");
	static Class<?> Vector3f         = nmsClassResolver.resolveSilent("Vector3f");
	static Class<?> Entity           = nmsClassResolver.resolveSilent("Entity");

	static ConstructorResolver DataWacherConstructorResolver = new ConstructorResolver(TYPE);

	static FieldResolver DataWatcherFieldResolver = new FieldResolver(TYPE);

	static MethodResolver DataWatcherMethodResolver   = new MethodResolver(TYPE);

	public static Object newDataWatcher(Object entity) throws ReflectiveOperationException {
		return DataWacherConstructorResolver.resolve(new Class[] { Entity }).newInstance(entity);
	}

	public static Object setValue(Object dataWatcher, int index, Object dataWatcherObject/*1.9*/, Object value) throws ReflectiveOperationException {
		if (MinecraftVersion.VERSION.olderThan(MinecraftReflection.Version.v1_9_R1)) {
			return V1_8.setValue(dataWatcher, index, value);
		} else {
			return V1_9.setValue(dataWatcher, dataWatcherObject, value);
		}
	}

	public static Object setValue(Object dataWatcher, int index, V1_9.ValueType type, Object value) throws ReflectiveOperationException {
		return setValue(dataWatcher, index, type.getType(), value);
	}

	public static Object setValue(Object dataWatcher, int index, Object value, FieldResolver dataWatcherObjectFieldResolver/*1.9*/, String... dataWatcherObjectFieldNames/*1.9*/) throws ReflectiveOperationException {
		if (MinecraftVersion.VERSION.olderThan(MinecraftReflection.Version.v1_9_R1)) {
			return V1_8.setValue(dataWatcher, index, value);
		} else {
			Object dataWatcherObject = dataWatcherObjectFieldResolver.resolve(dataWatcherObjectFieldNames).get(null/*Should be a static field*/);
			return V1_9.setValue(dataWatcher, dataWatcherObject, value);
		}
	}

	@Deprecated
	public static Object getValue(DataWatcher dataWatcher, int index) throws ReflectiveOperationException {
		if (MinecraftVersion.VERSION.olderThan(MinecraftReflection.Version.v1_9_R1)) {
			return V1_8.getValue(dataWatcher, index);
		} else {
			return V1_9.getValue(dataWatcher, index);
		}
	}

	public static Object getValue(Object dataWatcher, int index, V1_9.ValueType type) throws ReflectiveOperationException {
		return getValue(dataWatcher, index, type.getType());
	}

	public static Object getValue(Object dataWatcher, int index, Object dataWatcherObject/*1.9*/) throws ReflectiveOperationException {
		if (MinecraftReflection.VERSION.olderThan(MinecraftReflection.Version.v1_9_R1)) {
			return V1_8.getWatchableObjectValue(V1_8.getValue(dataWatcher, index));
		} else {
			return V1_9.getValue(dataWatcher, dataWatcherObject);
		}
	}

	//TODO: update type-ids to 1.9
	public static int getValueType(Object value) {
		int type = 0;
		if (value instanceof Number) {
			if (value instanceof Byte) {
				type = 0;
			} else if (value instanceof Short) {
				type = 1;
			} else if (value instanceof Integer) {
				type = 2;
			} else if (value instanceof Float) {
				type = 3;
			}
		} else if (value instanceof String) {
			type = 4;
		} else if (value != null && value.getClass().equals(ItemStack)) {
			type = 5;
		} else if (value != null && (value.getClass().equals(ChunkCoordinates) || value.getClass().equals(BlockPosition))) {
			type = 6;
		} else if (value != null && value.getClass().equals(Vector3f)) {
			type = 7;
		}

		return type;
	}

	/**
	 * Helper class for versions newer than 1.9
	 */
	public static class V1_9 {

		static Class<?> DataWatcherItem   = nmsClassResolver.resolveSilent("DataWatcher$Item");//>= 1.9 only
		static Class<?> DataWatcherObject = nmsClassResolver.resolveSilent("DataWatcherObject");//>= 1.9 only

		static ConstructorResolver DataWatcherItemConstructorResolver;//>=1.9 only

		static FieldResolver DataWatcherItemFieldResolver;//>=1.9 only
		static FieldResolver DataWatcherObjectFieldResolver;//>=1.9 only

		public static Object newDataWatcherItem(Object dataWatcherObject, Object value) throws ReflectiveOperationException {
			if (DataWatcherItemConstructorResolver == null) { DataWatcherItemConstructorResolver = new ConstructorResolver(DataWatcherItem); }
			return DataWatcherItemConstructorResolver.resolveFirstConstructor().newInstance(dataWatcherObject, value);
		}

		public static Object setItem(Object dataWatcher, int index, Object dataWatcherObject, Object value) throws ReflectiveOperationException {
			return setItem(dataWatcher, index, newDataWatcherItem(dataWatcherObject, value));
		}

		public static Object setItem(Object dataWatcher, int index, Object dataWatcherItem) throws ReflectiveOperationException {
			Map<Integer, Object> map = (Map<Integer, Object>) DataWatcherFieldResolver.resolveByLastTypeSilent(Map.class).get(dataWatcher);
			map.put(index, dataWatcherItem);
			return dataWatcher;
		}

		public static Object setValue(Object dataWatcher, Object dataWatcherObject, Object value) throws ReflectiveOperationException {
			DataWatcherMethodResolver.resolve("set").invoke(dataWatcher, dataWatcherObject, value);
			return dataWatcher;
		}

		//		public static Object getValue(Object dataWatcher, int index) throws ReflectiveOperationException {
		//			Map<Integer, Object> map = (Map<Integer, Object>) DataWatcherFieldResolver.resolve("c").get(dataWatcher);
		//			return map.get(index);
		//		}

		public static Object getItem(Object dataWatcher, Object dataWatcherObject) throws ReflectiveOperationException {
			return DataWatcherMethodResolver.resolve(new ResolverQuery("c", DataWatcherObject)).invoke(dataWatcher, dataWatcherObject);
		}

		public static Object getValue(Object dataWatcher, Object dataWatcherObject) throws ReflectiveOperationException {
			return DataWatcherMethodResolver.resolve("get").invoke(dataWatcher, dataWatcherObject);
		}

		public static Object getValue(Object dataWatcher, ValueType type) throws ReflectiveOperationException {
			return getValue(dataWatcher, type.getType());
		}

		public static Object getItemObject(Object item) throws ReflectiveOperationException {
			if (DataWatcherItemFieldResolver == null) { DataWatcherItemFieldResolver = new FieldResolver(DataWatcherItem); }
			return DataWatcherItemFieldResolver.resolve("a").get(item);
		}

		public static int getItemIndex(Object dataWatcher, Object item) throws ReflectiveOperationException {
			int index = -1;//Return -1 if the item is not in the DataWatcher
			Map<Integer, Object> map = (Map<Integer, Object>) DataWatcherFieldResolver.resolveByLastTypeSilent(Map.class).get(dataWatcher);
			for (Map.Entry<Integer, Object> entry : map.entrySet()) {
				if (entry.getValue().equals(item)) {
					index = entry.getKey();
					break;
				}
			}
			return index;
		}

		public static Type getItemType(Object item) throws ReflectiveOperationException {
			if (DataWatcherObjectFieldResolver == null) { DataWatcherObjectFieldResolver = new FieldResolver(DataWatcherObject); }
			Object object = getItemObject(item);
			Object serializer = DataWatcherObjectFieldResolver.resolve("b").get(object);
			Type[] genericInterfaces = serializer.getClass().getGenericInterfaces();
			if (genericInterfaces.length > 0) {
				Type type = genericInterfaces[0];
				if (type instanceof ParameterizedType) {
					Type[] actualTypes = ((ParameterizedType) type).getActualTypeArguments();
					if (actualTypes.length > 0) {
						return actualTypes[0];
					}
				}
			}
			return null;
		}

		public static Object getItemValue(Object item) throws ReflectiveOperationException {
			if (DataWatcherItemFieldResolver == null) { DataWatcherItemFieldResolver = new FieldResolver(DataWatcherItem); }
			return DataWatcherItemFieldResolver.resolve("b").get(item);
		}

		public static void setItemValue(Object item, Object value) throws ReflectiveOperationException {
			DataWatcherItemFieldResolver.resolve("b").set(item, value);
		}

		public enum ValueType {

			/**
			 * Byte
			 */
			ENTITY_FLAG("Entity", 57, 0 /*"ax", "ay"*/),
			/**
			 * Integer
			 */
			ENTITY_AIR_TICKS("Entity", 58, 1/*"ay", "az"*/),
			/**
			 * String
			 */
			ENTITY_NAME("Entity", 59, 2/*"az", "aA"*/),
			/**
			 * Byte &lt; 1.9 Boolean &gt; 1.9
			 */
			ENTITY_NAME_VISIBLE("Entity", 60, 3/*"aA", "aB"*/),
			/**
			 * Boolean
			 */
			ENTITY_SILENT("Entity", 61, 4/*"aB", "aC"*/),

			//////////

			//TODO: Add EntityLiving#as (Byte)
			ENTITY_as("EntityLiving", 2, 0/* "as", "at"*/),

			/**
			 * Float
			 */
			ENTITY_LIVING_HEALTH("EntityLiving", "HEALTH"),

			//TODO: Add EntityLiving#f (Integer) - Maybe active potions?
			ENTITY_LIVING_f("EntityLiving", 4, 2/*"f"*/),

			//TODO: Add EntityLiving#g (Boolean) - Maybe ambient potions?
			ENTITY_LIVING_g("EntityLiving", 5, 3/*"g"*/),

			//TODO: Add EntityLiving#h (Integer)
			ENTITY_LIVING_h("EntityLiving", 6, 4/*"h"*/),

			//////////

			/**
			 * Byte
			 */
			ENTITY_INSENTIENT_FLAG("EntityInsentient", 0, 0/* "a"*/),

			///////////

			/**
			 * Integer
			 */
			ENTITY_SLIME_SIZE("EntitySlime", 0, 0/* "bt", "bu"*/),

			/////////////

			//TODO: Add EntityWither#a (Integer)
			ENTITY_WITHER_a("EntityWither", 0, 0/*"a"*/),

			//TODO:  Add EntityWither#b (Integer)
			ENTITY_WIHER_b("EntityWither", 1, 1/*"b"*/),

			//TODO: Add EntityWither#c (Integer)
			ENTITY_WITHER_c("EntityWither", 2, 2/*"c"*/),

			//TODO: Add EntityWither#bv (Integer) - (DataWatcherObject<Integer>[] bv, seems to be an array of {a, b, c})
			ENTITY_WITHER_bv("EntityWither", 3, 3/*"bv", "bw"*/),

			//TODO: Add EntityWither#bw (Integer)
			ENTITY_WITHER_bw("EntityWither", 4, 4/*"bw", "bx"*/),

			//////////

			ENTITY_AGEABLE_CHILD("EntityAgeable", 0, 0),

			///////////

			ENTITY_HORSE_STATUS("EntityHorse", 3, 0),
			ENTITY_HORSE_HORSE_TYPE("EntityHorse", 4, 1),
			ENTITY_HORSE_HORSE_VARIANT("EntityHorse", 5, 2),
			ENTITY_HORSE_OWNER_UUID("EntityHorse", 6, 3),
			ENTITY_HORSE_HORSE_ARMOR("EntityHorse", 7, 4),

			/////////


			/**
			 * Float
			 */
			ENTITY_HUMAN_ABSORPTION_HEARTS("EntityHuman", 0, 0 /*"a"*/),

			/**
			 * Integer
			 */
			ENTITY_HUMAN_SCORE("EntityHuman", 1, 1 /*"b"*/),

			/**
			 * Byte
			 */
			ENTITY_HUMAN_SKIN_LAYERS("EntityHuman", 2, 2 /*"bp", "bq"*/),

			/**
			 * Byte (0 = left, 1 = right)
			 */
			ENTITY_HUMAN_MAIN_HAND("EntityHuman", 3, 3/*"bq", "br"*/);

			private Object type;

			ValueType(String className, String... fieldNames) {
				try {
					this.type = new FieldResolver(nmsClassResolver.resolve(className)).resolve(fieldNames).get(null);
				} catch (Exception e) {
					if (MinecraftReflection.VERSION.newerThan(MinecraftReflection.Version.v1_9_R1)) {
						System.err.println("[Imanity] Failed to find DataWatcherObject for " + className + " " + Arrays.toString(fieldNames));
					}
				}
			}

			ValueType(String className, int index) {
				try {
					this.type = new FieldResolver(nmsClassResolver.resolve(className)).resolveIndex(index).get(null);
				} catch (Exception e) {
					if (MinecraftReflection.VERSION.newerThan(MinecraftReflection.Version.v1_9_R1)) {
						System.err.println("[Imanity] Failed to find DataWatcherObject for " + className + " #" + index);
					}
				}
			}

			ValueType(String className, int index, int offset) {
				int firstObject = 0;
				try {
					Class<?> clazz = nmsClassResolver.resolve(className);
					for (Field field : clazz.getDeclaredFields()) {
						if ("DataWatcherObject".equals(field.getType().getSimpleName())) {
							break;
						}
						firstObject++;
					}
					this.type = new FieldResolver(clazz).resolveIndex(firstObject + offset).get(null);
				} catch (Exception e) {
					if (MinecraftReflection.VERSION.newerThan(MinecraftReflection.Version.v1_9_R1)) {
						System.err.println("[Imanity] Failed to find DataWatcherObject for " + className + " #" + index + " (" + firstObject + "+" + offset + ")");
					}
				}
			}

			public boolean hasType() {
				return getType() != null;
			}

			public Object getType() {
				return type;
			}
		}
	}

	/**
	 * Helper class for versions older than 1.8
	 */
	public static class V1_8 {

		static Class<?> WatchableObject = nmsClassResolver.resolveSilent("WatchableObject", "DataWatcher$WatchableObject");//<=1.8 only

		static ConstructorResolver WatchableObjectConstructorResolver;//<=1.8 only

		static FieldResolver WatchableObjectFieldResolver;//<=1.8 only

		public static Object newWatchableObject(int index, Object value) throws ReflectiveOperationException {
			return newWatchableObject(getValueType(value), index, value);
		}

		public static Object newWatchableObject(int type, int index, Object value) throws ReflectiveOperationException {
			if (WatchableObjectConstructorResolver == null) { WatchableObjectConstructorResolver = new ConstructorResolver(WatchableObject); }
			return WatchableObjectConstructorResolver.resolve(new Class[] {
					int.class,
					int.class,
					Object.class }).newInstance(type, index, value);
		}

		public static Object setValue(Object dataWatcher, int index, Object value) throws ReflectiveOperationException {
			int type = getValueType(value);

			Map map = (Map) DataWatcherFieldResolver.resolveByLastType(Map.class).get(dataWatcher);
			map.put(index, newWatchableObject(type, index, value));

			return dataWatcher;
		}

		public static Object getValue(Object dataWatcher, int index) throws ReflectiveOperationException {
			Map map = (Map) DataWatcherFieldResolver.resolveByLastType(Map.class).get(dataWatcher);

			return map.get(index);
		}

		public static int getWatchableObjectIndex(Object object) throws ReflectiveOperationException {
			if (WatchableObjectFieldResolver == null) { WatchableObjectFieldResolver = new FieldResolver(WatchableObject); }
			return WatchableObjectFieldResolver.resolve("b").getInt(object);
		}

		public static int getWatchableObjectType(Object object) throws ReflectiveOperationException {
			if (WatchableObjectFieldResolver == null) { WatchableObjectFieldResolver = new FieldResolver(WatchableObject); }
			return WatchableObjectFieldResolver.resolve("a").getInt(object);
		}

		public static Object getWatchableObjectValue(Object object) throws ReflectiveOperationException {
			if (WatchableObjectFieldResolver == null) { WatchableObjectFieldResolver = new FieldResolver(WatchableObject); }
			return WatchableObjectFieldResolver.resolve("c").get(object);
		}

		public static void setWatchableObjectValue(Object object, Object objectValue) throws ReflectiveOperationException {
			if (WatchableObjectFieldResolver == null) { WatchableObjectFieldResolver = new FieldResolver(WatchableObject); }
			WatchableObjectFieldResolver.resolve("c").set(object, objectValue);
		}

	}

	private DataWatcher() {
	}
}
