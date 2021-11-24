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

package io.fairyproject.bukkit.util.sound;

import io.fairyproject.ObjectSerializer;
import io.fairyproject.container.Component;
import org.bukkit.Sound;

@Component
public class SoundDataSerializer implements ObjectSerializer<SoundData, String> {
    @Override
    public String serialize(SoundData input) {
        return input.getSound().name() + ":" + input.getVolume() + ":" + input.getPitch();
    }

    @Override
    public SoundData deserialize(String output) {
        String[] split = output.split(":");
        String name = split[0];
        float volume = Float.parseFloat(split[1]);
        float pitch = Float.parseFloat(split[2]);
        return new SoundData(Sound.valueOf(name), volume, pitch);
    }

    @Override
    public Class<SoundData> inputClass() {
        return SoundData.class;
    }

    @Override
    public Class<String> outputClass() {
        return String.class;
    }
}
