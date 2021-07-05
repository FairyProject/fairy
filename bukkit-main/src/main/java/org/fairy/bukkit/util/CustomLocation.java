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

package org.fairy.bukkit.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.fairy.Fairy;
import org.fairy.bukkit.Imanity;
import org.fairy.config.annotation.ConfigurationElement;

import java.io.IOException;
import java.util.Objects;
import java.util.StringJoiner;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationElement
@JsonSerialize(using = CustomLocation.Serializer.class)
@JsonDeserialize(using = CustomLocation.Deserializer.class)
public class CustomLocation {

	private String world = "world";

	private double x = 0.0;
	private double y = 0.0;
	private double z = 0.0;

	private float yaw = 0.0F;
	private float pitch = 0.0F;

	public CustomLocation(final double x, final double y, final double z) {
		this(x, y, z, 0.0F, 0.0F);
	}

	public CustomLocation(final String world, final double x, final double y, final double z) {
		this(world, x, y, z, 0.0F, 0.0F);
	}

	public CustomLocation(final double x, final double y, final double z, final float yaw, final float pitch) {
		this("world", x, y, z, yaw, pitch);
	}

	public static CustomLocation fromBukkitLocation(final Location location) {
		return new CustomLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
				location.getYaw(), location.getPitch());
	}

	public static CustomLocation stringToLocation(final String string) {
		final String[] split = string.split(", ");

		final double x = Double.parseDouble(split[0]);
		final double y = Double.parseDouble(split[1]);
		final double z = Double.parseDouble(split[2]);

		final CustomLocation customLocation = new CustomLocation(x, y, z);

		if (split.length == 4) {
			customLocation.setWorld(split[3]);
		} else if (split.length >= 5) {
			customLocation.setYaw(Float.parseFloat(split[3]));
			customLocation.setPitch(Float.parseFloat(split[4]));

			if (split.length >= 6) {
				customLocation.setWorld(split[5]);
			}
		}
		return customLocation;
	}

	public static String locationToString(final CustomLocation loc) {
		final StringJoiner joiner = new StringJoiner(", ");
		joiner.add(Double.toString(loc.getX()));
		joiner.add(Double.toString(loc.getY()));
		joiner.add(Double.toString(loc.getZ()));
		if (loc.getYaw() == 0.0f && loc.getPitch() == 0.0f) {
			if (loc.getWorld().equals("world"))
				return joiner.toString();
			else {
				joiner.add(loc.getWorld());
				return joiner.toString();
			}
		} else {
			joiner.add(Float.toString(loc.getYaw()));
			joiner.add(Float.toString(loc.getPitch()));
			if (loc.getWorld().equals("world"))
				return joiner.toString();
			else {
				joiner.add(loc.getWorld());
				return joiner.toString();
			}
		}
	}

	public Location toBukkitLocation() {
		return new Location(this.toBukkitWorld(), this.x, this.y, this.z, this.yaw, this.pitch);
	}

	public double getGroundDistanceTo(final CustomLocation location) {
		return Math.sqrt(Math.pow(this.x - location.x, 2) + Math.pow(this.z - location.z, 2));
	}

	public double getDistanceTo(final CustomLocation location) {
		return Math.sqrt(Math.pow(this.x - location.x, 2) + Math.pow(this.y - location.y, 2) + Math.pow(this.z - location.z, 2));
	}

	public World toBukkitWorld() {
		if (this.world == null)
			return Bukkit.getServer().getWorlds().get(0);
		else
			return Bukkit.getServer().getWorld(this.world);
	}

	public CustomLocation add(CustomLocation location) {
		this.x += location.x;
		this.y += location.y;
		this.z += location.z;
		return this;
	}

	public CustomLocation subtract(CustomLocation location) {
		this.x -= location.x;
		this.y -= location.y;
		this.z -= location.z;
		return this;
	}

	public CustomLocation multiply(CustomLocation location) {
		this.x *= location.x;
		this.y *= location.y;
		this.z *= location.z;
		return this;
	}

	public CustomLocation divide(CustomLocation location) {
		this.x /= location.x;
		this.y /= location.y;
		this.z /= location.z;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CustomLocation location = (CustomLocation) o;

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
		return CustomLocation.locationToString(this);
	}

	public static int locToBlock(final double loc) {
		return NumberConversions.floor(loc);
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

	public void teleport(Player player, double range) {
		this.teleport(player, range, true);
	}

	public void teleport(Player player, double range, boolean safe) {
		double rand = -range + (range * 2) * Fairy.random().nextDouble();
		player.teleport(this.toBukkitLocation().add(rand, safe ? 0.5D : 0.0D, rand));
	}

	public static class Serializer extends StdSerializer<CustomLocation> {

		protected Serializer() {
			super(CustomLocation.class);
		}

		@Override
		public void serialize(CustomLocation customLocation, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
			jsonGenerator.writeString(customLocation.toString());
		}
	}

	public static class Deserializer extends StdDeserializer<CustomLocation> {

		protected Deserializer() {
			super(CustomLocation.class);
		}

		@Override
		public CustomLocation deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
			return CustomLocation.stringToLocation(jsonParser.getValueAsString());
		}
	}

}
