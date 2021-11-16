package io.fairyproject.mc.protocol.item;

import com.google.common.collect.Maps;

import java.util.Map;

public enum NameTagVisibility {

    ALWAYS("always", 0),
    NEVER("never", 1),
    HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
    HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

    private static final Map<String, NameTagVisibility> nameToTagVisibility = Maps.newHashMap();

    public final String name;
    public final int id;

    public static NameTagVisibility getByName(String name) {
        return nameToTagVisibility.get(name);
    }

    NameTagVisibility(String name, int id) {
        this.name = name;
        this.id = id;
    }

    static {
        for (NameTagVisibility visibility : values()) {
            nameToTagVisibility.put(visibility.name, visibility);
        }
    }

}
