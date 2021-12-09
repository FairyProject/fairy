package io.fairyproject.mc.protocol.item;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public enum ObjectiveDisplaySlot {
    BELOW_NAME("belowName"),
    PLAYER_LIST("list"),
    SIDEBAR("sidebar"),
    SIDEBAR_TEAM_BLACK(NamedTextColor.BLACK),
    SIDEBAR_TEAM_DARK_BLUE(NamedTextColor.DARK_BLUE),
    SIDEBAR_TEAM_DARK_GREEN(NamedTextColor.DARK_GREEN),
    SIDEBAR_TEAM_DARK_AQUA(NamedTextColor.DARK_AQUA),
    SIDEBAR_TEAM_DARK_RED(NamedTextColor.DARK_RED),
    SIDEBAR_TEAM_DARK_PURPLE(NamedTextColor.DARK_PURPLE),
    SIDEBAR_TEAM_GOLD(NamedTextColor.GOLD),
    SIDEBAR_TEAM_GRAY(NamedTextColor.GRAY),
    SIDEBAR_TEAM_DARK_GRAY(NamedTextColor.DARK_GRAY),
    SIDEBAR_TEAM_BLUE(NamedTextColor.BLUE),
    SIDEBAR_TEAM_GREEN(NamedTextColor.GREEN),
    SIDEBAR_TEAM_AQUA(NamedTextColor.AQUA),
    SIDEBAR_TEAM_RED(NamedTextColor.RED),
    SIDEBAR_TEAM_LIGHT_PURPLE(NamedTextColor.LIGHT_PURPLE),
    SIDEBAR_TEAM_YELLOW(NamedTextColor.YELLOW),
    SIDEBAR_TEAM_WHITE(NamedTextColor.WHITE);

    public static final net.kyori.adventure.util.Index<String, ObjectiveDisplaySlot> NAMES = net.kyori.adventure.util.Index.create(ObjectiveDisplaySlot.class, ObjectiveDisplaySlot::getId);
    public static final net.kyori.adventure.util.Index<Integer, ObjectiveDisplaySlot> IDS = net.kyori.adventure.util.Index.create(ObjectiveDisplaySlot.class, ObjectiveDisplaySlot::getSerializeId);

    private final String id;
    @Getter
    private final int serializeId;

    ObjectiveDisplaySlot(@NotNull String id) {
        this.id = id;
        this.serializeId = getSerializeId();
    }

    ObjectiveDisplaySlot(@NotNull NamedTextColor color) {
        this.id = "sidebar.team." + color;
        this.serializeId = getSerializeId();
    }

    /**
     * Get the string id of this display slot.
     *
     * @return the string id
     */
    public @NotNull String getId() {
        return id;
    }

    private int createSerializeId() {
        switch (this) {
            case PLAYER_LIST:
                return 0;
            case SIDEBAR:
                return 1;
            case BELOW_NAME:
                return 2;
            default:
                String string = this.id.substring("sidebar.team.".length());
                ChatFormatting chatFormatting = ChatFormatting.getByName(string);
                if (chatFormatting != null && chatFormatting.getId() >= 0) {
                    return chatFormatting.getId() + 3;
                }
                return -1;
        }
    }

    @Override
    public String toString() {
        return this.id;
    }
}
