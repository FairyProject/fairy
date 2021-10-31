package io.fairyproject.discord;

import io.fairyproject.Fairy;
import io.fairyproject.bean.PreInitialize;
import io.fairyproject.bean.Service;
import io.fairyproject.library.Library;
import io.fairyproject.library.LibraryRepository;
import io.fairyproject.module.Modular;

@Modular(
        value = "core-discord"
)
@Service(name = "discord-initializer")
public class DCInitializer {

    private static final LibraryRepository REPOSITORY = new LibraryRepository("https://m2.dv8tion.net/releases");
    private static final Library JDA = Library.builder()
            .gradle("net.dv8tion:JDA:4.3.0_339")
            .repository(REPOSITORY)
            .build();

    @PreInitialize
    public void initialize() {
        Fairy.getLibraryHandler().downloadLibraries(true, JDA);
    }

}
