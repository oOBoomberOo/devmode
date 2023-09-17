package me.boomber.devmode.utils;

import lombok.Data;
import me.boomber.devmode.data.Range;
import me.boomber.devmode.data.SourceCode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

@Data
public class TextHighlighter {
    private ChatFormatting[] highlightedTextFormat = new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.UNDERLINE};
    private ChatFormatting[] highlightedLineFormat = new ChatFormatting[]{ChatFormatting.WHITE};
    private ChatFormatting[] normalFormat = new ChatFormatting[]{ChatFormatting.GRAY};

    private String highlightFallback = "<--[HERE]";
    private String prefixFormat = " %3d | ";

    private final SourceCode source;

    public Component highlight(Range content, Range highlight) {
        var precedingLines = source.intersectedLines(content);
        var sourceCode = source.split(highlight, content);

        var code1 = sourceCode.before();
        var code2 = sourceCode.highlighted();
        var code3 = sourceCode.after();

        if (code2.isBlank()) {
            code2 = highlightFallback;
        }

        var startFrom = precedingLines.start() - 1;
        var highlightedFrom = startFrom + StringUtils.lineCount(code1) - 1;
        var endFrom = highlightedFrom + StringUtils.lineCount(code2) - 1;

        var beforeHighlight = prefixEachLine(code1, startFrom);
        var highlighted = prefixEachLine(code2, highlightedFrom);
        var afterHighlight = prefixEachLine(code3, endFrom);

        return Text.append(
                Text.literal(beforeHighlight, normalFormat),
                Text.literal(highlighted, highlightedTextFormat),
                Text.literal(afterHighlight, normalFormat)
        );
    }

    private String prefixEachLine(String text, int lineOffset) {
        var lineNumber = lineOffset;
        var result = new StringBuilder();

        for (char current : text.toCharArray()) {
            if (current == '\n') {
                lineNumber += 1;
                result.append("\n");
                result.append(prefix(lineNumber));
            } else {
                result.append(current);
            }
        }

        return result.toString();
    }

    private String prefix(int lineNumber) {
        return String.format(prefixFormat, lineNumber);
    }
}
