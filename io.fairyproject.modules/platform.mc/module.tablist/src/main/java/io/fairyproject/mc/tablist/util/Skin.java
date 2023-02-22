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

package io.fairyproject.mc.tablist.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.fairyproject.mc.MCGameProfile;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.util.Property;
import io.fairyproject.util.exceptionally.ThrowingSupplier;
import io.fairyproject.util.thread.ServerThreadLock;
import lombok.Setter;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Setter
public class Skin {

    private static final Map<UUID, Skin> SKIN_CACHE = new ConcurrentHashMap<>();
    public static Skin GRAY = new Skin(
            "eyJ0aW1lc3RhbXAiOjE0MTEyNjg3OTI3NjUsInByb2ZpbGVJZCI6IjNmYmVjN2RkMGE1ZjQwYmY5ZDExODg1YTU0NTA3MTEyIiwicHJvZmlsZU5hbWUiOiJsYXN0X3VzZXJuYW1lIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg0N2I1Mjc5OTg0NjUxNTRhZDZjMjM4YTFlM2MyZGQzZTMyOTY1MzUyZTNhNjRmMzZlMTZhOTQwNWFiOCJ9fX0=",
            "u8sG8tlbmiekrfAdQjy4nXIcCfNdnUZzXSx9BE1X5K27NiUvE1dDNIeBBSPdZzQG1kHGijuokuHPdNi/KXHZkQM7OJ4aCu5JiUoOY28uz3wZhW4D+KG3dH4ei5ww2KwvjcqVL7LFKfr/ONU5Hvi7MIIty1eKpoGDYpWj3WjnbN4ye5Zo88I2ZEkP1wBw2eDDN4P3YEDYTumQndcbXFPuRRTntoGdZq3N5EBKfDZxlw4L3pgkcSLU5rWkd5UH4ZUOHAP/VaJ04mpFLsFXzzdU4xNZ5fthCwxwVBNLtHRWO26k/qcVBzvEXtKGFJmxfLGCzXScET/OjUBak/JEkkRG2m+kpmBMgFRNtjyZgQ1w08U6HHnLTiAiio3JswPlW5v56pGWRHQT5XWSkfnrXDalxtSmPnB5LmacpIImKgL8V9wLnWvBzI7SHjlyQbbgd+kUOkLlu7+717ySDEJwsFJekfuR6N/rpcYgNZYrxDwe4w57uDPlwNL6cJPfNUHV7WEbIU1pMgxsxaXe8WSvV87qLsR7H06xocl2C0JFfe2jZR4Zh3k9xzEnfCeFKBgGb4lrOWBu1eDWYgtKV67M2Y+B3W5pjuAjwAxn0waODtEn/3jKPbc/sxbPvljUCw65X+ok0UUN1eOwXV5l2EGzn05t3Yhwq19/GxARg63ISGE8CKw="
    );

    public String skinValue;
    public String skinSignature;

    public Skin(String skinValue, String skinSig) {
        this.skinValue = skinValue;
        this.skinSignature = skinSig;
    }

    @Override
    public String toString() {
        return skinSignature + skinValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Skin skin = (Skin) o;

        if (!Objects.equals(skinValue, skin.skinValue)) return false;
        return Objects.equals(skinSignature, skin.skinSignature);
    }

    @Override
    public int hashCode() {
        int result = skinValue != null ? skinValue.hashCode() : 0;
        result = 31 * result + (skinSignature != null ? skinSignature.hashCode() : 0);
        return result;
    }

    public static Skin fromPlayer(MCPlayer player) {
        Skin skin = null;
        skin = SKIN_CACHE.computeIfAbsent(player.getUUID(), Skin::load);

        return skin == null ? Skin.GRAY : skin;
    }

    public static Skin download(UUID uuid) throws Exception {
        URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
        InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
        JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
        String texture = textureProperty.get("value").getAsString();
        String signature = textureProperty.get("signature").getAsString();

        return new Skin(texture, signature);
    }

    public static Skin load(UUID key) {
        MCPlayer player = MCPlayer.find(key);
        if (player != null) {
            try (ServerThreadLock ignored = ServerThreadLock.obtain()) {
                final MCGameProfile gameProfile = player.getGameProfile();
                if (!gameProfile.hasProperty("textures")) {
                    Property property = gameProfile.getProperties().stream()
                            .filter(p -> p.getName().equals("textures"))
                            .findFirst().orElse(null);
                    if (property == null) {
                        // Offline player I suppose
                        return Skin.GRAY;
                    }
                    String texture = property.getValue();
                    String signature = property.getSignature();

                    return new Skin(texture, signature);
                }
            }
        }

        return ThrowingSupplier.sneaky(() -> Skin.download(key)).get();
    }
}
