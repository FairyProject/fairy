package io.fairyproject.mc.mcp;

public enum Direction {
    DOWN,
    UP,
    NORTH,
    SOUTH,
    WEST,
    EAST,
    OTHER((short)255),
    INVALID;

    private final short face;

    Direction(short face) {
        this.face = face;
    }

    Direction() {
        this.face = (short)this.ordinal();
    }

    public static Direction getDirection(int face) {
        if (face == 255) {
            return OTHER;
        } else {
            return face >= 0 && face <= 5 ? values()[face] : INVALID;
        }
    }

    public short getFaceValue() {
        return this.face;
    }
}

