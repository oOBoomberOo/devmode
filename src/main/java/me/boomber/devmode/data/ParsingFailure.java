package me.boomber.devmode.data;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public record ParsingFailure(ResourceLocation id, Component source, String reason, Integer line) {
    public String name() {
        return id.getPath();
    }

    public Component formatMessage() {
        var file = join(text(name(), ChatFormatting.GREEN),
                text(":", ChatFormatting.DARK_GRAY),
                text("L", ChatFormatting.AQUA),
                text(line.toString(), ChatFormatting.AQUA));

        var header = join(text(" - ", ChatFormatting.RED), text(reason, ChatFormatting.DARK_RED), text("\n"));

        return join(file, header, source);
    }

    private static Component text(String text, ChatFormatting ...formatting) {
        return Component.literal(text).withStyle(formatting);
    }

    private static Component join(Component ...texts) {
        return Arrays.stream(texts).map(Component::copy).reduce(Component.empty(), MutableComponent::append);
    }

    private static Component sourceCode(List<String> lines, int row, int col) {
        var context = 2;
        var result = new ArrayList<Component>();

        for (var i = 0; i < lines.size(); i++) {
            if (i < row - context || i > row + context) {
                continue;
            }

            var content = lines.get(i);
            var prefix = String.format("%3d| ", i + 1);

            if (i == row) {
                var successPart = content.substring(0, col);
                var errorPart = content.substring(col);
                var isNotVisible = errorPart.isBlank();

                var before = text(prefix + successPart, ChatFormatting.WHITE);
                var after = text(isNotVisible ? "_" : errorPart, ChatFormatting.RED);
                result.add(join(before, after, text("\n")));
            } else {
                result.add(join(text(prefix + content, ChatFormatting.GRAY), text("\n")));
            }
        }

        return join(result.toArray(Component[]::new));
    }

    public static ParsingFailure from(ResourceLocation id, IllegalArgumentException exception, List<String> lines) {
        var result = new ExceptionParser(exception).parse();
        var source = sourceCode(lines, result.line - 1, result.column);
        return new ParsingFailure(id, source, result.reason, result.line);
    }

    public static class ExceptionParser {
        public ExceptionParser(IllegalArgumentException exception) {
            this.exception = exception;
        }

        private final IllegalArgumentException exception;
        private final Pattern pattern = Pattern.compile("Whilst parsing command on line (?<row>\\d+): (?<reason>.+(?=at position (?<column>\\d+).+).*|.+)", Pattern.CASE_INSENSITIVE);

        public ExceptionParsingResult parse() {
            var matcher = pattern.matcher(exception.getMessage());

            if (matcher.find()) {
                var lineNumber = Integer.parseInt(matcher.group("row"));
                var reason = matcher.group("reason");
                var position = matcher.group("column");

                return new ExceptionParsingResult(reason, lineNumber, position == null ? 0 : Integer.parseInt(position));
            }

            return new ExceptionParsingResult("", -1, 0);
        }
    }

    public record ExceptionParsingResult(String reason, int line, int column) {}
}
