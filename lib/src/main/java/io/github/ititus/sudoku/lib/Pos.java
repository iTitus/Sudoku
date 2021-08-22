package io.github.ititus.sudoku.lib;

import java.util.ArrayList;
import java.util.List;

import static io.github.ititus.sudoku.lib.board.Board.BLOCK_SIZE;
import static io.github.ititus.sudoku.lib.board.Board.SIZE;

public record Pos(
        int x,
        int y
) {

    public Pos {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
            throw new IllegalArgumentException();
        }
    }

    public int index() {
        return x + y * SIZE;
    }

    public List<Pos> allGroups() {
        List<Pos> l = new ArrayList<>(3 * SIZE);
        l.addAll(horizontal());
        l.addAll(vertical());
        l.addAll(block());
        return l;
    }

    public List<Pos> horizontal() {
        List<Pos> l = new ArrayList<>(SIZE);
        for (int x = 0; x < SIZE; x++) {
            l.add(new Pos(x, y));
        }

        return l;
    }

    public List<Pos> vertical() {
        List<Pos> l = new ArrayList<>(SIZE);
        for (int y = 0; y < SIZE; y++) {
            l.add(new Pos(x, y));
        }

        return l;
    }

    public List<Pos> block() {
        int startX = 3 * (x / 3);
        int startY = 3 * (y / 3);

        List<Pos> l = new ArrayList<>(SIZE);
        for (int y = 0; y < BLOCK_SIZE; y++) {
            for (int x = 0; x < BLOCK_SIZE; x++) {
                l.add(new Pos(startX + x, startY + y));
            }
        }

        return l;
    }
}
