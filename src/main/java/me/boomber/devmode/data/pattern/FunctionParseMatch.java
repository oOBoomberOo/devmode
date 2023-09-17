package me.boomber.devmode.data.pattern;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

public record FunctionParseMatch(Matcher matcher) {
    public int row() {
        return Integer.parseInt(matcher.group("row"));
    }

    public int column() {
        try {
            var column = matcher.group("column");

            if (column == null) {
                return 0;
            }

            return Integer.parseInt(column);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    @NotNull
    public String reason() {
        return matcher.group("reason");
    }
}
