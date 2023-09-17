package me.boomber.devmode.data;

import lombok.EqualsAndHashCode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class FunctionParseException extends RuntimeException {
    private final ResourceLocation location;
    private final FunctionParseResult parseResult;

    public ResourceLocation location() {
        return location;
    }

    public Component format() {
        return parseResult.format();
    }

    FunctionParseException(ResourceLocation location, FunctionParseResult parseResult, Exception cause) {
        super(cause);
        this.location = location;
        this.parseResult = parseResult;
    }

    public static FunctionParseException from(ResourceLocation id, IllegalArgumentException exception, List<String> lines) {
        var sourceCode = new SourceCode(lines);
        var parseResult = new FunctionExceptionParser(exception, sourceCode, id).parse();
        return new FunctionParseException(id, parseResult, exception);
    }
}
