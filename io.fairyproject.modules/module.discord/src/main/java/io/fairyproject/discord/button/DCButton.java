package io.fairyproject.discord.button;

import io.fairyproject.discord.DCBot;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class DCButton {

    private final String idOrUrl;
    private final ButtonStyle buttonStyle;
    private final String label;
    private final Emoji emoji;
    private final BiConsumer<User, ButtonInteraction> action;

    public Button toButton(DCBot bot) {
        Button button = Button.of(buttonStyle, idOrUrl, label, emoji);
        bot.getButtonReader().read(this.idOrUrl, this.action);
        return button;
    }

    public ActionRow toActionRow(DCBot bot) {
        final Button button = this.toButton(bot);
        return ActionRow.of(button);
    }

    public ActionRow[] toActionRows(DCBot bot) {
        return new ActionRow[]{this.toActionRow(bot)};
    }

    public static Builder url(String url) {
        return new Builder(url, ButtonStyle.LINK);
    }

    public static Builder primary(String id) {
        return new Builder(id, ButtonStyle.PRIMARY);
    }

    public static Builder secondary(String id) {
        return new Builder(id, ButtonStyle.SECONDARY);
    }

    public static Builder success(String id) {
        return new Builder(id, ButtonStyle.SUCCESS);
    }

    public static Builder danger(String id) {
        return new Builder(id, ButtonStyle.DANGER);
    }

    @RequiredArgsConstructor
    public static class Builder {

        private final String idOrUrl;
        private final ButtonStyle buttonStyle;
        private String label;
        private Emoji emoji;
        private BiConsumer<User, ButtonInteraction> action;

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder emoji(Emoji emoji) {
            this.emoji = emoji;
            return this;
        }

        public Builder emojiUnicode(String unicode) {
            this.emoji = Emoji.fromUnicode(unicode);
            return this;
        }

        public Builder action(BiConsumer<User, ButtonInteraction> action) {
            this.action = action;
            return this;
        }

        public Builder action(Consumer<User> action) {
            this.action = (u, b) -> action.accept(u);
            return this;
        }

        public DCButton build() {
            return new DCButton(this.idOrUrl, buttonStyle, label, emoji, action);
        }

    }

}
