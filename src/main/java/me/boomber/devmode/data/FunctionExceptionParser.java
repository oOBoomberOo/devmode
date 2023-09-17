package me.boomber.devmode.data;

import lombok.Data;
import net.minecraft.commands.CommandFunction;

import java.util.regex.Pattern;

/**
 * Due to a quirk in {@link CommandFunction#fromLines} api, any parsing exceptions bubbling up from that function is converted into {@link IllegalArgumentException} first.
 * <br>
 * This class try to recover information loss from that process by parsing the error message of {@link IllegalArgumentException}.
 */
@Data
public class FunctionExceptionParser {
    private final IllegalArgumentException exception;
    private Pattern pattern = Pattern.compile("Whilst parsing command on line (?<row>\\d+): (?<reason>.+(?=at position (?<column>\\d+).+).*|.+)", Pattern.CASE_INSENSITIVE);

    public FunctionException parse() {
        var matcher = pattern.matcher(exception.getMessage());

        if (matcher.find()) {
            var lineNumber = Integer.parseInt(matcher.group("row"));
            var reason = matcher.group("reason");
            var position = matcher.group("column");

            return new FunctionException(reason, lineNumber, position == null ? 0 : Integer.parseInt(position));
        }

        return new FunctionException("", -1, 0);
    }
}
