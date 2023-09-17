package me.boomber.devmode.data.pattern;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public record FunctionParsePattern(Pattern pattern) {
    public FunctionParsePattern(@NonNls @NotNull String regex) {
        this(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    }

    @Nullable
    public FunctionParseMatch match(String text) {
        var matcher = pattern.matcher(text);

        if (matcher.find()) {
            return new FunctionParseMatch(matcher);
        } else {
            return null;
        }
    }
}
