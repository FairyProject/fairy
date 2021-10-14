package io.fairyproject.mc.protocol.netty;

public interface NettyInjector {

    String getEncoderName();

    String getDecoderName();

    void inject() throws Exception;

}
