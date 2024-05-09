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

package io.fairyproject.config;

import io.fairyproject.config.filter.FieldFilter;
import io.fairyproject.config.filter.FieldFilters;
import io.fairyproject.config.format.FieldNameFormatter;
import io.fairyproject.config.format.FieldNameFormatters;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Parent class of all configurations.
 * <p>
 * This class contains the most basic methods that every configuration needs.
 *
 * @param <C> type of the configuration
 */
public abstract class Configuration<C extends Configuration<C>> {
    /**
     * {@code Comments} object containing all class and field comments
     * of this configuration
     */
    protected final Comments comments;
    private final Properties props;

    /**
     * Constructs a new {@code Configuration} object.
     *
     * @param properties {@code Properties} used to configure this configuration
     * @throws NullPointerException if {@code properties} is null
     */
    protected Configuration(Properties properties) {
        this.props = Objects.requireNonNull(properties);
        this.comments = Comments.ofClass(getClass());
    }

    /**
     * Load all entries from the map
     *
     * @return entries
     * @throws IOException IO Error
     */
    public final Map<String, Object> loadEntries() throws IOException {
        return getSource().loadConfiguration(getThis());
    }

    /**
     * Saves this {@code Configuration}.
     *
     * @throws ConfigurationException      if any field is not properly configured
     * @throws ConfigurationStoreException if an I/O error occurred while loading
     *                                     this configuration
     */
    public final void save() {
        try {
            preSave();
            FieldMapper.MappingInfo mappingInfo = FieldMapper.MappingInfo.from(this);
            Map<String, Object> map = FieldMapper
                    .instanceToMap(this, mappingInfo);
            getSource().saveConfiguration(getThis(), map);
        } catch (IOException e) {
            throw new ConfigurationStoreException(e);
        }
    }

    /**
     * Loads this {@code Configuration}.
     *
     * @throws ConfigurationException      if values cannot be converted back to their
     *                                     original representation
     * @throws ConfigurationStoreException if an I/O error occurred while loading
     *                                     this configuration
     */
    public final void load() {
        try {
            Map<String, Object> map = this.loadEntries();
            FieldMapper.MappingInfo mappingInfo = FieldMapper.MappingInfo.from(this);
            FieldMapper.instanceFromMap(this, map, mappingInfo);
            postLoad();
        } catch (IOException e) {
            throw new ConfigurationStoreException(e);
        }
    }

    /**
     * Returns the {@link ConfigurationSource} used for saving and loading this
     * {@code Configuration}.
     *
     * @return {@code ConfigurationSource} used for saving and loading
     */
    protected abstract ConfigurationSource<C> getSource();

    /**
     * Returns this {@code Configuration}.
     *
     * @return this {@code Configuration}
     */
    protected abstract C getThis();

    /**
     * Hook that is executed right before this {@code Configuration} is saved.
     * <p>
     * The default implementation of this method does nothing.
     */
    protected void preSave() {
        // to be overridden
    }

    /**
     * Hook that is executed right after this {@code Configuration} has
     * successfully been loaded.
     * <p>
     * The default implementation of this method does nothing.
     */
    protected void postLoad() {
        // to be overridden
    }

    Properties getProperties() {
        return props;
    }

    /**
     * Instances of a {@code Properties} class are used to configure different
     * aspects of a configuration.
     */
    protected static class Properties {
        private final FieldNameFormatter formatter;
        private final FieldFilter filter;

        /**
         * Constructs a new {@code Properties} object.
         *
         * @param builder {@code Builder} used for construction
         * @throws NullPointerException if {@code builder} is null
         */
        protected Properties(Builder<?> builder) {
            this.formatter = builder.formatter;
            this.filter = builder.filter;
        }

        static Builder<?> builder() {
            return new Builder() {
                @Override
                protected Builder<?> getThis() {
                    return this;
                }
            };
        }

        /**
         * Returns the {@code FieldNameFormatter} of a configuration.
         *
         * @return {@code FieldNameFormatter} of a configuration
         */
        public final FieldNameFormatter getFormatter() {
            return formatter;
        }

        /**
         * Returns the {@code FieldFilter} of a configuration
         *
         * @return {@code FieldFilter} of a configuration
         */
        public final FieldFilter getFilter() {
            return filter;
        }

        /**
         * Builder classes are used for constructing {@code Properties}.
         *
         * @param <B> type of the builder
         */
        protected static abstract class Builder<B extends Builder<B>> {
            private FieldNameFormatter formatter = FieldNameFormatters.IDENTITY;
            private FieldFilter filter = FieldFilters.DEFAULT;

            protected Builder() {}

            /**
             * Returns this {@code Builder}.
             *
             * @return this {@code Builder}
             */
            protected abstract B getThis();

            /**
             * Sets the {@link FieldNameFormatter} for a configuration.
             *
             * @param formatter formatter for configuration
             * @return this {@code Builder}
             * @throws NullPointerException if {@code formatter} is null
             */
            public final B setFormatter(FieldNameFormatter formatter) {
                this.formatter = Objects.requireNonNull(formatter);
                return getThis();
            }

            /**
             * Composes the given {@link FieldFilter} with the
             * {@code FieldFilters.DEFAULT} instance and any other
             * previously added filters.
             * <p>
             * The added filter is not evaluated for a field if the field has
             * already been filtered or by some other {@code FieldFilter}.
             *
             * @param filter field filter that is added
             * @return this {@code Builder}
             * @throws NullPointerException if {@code filter} is null
             */
            public final B addFilter(FieldFilter filter) {
                this.filter = this.filter.and(filter);
                return getThis();
            }

            /**
             * Builds a new {@code Properties} instance using the values set.
             *
             * @return new {@code Properties} instance
             */
            public Properties build() {
                return new Properties(this);
            }
        }
    }
}
