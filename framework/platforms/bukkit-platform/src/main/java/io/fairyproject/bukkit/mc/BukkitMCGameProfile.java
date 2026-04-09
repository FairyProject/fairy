package io.fairyproject.bukkit.mc;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import io.fairyproject.mc.MCGameProfile;
import io.fairyproject.mc.util.Property;
import io.fairyproject.util.EquivalentConverter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class BukkitMCGameProfile implements MCGameProfile {

    // Reflection cache for authlib API compatibility (old getter-style vs new record-style)
    private static final Method GP_GET_NAME;
    private static final Method GP_GET_ID;
    private static final Method GP_GET_PROPERTIES;
    private static final Method PROP_GET_NAME;
    private static final Method PROP_GET_VALUE;
    private static final Method PROP_GET_SIGNATURE;
    private static final Constructor<?> PROP_CONSTRUCTOR;

    static {
        GP_GET_NAME = resolveMethod(GameProfile.class, "name", "getName");
        GP_GET_ID = resolveMethod(GameProfile.class, "id", "getId");
        GP_GET_PROPERTIES = resolveMethod(GameProfile.class, "properties", "getProperties");
        PROP_GET_NAME = resolveMethod(com.mojang.authlib.properties.Property.class, "name", "getName");
        PROP_GET_VALUE = resolveMethod(com.mojang.authlib.properties.Property.class, "value", "getValue");
        PROP_GET_SIGNATURE = resolveMethod(com.mojang.authlib.properties.Property.class, "signature", "getSignature");
        PROP_CONSTRUCTOR = resolvePropConstructor();
    }

    private static Method resolveMethod(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                return clazz.getMethod(name);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new IllegalStateException("No method found on " + clazz.getName() + " for names: " + Arrays.toString(names));
    }

    private static Constructor<?> resolvePropConstructor() {
        try {
            return com.mojang.authlib.properties.Property.class.getConstructor(String.class, String.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot find Property constructor", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(Method method, Object obj) {
        try {
            return (T) method.invoke(obj);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static final EquivalentConverter<MCGameProfile> CONVERTER = new EquivalentConverter<MCGameProfile>() {
        @Override
        public Object getGeneric(MCGameProfile specific) {
            return ((BukkitMCGameProfile) specific).gameProfile;
        }

        @Override
        public MCGameProfile getSpecific(Object generic) {
            if (!(generic instanceof com.mojang.authlib.GameProfile)) {
                throw new ClassCastException();
            }
            return new BukkitMCGameProfile((com.mojang.authlib.GameProfile)generic);
        }

        @Override
        public Class<MCGameProfile> getSpecificType() {
            return MCGameProfile.class;
        }
    };
    private final GameProfile gameProfile;
    private final PropertySet properties;

    public BukkitMCGameProfile(String name, UUID uuid) {
        this(new GameProfile(uuid, name));
    }

    public BukkitMCGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
        this.properties = new PropertySet();
    }

    @Override
    public String getName() {
        return invoke(GP_GET_NAME, this.gameProfile);
    }

    @Override
    public UUID getUuid() {
        return invoke(GP_GET_ID, this.gameProfile);
    }

    private PropertyMap getPropertyMap() {
        return invoke(GP_GET_PROPERTIES, this.gameProfile);
    }

    @NotNull
    @Override
    public Set<Property> getProperties() {
        return properties;
    }

    @Override
    public boolean hasProperty(String property) {
        return getPropertyMap().containsKey(property);
    }

    @Override
    public void setProperty(Property property) {
        String name = property.getName();
        PropertyMap properties = getPropertyMap();
        properties.removeAll(name);
        try {
            properties.put(name, (com.mojang.authlib.properties.Property) PROP_CONSTRUCTOR.newInstance(name, property.getValue(), property.getSignature()));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setProperties(Collection<Property> properties) {
        properties.forEach(this::setProperty);
    }

    @Override
    public void clearProperties() {
        getPropertyMap().clear();
    }

    @Override
    public boolean removeProperty(String property) {
        return !getPropertyMap().removeAll(property).isEmpty();
    }

    private static Property toBukkit(com.mojang.authlib.properties.Property property) {
        return new Property(
                invoke(PROP_GET_NAME, property),
                invoke(PROP_GET_VALUE, property),
                invoke(PROP_GET_SIGNATURE, property)
        );
    }

    private class PropertySet extends AbstractSet<Property> {

        @Override
        @Nonnull
        public Iterator<Property> iterator() {
            return new ProfilePropertyIterator(getPropertyMap().values().iterator());
        }

        @Override
        public int size() {
            return getPropertyMap().size();
        }

        @Override
        public boolean add(Property property) {
            setProperty(property);
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends Property> c) {
            //noinspection unchecked
            setProperties((Collection<Property>) c);
            return true;
        }

        @Override
        public boolean contains(Object o) {
            return o instanceof ProfileProperty && getPropertyMap().containsKey(((ProfileProperty) o).getName());
        }

        private class ProfilePropertyIterator implements Iterator<Property> {
            private final Iterator<com.mojang.authlib.properties.Property> iterator;

            ProfilePropertyIterator(Iterator<com.mojang.authlib.properties.Property> iterator) {
                this.iterator = iterator;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Property next() {
                return toBukkit(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        }
    }
}
