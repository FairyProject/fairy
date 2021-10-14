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

package io.fairyproject.bukkit.packet.wrapper.client;

import com.google.common.collect.ImmutableMap;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.reflection.minecraft.MinecraftVersion;
import lombok.Getter;
import io.fairyproject.bukkit.packet.PacketDirection;
import io.fairyproject.bukkit.packet.type.PacketType;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.WrappedPacket;
import io.fairyproject.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;

import java.util.Map;

@AutowiredWrappedPacket(value = PacketType.Client.SETTINGS, direction = PacketDirection.READ)
@Getter
public class WrappedPacketInSettings extends WrappedPacket {
    private static Class<?> packetClass;
    private static Class<?> chatVisibilityEnumClass;

    private static boolean isLowerThan_v_1_8;
    private String locale;
    private int viewDistance;
    private ChatVisibility chatVisibility;
    private boolean chatColors;
    private Map<DisplayedSkinPart, Boolean> displayedSkinParts;

    public WrappedPacketInSettings(final Object packet) {
        super(packet);
    }

    public static void init() {
        packetClass = PacketTypeClasses.Client.SETTINGS;

        isLowerThan_v_1_8 = MinecraftVersion.getVersion().olderThan(MinecraftReflection.Version.v1_8_R1);

        try {
            chatVisibilityEnumClass = NMS_CLASS_RESOLVER.resolve("EnumChatVisibility");
        } catch (ClassNotFoundException e) {
            Class<?> entityHumanClass = NMS_CLASS_RESOLVER.resolveSilent("EntityHuman");
            //They are just on an outdated version
            assert entityHumanClass != null;
            chatVisibilityEnumClass = NMS_CLASS_RESOLVER.resolveSilent(entityHumanClass.getSimpleName() + "$EnumChatVisibility");
        }
    }

    public static Class<?> getChatVisibilityEnumClass() {
        return chatVisibilityEnumClass;
    }

    @Override
    protected void setup() {
        try {
            //LOCALE
            this.locale = readString(0);
            //VIEW DISTANCE
            this.viewDistance = readInt(0);

            //CHAT VISIBILITY
            Object chatVisibilityEnumObject = readObject(0, chatVisibilityEnumClass);
            String enumValueAsString = chatVisibilityEnumObject.toString();
            if (enumValueAsString.equals("FULL")) {
                chatVisibility = ChatVisibility.ENABLED;
            } else if (enumValueAsString.equals("SYSTEM")) {
                chatVisibility = ChatVisibility.COMMANDS_ONLY;
            } else {
                chatVisibility = ChatVisibility.HIDDEN;
            }

            //CHAT COLORS
            this.chatColors = readBoolean(0);

            //DISPLAYED SKIN PARTS
            ImmutableMap.Builder<DisplayedSkinPart, Boolean> builder = ImmutableMap.builder();

            if (isLowerThan_v_1_8) {
                //in 1.7.10 only the cape display skin part is sent
                boolean capeEnabled = readBoolean(1);
                builder.put(DisplayedSkinPart.CAPE, capeEnabled);
            } else {
                //in 1.8, all the skin parts are sent
                int skinPartFlags = readInt(1);
                builder.put(DisplayedSkinPart.CAPE, (skinPartFlags & 0x01) != 0);
                builder.put(DisplayedSkinPart.JACKET, (skinPartFlags & 0x02) != 0);
                builder.put(DisplayedSkinPart.LEFT_SLEEVE, (skinPartFlags & 0x04) != 0);
                builder.put(DisplayedSkinPart.RIGHT_SLEEVE, (skinPartFlags & 0x08) != 0);
                builder.put(DisplayedSkinPart.LEFT_PANTS, (skinPartFlags & 0x10) != 0);
                builder.put(DisplayedSkinPart.RIGHT_PANTS, (skinPartFlags & 0x20) != 0);
                builder.put(DisplayedSkinPart.HAT, (skinPartFlags & 0x40) != 0);
            }

            this.displayedSkinParts = builder.build();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Is the skin part enabled.
     * <p>
     * On 1.7.10, some skin parts will default to 'false' as 1.7.10
     * only sends the 'cape' skin part.
     *
     * @param part The skin part to check the status of.
     * @return Is the skin part enabled
     */
    public boolean isDisplaySkinPartEnabled(DisplayedSkinPart part) {
        //1.7.10, we will default the other skin parts to return false.
        if (!displayedSkinParts.containsKey(part)) {
            return false;
        }
        return displayedSkinParts.get(part);
    }

    /**
     * Enum for the client chat visibility setting
     */
    public enum ChatVisibility {
        ENABLED, COMMANDS_ONLY, HIDDEN
    }

    /**
     * Enum for the client displayed skin parts settings
     */
    public enum DisplayedSkinPart {
        CAPE, JACKET, LEFT_SLEEVE, RIGHT_SLEEVE, LEFT_PANTS, RIGHT_PANTS, HAT
    }
}
