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

package io.fairyproject.redis.server.thread;

import io.fairyproject.Fairy;
import io.fairyproject.redis.server.ImanityServer;
import io.fairyproject.redis.server.ServerHandler;

import java.util.Map;

public class FetchThread extends Thread {

    private final ServerHandler serverHandler;

    public FetchThread(ServerHandler serverHandler) {
        super();

        this.serverHandler = serverHandler;

        this.setName("Imanity Server Fetch Thread");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        while (Fairy.isRunning()) {
            try {
                this.fetch();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            try {
                Thread.sleep(5000L);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private void fetch() {
        for (String key : this.serverHandler.getRedis().getKeys(ServerHandler.METADATA + ":*")) {
            String name = key.substring(0, ServerHandler.METADATA.length());
            ImanityServer server = this.serverHandler.getServer(name);
            if (server == null) {
                server = new ImanityServer(name);
                this.serverHandler.addServer(name, server);
            }

            Map<String, Object> data = this.serverHandler.getRedis().getMap(key);
            server.load(data);
        }
    }
}
