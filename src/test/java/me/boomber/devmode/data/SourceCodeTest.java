package me.boomber.devmode.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SourceCodeTest {
    @Test
    void split_do_not_overlap() {
        var source = new SourceCode("\n123");
        var splitPoint = new Range(2, 3);
        var result = source.split(splitPoint);

        assertEquals("\n1", result.before());
        assertEquals("2", result.highlighted());
        assertEquals("3", result.after());
    }

    @Test
    void split_multiline() {
        var source = new SourceCode("""
                
                line 1
                line 2
                line 3\
                """);
        var selection = source.line(1);
        var result = source.split(selection);

        assertEquals(new Range(7, 14), selection);
        assertEquals("\nline 2", source.substring(selection));

        assertEquals("\nline 1", result.before());
        assertEquals("\nline 2", result.highlighted());
        assertEquals("\nline 3", result.after());
    }

    @Test
    void split_all() {
        var source = new SourceCode("""
                
                line 1
                line 2
                line 3""");
        var result = source.split(source.range());

        assertEquals("", result.before());
        assertEquals(source.sourceCode(), result.highlighted());
        assertEquals("", result.after());
    }
}