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

package io.fairyproject.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

public class CoordinatePair {
	private final String worldName;
	private final int x;
	private final int z;

	public CoordinatePair(final Block block) {
		this(block.getWorld(), block.getX(), block.getZ());
	}

	public CoordinatePair(final String worldName, final int x, final int z) {
		this.worldName = worldName;
		this.x = x;
		this.z = z;
	}

	public CoordinatePair(final World world, final int x, final int z) {
		this.worldName = world.getName();
		this.x = x;
		this.z = z;
	}

	public String getWorldName() {
		return this.worldName;
	}

	public World getWorld() {
		return Bukkit.getWorld(this.worldName);
	}

	public int getX() {
		return this.x;
	}

	public int getZ() {
		return this.z;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CoordinatePair))
			return false;
		final CoordinatePair that = (CoordinatePair) o;
		return this.x == that.x && this.z == that.z && ((this.worldName != null) ? this.worldName.equals(that.worldName) : (that.worldName == null));
	}

	@Override
	public int hashCode() {
		int result = (this.worldName != null) ? this.worldName.hashCode() : 0;
		result = 31 * result + this.x;
		result = 31 * result + this.z;
		return result;
	}
}
