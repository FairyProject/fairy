package io.fairyproject.mc.hologram;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.fairyproject.event.EventListener;
import io.fairyproject.event.EventNode;
import io.fairyproject.mc.*;
import io.fairyproject.mc.event.MCPlayerChangedWorldEvent;
import io.fairyproject.mc.event.MCPlayerJoinEvent;
import io.fairyproject.mc.event.MCPlayerMoveEvent;
import io.fairyproject.mc.event.MCPlayerQuitEvent;
import io.fairyproject.mc.event.trait.MCPlayerEvent;
import io.fairyproject.mc.hologram.line.HologramLine;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.mc.protocol.event.MCPlayerPacketReceiveEvent;
import io.fairyproject.mc.util.Position;
import io.fairyproject.util.ConditionUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class HologramImpl implements Hologram {


    private final MCWorld world;
    private Position pos;
    private MCEntity attached;
    private boolean spawned;
    private boolean autoViewable;
    private double verticalSpacing;
    private int viewDistance;

    @Nullable
    private EventNode<MCPlayerEvent> eventNode;

    private final Map<MCPlayer, EventNode<MCPlayerEvent>> viewers;
    private final Set<Consumer<MCPlayer>> attackHandlers;
    private final Set<Consumer<MCPlayer>> interactHandlers;
    private final List<HologramLine> lines;
    private final List<HologramEntity> entities;

    public HologramImpl(@NotNull Position pos) {
        this.world = pos.getMCWorld();
        this.pos = pos;
        this.autoViewable = true;
        this.verticalSpacing = 0.25;
        this.viewDistance = 4;
        this.lines = new ArrayList<>();
        this.entities = new ArrayList<>();
        this.viewers = new ConcurrentHashMap<>();
        this.attackHandlers = ConcurrentHashMap.newKeySet();
        this.interactHandlers = ConcurrentHashMap.newKeySet();
    }

    @Override
    public Hologram autoViewable(boolean autoViewable) {
        this.autoViewable = autoViewable;
        return this;
    }

    @Override
    public Hologram viewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
        return this;
    }

    @Override
    public Hologram attackHandler(Consumer<MCPlayer> attackHandler) {
        this.attackHandlers.add(attackHandler);
        return this;
    }

    @Override
    public Hologram interactHandler(Consumer<MCPlayer> interactHandler) {
        this.interactHandlers.add(interactHandler);
        return this;
    }

    @Override
    public Hologram lines(@NotNull List<HologramLine> lines) {
        synchronized (this) {
            this.lines.clear();
            this.lines.addAll(lines);
        }
        this.updateEntities();
        return this;
    }

    @Override
    public Hologram line(@NotNull HologramLine line) {
        synchronized (this) {
            this.lines.add(line);
        }
        this.updateEntities();
        return this;
    }

    @Override
    public Hologram line(int index, @NotNull HologramLine line) {
        synchronized (this) {
            this.lines.set(index, line);
        }
        this.updateEntities();
        return this;
    }

    @Override
    public Hologram position(@NotNull Position pos) {
        if (this.pos.getMCWorld() != this.world)
            throw new IllegalArgumentException("hologram doesn't support cross world teleportation.");
        this.pos = pos;
        this.viewers.keySet().forEach(this::update);
        return this;
    }

    @Override
    public Hologram verticalSpacing(double verticalSpacing) {
        this.verticalSpacing = verticalSpacing;
        return this;
    }

    @Override
    public Hologram attach(@Nullable MCEntity entity) {
        this.attached = entity;
        this.viewers.keySet().forEach(this::update);
        return this;
    }

    @Override
    public void removeLine(int index) {
        this.lines.remove(index);
    }

    @Override
    public void clear() {
        this.lines.clear();
    }

    @Override
    public Hologram spawn() {
        synchronized (this) {
            if (this.spawned)
                return this;
            this.spawned = true;
        }
        ConditionUtils.notNull(this.pos, "hologram position");

        this.updateEntities();
        if (this.autoViewable) {
            this.nearby().forEach(this::addViewer);

            EventNode<MCPlayerEvent> eventNode = EventNode.type("hologram:nearby", MCEventFilter.PLAYER);
            // add player when player is nearby on join
            eventNode.addListener(MCPlayerJoinEvent.class, event -> {
                MCPlayer player = event.getPlayer();
                if (this.isViewer(player))
                    return;

                if (this.chunkDistanceTo(player.getPosition()) <= this.viewDistance)
                    this.addViewer(player);
            });

            // add nearby non-viewer player to be viewed
            eventNode.addListener(EventListener.builder(MCPlayerMoveEvent.class)
                    .ignoreCancelled(true)
                    .filter(event -> !this.isViewer(event.getPlayer()))
                    .filter(MCEventFilter.DIFFERENT_CHUNK)
                    .handler(event -> {
                        MCPlayer player = event.getPlayer();

                        if (this.chunkDistanceTo(event.getToPos()) <= this.viewDistance)
                            this.addViewer(player);
                    })
                    .build()
            );
            this.world.getEventNode().addChild(eventNode);
            this.eventNode = eventNode;
        }

        return this;
    }

    @Override
    public synchronized void remove() {
        synchronized (this) {
            if (!this.spawned)
                return;
            this.spawned = false;
        }
        this.viewers.keySet().forEach(this::removeViewer);
        this.viewers.clear();
        if (this.eventNode != null) {
            this.world.getEventNode().removeChild(this.eventNode);
            this.eventNode = null;
        }
    }

    private synchronized void updateEntities() {
        if (!this.spawned)
            return;

        int index;
        for (index = 0; index < this.lines.size(); index++) {
            HologramLine line = this.lines.get(index);

            HologramEntity entity;
            if (index >= this.entities.size()) {
                entity = new HologramEntity();
                entity.setEntityId(MCEntity.Companion.BRIDGE.newEntityId());
                entity.setEntityUuid(UUID.randomUUID());
                entity.setY(-this.verticalSpacing * index);
                entity.setLine(line);

                this.entities.add(entity);
                this.viewers.keySet().forEach(entity::show);
            } else {
                entity = this.entities.get(index);
                entity.setLine(line);
                this.viewers.keySet().forEach(entity::update);
            }
        }

        if (index < this.entities.size() - 1) {
            for (; index < this.entities.size(); index++) {
                HologramEntity entity = this.entities.get(index);

                this.entities.remove(entity);
                this.viewers.keySet().forEach(entity::hide);
            }
        }
    }

    @Override
    public boolean isSpawned() {
        return this.spawned;
    }

    @Override
    public boolean isAutoViewable() {
        return this.autoViewable;
    }

    @Override
    public int getViewDistance() {
        return this.viewDistance;
    }

    @Override
    public @Nullable MCEntity getAttached() {
        return this.attached;
    }

    @Override
    public @NotNull Position getPosition() {
        return this.pos;
    }

    @Override
    public double getVerticalSpacing() {
        return this.verticalSpacing;
    }

    @Override
    public @NotNull List<HologramLine> getLines() {
        return Collections.unmodifiableList(this.lines);
    }

    @Override
    public boolean addViewer(@NotNull MCPlayer player) {
        if (this.spawned)
            this.show(player);

        return this.viewers.put(player, this.createEventNode(player)) != null;
    }

    private EventNode<MCPlayerEvent> createEventNode(MCPlayer player) {
        EventNode<MCPlayerEvent> eventNode = EventNode.type("hologram:player-update", MCEventFilter.PLAYER);
        // remove from viewing whenever player moves out from the range
        eventNode.addListener(EventListener.builder(MCPlayerMoveEvent.class)
                .ignoreCancelled(true)
                .filter(MCEventFilter.DIFFERENT_CHUNK)
                .handler(event -> {
                    if (this.chunkDistanceTo(player.getPosition()) <= this.viewDistance)
                        return;
                    this.removeViewer(player);
                })
                .build());
        // remove from viewing whenever player quits
        eventNode.addListener(MCPlayerQuitEvent.class, event -> this.removeViewer(event.getPlayer()));
        // remove from viewing whenever player changes world
        eventNode.addListener(MCPlayerChangedWorldEvent.class, event -> this.removeViewer(event.getPlayer()));
        // listen packet for interaction detection
        eventNode.addListener(MCPlayerPacketReceiveEvent.class, event -> {
            if (event.packetType() != PacketType.Play.Client.INTERACT_ENTITY)
                return;

            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event.getEvent());
            if (!this.isEntity(packet.getEntityId()))
                return;

            switch (packet.getAction()) {
                case ATTACK:
                    this.attackHandlers.forEach(consumer -> consumer.accept(player));
                    break;
                case INTERACT:
                case INTERACT_AT:
                    this.interactHandlers.forEach(consumer -> consumer.accept(player));
                    break;
                default:
                    throw new IllegalStateException("packet action is null");
            }
        });
        // add node as child to player's event node
        player.getEventNode().addChild(eventNode);
        return eventNode;
    }

    /**
     * Check if the provided id is a part of hologram's entity ids.
     *
     * @param entityId the entity id
     * @return true if it's a part of hologram
     */
    private boolean isEntity(int entityId) {
        synchronized (this) {
            return this.entities.stream().anyMatch(entity -> entity.entityId == entityId);
        }
    }

    // Internal
    private void show(@NotNull MCPlayer player) {
        this.entities.forEach(entity -> entity.show(player));
    }

    private void update(@NotNull MCPlayer player) {
        this.entities.forEach(entity -> entity.update(player));
    }

    private void hide(@NotNull MCPlayer player) {
        this.entities.forEach(entity -> entity.hide(player));
    }

    @Override
    public boolean removeViewer(@NotNull MCPlayer player) {
        if (this.spawned)
            this.hide(player);

        EventNode<MCPlayerEvent> eventNode = this.viewers.remove(player);
        if (eventNode != null) {
            player.getEventNode().removeChild(eventNode);
            return true;
        }
        return false;
    }

    @Override
    public @NotNull Set<@NotNull MCPlayer> getViewers() {
        return this.viewers.keySet();
    }

    private Stream<MCPlayer> nearby() {
        return this.world
                .getPlayers().stream()
                .filter(player -> this.chunkDistanceTo(player.getPosition()) <= this.viewDistance);
    }

    private double chunkDistanceTo(Position target) {
        int hologramChunkX = this.pos.getChunkX();
        int hologramChunkZ = this.pos.getChunkZ();

        int targetChunkX = target.getChunkX();
        int targetChunkZ = target.getChunkZ();

        return Math.sqrt(Math.pow(hologramChunkX - targetChunkX, 2) + Math.pow(hologramChunkZ - targetChunkZ, 2));
    }

    @Data
    @NoArgsConstructor
    private class HologramEntity {

        private HologramLine line;
        private double y;
        private int entityId;
        private UUID entityUuid;

        public void show(@NotNull MCPlayer player) {
            // spawn packet
            WrapperPlayServerSpawnLivingEntity packet = new WrapperPlayServerSpawnLivingEntity(
                    this.entityId,
                    this.entityUuid,
                    EntityTypes.ARMOR_STAND,
                    this.packetPosition(),
                    pos.getYaw(),
                    pos.getPitch(),
                    0,
                    new Vector3d(),
                    this.createEntityData(player)
            );

            MCProtocol.sendPacket(player, packet);
            this.update(player);
        }

        public void update(@NotNull MCPlayer player) {
            // teleport packet
            WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(
                    this.entityId,
                    this.packetPosition(),
                    pos.getYaw(),
                    pos.getPitch(),
                    false
            );

            // metadata packet
            WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(
                    this.entityId,
                    this.createEntityData(player)
            );

            // attach entity packet
            WrapperPlayServerAttachEntity attachEntityPacket = new WrapperPlayServerAttachEntity(
                    this.entityId,
                    attached != null ? attached.getId() : -1,
                    false
            );

            MCProtocol.sendPacket(player, teleportPacket);
            MCProtocol.sendPacket(player, metadataPacket);
            MCProtocol.sendPacket(player, attachEntityPacket);
        }

        public void hide(@NotNull MCPlayer player) {
            // remove entity packet
            WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(this.entityId);
            MCProtocol.sendPacket(player, packet);
        }

        private Vector3d packetPosition() {
            return new Vector3d(pos.getX(), pos.getY() + y, pos.getZ());
        }

        private List<EntityData> createEntityData(MCPlayer player) {
            List<EntityData> entityDataList = new ArrayList<>();

            // entity data bit mask
            entityDataList.add(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20));

            // custom name
            MCVersion version = MCServer.current().getVersion();
            if (version.isOrAbove(MCVersion.V1_13)) {
                entityDataList.add(new EntityData(2, EntityDataTypes.COMPONENT, this.line.render(player)));
            } else {
                entityDataList.add(new EntityData(2, EntityDataTypes.STRING, MCAdventure.asLegacyString(this.line.render(player), player.getLocale())));
            }

            // always show name tag
            if (version.isOrAbove(MCVersion.V1_9)) {
                entityDataList.add(new EntityData(3, EntityDataTypes.BOOLEAN, true));
            } else {
                entityDataList.add(new EntityData(3, EntityDataTypes.BYTE, (byte) 1));
            }

            // armorstand status bit mask
            if (version.isOrAbove(MCVersion.V1_17))
                entityDataList.add(new EntityData(15, EntityDataTypes.BYTE, (byte) 0x01));
            else if (version.isOrAbove(MCVersion.V1_15))
                entityDataList.add(new EntityData(14, EntityDataTypes.BYTE, (byte) 0x01));
            else if (version.isOrAbove(MCVersion.V1_14))
                entityDataList.add(new EntityData(13, EntityDataTypes.BYTE, (byte) 0x01));
            else if (version.isOrAbove(MCVersion.V1_10))
                entityDataList.add(new EntityData(11, EntityDataTypes.BYTE, (byte) 0x01));
            else
                entityDataList.add(new EntityData(10, EntityDataTypes.BYTE, (byte) 0x01));

            return entityDataList;
        }

    }
}
