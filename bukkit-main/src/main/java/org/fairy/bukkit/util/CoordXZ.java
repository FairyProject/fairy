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

public class CoordXZ {
	public int x, z;

	public CoordXZ(final int x, final int z) {
		this.x = x;
		this.z = z;
	}

	public CoordXZ(final int x, final int z, final boolean chunk) {
		if (chunk) {
			this.x = blockToChunk(x);
			this.z = blockToChunk(z);
			return;
		}
		this.x = x;
		this.z = z;
	}

	public static int blockToChunk(final int blockVal) {
		return blockVal >> 4;
	}

	public static int blockToRegion(final int blockVal) {
		return blockVal >> 9;
	}

	public static int chunkToRegion(final int chunkVal) {
		return chunkVal >> 5;
	}

	public static int chunkToBlock(final int chunkVal) {
		return chunkVal << 4;
	}

	public static int regionToBlock(final int regionVal) {
		return regionVal << 9;
	}

	public static int regionToChunk(final int regionVal) {
		return regionVal << 5;
	}


	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		else if (obj == null || obj.getClass() != this.getClass())
			return false;

		final CoordXZ test = (CoordXZ) obj;
		return test.x == this.x && test.z == this.z;
	}

	@Override
	public int hashCode() {
		return (this.x << 9) + this.z;
	}
}