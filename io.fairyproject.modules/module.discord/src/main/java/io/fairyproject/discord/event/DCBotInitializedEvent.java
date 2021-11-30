package io.fairyproject.discord.event;

import io.fairyproject.discord.DCBot;
import io.fairyproject.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Event
@RequiredArgsConstructor
@Getter
public class DCBotInitializedEvent {

    private final DCBot bot;

}
