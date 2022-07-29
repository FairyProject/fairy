package io.fairyproject.mc.map.framebuffers;

import io.fairyproject.mc.map.Framebuffer;
import io.fairyproject.util.ConditionUtils;

/**
 * {@link Framebuffer} with direct access to the colors array
 */
public class DirectFramebuffer extends BaseFramebuffer {

    private final byte[] colors = new byte[WIDTH * HEIGHT];

    /**
     * Mutable colors array
     *
     * @return
     */
    public byte[] getColors() {
        return colors;
    }

    public byte get(int x, int z) {
        return colors[Framebuffer.index(x, z)];
    }

    public DirectFramebuffer set(int x, int z, byte color) {
        ConditionUtils.is(x < WIDTH, "x " + x + " is not supposed to be higher than " + WIDTH);
        ConditionUtils.is(z < WIDTH, "z " + z + " is not supposed to be higher than " + HEIGHT);
        colors[Framebuffer.index(x, z)] = color;
        return this;
    }

    @Override
    public byte[] toMapColors() {
        return colors;
    }
}
