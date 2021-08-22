package io.github.ititus.sudoku.app;

import io.github.ititus.sudoku.lib.board.Board;

public class App {

    public static void main(String[] args) {
        Board simple1 = Board.loadVisual("simple_board.txt");
        simple1.print();
        System.out.println("#".repeat(80));
        simple1.solve();
        simple1.print();

        System.out.println();
        System.out.println("#".repeat(80));
        System.out.println();
        Board simple2 = Board.fromEncoded("030000000000195000008000060800060000400800001000020000060000280000419005000000070");
        simple2.print();
        System.out.println("#".repeat(80));
        simple2.solve();
        simple2.print();
    }
}
