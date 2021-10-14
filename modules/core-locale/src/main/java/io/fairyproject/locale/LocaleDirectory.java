package io.fairyproject.locale;

import io.fairyproject.bean.DependencyType;
import io.fairyproject.bean.ServiceDependency;
import io.fairyproject.bean.ServiceDependencyType;
import io.fairyproject.config.yaml.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@ServiceDependency(dependencies = "locale", type = @DependencyType(ServiceDependencyType.SUB_DISABLE))
public interface LocaleDirectory {

    /**
     * Tell where the locale folder is
     *
     * @return the Folder
     */
    File directory();

    /**
     * Tell where the resource folder is (in Jar)
     * If not null, we will copy every locale file from the folder
     *
     * @return Resource directory
     */
    @Nullable
    String resourceDirectory();

    /**
     * Give system the base configuration
     *
     * @param file the File
     * @return the Configuration
     */
    YamlConfiguration config(File file);

    /**
     * Default Locale that default configuration generates
     *
     * @return locale name
     */
    String defaultLocale();

}