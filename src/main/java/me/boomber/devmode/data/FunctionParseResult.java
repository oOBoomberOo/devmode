package me.boomber.devmode.data;

import me.boomber.devmode.utils.Text;
import me.boomber.devmode.utils.TextHighlighter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record FunctionParseResult(ResourceLocation location, SourceCode source, String reason, Range contentRange, Range highlightRange) {
    public Component format() {
        return Text.append(fileName(), Text.literal(" "), fileLocation(), Text.literal(" - "), errorMessage(), sourceCode());
    }

    private Component fileName() {
        var name = location.getPath();
        return Text.literal(name, ChatFormatting.GREEN, ChatFormatting.UNDERLINE);
    }

    private Component fileLocation() {
        var lineNumber = source.intersectedLines(highlightRange).start() - 1;
        var columnNumber = highlightRange.start() - source.line(lineNumber - 1).start() - 1;
        return Text.literal("%d:%d".formatted(lineNumber, columnNumber), ChatFormatting.DARK_AQUA);
    }

    private Component errorMessage() {
        return Text.literal(reason, ChatFormatting.DARK_RED);
    }

    private Component sourceCode() {
        return new TextHighlighter(source).highlight(contentRange, highlightRange);
    }
}
