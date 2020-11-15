package com.yurwar;

import com.yurwar.utils.RandomCollectionGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class AsyncArraysComputationController {
    private static final int DEFAULT_ARRAY_SIZE = 10;
    private static final String ARRAY_PRINT_FORMAT = "%s: %s%n";
    private final RandomCollectionGenerator generator;

    public AsyncArraysComputationController() {
        this.generator = new RandomCollectionGenerator();
    }

    public void executeTask() {
        List<Integer> c1 = generator.generateIntegers(DEFAULT_ARRAY_SIZE, -10, 10);
        List<Integer> c2 = generator.generateIntegers(DEFAULT_ARRAY_SIZE, -10, 10);
        List<Integer> c3 = generator.generateIntegers(DEFAULT_ARRAY_SIZE, -10, 10);


        printArray(c1, "First array");
        printArray(c2, "Second array");
        printArray(c1, "Third array");

        var multipliedC1Future = supplyAsync(() -> multiplyElements(c1, 2))
                .thenApplyAsync(this::sortList);

        var evenC2Future = supplyAsync(() -> filterElements(c2, el -> el % 2 == 0))
                .thenApplyAsync(this::sortList);

        var c3MaxElementFuture = supplyAsync(() ->
                c3.stream().max(Integer::compareTo));

        var inRangeFromMaxC3Future = c3MaxElementFuture
                .thenApplyAsync(maxOpt -> filterElements(c3, el -> maxOpt.map(max ->
                        isElementInRangeFromAnother(el, max, 4, 6))
                        .orElse(false)))
                .thenApplyAsync(this::sortList);

        printArray(getResultFromFuture(multipliedC1Future), "Multiplied first array");
        printArray(getResultFromFuture(evenC2Future), "Even second array");
        printArray(getResultFromFuture(inRangeFromMaxC3Future), "In range third array");

        var intersectedListsFuture = evenC2Future
                .thenCombineAsync(inRangeFromMaxC3Future, this::intersect);

        var result = intersectedListsFuture
                .thenCombineAsync(multipliedC1Future, this::difference)
                .thenApplyAsync(this::sortList);

        printArray(getResultFromFuture(intersectedListsFuture), "Intersected second and third");
        printArray(getResultFromFuture(result), "Intersected subtract first");
    }

    private List<Integer> getResultFromFuture(CompletableFuture<List<Integer>> multipliedC1Future) {
        try {
            return multipliedC1Future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isElementInRangeFromAnother(int el, int givenEl, int from, int to) {
        return isInRange(el, givenEl + from, givenEl + to) ||
                isInRange(el, givenEl - to, givenEl - from);
    }

    private List<Integer> sortList(List<Integer> list) {
        return list.stream().sorted().collect(Collectors.toList());
    }

    private boolean isInRange(int el, int lower, int upper) {
        return el >= lower && el <= upper;
    }

    private List<Integer> multiplyElements(List<Integer> elements, int multiplier) {
        return elements.stream()
                .map(el -> el * multiplier).collect(Collectors.toList());
    }

    private List<Integer> filterElements(List<Integer> elements, Predicate<Integer> predicate) {
        return elements.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    private List<Integer> intersect(List<Integer> first, List<Integer> second) {
        return first.stream()
                .distinct()
                .filter(second::contains)
                .collect(Collectors.toList());
    }

    private List<Integer> difference(List<Integer> diminishing, List<Integer> subtract) {
        List<Integer> result = new ArrayList<>(List.copyOf(diminishing));
        result.removeAll(subtract);
        return result;
    }

    private void printArray(List<Integer> list, String listName) {
        System.out.printf(ARRAY_PRINT_FORMAT, listName, list);
    }
}
