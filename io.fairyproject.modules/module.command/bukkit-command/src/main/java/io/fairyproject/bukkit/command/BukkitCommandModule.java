package io.fairyproject.bukkit.command;

import io.fairyproject.bean.BeanConstructor;
import io.fairyproject.bean.BeanContext;
import io.fairyproject.bean.PreInitialize;
import io.fairyproject.bean.Service;
import io.fairyproject.bukkit.command.presence.DefaultPresenceProvider;
import io.fairyproject.command.CommandService;
import io.fairyproject.module.Depend;
import io.fairyproject.module.Modular;

@Modular(
        value = "bukkit-command",
        depends = {@Depend("core-command"), @Depend("bukkit-xseries")}
)
@Service(name = "bukkit:command", dependencies = "command")
public class BukkitCommandModule {
    private final BeanContext beanContext;

    @BeanConstructor
    public BukkitCommandModule(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @PreInitialize
    public void preInit() {
        CommandService commandService = (CommandService) this.beanContext.getBean(CommandService.class);
        commandService.registerDefaultPresenceProvider(new DefaultPresenceProvider());
    }
}
