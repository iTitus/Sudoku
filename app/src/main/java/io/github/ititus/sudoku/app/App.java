package io.github.ititus.sudoku.app;

import io.github.ititus.sudoku.lib.board.Board;

public class App {

    public static void main(String[] args) {
        Board simple = Board.load("simple_board.txt");
        simple.print();
        System.out.println("#".repeat(80));
        simple.updatePossibilitiesUntilDone();
        simple.print();
    }
}
