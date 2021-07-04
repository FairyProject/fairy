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

package org.fairy.bukkit.packet.wrapper;

import org.bukkit.inventory.ItemStack;
import org.fairy.bukkit.reflection.wrapper.ChatComponentWrapper;
import org.fairy.bukkit.reflection.wrapper.GameProfileWrapper;

import java.util.List;

public interface WrapperPacketReader {

    boolean readBoolean(int index);

    byte readByte(int index);

    short readShort(int index);

    int readInt(int index);

    long readLong(int index);

    float readFloat(int index);

    double readDouble(int index);

    ItemStack readItemStack(int index);

    ChatComponentWrapper readChatComponent(int index);

    GameProfileWrapper readGameProfile(int index);

    <T> List<T> readList(int index);

    <T> T readObject(int index, Class<T> type);

    Object readAnyObject(int index);

    String readString(int index);
}
