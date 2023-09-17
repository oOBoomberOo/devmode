package me.boomber.devmode.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.boomber.devmode.text.Text;
import me.boomber.devmode.text.TextHighlighter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FunctionParsingException extends RuntimeException {
    private final ResourceLocation id;
    private final List<String> source;
    private final String reason;
    private final int row;
    private final int col;

    FunctionParsingException(ResourceLocation id, List<String> source, String reason, int row, int col, Exception cause) {
        super(cause);
        this.id = id;
        this.source = source;
        this.reason = reason;
        this.row = row;
        this.col = col;
    }

    public String name() {
        return id.getPath();
    }

    private Component number(int value) {
        return Text.literal(Integer.toString(value), ChatFormatting.AQUA);
    }

    public Component formatMessage() {
        var fileName = Text.literal(name(), ChatFormatting.GREEN, ChatFormatting.UNDERLINE);
        var location = Text.format("%s:%s", number(row), number(col)).withStyle(ChatFormatting.DARK_AQUA);
        var errorMsg = Text.literal(reason, ChatFormatting.RED);
        var sourceCode = new TextHighlighter(source).highlight(row, col);

        return Text.format("""
                %s %s - %s
                %s
                """.stripIndent(),
                fileName, location, errorMsg, sourceCode);
    }

    public static FunctionParsingException from(ResourceLocation id, IllegalArgumentException exception, List<String> lines) {
        var result = new FunctionExceptionParser(exception).parse();
        return new FunctionParsingException(id, lines, result.reason(), result.line(), result.column(), exception);
    }
}
