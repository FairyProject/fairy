package io.fairyproject.mc;

import io.fairyproject.mc.version.MCVersion;
import io.fairyproject.mc.version.MCVersionMapping;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer;
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

        // Paper before Adventure 4.14 only exposes the Gson serializer overload.
        // Prefer the JSON overload, since Adventure 5 removes the old Gson API.
        LegacyHoverEventSerializer serializer = adventureHook.getSerializer();
        if (serializer != null) {
            try {
                builder.legacyHoverEventSerializer(serializer);
            } catch (NoSuchMethodError oldAdventure) {
                builder.legacyHoverEventSerializer((net.kyori.adventure.text.serializer.gson.LegacyHoverEventSerializer) serializer);
            }
        }

        if (version.isHigherOrEqual(MCVersion.of(16))) {
            legacyBuilder
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat();
        } else {
            legacyBuilder
                    .character(LegacyComponentSerializer.SECTION_CHAR);
            builder
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
