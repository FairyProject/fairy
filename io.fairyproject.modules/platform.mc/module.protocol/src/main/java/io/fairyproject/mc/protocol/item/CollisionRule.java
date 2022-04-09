package io.fairyproject.mc.protocol.item;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;

public enum CollisionRule {
    ALWAYS("always", 0),
    NEVER("never", 1),
    HIDE_FOR_OTHER_TEAMS("pushOtherTeams", 2),
    HIDE_FOR_OWN_TEAM("pushOwnTeam", 3);

    private static final Map<String, CollisionRule> nameToTeamPush = Maps.newHashMap();
    public final String name;
    public final int id;

    @Nullable
    public static CollisionRule getByName(String name) {
        return nameToTeamPush.get(name);
    }

    private CollisionRule(String name, int id) {
        this.name = name;
        this.id = id;
    }

    static {

        for (CollisionRule teamPush : values()) {
            nameToTeamPush.put(teamPush.name, teamPush);
        }

    }
}