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

package org.fairy.redis.server;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Getter;
import lombok.Setter;
import org.fairy.bean.Beans;
import org.fairy.redis.server.enums.ServerState;
import org.fairy.util.JsonChain;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@JsonSerialize(using = ImanityServer.Serializer.class)
@JsonDeserialize(using = ImanityServer.Deserializer.class)
public class ImanityServer {

    private String name;
    private int onlinePlayers;
    private int maxPlayers;
    private ServerState serverState;

    private final Map<String, String> metadata = new HashMap<>();

    public ImanityServer(String name) {
        this.name = name;
    }

    public void load(Map<String, Object> data) {
        Object object = data.get("onlinePlayers");
        if (object instanceof Integer) {
            this.onlinePlayers = (int) object;
        }
        object = data.get("maxPlayers");
        if (object instanceof Integer) {
            this.maxPlayers = (int) object;
        }
        object = data.get("state");
        if (object instanceof String) {
            this.serverState = ServerState.valueOf(((String) object).toUpperCase());
        }

        object = data.get("metadata");
        if (object instanceof Map) {
            this.metadata.clear();
            this.metadata.putAll((Map<? extends String, ? extends String>) object);
        }
    }

    public int getInt(String key) {
        try {
            return Integer.parseInt(metadata.get(key));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public double getDouble(String key) {
        try {
            return Double.parseDouble(metadata.get(key));
        } catch (NumberFormatException ex) {
            return -1D;
        }
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(metadata.get(key));
    }

    public String getString(String key) {
        return metadata.get(key);
    }

    public JsonChain json() {
        return new JsonChain()
                .addProperty("serverName", this.name);
    }

    public static class Serializer extends StdSerializer<ImanityServer> {

        protected Serializer() {
            super(ImanityServer.class);
        }

        @Override
        public void serialize(ImanityServer imanityServer, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(imanityServer.getName());
        }
    }

    public static class Deserializer extends StdDeserializer<ImanityServer> {

        private ServerHandler serverHandler;

        protected Deserializer() {
            super(ImanityServer.class);
        }

        @Override
        public ImanityServer deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            if (serverHandler == null) {
                serverHandler = Beans.get(ServerHandler.class);
            }
            return serverHandler.getServer(jsonParser.getValueAsString());
        }
    }

}
