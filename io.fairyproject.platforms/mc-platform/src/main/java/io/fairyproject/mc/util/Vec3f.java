package io.fairyproject.mc.util;

import java.util.Objects;

public class Vec3f {
    private float x;
    private float y;
    private float z;

    /**
     * @param x X coordinate of the vector
     * @param y Y coordinate of the vector
     * @param z Z coordinate of the vector
     */
    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @return Return X coordinate of the vector
     */
    public float getX() {
        return x;
    }

    /**
     * @param x Set X coordinate of the vector
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * @return Return Y coordinate of the vector
     */
    public float getY() {
        return y;
    }

    /**
     * @param y Set Y coordinate of the vector
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * @return Return Z coordinate of the vector
     */
    public float getZ() {
        return z;
    }

    /**
     * @param z Set Z coordinate of the vector
     */
    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vec3f)) return false;
        Vec3f vec3i = (Vec3f) o;
        return x == vec3i.x && y == vec3i.y && z == vec3i.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
