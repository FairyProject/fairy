package io.fairyproject.command.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CoreCommandUtil {

    public List<String> filterTabComplete(String arg, List<String> commands) {
        return commands.stream()
                .distinct()
                .filter(cmd -> cmd != null && (arg.isEmpty() || StringUtils.startsWithIgnoreCase(cmd, arg)))
                .collect(Collectors.toList());
    }

    public String[] arrayFromRange(String[] array, int fromIndex, int toIndex) {
        return Arrays.copyOfRange(array, fromIndex, toIndex + 1);
    }

}
