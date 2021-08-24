package io.github.ititus.sudoku.lib.board;

import io.github.ititus.sudoku.lib.Number;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class Cell {

    private static final Cell EMPTY = new Cell(Number.NONE, Number.VALID);

    private final Number number;
    private final Set<Number> possibilities;
    private Set<Number> possibilitiesView;

    private Cell(Number number, Set<Number> possibilities) {
        if ((number.isPresent() && !possibilities.isEmpty()) || (number.isEmpty() && possibilities.size() <= 1)) {
            throw new IllegalArgumentException();
        }

        this.number = number;
        this.possibilities = possibilities;
    }

    public static Cell empty() {
        return EMPTY;
    }

    public static Cell of(Number number) {
        if (number.isEmpty()) {
            return empty();
        }

        return new Cell(number, Set.of());
    }

    public Cell withRemovedPossibility(Number possibility) {
        if (possibility.isEmpty()) {
            throw new IllegalArgumentException();
        } else if (number.isPresent()) {
            throw new IllegalStateException();
        } else if (!this.possibilities.contains(possibility)) {
            return this;
        }

        Set<Number> possibilities = EnumSet.copyOf(this.possibilities);
        possibilities.remove(possibility);

        if (possibilities.size() == 1) {
            return new Cell(possibilities.iterator().next(), Set.of());
        }

        return new Cell(number, possibilities);
    }

    public Number number() {
        return number;
    }

    public Set<Number> possibilities() {
        if (possibilitiesView == null) {
            possibilitiesView = possibilities.isEmpty() ? Set.of() : Collections.unmodifiableSet(possibilities);
        }

        return possibilitiesView;
    }

    public int numberOfPossibilities() {
        return possibilities.size();
    }

    public boolean hasPossibility(Number n) {
        return possibilities.contains(n);
    }

    public boolean isEmpty() {
        return number.isEmpty();
    }

    public boolean isPresent() {
        return number.isPresent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Cell)) {
            return false;
        }

        Cell cell = (Cell) o;
        return number == cell.number && possibilities.equals(cell.possibilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, possibilities);
    }
}
