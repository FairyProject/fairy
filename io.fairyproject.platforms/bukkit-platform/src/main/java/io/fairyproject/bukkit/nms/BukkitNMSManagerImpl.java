/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.bukkit.nms;

import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import io.fairyproject.bukkit.reflection.resolver.minecraft.OBCClassResolver;
import io.fairyproject.bukkit.version.OBCVersionDecoder;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.version.MCVersionMapping;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import lombok.Getter;

@Getter
public class BukkitNMSManagerImpl implements BukkitNMSManager {

    private final Class<?> serverClass;
    private final MCServer mcServer;
    private final MCVersionMappingRegistry versionMappingRegistry;
    private NMSClassResolver nmsClassResolver;
    private OBCClassResolver obcClassResolver;

    public BukkitNMSManagerImpl(MCServer mcServer, MCVersionMappingRegistry versionMappingRegistry, Class<?> serverClass) {
        this.serverClass = serverClass;
        this.mcServer = mcServer;
        this.versionMappingRegistry = versionMappingRegistry;
    }

    @PreInitialize
    public void onPreInitialize() {
        this.nmsClassResolver = setupNmsClassResolver();
        this.obcClassResolver = setupObcClassResolver();
    }

    private OBCClassResolver setupObcClassResolver() {
        return new OBCClassResolver(serverClass.getPackage().getName() + ".");
    }

    private NMSClassResolver setupNmsClassResolver() {
        MCVersionMapping mapping = this.versionMappingRegistry.findMapping(this.mcServer.getVersion());
        if (mapping.isNmsPrefix()) {
            String versionFormat = OBCVersionDecoder.create().decode(serverClass);

            return new NMSClassResolver("net.minecraft.server." + versionFormat + ".");
        } else {
            return new NMSClassResolver("net.minecraft.");
        }
    }

}