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

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@Data
@AllArgsConstructor
public class SoundData {

    private Sound sound;
    private float volume;
    private float pitch;

    public SoundData(Sound sound) {
        this(sound, 1F, 1F);
    }

    public void play(Player... players) {
        for (Player player : players) {
            player.playSound(player.getLocation(), this.sound, this.volume, this.pitch);
        }
    }

    public void play(Iterable<? extends Player> players) {
        for (Player player : players) {
            player.playSound(player.getLocation(), this.sound, this.volume, this.pitch);
        }
    }

    public void play(Location location) {
        location.getWorld().playSound(location, this.sound, this.volume, this.pitch);
    }

    public void play(Location location, Player... players) {
        for (Player player : players) {
            player.playSound(location, this.sound, this.volume, this.pitch);
        }
    }

    public void play(Location location, Iterable<Player> players) {
        for (Player player : players) {
            player.playSound(location, this.sound, this.volume, this.pitch);
        }
    }

    public static SoundData of(Sound sound) {
        return new SoundData(sound);
    }

    public static SoundData of(Sound sound, float volume, float pitch) {
        return new SoundData(sound, volume, pitch);
    }

    public static SoundData ofVolume(Sound sound, float volume) {
        return new SoundData(sound, volume, 1F);
    }

    public static SoundData ofPitch(Sound sound, float pitch) {
        return new SoundData(sound, 1F, pitch);
    }

}
