package io.fairyproject.container.object;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum LifeCycle {

    NONE(false),
    CONSTRUCT(false),
    PRE_INIT(false),
    POST_INIT(false),
    PRE_DESTROY(true),
    POST_DESTROY(true);

    private final boolean reverseOrder;

    public boolean isAfter(LifeCycle lifeCycle) {
        return this.ordinal() > lifeCycle.ordinal();
    }

    public boolean isBefore(LifeCycle lifeCycle) {
        return this.ordinal() < lifeCycle.ordinal();
    }

}