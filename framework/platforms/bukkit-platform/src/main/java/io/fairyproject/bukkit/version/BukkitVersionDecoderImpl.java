/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.bukkit.version;

import io.fairyproject.mc.version.MCVersion;
import org.bukkit.Server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitVersionDecoderImpl implements BukkitVersionDecoder {
    @Override
    public MCVersion decode(Server server) {
        String version = server.getVersion();
        Pattern pattern = Pattern.compile("MC: (\\d+\\.\\d+(\\.\\d+)?)");
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            String[] versionSplit = matcher.group(1).split("\\.");
            return MCVersion.of(Integer.parseInt(versionSplit[0]), Integer.parseInt(versionSplit[1]), versionSplit.length > 2 ? Integer.parseInt(versionSplit[2]) : 0);
        }

        throw new IllegalArgumentException("Unknown version: " + version);
    }
}
