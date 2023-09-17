package me.boomber.devmode.data;

import lombok.Data;
import me.boomber.devmode.data.pattern.FunctionParsePattern;
import net.minecraft.commands.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Due to a quirk in {@link CommandFunction#fromLines} api, any parsing exceptions bubbling up from that function is converted into {@link IllegalArgumentException} first.
 * <br>
 * This class try to recover information loss from that process by parsing the error message of {@link IllegalArgumentException}.
 */
@Data
public class FunctionExceptionParser {
    @NotNull
    private final IllegalArgumentException exception;
    @NotNull
    private final SourceCode sourceCode;
    @NotNull
    private final ResourceLocation resourceLocation;

    private FunctionParsePattern parseWithPosition = new FunctionParsePattern("Whilst parsing command on line (?<row>\\d+): (?<reason>.+ at position (?<column>\\d+).+)");
    private FunctionParsePattern parseWithoutPosition = new FunctionParsePattern("Whilst parsing command on line (?<row>\\d+): (?<reason>.+)");
    private FunctionParsePattern unknownCommand = new FunctionParsePattern("(?<reason>Unknown or invalid command .+ on line (?<row>\\d+) .+)");

    private List<FunctionParsePattern> patterns = List.of(parseWithPosition, parseWithoutPosition, unknownCommand);

    private int contextSize = 2;

    public FunctionParseResult parse() {
        for (var pattern : patterns) {
            var match = pattern.match(exception.getMessage());

            if (match == null) continue;

            var lineNumber = match.row() - 1;
            var reason = match.reason();
            var position = match.column();

            var contentRange = sourceCode.line(lineNumber - contextSize, lineNumber + contextSize);
            var highlightRange = sourceCode.line(lineNumber).add(position + 1, 0);

            return new FunctionParseResult(resourceLocation, sourceCode, reason, contentRange, highlightRange);
        }

        return new FunctionParseResult(resourceLocation, sourceCode, exception.getMessage(), sourceCode.range(), sourceCode.range());
    }
}
