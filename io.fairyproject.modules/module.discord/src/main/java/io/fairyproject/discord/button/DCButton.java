package io.fairyproject.discord.button;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@UtilityClass
public class DCButton {

    public Builder url(String url) {
        return new Builder(url, ButtonStyle.LINK);
    }

    public Builder primary(String id) {
        return new Builder(id, ButtonStyle.PRIMARY);
    }

    public Builder secondary(String id) {
        return new Builder(id, ButtonStyle.SECONDARY);
    }

    public Builder success(String id) {
        return new Builder(id, ButtonStyle.SUCCESS);
    }

    public Builder danger(String id) {
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

        public Button build() {
            Button button = Button.of(this.buttonStyle, this.idOrUrl, this.label);
            if (this.emoji != null) {
                button = button.withEmoji(emoji);
            }
            if (this.action != null) {

            }
            return button;
        }

    }

}
