package io.fairyproject.mc;

import io.fairyproject.mc.version.MCVersion;
import io.fairyproject.mc.version.MCVersionMapping;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.gson.LegacyHoverEventSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;

import java.util.Locale;

@UtilityClass
public class MCAdventure {

    public GsonComponentSerializer GSON;
    public LegacyComponentSerializer LEGACY;

    public void initialize(MCServer mcServer, MCVersionMappingRegistry mappingRegistry, AdventureHook adventureHook) {
        final GsonComponentSerializer.Builder builder = GsonComponentSerializer.builder();
        final LegacyComponentSerializer.Builder legacyBuilder = LegacyComponentSerializer.builder();

        MCVersion version = mcServer.getVersion();
        MCVersionMapping mapping = mappingRegistry.findMapping(version);

        if (!mapping.isHexColorSupport()) {
            builder.downsampleColors();
        }

        if (version.isHigherOrEqual(MCVersion.of(16))) {
            legacyBuilder
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat();
            builder.legacyHoverEventSerializer(adventureHook.getSerializer());
        } else {
            legacyBuilder
                    .character(LegacyComponentSerializer.SECTION_CHAR);
            builder
                    .legacyHoverEventSerializer(adventureHook.getSerializer())
                    .emitLegacyHoverEvent()
                    .downsampleColors();
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
        if (MCServer.current().getVersion().isHigherOrEqual(MCVersion.of(13))) {
            return asJsonString(component, locale);
        } else {
            return asLegacyString(component, locale);
        }
    }

    @Builder
    @Data
    public static class AdventureHook {
        private LegacyHoverEventSerializer serializer;
    }
}
