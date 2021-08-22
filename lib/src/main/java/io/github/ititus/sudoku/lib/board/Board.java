package io.github.ititus.sudoku.lib.board;

import io.github.ititus.sudoku.lib.Number;
import io.github.ititus.sudoku.lib.Pos;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board {

    public static final int SIZE = 9;
    public static final int BLOCK_SIZE = 3;

    private final Cell[] cells;
    private final Set<Pos> openPos;

    private Board() {
        this.cells = new Cell[SIZE * SIZE];
        this.openPos = new HashSet<>();
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                set(new Pos(x, y), Cell.empty());
            }
        }
    }

    public static Board loadVisual(String name) {
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

    public static Board fromEncoded(String encoded) {
        if (encoded.length() != SIZE * SIZE) {
            throw new IllegalArgumentException();
        }

        Board b = new Board();
        for (int i = 0; i < SIZE * SIZE; i++) {
            int n = encoded.charAt(i) - '0';
            if (n < 0 || n > SIZE) {
                throw new IllegalArgumentException();
            }

            b.set(new Pos(i % SIZE, i / SIZE), Cell.of(Number.of(n)));
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
        if (cell.isPresent()) {
            openPos.remove(pos);
        } else {
            openPos.add(pos);
        }

        return true;
    }

    public boolean isSolved() {
        return openPos.isEmpty();
    }

    public void solve() {
        if (!isSolved()) {
            logic();
            if (!isSolved()) {
                // TODO: backtrack
                throw new UnsupportedOperationException("backtracking not supported yet");
            }
        }
    }

    private void logic() {
        while (!isSolved()) {
            if (simple()) {
                continue;
            }

            if (intermediate()) {
                continue;
            }

            if (advanced()) {
                continue;
            }

            break;
        }
    }

    private boolean simple() {
        boolean change = false;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                Pos pos = new Pos(x, y);
                Cell cell = get(pos);
                if (cell.isEmpty()) {
                    continue;
                }

                for (Pos other : pos.allGroups()) {
                    Cell otherCell = get(other);
                    if (otherCell.isPresent()) {
                        continue;
                    }

                    change |= set(other, otherCell.withRemovedPossibility(cell.number()));
                }
            }
        }

        return change;
    }

    private boolean intermediate() {
        for (int y = 0; y < SIZE; y++) {
            Pos pos = new Pos(0, y);
            if (intermediate(pos.horizontal())) {
                return true;
            }
        }

        for (int x = 0; x < SIZE; x++) {
            Pos pos = new Pos(x, 0);
            if (intermediate(pos.vertical())) {
                return true;
            }
        }

        for (int i = 0; i < SIZE; i++) {
            Pos pos = new Pos(3 * (i % 3), 3 * (i / 3));
            if (intermediate(pos.block())) {
                return true;
            }
        }

        return false;
    }

    private boolean intermediate(List<Pos> group) {
        boolean change = false;
        outer:
        for (Number n : Number.VALID) {
            Pos singlePos = null;
            for (Pos pos : group) {
                Cell cell = get(pos);
                if (cell.isPresent() || !cell.hasPossibility(n)) {
                    continue;
                } else if (singlePos != null) {
                    continue outer;
                }

                singlePos = pos;
            }

            if (singlePos != null) {
                change |= set(singlePos, Cell.of(n));
            }
        }

        return change;
    }

    private boolean advanced() {
        for (int i = 0; i < SIZE; i++) {
            Pos pos = new Pos(3 * (i % 3), 3 * (i / 3));
            if (advanced(pos.block())) {
                return true;
            }
        }

        return false;
    }

    private boolean advanced(List<Pos> block) {
        for (Number n : Number.VALID) {
            List<Pos> line = new ArrayList<>();
            for (Pos pos : block) {
                Cell cell = get(pos);
                if (cell.isPresent() || !cell.hasPossibility(n)) {
                    continue;
                }

                line.add(pos);
            }

            if (line.size() == 1) {
                throw new IllegalStateException();
            } else if (line.size() < 2 || line.size() > 3) {
                continue;
            }

            Pos first = line.get(0);
            if (advanced(n, line, first.horizontal())) {
                return true;
            } else if (advanced(n, line, first.vertical())) {
                return true;
            }
        }

        return false;
    }

    private boolean advanced(Number n, List<Pos> line, List<Pos> group) {
        if (!group.containsAll(line)) {
            return false;
        }

        boolean change = false;
        for (Pos pos : group) {
            if (line.contains(pos)) {
                continue;
            }

            Cell cell = get(pos);
            if (cell.isPresent()) {
                continue;
            }

            change |= set(pos, cell.withRemovedPossibility(n));
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

    public String encode() {
        StringBuilder b = new StringBuilder(SIZE * SIZE);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                b.append(get(new Pos(x, y)).number().number());
            }
        }

        return b.toString();
    }
}
