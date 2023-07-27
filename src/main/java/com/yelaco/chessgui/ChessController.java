package com.yelaco.chessgui;

import com.yelaco.common.Game;
import com.yelaco.common.GameStatus;
import com.yelaco.common.Player;
import com.yelaco.common.HumanPlayer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ChessController implements Initializable {
    private Game game;
    private Player currentPlayer;
    private boolean makingMove = false;
    private ImageView movingPiece = null;
    private BorderPane[][] spots = new BorderPane[8][8];

    @FXML
    private AnchorPane apane;
    @FXML
    private GridPane gpane;

    public void displayResult() {
        var resultDialog = new Dialog<String>();
        resultDialog.setTitle("Result");
        resultDialog.setContentText("Game ended with " + game.getStatus());
        resultDialog.getDialogPane().getButtonTypes().add(new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        resultDialog.showAndWait();
    }

    public void processMove(MouseEvent event) {
        var imgView = (ImageView) event.getSource();
        var pane = (BorderPane) imgView.getParent();

        // if you pick a piece to move, the next click must be the spot you want to move to
        if (makingMove) {
            try {
                var move = filterInput(movingPiece.getParent().getId(), pane.getId());
                if (game.playerMove(currentPlayer, move[0], move[1], move[2], move[3])) {
                    // move piece
                    var movesPlayed = game.getMovesPlayed();
                    var ltmove = movesPlayed.get(movesPlayed.size()-1);
                    var startView = (ImageView) spots[ltmove.getStart().getX()][ltmove.getStart().getY()].getCenter();
                    var endView = (ImageView) spots[ltmove.getEnd().getX()][ltmove.getEnd().getY()].getCenter();

                    endView.setImage(movingPiece.getImage());
                    startView.setImage(null);
                    //
                    if (ltmove.isCastlingMove()) {
                        var rookStart = (ImageView) spots[ltmove.getRookCastled()[0].getX()][ltmove.getRookCastled()[0].getY()].getCenter();
                        var rookEnd = (ImageView) spots[ltmove.getRookCastled()[1].getX()][ltmove.getRookCastled()[1].getY()].getCenter();
                        rookEnd.setImage(rookStart.getImage());
                        rookStart.setImage(null);
                    } else if (ltmove.isEnpassant()) {
                        var spotKilled = (ImageView) spots[ltmove.getSpotKilled().getX()][ltmove.getSpotKilled().getY()].getCenter();
                        spotKilled.setImage(null);
                    } else if (ltmove.isPromotion()) {
                        var pieceImg = "queen1.png";
                        if (ltmove.getPieceMoved().isWhite()) {
                            pieceImg = "queen2.png";
                        }
                        var rootpath = new File(endView.getImage().getUrl()).getParent();
                        if (rootpath.contains("\\")) {
                            endView.setImage(new Image(rootpath + "\\" + pieceImg));
                        } else {
                            endView.setImage(new Image(rootpath + "/" + pieceImg));
                        }
                    }
                } else {
                    System.out.println("Invalid moved from " + move[0] + move[1] + " to " + move[2] + move[3]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                makingMove = false;
                movingPiece = null;
            }

            currentPlayer = game.getCurrentTurn();
            if (game.isOver()) {
                System.out.println("Game ended with " + game.getStatus());
                apane.setDisable(true);
                displayResult();
            }
        } else if (imgView.getImage() != null){
            // wait for the end spot
            makingMove = true;
            movingPiece = imgView;
        }
    }

    private int[] filterInput(String moveFrom, String moveTo) throws Exception {
        if (moveFrom.length() != 2 || moveTo.length() != 2) {
            throw new Exception("Invalid input: " + moveFrom + " " + moveTo);
        }

        int[] moves = new int[4];
        moves[0] = moveFrom.charAt(0) - 'a';
        moves[1] = moveFrom.charAt(1) - '0' - 1;
        moves[2] = moveTo.charAt(0) - 'a';
        moves[3] = moveTo.charAt(1) - '0' - 1;

        for (int i = 0; i < 4; i++) {
            if (moves[i] < 0 || moves[i] > 7) {
                throw new Exception("Invalid input. Please input like this \"e2 e4\" -> move piece from e2 to e4");
            }
        }

        return moves;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        game = new Game();
        game.init(new HumanPlayer(true), new HumanPlayer(false));
        currentPlayer = game.getCurrentTurn();

        int idx = 0;
        for (int j = 7; j >= 0; j--) {
            for (int i = 0; i < 8; i++) {
                spots[i][j] = (BorderPane) gpane.getChildren().get(idx++);
            }
        }
    }
}