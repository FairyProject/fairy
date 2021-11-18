package io.fairyproject.bukkit.mc;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.fairyproject.mc.MCGameProfile;
import io.fairyproject.mc.util.Property;
import io.fairyproject.util.collection.ConvertedSet;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class PaperMCGameProfile implements MCGameProfile {

    private final PlayerProfile playerProfile;
    private final Set<Property> properties;

    public PaperMCGameProfile(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
        this.properties = new ConvertedSet<ProfileProperty, Property>(this.playerProfile.getProperties()) {
            @Override
            protected Property toOuter(ProfileProperty profileProperty) {
                return new Property(profileProperty.getName(), profileProperty.getValue(), profileProperty.getSignature());
            }

            @Override
            protected ProfileProperty toInner(Property property) {
                return toInner(property);
            }
        };
    }

    @Override
    public String getName() {
        return this.playerProfile.getName();
    }

    @Override
    public UUID getUuid() {
        return this.playerProfile.getId();
    }

    @Override
    public @NotNull Set<Property> getProperties() {
        return this.properties;
    }

    @Override
    public boolean hasProperty(@Nullable String property) {
        return this.playerProfile.hasProperty(property);
    }

    @Override
    public void setProperty(@NotNull Property property) {
        this.playerProfile.setProperty(this.toInner(property));
    }

    @Override
    public void setProperties(@NotNull Collection<Property> properties) {
        this.getProperties().clear();
        this.getProperties().addAll(properties);
    }

    @Override
    public boolean removeProperty(@Nullable String property) {
        return this.playerProfile.removeProperty(property);
    }

    @Override
    public void clearProperties() {
        this.playerProfile.clearProperties();
    }

    private ProfileProperty toInner(Property property) {
        return new ProfileProperty(property.getName(), property.getValue(), property.getSignature());
    }
}
