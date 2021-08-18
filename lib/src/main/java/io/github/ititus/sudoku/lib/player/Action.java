package io.github.ititus.sudoku.lib.player;

import io.github.ititus.sudoku.lib.Number;
import io.github.ititus.sudoku.lib.Pos;

public record Action(
        Pos pos,
        Number number
) {
}
