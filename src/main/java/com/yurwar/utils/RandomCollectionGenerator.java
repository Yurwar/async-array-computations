package com.yurwar.utils;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomCollectionGenerator {
    private Random random = new Random();

    public List<Double> generateDoubles(int size) {
        return IntStream.range(0, size)
                .mapToObj(index -> random.nextDouble())
                .collect(Collectors.toList());
    }

    public List<Integer> generateIntegers(int size, int min, int max) {
        return IntStream.range(0, size)
                .mapToObj(index -> random.nextInt((max - min) + 1) + min)
                .collect(Collectors.toList());
    }
}
