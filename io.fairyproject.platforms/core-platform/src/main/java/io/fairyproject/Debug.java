package io.fairyproject;

import io.fairyproject.util.Stacktrace;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Debug {

    // Is fairy framework currently running under unit testing?
    public boolean UNIT_TEST = false;

    // the boolean to determine if you are in Fairy Project IDE
    public boolean IN_FAIRY_IDE = false;

    // the boolean to determine if you are in the project IDE that develops based on Fairy
    public boolean IN_DEV_IDE = false;

    // the runnable to let you setup break point in IDE
    public Runnable BREAKPOINT;

    public void doPause() {
        if (!Debug.isInIde()) {
            return;
        }

        System.err.println("Pausing...");

        if (BREAKPOINT == null) {
            System.err.println("You don't seems to have breakpoint setup!");
        } else {
            BREAKPOINT.run();
        }
    }

    public void logExceptionAndPause(Throwable throwable) {
        Stacktrace.print(throwable);
        Debug.doPause();
    }

    public boolean isInIde() {
        return IN_FAIRY_IDE || IN_DEV_IDE;
    }

}
