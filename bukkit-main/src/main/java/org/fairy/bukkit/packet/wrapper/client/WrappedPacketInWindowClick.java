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

package org.fairy.bukkit.packet.wrapper.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.fairy.bukkit.packet.PacketDirection;
import org.fairy.bukkit.packet.type.PacketType;
import org.fairy.bukkit.packet.type.PacketTypeClasses;
import org.fairy.bukkit.packet.wrapper.WrappedPacket;
import org.fairy.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.fairy.bukkit.reflection.resolver.FieldResolver;

import java.util.*;

@Getter
@AutowiredWrappedPacket(value = PacketType.Client.WINDOW_CLICK, direction = PacketDirection.READ)
public class WrappedPacketInWindowClick extends WrappedPacket {

    private static Map<String, Integer> INVENTORY_CLICK_TYPE_CACHE;
    private static Map<Integer, List<WindowClickType>> WINDOW_CLICK_TYPE_CACHE;
    private static Class<?> packetClass, invClickTypeClass;
    private static boolean CLICK_MODE_PRIMITIVE = false;
    private int id;
    private int slot;
    private int button;
    private short actionNumber;
    private int mode;
    private ItemStack clickedItem;

    public WrappedPacketInWindowClick(Object packet) {
        super(packet);
    }

    public static void init() {
        packetClass = PacketTypeClasses.Client.WINDOW_CLICK;
        invClickTypeClass = NMS_CLASS_RESOLVER.resolveSilent("InventoryClickType");

        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
        builder.put("PICKUP", 0);
        builder.put("QUICK_MOVE", 1);
        builder.put("SWAP", 2);
        builder.put("CLONE", 3);
        builder.put("THROW", 4);
        builder.put("QUICK_CRAFT", 5);
        builder.put("PICKUP_ALL", 6);
        INVENTORY_CLICK_TYPE_CACHE = builder.build();

        ImmutableMap.Builder<Integer, List<WindowClickType>> windowCacheBuilder = ImmutableMap.builder();

        //MODE 0
        windowCacheBuilder.put(0, ImmutableList.of(
                WindowClickType.LEFT_MOUSE_CLICK,
                WindowClickType.RIGHT_MOUSE_CLICK
        ));

        //MODE 1
        windowCacheBuilder.put(1, ImmutableList.of(
                WindowClickType.SHIFT_LEFT_MOUSE_CLICK,
                WindowClickType.SHIFT_RIGHT_MOUSE_CLICK
        ));

        //MODE 2
        windowCacheBuilder.put(2, ImmutableList.of(
                WindowClickType.KEY_NUMBER1,
                WindowClickType.KEY_NUMBER2,
                WindowClickType.KEY_NUMBER3,
                WindowClickType.KEY_NUMBER4,
                WindowClickType.KEY_NUMBER5,
                WindowClickType.KEY_NUMBER6,
                WindowClickType.KEY_NUMBER7,
                WindowClickType.KEY_NUMBER8,
                WindowClickType.KEY_NUMBER9
        ));

        //MODE 3
        windowCacheBuilder.put(3, ImmutableList.of(
                WindowClickType.UNKNOWN,
                WindowClickType.UNKNOWN,
                WindowClickType.CREATIVE_MIDDLE_CLICK
        ));

        //MODE 4
        windowCacheBuilder.put(4, ImmutableList.of(
                WindowClickType.KEY_DROP,
                WindowClickType.KEY_DROP_STACK
        ));

        //MODE 5
        windowCacheBuilder.put(5, ImmutableList.of(
                WindowClickType.STARTING_LEFT_MOUSE_DRAG,
                WindowClickType.ADD_SLOT_LEFT_MOUSE_DRAG,
                WindowClickType.ENDING_LEFT_MOUSE_DRAG,
                WindowClickType.UNKNOWN,
                WindowClickType.STARTING_RIGHT_MOUSE_DRAG,
                WindowClickType.ADD_SLOT_RIGHT_MOUSE_DRAG,
                WindowClickType.CREATIVE_STARTING_MIDDLE_MOUSE_DRAG,
                WindowClickType.ADD_SLOT_MIDDLE_MOUSE_DRAG,
                WindowClickType.ENDING_MIDDLE_MOUSE_DRAG
        ));

        windowCacheBuilder.put(6, ImmutableList.of(WindowClickType.DOUBLE_CLICK));

        WINDOW_CLICK_TYPE_CACHE = windowCacheBuilder.build();

        CLICK_MODE_PRIMITIVE = new FieldResolver(packetClass)
            .resolveSilent(int.class, 3)
            .exists();
    }

    @Override
    protected void setup() {
        this.id = readInt(0);
        this.slot = readInt(1);
        this.button = readInt(2);
        this.actionNumber = readShort(0);
        this.clickedItem = readItemStack(0);
        Object clickMode = readAnyObject(5);

        if (CLICK_MODE_PRIMITIVE) {
            mode = (int) clickMode;
        } else {
            mode = INVENTORY_CLICK_TYPE_CACHE.get(clickMode.toString());
        }
    }

    /**
     * Get the window click type.
     * @return Get Window Click Type
     */
    public WindowClickType getWindowClickType() {
        if (WINDOW_CLICK_TYPE_CACHE.get(mode) == null) {
            return WindowClickType.UNKNOWN;
        }
        if (button + 1 > WINDOW_CLICK_TYPE_CACHE.size()) {
            return WindowClickType.UNKNOWN;
        }

        if (mode == 4) {
            if (slot == -999) {
                if (button == 0) {
                    return WindowClickType.LEFT_CLICK_OUTSIDE_WINDOW_HOLDING_NOTHING;
                } else if (button == 1) {
                    return WindowClickType.RIGHT_CLICK_OUTSIDE_WINDOW_HOLDING_NOTHING;
                }
            }
        }
        return WINDOW_CLICK_TYPE_CACHE.get(mode).get(button);
    }

    public enum WindowClickType {
        LEFT_MOUSE_CLICK, RIGHT_MOUSE_CLICK,
        SHIFT_LEFT_MOUSE_CLICK, SHIFT_RIGHT_MOUSE_CLICK,

        CREATIVE_MIDDLE_CLICK, CREATIVE_STARTING_MIDDLE_MOUSE_DRAG,

        KEY_NUMBER1, KEY_NUMBER2, KEY_NUMBER3, KEY_NUMBER4,
        KEY_NUMBER5, KEY_NUMBER6, KEY_NUMBER7, KEY_NUMBER8,
        KEY_NUMBER9, KEY_DROP, KEY_DROP_STACK,

        LEFT_CLICK_OUTSIDE_WINDOW_HOLDING_NOTHING,
        RIGHT_CLICK_OUTSIDE_WINDOW_HOLDING_NOTHING,

        STARTING_LEFT_MOUSE_DRAG,
        STARTING_RIGHT_MOUSE_DRAG,

        ADD_SLOT_LEFT_MOUSE_DRAG,
        ADD_SLOT_RIGHT_MOUSE_DRAG,
        ADD_SLOT_MIDDLE_MOUSE_DRAG,

        ENDING_LEFT_MOUSE_DRAG,
        ENDING_RIGHT_MOUSE_DRAG,
        ENDING_MIDDLE_MOUSE_DRAG,

        DOUBLE_CLICK,

        UNKNOWN
    }
}
