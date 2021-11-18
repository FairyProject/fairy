package io.fairyproject.mc;

import io.fairyproject.mc.util.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface MCGameProfile {

    static MCGameProfile create(UUID uuid, String name) {
        return Companion.BRIDGE.create(name, uuid);
    }

    static <T> MCGameProfile from(T t) {
        return Companion.BRIDGE.from(t);
    }

    String getName();

    UUID getUuid();

    /**
     * @return A Mutable set of this players properties, such as textures.
     * Values specified here are subject to implementation details.
     */
    @NotNull Set<Property> getProperties();

    /**
     * Check if the Profile has the specified property
     * @param property Property name to check
     * @return If the property is set
     */
    boolean hasProperty(@Nullable String property);

    /**
     * Sets a property. If the property already exists, the previous one will be replaced
     * @param property Property to set.
     */
    void setProperty(@NotNull Property property);

    /**
     * Sets multiple properties. If any of the set properties already exist, it will be replaced
     * @param properties The properties to set
     */
    void setProperties(@NotNull Collection<Property> properties);

    /**
     * Removes a specific property from this profile
     * @param property The property to remove
     * @return If a property was removed
     */
    boolean removeProperty(@Nullable String property);

    /**
     * Removes a specific property from this profile
     * @param property The property to remove
     * @return If a property was removed
     */
    default boolean removeProperty(@NotNull Property property) {
        return removeProperty(property.getName());
    }

    /**
     * Removes all properties in the collection
     * @param properties The properties to remove
     * @return If any property was removed
     */
    default boolean removeProperties(@NotNull Collection<Property> properties) {
        boolean removed = false;
        for (Property property : properties) {
            if (removeProperty(property)) {
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Clears all properties on this profile
     */
    void clearProperties();

    class Companion {
        public static Bridge BRIDGE;
    }

    interface Bridge {
        MCGameProfile create(String name, UUID uuid);

        MCGameProfile from(Object object);
    }

}
