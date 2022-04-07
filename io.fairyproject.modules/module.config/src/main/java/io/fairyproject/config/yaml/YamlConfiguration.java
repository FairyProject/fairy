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

package io.fairyproject.config.yaml;

import io.fairyproject.config.Comments;
import io.fairyproject.config.Configuration;
import io.fairyproject.config.ConfigurationSource;
import io.fairyproject.config.ConfigurationStoreException;
import org.apache.logging.log4j.LogManager;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class YamlConfiguration extends Configuration<YamlConfiguration> {

    private final YamlSource source;

    protected YamlConfiguration(Path path, YamlProperties properties) {
        super(properties);
        this.source = new YamlSource(path, properties);
    }

    protected YamlConfiguration(Path path) {
        this(path, YamlProperties.DEFAULT);
    }

    @Override
    protected final ConfigurationSource<YamlConfiguration> getSource() {
        return source;
    }

    @Override
    protected final YamlConfiguration getThis() {
        return this;
    }

    public final void loadAndSave() {
        try {
            load();
            save();
        } catch (ConfigurationStoreException e) {
            if (e.getCause() instanceof NoSuchFileException) {
                postLoad();
                save();
            } else {
                throw e;
            }
        }
    }

    Comments getComments() {
        return comments;
    }

    public static class YamlProperties extends Properties {
        public static final YamlProperties DEFAULT = builder().build();
        private final List<String> prependedComments;
        private final List<String> appendedComments;
        private final BaseConstructor constructor;
        private final Representer representer;
        private final DumperOptions options;
        private final Resolver resolver;

        protected YamlProperties(Builder<?> builder) {
            super(builder);
            this.prependedComments = builder.prependedComments;
            this.appendedComments = builder.appendedComments;
            this.constructor = builder.constructor;
            this.representer = builder.representer;
            this.options = builder.options;
            this.resolver = builder.resolver;
        }

        public static Builder<?> builder() {
            return new Builder() {
                @Override
                protected Builder<?> getThis() {
                    return this;
                }
            };
        }

        public final List<String> getPrependedComments() {
            return prependedComments;
        }

        public final List<String> getAppendedComments() {
            return appendedComments;
        }

        public final BaseConstructor getConstructor() {
            return constructor;
        }

        public final Representer getRepresenter() {
            return representer;
        }

        public final DumperOptions getOptions() {
            return options;
        }

        public final Resolver getResolver() {
            return resolver;
        }

        public static abstract class Builder<B extends Builder<B>>
                extends Properties.Builder<B> {
            private List<String> prependedComments = Collections.emptyList();
            private List<String> appendedComments = Collections.emptyList();
            private BaseConstructor constructor = new Constructor();
            private Representer representer = new Representer();
            private DumperOptions options = new DumperOptions();
            private Resolver resolver = new Resolver();

            protected Builder() {
                options.setIndent(2);
                options.setDefaultFlowStyle(FlowStyle.BLOCK);
            }

            /**
             * Sets the comments prepended to a configuration.
             *
             * @param prependedComments List of comments that are prepended
             * @return this {@code Builder}
             * @throws NullPointerException if {@code prependedComments ist null}
             */
            public final B setPrependedComments(List<String> prependedComments) {
                this.prependedComments = Objects.requireNonNull(prependedComments);
                return getThis();
            }

            /**
             * Sets the comments appended to a configuration.
             *
             * @param appendedComments List of comments that are appended
             * @return this {@code Builder}
             * @throws NullPointerException if {@code appendedComments ist null}
             */
            public final B setAppendedComments(List<String> appendedComments) {
                this.appendedComments = Objects.requireNonNull(appendedComments);
                return getThis();
            }

            /**
             * Sets the {@link BaseConstructor} used by the underlying YAML-parser.
             *
             * @param constructor {@code BaseConstructor} used by YAML-parser.
             * @return this {@code Builder}
             * @throws NullPointerException if {@code constructor ist null}
             * @see <a href="https://bitbucket.org/asomov/snakeyaml/wiki/Documentation">snakeyaml-Documentation</a>
             */
            public final B setConstructor(BaseConstructor constructor) {
                this.constructor = Objects.requireNonNull(constructor);
                return getThis();
            }

            /**
             * Sets the {@link Representer} used by the underlying YAML-parser.
             *
             * @param representer {@code Representer} used by YAML-parser.
             * @return this {@code Builder}
             * @throws NullPointerException if {@code representer ist null}
             * @see <a href="https://bitbucket.org/asomov/snakeyaml/wiki/Documentation">snakeyaml-Documentation</a>
             */
            public final B setRepresenter(Representer representer) {
                this.representer = Objects.requireNonNull(representer);
                return getThis();
            }

            /**
             * Sets the {@link DumperOptions} used by the underlying YAML-parser.
             *
             * @param options {@code DumperOptions} used by YAML-parser.
             * @return this {@code Builder}
             * @throws NullPointerException if {@code options ist null}
             * @see <a href="https://bitbucket.org/asomov/snakeyaml/wiki/Documentation">snakeyaml-Documentation</a>
             */
            public final B setOptions(DumperOptions options) {
                this.options = Objects.requireNonNull(options);
                return getThis();
            }

            /**
             * Sets the {@link Resolver} used by the underlying YAML-parser.
             *
             * @param resolver {@code Resolver} used by YAML-parser.
             * @return this {@code Builder}
             * @throws NullPointerException if {@code resolver ist null}
             * @see <a href="https://bitbucket.org/asomov/snakeyaml/wiki/Documentation">snakeyaml-Documentation</a>
             */
            public final B setResolver(Resolver resolver) {
                this.resolver = Objects.requireNonNull(resolver);
                return getThis();
            }

            /**
             * Builds a new {@code YamlProperties} instance using the values set.
             *
             * @return new {@code YamlProperties} instance
             */
            public YamlProperties build() {
                return new YamlProperties(this);
            }
        }
    }
}
