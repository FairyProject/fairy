package io.fairyproject.mc.util;

import java.util.Objects;

public class Vec2i {
    private int x;
    private int y;

    /**
     * @param x X coordinate of the vector
     * @param y Y coordinate of the vector
     */
    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return Return X coordinate of the vector
     */
    public int getX() {
        return x;
    }

    /**
     * @param x Set X coordinate of the vector
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return Return Y coordinate of the vector
     */
    public int getY() {
        return y;
    }

    /**
     * @param y Set Y coordinate of the vector
     */
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vec2i)) return false;
        Vec2i vec2i = (Vec2i) o;
        return x == vec2i.x && y == vec2i.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
