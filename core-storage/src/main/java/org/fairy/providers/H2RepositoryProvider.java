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

package org.fairy.providers;

import com.google.common.collect.ImmutableMap;
import org.fairy.RepositoryType;
import org.fairy.mysql.config.AbstractSqlRepositoryProvider;
import org.fairy.mysql.connection.file.H2ConnectionFactory;

import java.nio.file.Path;
import java.util.Map;

public class H2RepositoryProvider extends AbstractSqlRepositoryProvider<H2ConnectionFactory> {

    private final Path parentFolder;
    private Path path;

    public H2RepositoryProvider(String id, Path parentFolder) {
        super(id);
        this.parentFolder = parentFolder;
    }

    @Override
    public Map<String, String> getDefaultOptions() {
        return ImmutableMap.of(
                "path", "imanity-h2"
        );
    }

    @Override
    public void registerOptions(Map<String, String> map) {
        this.path = this.parentFolder.resolve(map.get("path"));
    }

    @Override
    public H2ConnectionFactory createFactory() {
        return new H2ConnectionFactory(this.path);
    }

    @Override
    public Class<H2ConnectionFactory> factoryClass() {
        return H2ConnectionFactory.class;
    }

    @Override
    public RepositoryType type() {
        return RepositoryType.H2;
    }
}
