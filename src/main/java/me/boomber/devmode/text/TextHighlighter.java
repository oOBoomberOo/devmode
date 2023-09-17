package me.boomber.devmode.text;

import lombok.Data;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

@Data
public class TextHighlighter {
    private ChatFormatting[] highlightedTextFormat = new ChatFormatting[] { ChatFormatting.RED, ChatFormatting.UNDERLINE };
    private ChatFormatting[] highlightedLineFormat = new ChatFormatting[] { ChatFormatting.WHITE };
    private ChatFormatting[] normalFormat = new ChatFormatting[] { ChatFormatting.GRAY };

    private String highlightFallback = " ";
    private String prefixFormat = " %3d | ";

    private final List<String> source;

    public Component highlight(int row, int col) {
        var prefixed = ListUtils.mapWithIndex(source, (content, index) -> {
            var line = index + 1;
            var isHighlightedLine = line == row;

            var prefix = String.format(prefixFormat, line);

            if (isHighlightedLine) {
                return Text.append(Text.literal(prefix), highlightCode(content, col)).withStyle(highlightedLineFormat);
            } else {
                return Text.literal(prefix + content, ChatFormatting.GRAY);
            }
        });

        var result = ListUtils.subset(prefixed, row - 2, row + 2);
        var separator = Text.literal("\n");
        return Text.join(result, separator);
    }

    private Component highlightCode(String content, int col) {
        var normal = content.substring(0, col);
        var highlighted = content.substring(col);

        if (highlighted.isBlank()) {
            highlighted = highlightFallback;
        }

        return Text.append(
                Text.literal(normal),
                Text.literal(highlighted, highlightedTextFormat)
        );
    }
}
