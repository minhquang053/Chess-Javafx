package com.yelaco.chessgui;

import com.yelaco.common.*;
import com.yelaco.piece.Piece;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Stop;
import javafx.scene.transform.Rotate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class PlayController implements Initializable {
    public Game game;
    private Player currentPlayer;
    private boolean makingMove = false;
    private ImageView movingPiece = null;
    ImageView promotingPiece = null;
    private String rootPath = null;
    private String delimiter = null;
    private String rootPathNew = null;
    private boolean player1;

    private ArrayList<ImageView> showMoveImgView;
    private HashMap<BorderPane, String> canMovePane;
    private HashMap<BorderPane, String> highlightMovePane;
    private BorderPane prevClick;
    private String prevClickStyle;
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

    @FXML
    HBox rootProm;
    BorderPane showingProm;

    public void displayResult() {
        var resultDialog = new Dialog<String>();
        resultDialog.setTitle("Result");
        resultDialog.setContentText("Game ended with " + game.getStatus());
        resultDialog.getDialogPane().getButtonTypes().add(new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        resultDialog.showAndWait();
    }

    public void displayGameOver() {
        apane.setDisable(true);
        timer.cancel();
        timer.purge();
        displayResult();

    }

    public void displayPromoteChoice(boolean isWhite, int col, int row) {
        try {
            String res = null;
            if (isWhite && row == 7) {
                res = "white-promote.fxml";
                rootProm.setAlignment(Pos.TOP_LEFT);
            } else if (isWhite && row == 0) {
                res = "rev-white-promote.fxml";
                rootProm.setAlignment(Pos.BOTTOM_LEFT);
            } else if (!isWhite && row == 7) {
                res = "black-promote.fxml";
                rootProm.setAlignment(Pos.TOP_LEFT);
            } else if (!isWhite && row == 0) {
                res = "rev-black-promote.fxml";
                rootProm.setAlignment(Pos.BOTTOM_LEFT);
            }
            assert res != null;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(res));
            loader.setController(new PromoteController(this));
            rootProm.setVisible(true);
            var proms = rootProm.getChildren();
            showingProm = (BorderPane) proms.get(col);
            showingProm.setCenter(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void exitPromoteChoice(MouseEvent event) {
        rootProm.setVisible(false);
        showingProm.setCenter(null);
        var move = game.getLastMovePlayed();
        var start = spots[move.getStart().getX()][move.getStart().getY()];
        var end = spots[move.getEnd().getX()][move.getEnd().getY()];
        ((ImageView) start.getCenter()).setImage(new Image(game.pieceToUrl(move.getPieceMoved())));
        ((ImageView) end.getCenter()).setImage(
                move.getPieceKilled() == null ? null : new Image(game.pieceToUrl(move.getPieceKilled()))
        );
        game.reverseLastMove();
        currentPlayer = game.getCurrentTurn();
    }



    private void highlightMove(boolean isHighlighted, int startX, int startY, int endX, int endY, BorderPane sp, BorderPane ep) {
        if (isHighlighted) {
            BorderPane start = spots[startX][startY];
            BorderPane end = spots[endX][endY];
            highlightMovePane.put(start, start.getStyle());
            highlightMovePane.put(end, end.getStyle());
            if ( (startX + startY) % 2 == 1) {
                start.setStyle("-fx-background-color: rgba(244,246,128,255);");
            } else {
                start.setStyle("-fx-background-color: rgba(187,204,68,255);");
            }
            if ( (endX + endY) % 2 == 1) {
                end.setStyle("-fx-background-color: rgba(244,246,128,255);");
            } else {
                end.setStyle("-fx-background-color: rgba(187,204,68,255);");
            }
        } else {
            sp.setStyle(highlightMovePane.get(sp));
            ep.setStyle(highlightMovePane.get(ep));
            highlightMovePane.clear();
        }

    }

    private void playSound(SoundEffect gameSound) {
        ChessAudio.playSound(gameSound);
    }

    public void makeMoveSound(Move move) {
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
        } else {
            // due to internal logic, the current turn will be switched before making a sound
            if (player1 != currentPlayer.isWhiteSide) {
                playSound(SoundEffect.SELF_MOVE);
            } else {
                playSound(SoundEffect.OPPONENT_MOVE);
            }
        }
    }

    private boolean isSameSide(String pieceName, boolean isWhite) {
        return "wk.png/wq.png/wb.png/wn.png/wr.png/wp.png".contains(pieceName) == isWhite;
    }

    private void showAvailableMoves(boolean isShow, MouseEvent event) {
        clickHighlight(isShow, event);
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
                    bpane.setStyle(bpane.getStyle() + "-fx-background-image: url(\"" + rootPathNew + "img/killDot.png\"); -fx-background-size: 100");
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

    /*
     * To highlight the selected piece while showing its available moves on screen
     */
    private void clickHighlight(boolean isHighlight, MouseEvent event) {
        if (isHighlight) {
            BorderPane pane = (BorderPane) ((ImageView) event.getSource()).getParent();
            prevClick = pane;
            prevClickStyle = pane.getStyle();
            String cord = pane.getId();
            int x = cord.charAt(0) - 'a';
            int y = cord.charAt(1) - '0' - 1;
            if ((x + y) % 2 == 1) {
                pane.setStyle("-fx-background-color: rgba(244,246,128,255);");
            } else {
                pane.setStyle("-fx-background-color: rgba(187,204,68,255);");
            }
        } else {
            prevClick.setStyle(prevClickStyle);
            prevClick = null;
            prevClickStyle = null;
        }
    }

    public void processMove(MouseEvent event) {
        var imgView = (ImageView) event.getSource();
        var pane = (BorderPane) imgView.getParent();

        // if you pick a piece to move, the next click must be the spot you want to move to
        if (makingMove) {
            showAvailableMoves(false, null);
            if (!highlightMovePane.isEmpty()) {
                highlightMove(false, -1, -1, -1, -1, highlightMovePane.keySet().stream().toList().get(0),
                        highlightMovePane.keySet().stream().toList().get(1));
            }

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
                    if (!ltmove.isPromotion()) {
                        // promotion will require other audio player
                        makeMoveSound(ltmove);
                    }

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
                        promotingPiece = endView;
                    }
                    highlightMove(true,
                            ltmove.getStart().getX(), ltmove.getStart().getY(), ltmove.getEnd().getX(), ltmove.getEnd().getY(),
                            (BorderPane) startView.getParent(), (BorderPane) endView.getParent());
                } else if (moveStat == MoveStatus.SAME_SIDE) {
                    movingPiece = imgView;
                    showAvailableMoves(true, event);
                    return;
                } else {
                    // invalid move
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
            if ( !isSameSide(pieceName, currentPlayer.isWhiteSide()) ) {
                return;
            }
            // wait for the end spot
            makingMove = true;
            movingPiece = imgView;
            showAvailableMoves(true, event);
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
        String[] tmp = url.toString().split("/");
        tmp[tmp.length-1] = "";
        rootPathNew = String.join("/", tmp);

        game = new Game();
        game.init(new HumanPlayer(true), new ComputerPlayer(false));
        game.setPlayController(this);
        game.setRootPath(rootPathNew);
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

        player1 = currentPlayer.isWhiteSide;

        highlightMovePane = new HashMap<>();
    }
}