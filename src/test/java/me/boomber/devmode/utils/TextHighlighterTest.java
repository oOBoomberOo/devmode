package me.boomber.devmode.utils;

import me.boomber.devmode.data.Range;
import me.boomber.devmode.data.SourceCode;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextHighlighterTest {
    SourceCode sourceCode(String source) {
        return new SourceCode(source);
    }

    Component highlight(SourceCode source, Range highlight) {
        var highlighter = new TextHighlighter(source);
        highlighter.setPrefixFormat("%d|");
        return highlighter.highlight(source.range(), highlight);
    }

    @Test
    void prefixed_all_lines_with_line_number() {
        var source = sourceCode("""
                
                line 1
                line 2
                line 3\
                """);
        var result = highlight(source, source.line(1));
        var expect = """
                
                1|line 1
                2|line 2
                3|line 3\
                """;
        assertEquals(expect, result.getString());
    }

    @Test
    void prefixed_single_line() {
        var source = sourceCode("\nsingle line");
        var result = highlight(source, source.line(0));
        var expect = "\n1|single line";
        assertEquals(expect, result.getString());
    }
}