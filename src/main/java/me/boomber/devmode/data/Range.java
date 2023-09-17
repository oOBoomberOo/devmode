package me.boomber.devmode.data;

public record Range(int start, int end) {
    public Range withStart(int start) {
        return new Range(start, end);
    }

    public Range withEnd(int end) {
        return new Range(start, end);
    }

    public Range add(int start, int end) {
        return new Range(this.start + start, this.end + end);
    }

    public boolean contains(Range splitPoint) {
        return start <= splitPoint.start() && splitPoint.end() <= end;
    }
}
