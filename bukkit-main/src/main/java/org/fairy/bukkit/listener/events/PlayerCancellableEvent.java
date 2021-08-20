package org.fairy.bukkit.listener.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.function.Function;

public class PlayerCancellableEvent extends PlayerCallableEvent implements Cancellable {

    @Getter
    @Setter
    private boolean cancelled;

    public PlayerCancellableEvent(Player who) {
        super(who);
    }

    public <T> T supplyCancelled(Function<Boolean, T> f) {
        this.call();
        return f.apply(this.cancelled);
    }
}
