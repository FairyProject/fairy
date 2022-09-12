//package io.fairyproject.bukkit.mc.entity;
//
//import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
//import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
//import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
//import io.fairyproject.bukkit.reflection.MinecraftReflection;
//import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
//import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
//import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
//import io.fairyproject.bukkit.reflection.wrapper.FieldWrapper;
//import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;
//import io.fairyproject.mc.MCAdventure;
//import io.fairyproject.mc.MCServer;
//import lombok.SneakyThrows;
//import lombok.experimental.UtilityClass;
//import org.bukkit.entity.Entity;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.function.Function;
//
//@UtilityClass
//public class BukkitEntityData {
//
//    private static MethodWrapper<?> SERIALIZE_COMPONENT = null;
//    private FieldWrapper<?> DATA_WATCHER_FIELD;
//    private FieldWrapper<?> MAP_FIELD;
//    private FieldWrapper<?> VALUE_FIELD;
//    private Function<Object, Object> SERIALIZER_METHOD;
//    private Function<Object, EntityDataType<?>> UNWARP_SERIALIZER;
//
//    @SuppressWarnings("unchecked")
//    public List<EntityData> unwarp(@NotNull Entity entity) throws ReflectiveOperationException {
//        if (DATA_WATCHER_FIELD == null) {
//            Class<?> entityClass = getEntityClass();
//            Class<?> dataWatcherClass = getDataWatcherClass();
//            Class<?> dataWatcherObjectClass = getDataWatcherObjectClass();
//            int valueIndex = 1;
//            if (dataWatcherObjectClass == null) {
//                dataWatcherObjectClass = dataWatcherClass.getDeclaredClasses()[0];
//                valueIndex = 2;
//            }
//            Class<?> dataWatcherSerializerClass = getDataWatcherSerializerClass();
//
//            Field field = new FieldResolver(entityClass).resolveByFirstType(dataWatcherClass);
//            field.setAccessible(true);
//            DATA_WATCHER_FIELD = new FieldWrapper<>(field);
//
//            MAP_FIELD = new FieldResolver(dataWatcherClass).resolveByFirstTypeDynamic(Map.class);
//            VALUE_FIELD = new FieldWrapper<>(new FieldResolver(dataWatcherObjectClass).resolveIndex(valueIndex));
//
//            if (dataWatcherSerializerClass != null) {
//                MethodWrapper<?> method = new MethodResolver(dataWatcherObjectClass).resolve(
//                        dataWatcherSerializerClass,
//                        0
//                );
//                SERIALIZER_METHOD = method::invoke;
//            } else {
//                FieldWrapper<Integer> fieldWrapper = new FieldResolver(dataWatcherObjectClass).resolve(int.class, 0);
//                SERIALIZER_METHOD = fieldWrapper::get;
//            }
//        }
//        Object handle = MinecraftReflection.getHandle(entity);
//        Object dataWatcher = DATA_WATCHER_FIELD.get(handle);
//
//        Map<Integer, Object> map = (Map<Integer, Object>) MAP_FIELD.get(dataWatcher);
//        List<EntityData> retVal = new ArrayList<>();
//        for (Map.Entry<Integer, Object> entry : map.entrySet()) {
//            int index = entry.getKey();
//            Object watchableObject = entry.getValue();
//
//            Object value = VALUE_FIELD.get(watchableObject);
//            Object serializer = SERIALIZER_METHOD.apply(watchableObject);
//            EntityDataType<?> dataType = unwarpDataType(serializer);
//
//            retVal.add(new EntityData(index, dataType, ));
//        }
//    }
//
//    @SneakyThrows
//    public static Object warpValue(Object value) {
//        if (value instanceof Optional<?>) {
//            return ((Optional<?>) value).map(BukkitEntityData::warpValue);
//        }
//
//        if (is(MinecraftReflection.getIChatBaseComponentClass(), value)) {
//            if (SERIALIZE_COMPONENT == null) {
//                Class<?> chatSerializerClass = getChatSerializerClass();
//
//                SERIALIZE_COMPONENT = new MethodResolver(chatSerializerClass)
//                        .resolve(String.class, 0, MinecraftReflection.getIChatBaseComponentClass());
//            }
//
//            String json = (String) SERIALIZE_COMPONENT.invoke(null, value);
//            return MCAdventure.GSON.deserialize(json);
//        }
//
//
//    }
//
//    public static boolean is(Class<?> clazz, Object object) {
//        if (clazz == null || object == null) {
//            return false;
//        }
//
//        return clazz.isAssignableFrom(object.getClass());
//    }
//
//    @SuppressWarnings("unchecked")
//    public static EntityDataType<?> unwarpDataType(@NotNull Object serializer) throws ReflectiveOperationException {
//        if (UNWARP_SERIALIZER == null) {
//            Class<?> dataWatcherClass = getDataWatcherClass();
//
//            if (serializer instanceof Integer) {
//                // 1.8
//                UNWARP_SERIALIZER = t -> {
//                    int id = (int) serializer;
//                    return EntityDataTypes.getById(MCServer.current().getVersion().toClientVersion(), id);
//                };
//            } else {
//                // 1.9+
//                Class<?> dataWatcherRegistryClass = getDataWatcherRegistryClass();
//                Class<?> serializerClass = getDataWatcherSerializerClass();
//
//                MethodWrapper<?> methodWrapper = new MethodResolver(dataWatcherRegistryClass).resolve(int.class, 0, serializerClass);
//                UNWARP_SERIALIZER = t -> {
//                    int id = (int) methodWrapper.invoke(null, t);
//                    return EntityDataTypes.getById(MCServer.current().getVersion().toClientVersion(), id);
//                };
//            }
//        }
//
//        return UNWARP_SERIALIZER.apply(serializer);
//    }
//
//    public static Class<?> getEntityClass() throws ClassNotFoundException {
//        return new NMSClassResolver().resolve("world.entity.Entity", "Entity");
//    }
//
//    public static Class<?> getDataWatcherClass() throws ClassNotFoundException {
//        return new NMSClassResolver().resolve(
//                "network.syncher.DataWatcher",
//                "network.syncher.SynchedEntityData",
//                "DataWatcher"
//        );
//    }
//
//    @Nullable
//    public static Class<?> getDataWatcherObjectClass() {
//        return new NMSClassResolver().resolveSilent(
//                "network.syncher.DataWatcherObject",
//                "network.syncher.EntityDataAccessor",
//                "DataWatcherObject"
//        );
//    }
//
//    @Nullable
//    public static Class<?> getDataWatcherSerializerClass()  {
//        return new NMSClassResolver().resolveSilent(
//                "network.syncher.DataWatcherSerializer",
//                "network.syncher.EntityDataSerializer",
//                "DataWatcherSerializer"
//        );
//    }
//
//    public static Class<?> getChatSerializerClass() throws ClassNotFoundException {
//        return new NMSClassResolver().resolve("network.chat.IChatBaseComponent$ChatSerializer", "network.chat.Component$Serializer", "IChatBaseComponent$ChatSerializer");
//    }
//
//    public static Class<?> getDataWatcherRegistryClass() throws ClassNotFoundException {
//        return new NMSClassResolver().resolve(
//                "network.syncher.DataWatcherRegistry",
//                "network.syncher.EntityDataSerializers",
//                "DataWatcherRegistry"
//        );
//    }
//
//}
