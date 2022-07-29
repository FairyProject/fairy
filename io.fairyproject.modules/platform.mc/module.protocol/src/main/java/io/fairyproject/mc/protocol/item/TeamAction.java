package io.fairyproject.mc.protocol.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TeamAction {

    ADD(0, true, true),
    REMOVE(1, false, false),
    CHANGE(2, true, false),
    JOIN(3, false, true),
    LEAVE(4, false, true);

    private final int id;
    private final boolean hasParameters;
    private final boolean hasPlayers;

    public static TeamAction getById(int id) {
        for (TeamAction action : TeamAction.values()) {
            if (action.id == id) {
                return action;
            }
        }
        throw new IllegalArgumentException(String.valueOf(id));
    }

}
