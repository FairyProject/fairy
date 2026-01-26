package io.example.hytale.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.hytale.entity.RegisterAsEntitySystem;
import org.jline.utils.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@InjectableComponent
@RegisterAsEntitySystem
public class ExampleSystem extends EntityTickingSystem<Player> {
    @Override
    public void tick(float v, int i, @Nonnull ArchetypeChunk<Player> archetypeChunk, @Nonnull Store<Player> store, @Nonnull CommandBuffer<Player> commandBuffer) {
        Log.info("Ticking players in ExampleSystem...");
    }

    @Nullable
    @Override
    public Query<Player> getQuery() {
        return Query.any();
    }
}
