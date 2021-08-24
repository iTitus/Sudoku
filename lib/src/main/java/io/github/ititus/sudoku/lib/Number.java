package io.github.ititus.sudoku.lib;

import java.util.*;

import static io.github.ititus.sudoku.lib.board.Board.SIZE;

public enum Number {

    NONE, N1, N2, N3, N4, N5, N6, N7, N8, N9;

    public static final Set<Number> VALID = Collections.unmodifiableSet(EnumSet.range(N1, N9));
    public static final List<Number> VALUES = List.of(values());

    public static Number of(int n) {
        if (n < 0 || n > SIZE) {
            throw new NoSuchElementException();
        }

        return VALUES.get(n);
    }

    public boolean isEmpty() {
        return this == NONE;
    }

    public boolean isPresent() {
        return this != NONE;
    }

    public int number() {
        return ordinal();
    }
}
