package io.fairyproject.discord;

import io.fairyproject.discord.channel.DCTextChannel;
import io.fairyproject.discord.proxies.ProxyGuild;
import io.fairyproject.util.collection.ConvertedList;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@RequiredArgsConstructor
public class DCGuild implements ProxyGuild {

    private final Guild guild;
    private final DCBot bot;

    @Override
    public Guild original() {
        return this.guild;
    }

    @Nullable
    public DCTextChannel getDCTextChannelById(long id) {
        final TextChannel textChannel = guild.getTextChannelById(id);
        assert textChannel != null;
        return new DCTextChannel(textChannel, bot);
    }

    @Nullable
    public DCTextChannel getDCTextChannelById(@NotNull String id) {
        final TextChannel textChannel = guild.getTextChannelById(id);
        assert textChannel != null;
        return new DCTextChannel(textChannel, bot);
    }

    @NotNull
    public List<DCTextChannel> getDCTextChannelsByName(@NotNull String name, boolean ignoreCase) {
        final List<TextChannel> textChannels = guild.getTextChannelsByName(name, ignoreCase);
        return new ConvertedList<TextChannel, DCTextChannel>(textChannels) {
            @Override
            protected DCTextChannel toOuter(TextChannel textChannel) {
                return new DCTextChannel(textChannel, bot);
            }
            @Override
            protected TextChannel toInner(DCTextChannel dcTextChannel) {
                return dcTextChannel.original();
            }
        };
    }
}
