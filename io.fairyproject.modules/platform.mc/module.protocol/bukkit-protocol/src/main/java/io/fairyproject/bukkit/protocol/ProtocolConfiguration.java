package io.fairyproject.bukkit.protocol;

import io.fairyproject.Fairy;
import io.fairyproject.config.annotation.Comment;
import io.fairyproject.config.yaml.YamlConfiguration;
import lombok.Getter;

import java.io.File;
import java.nio.file.Path;

@Getter
public class ProtocolConfiguration extends YamlConfiguration {
    public ProtocolConfiguration() {
        super(new File(Fairy.getPlatform().getDataFolder(), "modules/protocol/yml").toPath());
    }

    @Comment({
            "PacketEvents v1 configuration"
    })



}
