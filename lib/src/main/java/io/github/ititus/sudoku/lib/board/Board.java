package io.github.ititus.sudoku.lib.board;

import io.github.ititus.commons.data.pair.Pair;
import io.github.ititus.sudoku.lib.Number;
import io.github.ititus.sudoku.lib.Pos;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Board {

    public static final int SIZE = 9;
    public static final int BLOCK_SIZE = 3;

    private static final boolean DO_LOGGING = false;
    private static final boolean LOG_ONLY_NUMBERS = true;

    private static final boolean ZERO_FOR_EMPTY = false;
    private static final boolean SPACES = true;

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

    private Board(Cell[] cells, Set<Pos> openPos) {
        this.cells = cells;
        this.openPos = openPos;
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
            List<String> lines = br.lines().filter(l -> !l.isEmpty() && !l.startsWith("#")).toList();
            if (lines.size() == 1) {
                String line = lines.get(0);
                if (line.length() == SIZE * SIZE) {
                    return fromEncoded(line);
                }
            }

            int y = 0;
            for (String line : lines) {
                if (y >= SIZE) {
                    throw new RuntimeException();
                }

                if (line.length() == SIZE) {
                    for (int x = 0; x < SIZE; x++) {
                        char c = line.charAt(x);
                        b.set(new Pos(x, y), Cell.of(Number.of(c == ' ' ? 0 : c - '0')));
                    }
                } else if (line.length() == 2 * SIZE - 1) {
                    for (int x = 0; x < SIZE; x++) {
                        char c = line.charAt(2 * x);
                        b.set(new Pos(x, y), Cell.of(Number.of(c == ' ' ? 0 : c - '0')));
                    }
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
            char c = encoded.charAt(i);
            b.set(new Pos(i % SIZE, i / SIZE), Cell.of(Number.of(c == ' ' ? 0 : c - '0')));
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
                backtrack();
            }
        }
    }

    private void logic() {
        log("Logic Start");
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
                        if (otherCell.number() == cell.number() && !other.equals(pos)) {
                            throw new RuntimeException();
                        }

                        continue;
                    }

                    Cell newCell = otherCell.withRemovedPossibility(cell.number());
                    if (set(other, newCell)) {
                        if (LOG_ONLY_NUMBERS) {
                            if (newCell.isPresent()) {
                                log("Simple@" + other + ": " + newCell);
                            }
                        } else {
                            log("Simple@" + other + ": " + otherCell + " -> " + newCell);
                        }
                        change = true;
                    }
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
                Cell oldCell = get(singlePos);
                Cell newCell = Cell.of(n);
                if (set(singlePos, newCell)) {
                    if (LOG_ONLY_NUMBERS) {
                        if (newCell.isPresent()) {
                            log("Intermediate@" + singlePos + ": " + newCell);
                        }
                    } else {
                        log("Intermediate@" + singlePos + ": " + oldCell + " -> " + newCell);
                    }
                    change = true;
                }
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

            Cell newCell = cell.withRemovedPossibility(n);
            if (set(pos, newCell)) {
                if (LOG_ONLY_NUMBERS) {
                    if (newCell.isPresent()) {
                        log("Advanced@" + pos + ": " + newCell);
                    }
                } else {
                    log("Advanced@" + pos + ": " + cell + " -> " + newCell);
                }
                change = true;
            }
        }

        return change;
    }

    private void backtrack() {
        log("Backtrack Start");
        Pair<Pos, Cell> posCellPair = openPos.stream()
                .map(p -> Pair.of(p, get(p)))
                .min(Comparator.comparingInt((Pair<Pos, Cell> p) -> p.b().numberOfPossibilities()).thenComparingInt((Pair<Pos, Cell> p) -> p.a().index()))
                .orElseThrow();

        Pos pos = posCellPair.a();
        Cell cell = posCellPair.b();
        for (Number possibility : cell.possibilities()) {
            Board copy = copy();
            copy.set(pos, Cell.of(possibility));
            log("Backtrack Set@" + pos + ": " + possibility);

            try {
                copy.solve();
            } catch (Exception ignored) {
                log("Backtrack Reset@" + pos + ": " + possibility);
                continue;
            }

            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    Pos p = new Pos(x, y);
                    set(p, copy.get(p));
                }
            }

            return;
        }

        throw new RuntimeException();
    }

    public Board copy() {
        return new Board(Arrays.copyOf(cells, cells.length), new HashSet<>(openPos));
    }

    public void print() {
        print(System.out);
    }

    public void print(PrintStream out) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (SPACES && x > 0) {
                    out.print(' ');
                }

                Number n = get(new Pos(x, y)).number();
                if (ZERO_FOR_EMPTY || n.isPresent()) {
                    out.print(n.number());
                } else {
                    out.print(' ');
                }
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

    private static void log(String s) {
        if (DO_LOGGING) {
            System.out.println(s);
        }
    }
}
