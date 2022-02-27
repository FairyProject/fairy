package io.fairyproject.mc.util;

import java.util.Objects;

public class Vec3i {
    private int x;
    private int y;
    private int z;

    /**
     * @param x X coordinate of the vector
     * @param y Y coordinate of the vector
     * @param z Z coordinate of the vector
     */
    public Vec3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    /**
     * @return Return Z coordinate of the vector
     */
    public int getZ() {
        return z;
    }

    /**
     * @param z Set Z coordinate of the vector
     */
    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vec3i)) return false;
        Vec3i vec3i = (Vec3i) o;
        return x == vec3i.x && y == vec3i.y && z == vec3i.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
