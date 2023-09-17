package me.boomber.devmode.data;

import me.boomber.devmode.utils.StringUtils;

import java.util.List;

public record SourceCode(String sourceCode) {
    public SourceCode(List<String> lines) {
        this("\n" + String.join("\n", lines));
    }

    public SourceCode(String... lines) {
        this(List.of(lines));
    }

    public Range range() {
        return new Range(0, sourceCode.length());
    }

    public String substring(Range span) {
        return sourceCode.substring(span.start(), span.end());
    }

    public Range line(int lineNumber) {
        return line(lineNumber, lineNumber);
    }

    public Range line(int lineFrom, int lineTo) {
        var start = 0;
        var end = sourceCode.length();

        var chars = sourceCode.toCharArray();
        var line = 0;

        for (int i = 0; i < chars.length; i++) {
            var c = chars[i];

            if (c == '\n') {
                if (line <= lineFrom) {
                    start = i;
                }

                if (line > lineTo) {
                    end = i;
                    break;
                }

                line += 1;
            }
        }

        return new Range(start, end);
    }

    public SplitSourceCode split(Range splitPoint) {
        return split(splitPoint, range());
    }

    public SplitSourceCode split(Range splitPoint, Range content) {
        assert content.contains(splitPoint);

        return new SplitSourceCode(
            sourceCode.substring(content.start(), splitPoint.start()),
            sourceCode.substring(splitPoint.start(), splitPoint.end()),
            sourceCode.substring(splitPoint.end(), Math.min(sourceCode.length(), content.end()))
        );
    }

    public Range intersectedLines(Range span) {
        var skipped = sourceCode.substring(0, span.start());
        var rest = sourceCode.substring(span.start(), span.end());

        var start = StringUtils.lineCount(skipped);
        var end = start + StringUtils.lineCount(rest);

        return new Range(start, end);
    }

    public record SplitSourceCode(String before, String highlighted, String after) {
        public String join() {
            return before + highlighted + after;
        }
    }
}
