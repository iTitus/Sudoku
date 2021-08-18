package io.github.ititus.sudoku.lib.board;

import io.github.ititus.sudoku.lib.Number;

public interface BoardPrinter {

    void print(Board board);

    void announceGameEnd(Board board, Number winner);

}
