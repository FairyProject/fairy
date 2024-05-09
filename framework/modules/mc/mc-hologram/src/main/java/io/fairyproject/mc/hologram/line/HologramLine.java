package io.fairyproject.mc.hologram.line;

import io.fairyproject.mc.MCPlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public interface HologramLine {

    static @NotNull HologramLine create(@NotNull Component component) {
        return mcPlayer -> component;
    }

    static @NotNull HologramLine createLegacy(@NotNull String unparsed) {
        return mcPlayer -> Component.text(unparsed);
    }

    static @NotNull HologramLine create(@NotNull Supplier<Component> supplier) {
        return mcPlayer -> supplier.get();
    }

    static @NotNull HologramLine createLegacy(@NotNull Supplier<String> supplier) {
        return mcPlayer -> Component.text(supplier.get());
    }

    static @NotNull HologramLine create(@NotNull Function<MCPlayer, Component> function) {
        return function::apply;
    }

    static @NotNull HologramLine createLegacy(@NotNull Function<MCPlayer, String> function) {
        return mcPlayer -> Component.text(function.apply(mcPlayer));
    }

    @Nullable Component render(@NotNull MCPlayer mcPlayer);

}
