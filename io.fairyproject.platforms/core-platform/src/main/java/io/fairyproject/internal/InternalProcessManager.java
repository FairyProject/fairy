package io.fairyproject.internal;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * A internal process manager that is used to manage internal instances.
 *
 * @since 0.6.3
 * @author leegod
 */
@ApiStatus.Internal
public class InternalProcessManager {

    private static final InternalProcessManager INSTANCE = new InternalProcessManager();
    private final List<InternalProcess> processes = new ArrayList<>();

    public static InternalProcessManager get() {
        return INSTANCE;
    }

    /**
     * Register a internal process.
     *
     * @param process the process
     * @since 0.6.3
     */
    public <T extends InternalProcess> T register(T process) {
        this.processes.add(process);
        return process;
    }

    /**
     * Load all registered processes.
     *
     * @since 0.6.3
     */
    public void init() {
        this.processes.forEach(InternalProcess::init);
    }

    /**
     * Unload all and cleanup registered processes.
     *
     * @since 0.6.3
     */
    public void destroy() {
        this.processes.forEach(InternalProcess::destroy);
        this.processes.clear();
    }

}
