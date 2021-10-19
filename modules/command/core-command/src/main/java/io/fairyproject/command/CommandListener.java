package io.fairyproject.command;

public interface CommandListener {

    void onCommandInitial(BaseCommand command, String[] alias);

}
