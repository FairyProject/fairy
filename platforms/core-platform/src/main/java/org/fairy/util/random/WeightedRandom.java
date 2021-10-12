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

package org.fairy.util.random;

import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class WeightedRandom {

    private final Random DEFAULT_RANDOM = new Random();

    public int getTotalWeight(Collection<? extends WeightedItem> collection) {
        return collection.stream()
                .map(WeightedItem::getWeight)
                .reduce(0, Integer::sum);
    }

    public <T extends WeightedItem> T getRandomItem(List<? extends T> collection, Random random) {
        return getItemFor(collection, random.nextInt(getTotalWeight(collection)));
    }

    private <T extends WeightedItem> T getItemFor(List<T> collection, int value) {
        Iterator<T> iterator = collection.iterator();

        T item;
        do {
            if (!iterator.hasNext()) {
                return null;
            }

            item = iterator.next();
            value -= item.getWeight();
        } while (value >= 0);

        return item;
    }

    public <T extends WeightedItem> T getRandomItem(Collection<? extends T> collection, Random random) {
        return getRandomItem(new ArrayList<>(collection), random);
    }

    public <T extends WeightedItem> T getRandomItem(List<? extends T> collection) {
        return getRandomItem(collection, DEFAULT_RANDOM);
    }

    public <T extends WeightedItem> T getRandomItem(Collection<? extends T> collection) {
        return getRandomItem(collection, DEFAULT_RANDOM);
    }

}
