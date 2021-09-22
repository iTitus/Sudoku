package io.github.ititus.sudoku.app;

import io.github.ititus.sudoku.lib.board.Board;

public class App {

    public static void main(String[] args) {
        solveBoard(Board.loadVisual("simple_board.txt"));
        solveBoard(Board.fromEncoded("030000000000195000008000060800060000400800001000020000060000280000419005000000070"));
        solveBoard(Board.fromEncoded("300600000500007000870090400080050000064000790000020030001040078000300002000005004"));
        solveBoard(Board.fromEncoded("023065089900004005500900000600300018380590002000086300230000006807020003096053820"));
    }

    private static void solveBoard(Board board) {
        board.print();
        System.out.println("#".repeat(80));
        board.solve();
        board.print();

        System.out.println();
        System.out.println("#".repeat(120));
        System.out.println();
    }
}
