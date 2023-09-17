package me.boomber.devmode.utils;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ListUtils {
    public <T> List<T> subset(List<T> list, int from, int to) {
        var a = Math.max(from, 0);
        var b = Math.min(to, list.size());
        return list.subList(a, b);
    }

    public <T, U> List<U> mapWithIndex(List<T> input, MapWithIndex<T, U> func) {
        var result = new ArrayList<U>();

        for (int i = 0; i < input.size(); i++) {
            var value = input.get(i);
            var mapped = func.applyWithIndex(value, i);
            result.add(mapped);
        }

        return result;
    }

    @FunctionalInterface
    public interface MapWithIndex<T, U> {
        U applyWithIndex(T t, int index);
    }
}
