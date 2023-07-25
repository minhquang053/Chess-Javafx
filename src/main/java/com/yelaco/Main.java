package com.yelaco;

import com.yelaco.chessgui.ChessApplication;
import com.yelaco.common.Game;
import com.yelaco.common.HumanPlayer;
import javafx.application.Application;

public class Main {

    public static void main(String[] args) {
        ChessApplication.launch();
        var game = new Game();
        game.init(new HumanPlayer(true), new HumanPlayer(false));
        var currentPlayer = game.getCurrentTurn();

        while (!game.isOver()) {
            try {
                var moves = getInput();
                if (!game.playerMove(
                        currentPlayer,
                        moves[0],
                        moves[1],
                        moves[2],
                        moves[3])
                ) {
                    System.out.println("Invalid move, try again");
                } else {
                    ChessApplication.switchScene();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Invalid move, try again");
            }

            currentPlayer = game.getCurrentTurn();
        }

        System.out.println("\n| Game ended with " + game.getStatus() + " |");
    }

    private static int[] getInput() {
        return null;
    }

}
