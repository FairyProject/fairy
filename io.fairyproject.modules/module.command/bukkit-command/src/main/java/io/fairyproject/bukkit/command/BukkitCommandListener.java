package io.fairyproject.bukkit.command;

import io.fairyproject.Debug;
import io.fairyproject.bukkit.listener.RegisterAsListener;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.bukkit.command.util.CommandUtil;
import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.CommandListener;
import io.fairyproject.metadata.MetadataKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@InjectableComponent
@RegisterAsListener
public class BukkitCommandListener implements CommandListener {

    private final MetadataKey<BukkitCommandExecutor> metadata = MetadataKey.create("bukkit:executor", BukkitCommandExecutor.class);

    private CommandMap commandMap;
    private Map<String, Command> knownMap;

    @PostInitialize
    @SuppressWarnings("unchecked")
    public void onPostInitialize() throws NoSuchFieldException, IllegalAccessException {
        if (Debug.UNIT_TEST) {
            this.knownMap = new HashMap<>();
            return;
        }

        this.commandMap = CommandUtil.getCommandMap();

        Field knownMapField = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownMapField.setAccessible(true);

        this.knownMap = (Map<String, Command>) knownMapField.get(this.commandMap);
    }

    @Override
    public void onCommandInitial(BaseCommand command, String[] alias) {
        if (this.commandMap == null)
            return;

        final BukkitCommandExecutor bukkitCommandExecutor = new BukkitCommandExecutor(command, alias);

        commandMap.register(bukkitCommandExecutor.getFallbackPrefix(), bukkitCommandExecutor);
        command.getMetadata().put(metadata, bukkitCommandExecutor);

        // Synchronize commands
        CommandUtil.syncCommands();
    }

    @Override
    public void onCommandRemoval(BaseCommand command) {
        command.getMetadata().ifPresent(metadata, bukkitCommandExecutor -> {
            knownMap.remove(bukkitCommandExecutor.getFallbackPrefix() + ":" + bukkitCommandExecutor.getLabel());
            knownMap.remove(bukkitCommandExecutor.getLabel());

            for (String alias : bukkitCommandExecutor.getAliases()) {
                knownMap.remove(bukkitCommandExecutor.getFallbackPrefix() + ":" + alias);
                knownMap.remove(alias);
            }
        });
    }
}
