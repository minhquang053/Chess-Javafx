package com.yelaco.chessgui;

import com.yelaco.common.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.io.File;
import java.net.URL;
import java.util.*;

public class ChessController implements Initializable {
    public Game game;
    private Player currentPlayer;
    private boolean makingMove = false;
    private ImageView movingPiece = null;
    private String rootPath = null;
    private String delimiter = null;

    private ArrayList<ImageView> showMoveImgView;
    private HashMap<BorderPane, String> canMovePane;
    private BorderPane[][] spots = new BorderPane[8][8];
    private ChessTimer task;
    private Timer timer;

    @FXML
    private AnchorPane apane;
    @FXML
    private GridPane gpane;

    // player white clock
    @FXML
    public Label p1clock;

    //player black clock
    @FXML
    public Label p2clock;


    public void displayResult() {
        var resultDialog = new Dialog<String>();
        resultDialog.setTitle("Result");
        resultDialog.setContentText("Game ended with " + game.getStatus());
        resultDialog.getDialogPane().getButtonTypes().add(new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        resultDialog.showAndWait();
    }

    public void displayGameOver() {
        System.out.println("Game ended with " + game.getStatus());
        apane.setDisable(true);
        timer.cancel();
        timer.purge();
        displayResult();
    }

    private void playSound(SoundEffect gameSound) {
        ChessAudio.playSound(gameSound);
    }

    private void makeMoveSound(Move move) {
        if (game.isOver()) {
            switch (game.getStatus()) {
                case STALEMATE -> {
                    playSound(SoundEffect.STALEMATE);
                }
                case BLACK_WIN, WHITE_WIN -> {
                    playSound(SoundEffect.CHECKMATE);
                }
            }
            return;
        }
        if (move.isCheckMove()) {
            playSound(SoundEffect.CHECK);
        } else if (move.isCastlingMove()){
            playSound(SoundEffect.CASTLING);
        } else if (move.isPromotion()){
            playSound(SoundEffect.PROMOTION);
        } else if (move.getPieceKilled() != null || move.isEnpassant()) {
            playSound(SoundEffect.CAPTURE_PIECE);
        }else {
            playSound(SoundEffect.MAKE_MOVE);
        }
    }

    private void showAvailableMoves(boolean isShow) {
        if (isShow) {
            Image moveDot = new Image(rootPath + delimiter + "img" + delimiter + "moveDot.png");
            String movePane = movingPiece.getParent().getId();

            int mX = movePane.charAt(0) - 'a';
            int mY = movePane.charAt(1) - '0' - 1;
            var availMoves = game.getAvailiableMove(mX, mY);
            for (String move: availMoves) {
                var bpane = (BorderPane) spots[move.charAt(0) - '0'][move.charAt(1) - '0'];
                var moveImgView = (ImageView) spots[move.charAt(0) - '0'][move.charAt(1) - '0'].getCenter();
                if (moveImgView.getImage() == null) {
                    moveImgView.setImage(moveDot);
                } else {
                    canMovePane.put(bpane, bpane.getStyle());
                    bpane.setStyle("-fx-background-color:green");
                }
                showMoveImgView.add(moveImgView);
            }
        } else {
            for (ImageView imgView : showMoveImgView) {
                if (imgView.getImage().getUrl().contains("moveDot.png")) imgView.setImage(null);
            }
            for (BorderPane bpane: canMovePane.keySet()) {
                var prevStyle = (String) canMovePane.get(bpane);
                bpane.setStyle(prevStyle);
            }
            showMoveImgView.clear();
            canMovePane.clear();
        }
    }

    public void processMove(MouseEvent event) {
        var imgView = (ImageView) event.getSource();
        var pane = (BorderPane) imgView.getParent();

        // if you pick a piece to move, the next click must be the spot you want to move to
        if (makingMove) {
            showAvailableMoves(false);

            try {
                var move = filterInput(movingPiece.getParent().getId(), pane.getId());
                var moveStat = game.playerMove(currentPlayer, move[0], move[1], move[2], move[3]);

                if (moveStat == MoveStatus.SUCCESS) {
                    currentPlayer = game.getCurrentTurn();
                    task.switchTurn(currentPlayer.isWhiteSide);


                    // move piece
                    var movesPlayed = game.getMovesPlayed();
                    var ltmove = movesPlayed.get(movesPlayed.size()-1);

                    // audio
                    makeMoveSound(ltmove);

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
                        endView.setImage(new Image(rootPath + delimiter + "img" + delimiter + pieceImg));
                    }
                } else if (moveStat == MoveStatus.SAME_SIDE) {
                    movingPiece = imgView;
                    showAvailableMoves(true);
                    return;
                } else {
                    System.out.println("Invalid moved from " + move[0] + move[1] + " to " + move[2] + move[3]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            makingMove = false;
            movingPiece = null;

            if (game.isOver()) {
                displayGameOver();
            }
        } else {
            Image img = imgView.getImage();
            if (img == null) {
                return;
            }
            String pieceName = new File(img.getUrl()).getName();
            if ( (currentPlayer.isWhiteSide() && pieceName.contains("1.png"))
                    || (!currentPlayer.isWhiteSide() && pieceName.contains("2.png")) ) {
                return;
            }
            if (imgView.getImage() == null) {
                return;
            }
            // wait for the end spot
            makingMove = true;
            movingPiece = imgView;
            showAvailableMoves(true);
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

        showMoveImgView = new ArrayList<>();
        canMovePane = new HashMap<>();

        rootPath = new File(url.toString()).getParent();
        if (rootPath.contains("\\")) {
            delimiter = "\\";
        } else {
            delimiter = "/";
        }

        ChessAudio.setup(rootPath);

        playSound(SoundEffect.START_GAME);

        timer = new Timer();
        task = new ChessTimer(600,this);
        timer.schedule(task, 500, 1000 );
    }
}