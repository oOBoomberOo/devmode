package me.boomber.devmode.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class StringUtils {
    private final Pattern LINE_PATTERN = Pattern.compile("\\r\\n|\\v");

    public int lineCount(String string) {
        Matcher matcher = LINE_PATTERN.matcher(string);
        int i = 1;
        while (matcher.find()) {
            ++i;
        }
        return i;
    }
}
