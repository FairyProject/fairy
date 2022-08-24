package io.fairyproject.discord.event;

import io.fairyproject.discord.DCBot;
import io.fairyproject.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class DCBotInitializedEvent implements Event {

    private final DCBot bot;

}
