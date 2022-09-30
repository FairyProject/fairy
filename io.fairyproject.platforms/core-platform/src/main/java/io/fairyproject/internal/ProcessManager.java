package io.fairyproject.internal;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A internal process manager that is used to manage internal instances.
 *
 * @since 0.6.3
 * @author leegod
 */
@ApiStatus.Internal
public class ProcessManager {

    private static final ProcessManager INSTANCE = new ProcessManager();
    private final List<Process> processes = new ArrayList<>();

    public static ProcessManager get() {
        return INSTANCE;
    }

    /**
     * Register an internal process.
     *
     * @param process the process
     * @since 0.6.3
     */
    public <T extends Process> T register(T process) {
        this.processes.add(process);
        return process;
    }

    /**
     * Preload all registered processes.
     *
     * @since 0.6.3
     */
    public void preload() {
        this.processes.forEach(Process::preload);
    }

    /**
     * Load all the processes.
     *
     * @since 0.6.3
     */
    public void load() {
        this.processes.forEach(Process::load);
    }

    /**
     * Enable all registered processes.
     *
     * @since 0.6.3
     */
    public void enable() {
        this.processes.forEach(Process::enable);
    }

    /**
     * Unload all and cleanup registered processes.
     *
     * @since 0.6.3
     */
    public void destroy() {
        this.processes.forEach(Process::destroy);
        this.processes.clear();
    }

    public List<Process> getProcesses() {
        return Collections.unmodifiableList(this.processes);
    }

}
