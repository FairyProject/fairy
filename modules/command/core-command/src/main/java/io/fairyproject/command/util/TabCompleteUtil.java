package io.fairyproject.command.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class TabCompleteUtil {

    public List<String> filterTabComplete(String arg, List<String> commands) {
        return commands.stream()
                .distinct()
                .filter(cmd -> cmd != null && (arg.isEmpty() || StringUtils.startsWithIgnoreCase(cmd, arg)))
                .collect(Collectors.toList());
    }

}
