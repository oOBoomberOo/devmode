package me.boomber.devmode.data;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FunctionExceptionParserTest {
    FunctionParseResult parse(String reason, String source) {
        var exception = new IllegalArgumentException(reason);
        var sourceCode = new SourceCode(source.split("\n"));
        var location = new ResourceLocation("dummy.mcfunction");
        return new FunctionExceptionParser(exception, sourceCode, location).parse();
    }

    @Test
    void parse_invalid_command() {
        var result = parse("Unknown or invalid command '/say hi' on line 3 (did you mean 'say'? Do not use a preceding forwards slash.)",
                """
                        #built using mc-build (https://github.com/mc-build/mc-build)
                                                
                        /say hi
                        """
        );

        var expect = """
                dummy.mcfunction 3:0 - Unknown or invalid command '/say hi' on line 3 (did you mean 'say'? Do not use a preceding forwards slash.)
                   1 | #built using mc-build (https://github.com/mc-build/mc-build)
                   2 |\s
                   3 | /say hi\
                """;

        assertEquals(expect, result.format().getString());
    }

    @Test
    void parse_unknown_item() {
        var result = parse("Whilst parsing command on line 3: Unknown item 'minecraft:cookies' at position 8: give @s <--[HERE]",
                """
                        #built using mc-build (https://github.com/mc-build/mc-build)
                                                
                        give @s cookies
                        """);
        var expect = """
                dummy.mcfunction 3:8 - Unknown item 'minecraft:cookies' at position 8: give @s <--[HERE]
                   1 | #built using mc-build (https://github.com/mc-build/mc-build)
                   2 |\s
                   3 | give @s cookies\
                   """;
        assertEquals(expect, result.format().getString());
    }

    @Test
    void parse_expected_symbol() {
        var result = parse("Whilst parsing command on line 3: Expected ' ' at position 13: ...ticle dust<--[HERE]",
                """
                        #built using mc-build (https://github.com/mc-build/mc-build)
                        
                        particle dust
                        """);
        var expect = """
                dummy.mcfunction 3:13 - Expected ' ' at position 13: ...ticle dust<--[HERE]
                   1 | #built using mc-build (https://github.com/mc-build/mc-build)
                   2 |\s
                   3 | particle dust<--[HERE]\
                """;
        assertEquals(expect, result.format().getString());
    }

    @Test
    void parse_error_with_location() {
        var exception = new IllegalArgumentException("Whilst parsing command on line 2: Unknown command. Try /help for a list of commands");
        var source = new SourceCode("say test", "hello");
        var location = new ResourceLocation("functions/foo.mcfunction");
        var result = new FunctionExceptionParser(exception, source, location).parse();
        var expect = """
                functions/foo.mcfunction 2:0 - Unknown command. Try /help for a list of commands
                   1 | say test
                   2 | hello\
                """;

        assertEquals(expect, result.format().getString());
    }

    @Test
    void parse_with_cull_context() {
        var exception = new IllegalArgumentException("Whilst parsing command on line 4: Unknown command at position 12. Try /help for a list of commands");
        var source = new SourceCode(
                "give @s cookie",
                "# a",
                "say never gonna give you up",
                "execute run error",
                "# comment",
                "# b",
                "clear @s"
        );
        var location = new ResourceLocation("functions/foo.mcfunction");
        var result = new FunctionExceptionParser(exception, source, location).parse().format();
        var expect = """
                functions/foo.mcfunction 4:12 - Unknown command at position 12. Try /help for a list of commands
                   2 | # a
                   3 | say never gonna give you up
                   4 | execute run error
                   5 | # comment
                   6 | # b\
                """;
        assertEquals(expect, result.getString());
    }
}