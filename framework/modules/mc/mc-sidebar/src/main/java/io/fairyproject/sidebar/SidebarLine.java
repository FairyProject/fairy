package io.fairyproject.sidebar;

import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@RequiredArgsConstructor
@Getter
public class SidebarLine {
    private final Component component;
    private final ScoreFormat format;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SidebarLine that = (SidebarLine) o;
        return Objects.equals(component, that.component) && Objects.equals(format, that.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(component, format);
    }

    @NotNull
    public static SidebarLine of(@NotNull Component component) {
        return new SidebarLine(component, ScoreFormat.blankScore());
    }

    @NotNull
    public static SidebarLine of(@NotNull Component component, @NotNull ScoreFormat format) {
        return new SidebarLine(component, format);
    }
}
