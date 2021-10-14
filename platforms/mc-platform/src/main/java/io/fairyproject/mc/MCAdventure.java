package io.fairyproject.mc;

import io.fairyproject.mc.protocol.MCProtocol;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;

import java.util.Locale;

@UtilityClass
public class MCAdventure {

    public GsonComponentSerializer GSON;

    public void initialize() {
        final GsonComponentSerializer.Builder builder = GsonComponentSerializer.builder();
        if (!MCProtocol.INSTANCE.getProtocolMapping().getVersion().isHexColorSupport()) {
            builder.downsampleColors();
        }

        GSON = builder.build();
    }

    public String asJsonString(Component component, Locale locale) {
        return GSON.serialize(
                GlobalTranslator.render(component, locale)
        );
    }

}
