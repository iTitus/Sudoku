package io.github.ititus.sudoku.lib.board;

import io.github.ititus.sudoku.lib.Number;
import io.github.ititus.sudoku.lib.Pos;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Board {

    public static final int SIZE = 9;
    public static final int BLOCK_SIZE = 3;

    private final Cell[] cells;

    private Board() {
        this.cells = new Cell[SIZE * SIZE];
    }

    public static Board load(String name) {
        if (!name.startsWith("/")) {
            name = "/" + name;
        }

        InputStream is = Board.class.getResourceAsStream(name);
        if (is == null) {
            throw new RuntimeException();
        }

        Board b = new Board();
        try (is; BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<String> lines = br.lines().toList();
            int y = 0;
            for (String line : lines) {
                if (y >= SIZE) {
                    throw new RuntimeException();
                } else if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                String[] split = line.split(" ");
                if (split.length != SIZE) {
                    throw new RuntimeException();
                }

                for (int x = 0; x < SIZE; x++) {
                    int n = Integer.parseInt(split[x]);
                    b.set(new Pos(x, y), Cell.of(Number.of(n)));
                }

                y++;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return b;
    }

    public Cell get(Pos pos) {
        return cells[pos.index()];
    }

    public boolean set(Pos pos, Cell cell) {
        Cell old = get(pos);
        if (cell.equals(old)) {
            return false;
        }

        cells[pos.index()] = cell;
        return true;
    }

    public void updatePossibilitiesUntilDone() {
        // noinspection StatementWithEmptyBody
        while (updatePossibilities()) {
        }
    }

    public boolean updatePossibilities() {
        boolean change = false;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                Pos pos = new Pos(x, y);
                Cell cell = get(pos);
                if (cell.isPresent()) {
                    for (Pos other : pos.allGroups()) {
                        Cell otherCell = get(other);
                        if (otherCell.isEmpty()) {
                            change |= set(other, otherCell.withRemovedPossibility(cell.number()));
                        }
                    }
                }
            }
        }

        return change;
    }

    public void print() {
        print(System.out);
    }

    public void print(PrintStream out) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (x > 0) {
                    out.print(' ');
                }

                out.print(get(new Pos(x, y)).number().number());
            }

            out.println();
        }
    }
}
