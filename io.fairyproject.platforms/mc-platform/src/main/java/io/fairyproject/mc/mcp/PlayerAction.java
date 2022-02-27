package io.fairyproject.mc.mcp;

public enum PlayerAction {
        START_SNEAKING((byte)0, "PRESS_SHIFT_KEY"),
        STOP_SNEAKING((byte)1, "RELEASE_SHIFT_KEY"),
        STOP_SLEEPING((byte)2),
        START_SPRINTING((byte)3),
        STOP_SPRINTING((byte)4),
        START_RIDING_JUMP((byte)5),
        STOP_RIDING_JUMP((byte)6),
        OPEN_INVENTORY((byte)7),
        START_FALL_FLYING((byte)8),
        @Deprecated
        RIDING_JUMP((byte)5);

        private final byte actionID;
        private final String alias;

        PlayerAction(byte actionID) {
            this.actionID = actionID;
            this.alias = "empty";
        }

        PlayerAction(byte actionID, String alias) {
            this.actionID = actionID;
            this.alias = alias;
        }

    public String getAlias() {
        return alias;
    }

    public byte getActionValue() {
            return this.actionID;
        }
}