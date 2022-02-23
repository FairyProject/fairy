package io.fairyproject.debug;

import io.fairyproject.container.Service;
import io.fairyproject.container.ThreadingMode;

@Service
@ThreadingMode(ThreadingMode.Mode.ASYNC)
public class DebugService {

    public DebugService() {
        System.out.println(Thread.currentThread().getName());
    }

}
