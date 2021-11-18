package io.fairyproject.bukkit.protocol;

import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import io.fairyproject.bukkit.reflection.wrapper.FieldWrapper;
import io.fairyproject.bukkit.reflection.wrapper.ObjectWrapper;
import io.fairyproject.mc.protocol.netty.NettyInjector;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.bukkit.plugin.PluginDescriptionFile;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class BukkitNettyInjector implements NettyInjector {

    private ChannelFuture channelFuture;

    @Override
    public String getEncoderName() {
        return "encoder";
    }

    @Override
    public String getDecoderName() {
        return "decoder";
    }

    @Override
    public void inject() throws Exception {
        Object connection = getServerConnection();
        if (connection == null) {
            throw new IllegalStateException("Unable to find ServerConnection. please create an issue on our GitHub.");
        }

        final FieldResolver fieldResolver = new FieldResolver(connection.getClass());
        final FieldWrapper<List> fieldWrapper = fieldResolver.resolveWithGenericType(List.class, ChannelFuture.class);

        try {
            this.channelFuture = (ChannelFuture) fieldWrapper.get(connection).get(0);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalStateException("Failed to inject. Bootstrap connection future doesn't exists!");
        }

        ChannelHandler channelHandler = null;
        FieldWrapper<ChannelInitializer<?>> field = null;
        for (Map.Entry<String, ChannelHandler> entry : this.channelFuture.channel().pipeline()) {
            field = new FieldResolver(entry.getValue().getClass()).resolveWrapper("childHandler");
            if (!field.exists()) {
                continue;
            }
            channelHandler = entry.getValue();
        }

        if (channelHandler == null) {
            channelHandler = channelFuture.channel().pipeline().first();
        }
        try {
            ChannelInitializer<SocketChannel> oldInit = (ChannelInitializer<SocketChannel>) field.get(channelHandler);
            ChannelInitializer newInit = new BukkitMCChannelInitializer(oldInit);

            field.set(channelHandler, newInit);
        } catch (Exception e) {
            // let's find who to blame!
            ClassLoader cl = channelHandler.getClass().getClassLoader();
            Class<?> pluginClassLoader = Class.forName("org.bukkit.plugin.java.PluginClassLoader");
            if (cl.getClass() == pluginClassLoader) {
                PluginDescriptionFile yaml = new ObjectWrapper(pluginClassLoader).getFieldByFirstType(PluginDescriptionFile.class);
                throw new Exception("Unable to inject, due to " + channelHandler.getClass().getName() + ", try without the plugin " + yaml.getName() + "?");
            } else {
                throw new Exception("Unable to find core component 'childHandler', please check your plugins. issue: " + channelHandler.getClass().getName());
            }
        }
    }

    public static Object getServerConnection() throws Exception {
        Class<?> serverClazz = new NMSClassResolver().resolve("MinecraftServer");
        final MethodResolver methodResolver = new MethodResolver(serverClazz);
        Object server = methodResolver.resolve(serverClazz, 0).invoke(null);
        Object connection = null;
        for (Method m : serverClazz.getDeclaredMethods()) {
            if (m.getReturnType().getSimpleName().equals("ServerConnection")) {
                if (m.getParameterTypes().length == 0) {
                    connection = m.invoke(server);
                }
            }
        }
        return connection;
    }
}
