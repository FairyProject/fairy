package io.fairyproject.mc.protocol.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ObjectiveRenderType {
    INTEGER("integer"),
    HEARTS("hearts");

    @Getter
    private final String id;

    public static ObjectiveRenderType byId(String id) {
        for (ObjectiveRenderType renderType : values()) {
            if (renderType.id.equals(id)) {
                return renderType;
            }
        }
        return null;
    }
}
