package io.fairyproject.bukkit.mc;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import io.fairyproject.mc.MCGameProfile;
import io.fairyproject.mc.util.Property;
import io.fairyproject.util.EquivalentConverter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

public class BukkitMCGameProfile implements MCGameProfile {

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
        return this.gameProfile.getName();
    }

    @Override
    public UUID getUuid() {
        return this.gameProfile.getId();
    }

    @NotNull
    @Override
    public Set<Property> getProperties() {
        return properties;
    }

    @Override
    public boolean hasProperty(String property) {
        return this.gameProfile.getProperties().containsKey(property);
    }

    @Override
    public void setProperty(Property property) {
        String name = property.getName();
        PropertyMap properties = this.gameProfile.getProperties();
        properties.removeAll(name);
        properties.put(name, new com.mojang.authlib.properties.Property(name, property.getValue(), property.getSignature()));
    }

    @Override
    public void setProperties(Collection<Property> properties) {
        properties.forEach(this::setProperty);
    }

    @Override
    public void clearProperties() {
        this.gameProfile.getProperties().clear();
    }

    @Override
    public boolean removeProperty(String property) {
        return !this.gameProfile.getProperties().removeAll(property).isEmpty();
    }

    private static Property toBukkit(com.mojang.authlib.properties.Property property) {
        return new Property(property.getName(), property.getValue(), property.getSignature());
    }

    private class PropertySet extends AbstractSet<Property> {

        @Override
        @Nonnull
        public Iterator<Property> iterator() {
            return new ProfilePropertyIterator(gameProfile.getProperties().values().iterator());
        }

        @Override
        public int size() {
            return gameProfile.getProperties().size();
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
            return o instanceof ProfileProperty && gameProfile.getProperties().containsKey(((ProfileProperty) o).getName());
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
