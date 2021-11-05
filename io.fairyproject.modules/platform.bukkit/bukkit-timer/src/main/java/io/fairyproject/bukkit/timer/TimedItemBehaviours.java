package io.fairyproject.bukkit.timer;

import io.fairyproject.bukkit.util.items.behaviour.ItemBehaviour;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCPlayer;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

@UtilityClass
public class TimedItemBehaviours {

    public ItemBehaviour cooldown(ItemBehaviour behaviour, long defaultCooldown, Plugin plugin) {
        return cooldown(behaviour, defaultCooldown, null, null, plugin);
    }

    public ItemBehaviour cooldown(ItemBehaviour behaviour, long defaultCooldown, @Nullable Component cooldownMessage, @Nullable Component removeMessage, Plugin plugin) {
        PlayerCooldown cooldown = new PlayerCooldown(defaultCooldown, (player, cause) -> {
            if (removeMessage != null) {
                player.sendMessage(MCAdventure.asJsonString(removeMessage, MCPlayer.from(player).getLocale()));
            }
        }, plugin);
        return behaviour.filter((player, itemStack) -> {
            if (cooldown.isCooldown(player)) {
                if (cooldownMessage != null) {
                    player.sendMessage(MCAdventure.asJsonString(cooldownMessage, MCPlayer.from(player).getLocale()));
                }
                return false;
            }
            cooldown.addCooldown(player);
            return true;
        });
    }

}
