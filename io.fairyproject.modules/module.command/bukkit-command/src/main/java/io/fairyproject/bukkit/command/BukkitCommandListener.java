package io.fairyproject.bukkit.command;

import io.fairyproject.container.Component;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.bukkit.command.util.CommandUtil;
import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.CommandListener;
import io.fairyproject.metadata.MetadataKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.util.Map;

@Component
public class BukkitCommandListener implements CommandListener {

    private final MetadataKey<BukkitCommandExecutor> metadata = MetadataKey.create("bukkit:executor", BukkitCommandExecutor.class);

    private CommandMap commandMap;
    private Field knownMapField;

    @PostInitialize
    public void onPostInitialize() {
        this.commandMap = CommandUtil.getCommandMap();
        try {
            this.knownMapField = this.commandMap.getClass().getDeclaredField("knownCommands");
            this.knownMapField.setAccessible(true);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void onCommandInitial(BaseCommand command, String[] alias) {
        final BukkitCommandExecutor bukkitCommandExecutor = new BukkitCommandExecutor(command, alias);

        commandMap.register(bukkitCommandExecutor.getFallbackPrefix(), bukkitCommandExecutor);
        command.getMetadata().put(metadata, bukkitCommandExecutor);
    }

    @Override
    public void onCommandRemoval(BaseCommand command) {
        command.getMetadata().ifPresent(metadata, bukkitCommandExecutor -> {
            final Map<String, Command> map;
            try {
                map = (Map<String, Command>) this.knownMapField.get(this.commandMap);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            map.remove(bukkitCommandExecutor.getFallbackPrefix() + ":" + bukkitCommandExecutor.getLabel());
            map.remove(bukkitCommandExecutor.getLabel());

            for (String alias : bukkitCommandExecutor.getAliases()) {
                map.remove(bukkitCommandExecutor.getFallbackPrefix() + ":" + alias);
                map.remove(alias);
            }
        });
    }
}
