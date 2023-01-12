/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.mc.util;

import io.fairyproject.Fairy;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.util.math.CoordinateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @since 0.6.2b1-SNAPSHOT
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Position implements Cloneable {

	private String world = "world";

	private double x = 0.0;
	private double y = 0.0;
	private double z = 0.0;

	private float yaw = 0.0F;
	private float pitch = 0.0F;

	public Position(final double x, final double y, final double z) {
		this(x, y, z, 0.0F, 0.0F);
	}

	public Position(final String world, final double x, final double y, final double z) {
		this(world, x, y, z, 0.0F, 0.0F);
	}

	public Position(final double x, final double y, final double z, final float yaw, final float pitch) {
		this("world", x, y, z, yaw, pitch);
	}

	public static Position fromString(final String string) {
		final String[] split = string.split(", ");

		final double x = Double.parseDouble(split[0]);
		final double y = Double.parseDouble(split[1]);
		final double z = Double.parseDouble(split[2]);

		final Position pos = new Position(x, y, z);

		if (split.length == 4) {
			pos.setWorld(split[3]);
		} else if (split.length >= 5) {
			pos.setYaw(Float.parseFloat(split[3]));
			pos.setPitch(Float.parseFloat(split[4]));

			if (split.length >= 6) {
				pos.setWorld(split[5]);
			}
		}
		return pos;
	}

	public double groundDistanceTo(final Position location) {
		return Math.sqrt(Math.pow(this.x - location.x, 2) + Math.pow(this.z - location.z, 2));
	}

	public double distanceTo(final Position location) {
		return Math.sqrt(Math.pow(this.x - location.x, 2) + Math.pow(this.y - location.y, 2) + Math.pow(this.z - location.z, 2));
	}

	public MCWorld getMCWorld() {
		if (this.world == null)
			return MCWorld.all().get(0);
		else
			return MCWorld.getByName(this.world);
	}

	public Position add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Position subtract(double x, double y, double z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	public Position multiply(double x, double y, double z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}

	public Position divide(double x, double y, double z) {
		this.x /= x;
		this.y /= y;
		this.z /= z;
		return this;
	}

	public Position add(Position pos) {
		this.x += pos.x;
		this.y += pos.y;
		this.z += pos.z;
		return this;
	}

	public Position subtract(Position pos) {
		this.x -= pos.x;
		this.y -= pos.y;
		this.z -= pos.z;
		return this;
	}

	public Position multiply(Position pos) {
		this.x *= pos.x;
		this.y *= pos.y;
		this.z *= pos.z;
		return this;
	}

	public Position divide(Position location) {
		this.x /= location.x;
		this.y /= location.y;
		this.z /= location.z;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Position location = (Position) o;

		if (Double.compare(location.x, x) != 0) return false;
		if (Double.compare(location.y, y) != 0) return false;
		if (Double.compare(location.z, z) != 0) return false;
		if (Float.compare(location.yaw, yaw) != 0) return false;
		if (Float.compare(location.pitch, pitch) != 0) return false;
		return Objects.equals(world, location.world);
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = world != null ? world.hashCode() : 0;
		temp = Double.doubleToLongBits(x);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (yaw != +0.0f ? Float.floatToIntBits(yaw) : 0);
		result = 31 * result + (pitch != +0.0f ? Float.floatToIntBits(pitch) : 0);
		return result;
	}

	@Override
	public String toString() {
		final StringJoiner joiner = new StringJoiner(", ");
		joiner.add(Double.toString(this.getX()));
		joiner.add(Double.toString(this.getY()));
		joiner.add(Double.toString(this.getZ()));
		if (this.getYaw() == 0.0f && this.getPitch() == 0.0f) {
			joiner.add(this.getWorld());
			return joiner.toString();
		} else {
			joiner.add(Float.toString(this.getYaw()));
			joiner.add(Float.toString(this.getPitch()));
			joiner.add(this.getWorld());
			return joiner.toString();
		}
	}

	public static int locToBlock(final double loc) {
		return (int) Math.floor(loc);
	}

	public int getBlockX() {
		return locToBlock(x);
	}

	public int getBlockY() {
		return locToBlock(y);
	}

	public int getBlockZ() {
		return locToBlock(z);
	}

	public int getChunkX() {
		return CoordinateUtil.worldToChunk(this.getBlockX());
	}

	public int getChunkY() {
		return CoordinateUtil.worldToChunk(this.getBlockY());
	}

	public int getChunkZ() {
		return CoordinateUtil.worldToChunk(this.getBlockZ());
	}

	public void teleport(MCPlayer player, double range) {
		this.teleport(player, range, true);
	}

	public void teleport(MCPlayer player, double range, boolean safe) {
		double rand = -range + (range * 2) * Fairy.random().nextDouble();
		player.teleport(this.clone().add(rand, safe ? 1.5D : 0.0D, rand));
	}

	@Override
	public Position clone() {
		return new Position(
				this.world,
				this.x,
				this.y,
				this.z,
				this.yaw,
				this.pitch
		);
	}

}
