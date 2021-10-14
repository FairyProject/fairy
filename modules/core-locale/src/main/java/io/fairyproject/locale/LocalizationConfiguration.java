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

package io.fairyproject.locale;

import io.fairyproject.Fairy;
import io.fairyproject.config.annotation.Comment;
import io.fairyproject.config.yaml.YamlConfiguration;
import lombok.Getter;

import java.io.File;

@Getter
public class LocalizationConfiguration extends YamlConfiguration {

    @Comment({
            "What storage mode should localization be used?",
            "TEMPORARY - Localization Player Data will only be stored in memory",
            "FULL - Fully functional Localization Player Data, will be stored based on storage type"
    })
    private StorageMode storageMode = StorageMode.TEMPORARY;
    private String defaultLocale = "en_us";

    public LocalizationConfiguration() {
        super(new File(Fairy.getPlatform().getDataFolder(), "modules/localization.yml").toPath());
    }

    public enum StorageMode {

        TEMPORARY,
        FULL

    }
}
