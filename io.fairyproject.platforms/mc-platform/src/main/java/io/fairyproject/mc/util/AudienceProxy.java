package io.fairyproject.mc.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.pointer.Pointer;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class AudienceProxy implements Audience {

    public abstract Audience audience();

    @Override
    public @NotNull Audience filterAudience(@NotNull Predicate<? super Audience> filter) {
        return this.audience().filterAudience(filter);
    }

    @Override
    public void forEachAudience(@NotNull Consumer<? super Audience> action) {
        this.audience().forEachAudience(action);
    }

    @Override
    public void sendMessage(@NotNull ComponentLike message) {
        this.audience().sendMessage(message);
    }

    @Override
    public void sendMessage(@NotNull Identified source, @NotNull ComponentLike message) {
        this.audience().sendMessage(source, message);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull ComponentLike message) {
        this.audience().sendMessage(source, message);
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        this.audience().sendMessage(message);
    }

    @Override
    public void sendMessage(@NotNull Identified source, @NotNull Component message) {
        this.audience().sendMessage(source, message);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message) {
        this.audience().sendMessage(source, message);
    }

    @Override
    public void sendMessage(@NotNull ComponentLike message, @NotNull MessageType type) {
        this.audience().sendMessage(message, type);
    }

    @Override
    public void sendMessage(@NotNull Identified source, @NotNull ComponentLike message, @NotNull MessageType type) {
        this.audience().sendMessage(source, message, type);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull ComponentLike message, @NotNull MessageType type) {
        this.audience().sendMessage(source, message, type);
    }

    @Override
    public void sendMessage(@NotNull Component message, @NotNull MessageType type) {
        this.audience().sendMessage(message, type);
    }

    @Override
    public void sendMessage(@NotNull Identified source, @NotNull Component message, @NotNull MessageType type) {
        this.audience().sendMessage(source, message, type);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        this.audience().sendMessage(source, message, type);
    }

    @Override
    public void sendActionBar(@NotNull ComponentLike message) {
        this.audience().sendActionBar(message);
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        this.audience().sendActionBar(message);
    }

    @Override
    public void sendPlayerListHeader(@NotNull ComponentLike header) {
        this.audience().sendPlayerListHeader(header);
    }

    @Override
    public void sendPlayerListHeader(@NotNull Component header) {
        this.audience().sendPlayerListHeader(header);
    }

    @Override
    public void sendPlayerListFooter(@NotNull ComponentLike footer) {
        this.audience().sendPlayerListFooter(footer);
    }

    @Override
    public void sendPlayerListFooter(@NotNull Component footer) {
        this.audience().sendPlayerListFooter(footer);
    }

    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull ComponentLike header, @NotNull ComponentLike footer) {
        this.audience().sendPlayerListHeaderAndFooter(header, footer);
    }

    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
        this.audience().sendPlayerListHeaderAndFooter(header, footer);
    }

    @Override
    public void showTitle(@NotNull Title title) {
        this.audience().showTitle(title);
    }

    @Override
    public <T> void sendTitlePart(@NotNull TitlePart<T> part, @NotNull T value) {
        this.audience().sendTitlePart(part, value);
    }

    @Override
    public void clearTitle() {
        this.audience().clearTitle();
    }

    @Override
    public void resetTitle() {
        this.audience().resetTitle();
    }

    @Override
    public void showBossBar(@NotNull BossBar bar) {
        this.audience().showBossBar(bar);
    }

    @Override
    public void hideBossBar(@NotNull BossBar bar) {
        this.audience().hideBossBar(bar);
    }

    @Override
    public void playSound(@NotNull Sound sound) {
        this.audience().playSound(sound);
    }

    @Override
    public void playSound(@NotNull Sound sound, double x, double y, double z) {
        this.audience().playSound(sound, x, y, z);
    }

    @Override
    public void stopSound(@NotNull Sound sound) {
        this.audience().stopSound(sound);
    }

    @Override
    public void playSound(@NotNull Sound sound, Sound.@NotNull Emitter emitter) {
        this.audience().playSound(sound, emitter);
    }

    @Override
    public void stopSound(@NotNull SoundStop stop) {
        this.audience().stopSound(stop);
    }

    @Override
    public void openBook(Book.@NotNull Builder book) {
        this.audience().openBook(book);
    }

    @Override
    public void openBook(@NotNull Book book) {
        this.audience().openBook(book);
    }

    @Override
    public @NotNull <T> Optional<T> get(@NotNull Pointer<T> pointer) {
        return this.audience().get(pointer);
    }

    @Override
    public <T> @Nullable T getOrDefault(@NotNull Pointer<T> pointer, @Nullable T defaultValue) {
        return this.audience().getOrDefault(pointer, defaultValue);
    }

    @Override
    public <T> @UnknownNullability T getOrDefaultFrom(@NotNull Pointer<T> pointer, @NotNull Supplier<? extends T> defaultValue) {
        return this.audience().getOrDefaultFrom(pointer, defaultValue);
    }

    @Override
    public @NotNull Pointers pointers() {
        return this.audience().pointers();
    }
}
