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

package io.fairyproject.mc.library;

import io.fairyproject.library.Library;
import io.fairyproject.library.LibraryBundle;
import io.fairyproject.library.relocate.Relocation;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class AdventureLibraryBundle implements LibraryBundle {

    protected final String version;

    @Override
    public List<Library> libraries() {
        return Arrays.asList(
                Library.builder()
                        .groupId("net{}kyori")
                        .artifactId("examination-api")
                        .version("1.3.0")
                        .build(),
                Library.builder()
                        .groupId("net{}kyori")
                        .artifactId("examination-string")
                        .version("1.3.0")
                        .build(),
                Library.builder()
                        .groupId("net{}kyori")
                        .artifactId("adventure-api")
                        .version(this.version)
                        .build(),
                Library.builder()
                        .groupId("net{}kyori")
                        .artifactId("adventure-key")
                        .version(this.version)
                        .build(),
                Library.builder()
                        .groupId("net{}kyori")
                        .artifactId("adventure-text-serializer-gson")
                        .version(this.version)
                        .build(),
                Library.builder()
                        .groupId("net{}kyori")
                        .artifactId("adventure-text-serializer-legacy")
                        .version(this.version)
                        .build(),
                Library.builder()
                        .groupId("net{}kyori")
                        .artifactId("adventure-nbt")
                        .version(this.version)
                        .build(),
                Library.builder()
                        .groupId("net{}kyori")
                        .artifactId("adventure-text-minimessage")
                        .version(this.version)
                        .build(),
                Library.builder()
                        .groupId("net{}kyori")
                        .artifactId("adventure-text-serializer-gson-legacy-impl")
                        .version(this.version)
                        .build(),
                Library.builder()
                        .groupId("net{}kyori")
                        .artifactId("adventure-text-serializer-plain")
                        .version(this.version)
                        .build()
        );
    }

    @Override
    public List<Relocation> relocations() {
        return Collections.singletonList(Relocation.of("net{}kyori", "io.fairyproject.libs.kyori"));
    }
}
