package io.fairyproject.mc.util;

import java.util.Objects;

public class Vec3d {
    private double x;
    private double y;
    private double z;

    /**
     * @param x X coordinate of the vector
     * @param y Y coordinate of the vector
     * @param z Z coordinate of the vector
     */
    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @return Return X coordinate of the vector
     */
    public double getX() {
        return x;
    }

    /**
     * @param x Set X coordinate of the vector
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return Return Y coordinate of the vector
     */
    public double getY() {
        return y;
    }

    /**
     * @param y Set Y coordinate of the vector
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * @return Return Z coordinate of the vector
     */
    public double getZ() {
        return z;
    }

    /**
     * @param z Set Z coordinate of the vector
     */
    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vec3d)) return false;
        Vec3d vec3i = (Vec3d) o;
        return x == vec3i.x && y == vec3i.y && z == vec3i.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
