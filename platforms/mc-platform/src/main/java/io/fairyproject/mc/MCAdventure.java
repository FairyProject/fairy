package io.fairyproject.mc;

import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.MCVersion;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;

import java.util.Locale;

@UtilityClass
public class MCAdventure {

    public GsonComponentSerializer GSON;
    public LegacyComponentSerializer LEGACY;

    public void initialize() {
        final GsonComponentSerializer.Builder builder = GsonComponentSerializer.builder();
        if (!MCProtocol.INSTANCE.getProtocolMapping().getVersion().isHexColorSupport()) {
            builder.downsampleColors();
        }

        final LegacyComponentSerializer.Builder legacyBuilder = LegacyComponentSerializer.builder();
        if (MCProtocol.INSTANCE.getProtocolMapping().getVersion().isHexColorSupport()) {
            legacyBuilder.hexColors();
        }

        GSON = builder.build();
        LEGACY = legacyBuilder.build();
    }

    public String asJsonString(Component component, Locale locale) {
        return GSON.serialize(
                GlobalTranslator.render(component, locale)
        );
    }

    public String asLegacyString(Component component, Locale locale) {
        return LEGACY.serialize(
                GlobalTranslator.render(component, locale)
        );
    }

    public String asItemString(Component component, Locale locale) {
        if (MCProtocol.INSTANCE.getProtocolMapping().getVersion().isOrAbove(MCVersion.V1_13)) {
            return asJsonString(component, locale);
        } else {
            return asLegacyString(component, locale);
        }
    }

}
