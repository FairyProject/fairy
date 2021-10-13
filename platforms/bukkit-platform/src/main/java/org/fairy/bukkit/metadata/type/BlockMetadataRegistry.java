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

package org.fairy.bukkit.metadata.type;

import org.bukkit.block.Block;
import org.fairy.mc.util.BlockPosition;
import org.fairy.metadata.MetadataKey;
import org.fairy.metadata.MetadataMap;
import org.fairy.metadata.MetadataRegistry;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

/**
 * A registry which provides and stores {@link MetadataMap}s for {@link Block}s.
 */
public interface BlockMetadataRegistry extends MetadataRegistry<BlockPosition> {

    /**
     * Produces a {@link MetadataMap} for the given block.
     *
     * @param block the block
     * @return a metadata map
     */
    @Nonnull
    MetadataMap provide(@Nonnull Block block);

    /**
     * Gets a {@link MetadataMap} for the given block, if one already exists and has
     * been cached in this registry.
     *
     * @param block the block
     * @return a metadata map, if present
     */
    @Nonnull
    Optional<MetadataMap> get(@Nonnull Block block);

    /**
     * Gets a map of the blocks with a given metadata key
     *
     * @param key the key
     * @param <K> the key type
     * @return an immutable map of blocks to key value
     */
    @Nonnull
    <K> Map<BlockPosition, K> getAllWithKey(@Nonnull MetadataKey<K> key);

}
