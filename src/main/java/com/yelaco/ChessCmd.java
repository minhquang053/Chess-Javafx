package com.yelaco;

import com.yelaco.common.Game;
import com.yelaco.common.HumanPlayer;

import java.util.Scanner;

public class ChessCmd {
    private static int[] filterInput(String move) throws Exception {
        if (move.length() > 5 || move.charAt(2) != ' ') {
            throw new Exception("Invalid input. Please input like this \"e2 e4\" -> move piece from e2 to e4");
        }

        int[] moves = new int[4];
        moves[0] = move.charAt(0) - 'a';
        moves[1] = move.charAt(1) - '0' - 1;
        moves[2] = move.charAt(3) - 'a';
        moves[3] = move.charAt(4) - '0' - 1;

        for (int i = 0; i < 4; i++) {
            if (moves[i] < 0 || moves[i] > 7) {
                throw new Exception("Invalid input. Please input like this \"e2 e4\" -> move piece from e2 to e4");
            }
        }

        return moves;
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.init(new HumanPlayer(true), new HumanPlayer(false));

        var currentPlayer = game.getCurrentTurn();

        Scanner sc = new Scanner(System.in);

        while(!game.isOver()) {
            if (currentPlayer.isWhiteSide()) {
                System.out.print("White to move: ");
            } else {
                System.out.print("Black to move: ");
            }
            String move = sc.nextLine();

            try {
                var moves = filterInput(move);
                if (!game.playerMove(
                        currentPlayer,
                        moves[0],
                        moves[1],
                        moves[2],
                        moves[3])
                ) {
                    System.out.println("Invalid move, try again");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Invalid move, try again");
            }
            currentPlayer = game.getCurrentTurn();
        }

        System.out.println("\n| Game ended with " + game.getStatus() + " |");
    }
}