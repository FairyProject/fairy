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

package org.fairy.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.UuidRepresentation;
import org.fairy.AbstractRepositoryProvider;
import org.fairy.MongoRepository;
import org.fairy.Repository;
import org.fairy.util.Utility;
import org.mongojack.JacksonMongoCollection;

import java.io.Serializable;

public abstract class AbstractMongoRepositoryProvider extends AbstractRepositoryProvider {

    private MongoClient client;
    private MongoDatabase database;

    public AbstractMongoRepositoryProvider(String id) {
        super(id);
    }

    @Override
    public void build0() {
        this.getIOLock().lock();
        if (this.client != null) {
            Utility.sneaky(this::close);
        }
        MongoClientSettings clientSettings = this.mongoClientSettings();
        this.client = MongoClients.create(clientSettings);
        this.database = this.client.getDatabase(this.database());
        this.getIOLock().unlock();
    }

    public abstract String database();

    @Override
    public <E, ID extends Serializable> Repository<E, ID> createRepository(Class<E> entityType, String repoId) {
        return new MongoRepository(this, entityType, repoId);
    }

    public <E> JacksonMongoCollection<E> createCollection(ObjectMapper objectMapper, String name, Class<E> entityType) {
        return JacksonMongoCollection.builder()
                .withObjectMapper(objectMapper)
                .build(this.database, name, entityType, UuidRepresentation.JAVA_LEGACY);
    }

    public MongoClientSettings mongoClientSettings() {
        MongoClientSettings.Builder builder = MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.JAVA_LEGACY);
        this.setupClientSettings(builder);
        return builder.build();
    }

    protected void setupClientSettings(MongoClientSettings.Builder builder) {

    }

    @Override
    public void close() throws Exception {
        this.client.close();
    }
}
