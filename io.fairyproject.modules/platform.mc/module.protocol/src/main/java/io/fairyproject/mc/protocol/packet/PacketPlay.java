package io.fairyproject.mc.protocol.packet;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.protocol.item.*;
import lombok.*;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Deprecated
public class PacketPlay {
    @Deprecated
    public static class Out {
        @Getter @Setter @Deprecated
        public static class ScoreboardScore extends WrapperPlayServerUpdateScore {

            public ScoreboardScore(String entityName, Action action, String objectiveName, Optional<Integer> value) {
                super(entityName, action, objectiveName, value);
            }

            public static Factory builder() {
                return new Factory();
            }

            public static class Factory {
                private String owner;
                private String objectiveName;
                private Integer score;
                private ScoreAction action;

                public Factory owner(String owner) {
                    this.owner = owner;
                    return this;
                }

                public Factory objectiveName(String objectiveName) {
                    this.objectiveName = objectiveName;
                    return this;
                }

                public Factory score(Integer score) {
                    this.score = score;
                    return this;
                }

                public Factory action(ScoreAction action) {
                    this.action = action;
                    return this;
                }

                public ScoreboardScore build() {
                    final Action action = Action.values()[this.action.ordinal()];

                    return new ScoreboardScore(
                            owner,
                            action,
                            objectiveName,
                            Optional.ofNullable(score)
                    );
                }
            }
        }

        @Getter @Setter @Deprecated
        public static class ScoreboardDisplayObjective extends WrapperPlayServerDisplayScoreboard {
            public ScoreboardDisplayObjective(int position, String scoreName) {
                super(position, scoreName);
            }

            public static Factory builder() {
                return new Factory();
            }

            public static class Factory {
                private ObjectiveDisplaySlot displaySlot;
                private String objectiveName;

                public Factory displaySlot(ObjectiveDisplaySlot displaySlot) {
                    this.displaySlot = displaySlot;
                    return this;
                }

                public Factory objectiveName(String objectiveName) {
                    this.objectiveName = objectiveName;
                    return this;
                }

                public ScoreboardDisplayObjective build() {
                    return new ScoreboardDisplayObjective(
                            displaySlot.getSerializeId(),
                            objectiveName
                    );
                }
            }
        }

        @Getter @Setter @Deprecated
        public static class ScoreboardObjective extends WrapperPlayServerScoreboardObjective {
            public ScoreboardObjective(String name, ObjectiveMode mode, Optional<String> displayName, Optional<HealthDisplay> display) {
                super(name, mode, displayName, display);
            }

            public static Factory builder() {
                return new Factory();
            }

            public static class Factory {
                private String objectiveName;
                private Component displayName;
                private ObjectiveRenderType renderType;
                private Integer method;

                public Factory objectiveName(String objectiveName) {
                    this.objectiveName = objectiveName;
                    return this;
                }

                public Factory displayName(Component displayName) {
                    this.displayName = displayName;
                    return this;
                }

                public Factory renderType(ObjectiveRenderType renderType) {
                    this.renderType = renderType;
                    return this;
                }

                public Factory method(Integer method) {
                    this.method = method;
                    return this;
                }

                public ScoreboardObjective build() {
                    final ObjectiveMode mode = ObjectiveMode.values()[renderType.ordinal()];
                    final Optional<HealthDisplay> method = Optional.ofNullable(this.method == null
                            ? null
                            : HealthDisplay.values()[this.method]
                    );

                    return new ScoreboardObjective(
                            objectiveName,
                            mode,
                            displayName == null
                                    ? Optional.empty()
                                    : Optional.of(MCAdventure.asJsonString(displayName, Locale.getDefault())),
                            method
                    );
                }
            }
        }

        @Getter @Setter @Deprecated
        public static class Tablist extends WrapperPlayServerPlayerListHeaderAndFooter {

            public Tablist(Component headerComponent, Component footerComponent) {
                super(headerComponent, footerComponent);
            }

            public Tablist(String headerJson, String footerJson) {
                super(headerJson, footerJson);
            }

            public static Factory builder() {
                return new Factory();
            }

            public static class Factory {
                private Component header, footer;

                public Factory header(Component header) {
                    this.header = header;
                    return this;
                }

                public Factory footer(Component footer) {
                    this.footer = footer;
                    return this;
                }

                public Tablist build() {
                    return new Tablist(header, footer);
                }
            }
        }

        @Getter @Setter @Deprecated
        public static class PlayerInfo extends WrapperPlayServerPlayerInfo {

            public PlayerInfo(@NotNull Action action, List<PlayerData> playerDataList) {
                super(action, playerDataList);
            }

            public PlayerInfo(@NotNull Action action, PlayerData... playerData) {
                super(action, playerData);
            }

            public PlayerInfo(@NotNull Action action, PlayerData playerData) {
                super(action, playerData);
            }

            public static Factory builder() {
                return new Factory();
            }

            public static class Factory {
                private PlayerInfoAction action;
                @Singular
                private List<PlayerInfoData> entries;

                public Factory action(PlayerInfoAction action) {
                    this.action = action;
                    return this;
                }

                public Factory entries(List<PlayerInfoData> entries) {
                    this.entries = entries;
                    return this;
                }

                public Factory entries(PlayerInfoData... entries) {
                    this.entries = Arrays.asList(entries);
                    return this;
                }

                public PlayerInfo build() {
                    final Action action = Action.values()[this.action.ordinal()];
                    final List<PlayerData> playerDataList = new ArrayList<>();
                    if (entries != null) {
                        for (PlayerInfoData entry : entries) {
                            final UserProfile profile = new UserProfile(
                                    entry.getGameProfile().getUuid(),
                                    entry.getGameProfile().getName(),
                                    entry.getGameProfile().getProperties()
                                            .stream()
                                            .map(e -> new TextureProperty(e.getName(), e.getValue(), e.getSignature()))
                                            .collect(Collectors.toList())
                            );
                            final PlayerData playerData = new PlayerData(
                                    entry.getComponent(),
                                    profile,
                                    GameMode.getById(entry.getGameMode().getValue()),
                                    entry.getPing()
                            );

                            playerDataList.add(playerData);
                        }
                    }

                    return new PlayerInfo(action, playerDataList);
                }
            }
        }

        @Getter @Setter @Deprecated
        public static class ScoreboardTeam extends WrapperPlayServerTeams {
            public ScoreboardTeam(String teamName, TeamMode teamMode, Optional<ScoreBoardTeamInfo> teamInfo, Collection<String> entities) {
                super(teamName, teamMode, teamInfo, entities);
            }

            public ScoreboardTeam(String teamName, TeamMode teamMode, Optional<ScoreBoardTeamInfo> teamInfo, String... entities) {
                super(teamName, teamMode, teamInfo, entities);
            }

            public ScoreboardTeam(PacketSendEvent event) {
                super(event);
            }

            public void setTeamAction(TeamAction action) {
                super.setTeamMode(TeamMode.values()[action.ordinal()]);
            }

            public void setParameters(final Optional<Parameters> parameters) {
                super.setTeamInfo(
                        parameters.map(e -> new ScoreBoardTeamInfo(
                                e.getDisplayName(),
                                e.getPlayerPrefix(),
                                e.getPlayerSuffix(),
                                e.getNametagVisibility(),
                                e.getCollisionRule(),
                                null,
                                OptionData.fromValue((byte) e.getOptions())
                        ))
                );
            }

            public static Factory builder() {
                return new Factory();
            }

            public static class Factory {
                private String name;
                private TeamAction teamAction;
                @Singular
                private Collection<String> players = Collections.emptyList();
                private Optional<Parameters> parameters = Optional.empty();

                public Factory name(String name) {
                    this.name = name;
                    return this;
                }

                public Factory teamAction(TeamAction teamAction) {
                    this.teamAction = teamAction;
                    return this;
                }

                public Factory players(Collection<String> players) {
                    this.players = players;
                    return this;
                }

                public Factory players(String... players) {
                    this.players = Arrays.asList(players);
                    return this;
                }

                public Factory parameters(Optional<Parameters> parameters) {
                    this.parameters = parameters;
                    return this;
                }

                public ScoreboardTeam build() {
                    final String name = this.name;
                    final TeamMode mode = TeamMode.values()[teamAction.ordinal()];
                    /*final Optional<ScoreBoardTeamInfo> info = parameters.map(e -> new ScoreBoardTeamInfo(
                            e.getDisplayName(),
                            e.getPlayerPrefix(),
                            e.getPlayerSuffix(),
                            e.getNametagVisibility(),
                            e.getCollisionRule(),
                            e.getColor() == null ? null : e.getColor().getColor() == null ? null
                                    : NamedTextColor.ofExact(e.getColor().getColor()),
                            OptionData.fromValue((byte) e.getOptions())
                    ));*/

                    return new ScoreboardTeam(name, mode, null, players);
                }
            }

            @Builder @NoArgsConstructor @AllArgsConstructor @Getter @Setter
            public static class Parameters {
                private Component displayName = Component.empty();
                private Component playerPrefix = Component.empty();
                private Component playerSuffix = Component.empty();
                private NameTagVisibility nametagVisibility = NameTagVisibility.ALWAYS;
                private CollisionRule collisionRule = CollisionRule.ALWAYS;
                private ChatFormatting color = ChatFormatting.BLACK;
                private int options;
            }
        }
    }
}
