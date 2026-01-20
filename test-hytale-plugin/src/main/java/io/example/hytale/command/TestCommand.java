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

package io.example.hytale.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.example.hytale.component.HelloComponent;
import io.example.hytale.huds.ExampleHud;
import io.example.hytale.pages.ExamplePage;
import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.MessageType;
import io.fairyproject.command.annotation.Arg;
import io.fairyproject.command.annotation.Command;
import io.fairyproject.container.Autowired;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.hytale.command.event.HytalePlayerCommandContext;

/**
 * Test command for demonstrating Fairy's command system on Hytale.
 *
 * <p>This command uses {@link HytalePlayerCommandContext} which means:</p>
 * <ul>
 *   <li>It can only be executed by players (not console)</li>
 *   <li>It runs on the correct thread for the player's world</li>
 *   <li>It has access to player-specific data like World, PlayerRef, etc.</li>
 * </ul>
 *
 * <p>The executor type is automatically detected based on the context parameter type.</p>
 */
@Command({"test", "t", "hytaletest"})
@InjectableComponent
public class TestCommand extends BaseCommand {

    @Autowired
    private HelloComponent helloComponent;

    public TestCommand() {
        System.out.println("[TestCommand] Command component created!");
    }

    @Override
    public String getDescription() {
        return "Test command for Hytale";
    }

    /**
     * /test - Shows help message
     */
    @Command("#")
    public void noArgs(HytalePlayerCommandContext context) {
        context.sendMessage(MessageType.INFO, "=== Hytale Test Plugin Commands ===");
        context.sendMessage(MessageType.INFO, "/test hello [name] - Greet someone");
        context.sendMessage(MessageType.INFO, "/test info - Show plugin info");
        context.sendMessage(MessageType.INFO, "/test echo <message> - Echo a message");
        context.sendMessage(MessageType.INFO, "/test add <a> <b> - Add two numbers");
        context.sendMessage(MessageType.INFO, "/test player - Show player info");
        context.sendMessage(MessageType.INFO, "/test world - Show world info");
    }

    /**
     * /test hello [name] - Greets the player or a specified name
     */
    @Command("hello")
    public void hello(HytalePlayerCommandContext context, @Arg(defaultValue = "World") String name) {
        String greeting = helloComponent.sayHello(name);
        context.sendMessage(MessageType.INFO, greeting);
    }

    /**
     * /test info - Shows plugin information
     */
    @Command("info")
    public void info(HytalePlayerCommandContext context) {
        context.sendMessage(MessageType.INFO, "Plugin: Hytale Test Plugin");
        context.sendMessage(MessageType.INFO, "Framework: Fairy Project");
        context.sendMessage(MessageType.INFO, "Platform: Hytale");
        context.sendMessage(MessageType.INFO, "Sender: " + context.name());
    }

    /**
     * /test echo <message> - Echoes a message back to the sender
     */
    @Command("echo")
    public void echo(HytalePlayerCommandContext context, @Arg String message) {
        context.sendMessage(MessageType.INFO, "Echo: " + message);
    }

    /**
     * /test add <a> <b> - Adds two numbers together
     */
    @Command("add")
    public void add(HytalePlayerCommandContext context, @Arg int a, @Arg int b) {
        int result = a + b;
        context.sendMessage(MessageType.INFO, a + " + " + b + " = " + result);
    }

    /**
     * /test warn - Test warning message
     */
    @Command("warn")
    public void warn(HytalePlayerCommandContext context) {
        context.sendMessage(MessageType.WARN, "This is a warning message!");
    }

    /**
     * /test error - Test error message
     */
    @Command("error")
    public void error(HytalePlayerCommandContext context) {
        context.sendMessage(MessageType.ERROR, "This is an error message!");
    }

    /**
     * /test player - Shows player-specific information
     * Demonstrates access to HytalePlayerCommandContext data
     */
    @Command("player")
    public void player(HytalePlayerCommandContext context) {
        context.sendMessage(MessageType.INFO, "=== Player Info ===");
        context.sendMessage(MessageType.INFO, "Username: " + context.getPlayerName());
        context.sendMessage(MessageType.INFO, "UUID: " + context.getPlayerUuid());
        context.sendMessage(MessageType.INFO, "Valid: " + context.isPlayerValid());
        context.sendMessage(MessageType.INFO, "In Store Thread: " + context.isInStoreThread());
    }

    @Command("ui")
    public void ui(HytalePlayerCommandContext context) {
        Player playerComponent = context.getPlayerComponent();
        Ref<EntityStore> ref = context.getRef();
        PlayerRef playerRef = context.getPlayerRef();
        playerComponent.getPageManager().openCustomPage(ref, context.getStore(), new ExamplePage(playerRef));
    }

    /**
     * /test world - Shows world information
     * Demonstrates thread-safe access to world data
     */
    @Command("world")
    public void world(HytalePlayerCommandContext context) {
        context.sendMessage(MessageType.INFO, "=== World Info ===");
        context.sendMessage(MessageType.INFO, "World Name: " + context.getWorld().getName());
        context.sendMessage(MessageType.INFO, "Store Index: " + context.getStore().getStoreIndex());
        context.sendMessage(MessageType.INFO, "Entity Count: " + context.getStore().getEntityCount());
        context.sendMessage(MessageType.INFO, "Thread Safe: " + context.isInStoreThread());
    }
}
