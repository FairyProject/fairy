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

package io.fairyproject.redis.message;

import io.fairyproject.container.*;
import io.fairyproject.redis.RedisService;
import io.fairyproject.redis.message.annotation.HandleMessage;
import io.fairyproject.redis.server.ServerHandler;
import io.fairyproject.redis.subscription.RedisPubSub;
import io.fairyproject.util.AccessUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(name = "messageService")
@ServiceDependency(value = ServerHandler.class, type = ServiceDependencyType.SUB_DISABLE)
public class MessageService {

    private RedisPubSub<Object> redisPubSub;
    private String channel;

    private Map<Class<?>, List<MessageListenerData>> messageListeners;

    @Autowired
    private RedisService redisService;

    @PreInitialize
    public void preInit() {
        this.messageListeners = new ConcurrentHashMap<>(12);

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {

            @Override
            public Object newInstance(Class<?> type) {
                Object instance = super.newInstance(type);
                registerListener((MessageListener) instance);

                return instance;
            }

            @Override
            public Class<?>[] type() {
                return new Class[] {MessageListener.class};
            }

        });
    }

    @PostInitialize
    public void init() {
        this.channel = "imanity-server";

        this.redisPubSub = new RedisPubSub<>(this.channel, this.redisService, Object.class);
        this.redisPubSub.subscribe((message -> {
            List<MessageListenerData> listeners = this.messageListeners.get(message.getClass());
            if (listeners == null) {
                return;
            }
            for (MessageListenerData data : listeners) {
                try {
                    data.getMethod().invoke(data.getInstance(), message);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }));
    }

    public void sendMessage(Object message) {
        try {
            if (message == null) {
                throw new IllegalStateException("The Message given a null serialized data!");
            }

            Class<?> type = message.getClass();
            if (!this.isAnnotated(type)) {
                throw new IllegalArgumentException("The Message " + message.getClass() + " does not have @Message Annotation!");
            }

            this.redisPubSub.publish(message);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public boolean isAnnotated(Class<?> messageClass) {
        while (messageClass != null && messageClass != Object.class) {
            if (messageClass.getAnnotation(Message.class) != null) {
                return true;
            }

            messageClass = messageClass.getSuperclass();
        }

        return false;
    }

    public void registerListener(MessageListener messageListener) {
        Method[] methods = messageListener.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.getDeclaredAnnotation(HandleMessage.class) == null) {
                continue;
            }
            if (method.getParameters().length != 1) {
                continue;
            }
            Class<?> messageClass = method.getParameterTypes()[0];

            List<MessageListenerData> listeners;
            if (this.messageListeners.containsKey(messageClass)) {
                listeners = this.messageListeners.get(messageClass);
            } else {
                listeners = new ArrayList<>();
                this.messageListeners.put(messageClass, listeners);
            }

            try {
                AccessUtil.setAccessible(method);
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException(e);
            }
            listeners.add(new MessageListenerData(messageListener, method, messageClass));
        }
    }
}
