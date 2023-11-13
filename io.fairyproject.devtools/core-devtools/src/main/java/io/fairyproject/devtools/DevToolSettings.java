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

package io.fairyproject.devtools;

import io.fairyproject.config.yaml.YamlConfiguration;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * DevTools settings.
 */
@Getter
@SuppressWarnings("FieldMayBeFinal")
public class DevToolSettings extends YamlConfiguration {

    private Restart restart = new Restart();

    protected DevToolSettings(Path path) {
        super(path);

        if (Files.exists(path)) {
            this.load();
        }
    }

    @Getter
    public static class Restart {

        private boolean enabled = true;

        private Duration classpathScanInterval = Duration.ofSeconds(1);

        private Duration quietPeriod = Duration.ofSeconds(1);

    }

}
