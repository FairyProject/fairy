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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.fairy.mongo.AbstractMongoRepositoryProvider;
import org.mongojack.JacksonMongoCollection;

import java.util.Map;

public class MongoRepositoryProvider extends AbstractMongoRepositoryProvider {

    private String connectionString, database, connectionPrefix;

    public MongoRepositoryProvider(String id) {
        super(id);
    }

    @Override
    public Map<String, String> getDefaultOptions() {
        return ImmutableMap.of(
                "connectionString", "mongodb://localhost:27017",
                "database", "database",
                "connectionPrefix", "imanity_"
        );
    }

    @Override
    public void registerOptions(Map<String, String> map) {
        this.connectionString = map.get("connectionString");
        this.database = map.get("database");
        this.connectionPrefix = map.get("connectionPrefix");
    }

    @Override
    protected void setupClientSettings(MongoClientSettings.Builder builder) {
        builder.applyConnectionString(new ConnectionString(this.connectionString));
    }

    @Override
    public <E> JacksonMongoCollection<E> createCollection(ObjectMapper objectMapper, String name, Class<E> entityType) {
        return super.createCollection(objectMapper, this.connectionPrefix + name, entityType);
    }

    @Override
    public String database() {
        return this.database;
    }
}
