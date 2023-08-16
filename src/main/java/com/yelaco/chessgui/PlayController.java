package com.yelaco.chessgui;

import com.yelaco.common.*;
import com.yelaco.engine.Stockfish;
import com.yelaco.piece.King;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class PlayController implements Initializable {
    public Game game;
    private String computerElo;
    private String computerName;
    Player currentPlayer;
    private boolean makingMove = false;
    private ImageView movingPiece = null;
    ImageView promotingPiece = null;
    private String rootPath = null;
    public boolean player1;

    private ArrayList<ImageView> showMoveImgView;
    private HashMap<BorderPane, String> canMovePane;
    private HashMap<BorderPane, String> highlightMovePane;
    private BorderPane prevClick;
    private String prevClickStyle;
    private Image moveDot;
    private BorderPane[][] spots = new BorderPane[8][8];
    private ChessTimer task;
    private Timer timer;
    private ArrayList<String> whiteCaptured;
    private ArrayList<String> blackCaptured;
    private ArrayList<String> pieceOrder;

    private Stockfish sfClient;

    @FXML
    private AnchorPane apane;
    @FXML
    private GridPane gpane;
    @FXML
    private Label opName;

    // player white clock
    @FXML
    public Label p1clock;

    //player black clock
    @FXML
    public Label p2clock;

    @FXML
    HBox rootProm;
    BorderPane showingProm;

    @FXML
    public ListView<String> whiteList;
    @FXML
    public ListView<String> blackList;
    @FXML
    public ListView<String> numList;
    @FXML
    public Pane whiteStack;
    @FXML
    public Pane blackStack;
    @FXML
    public Label eloLabel;

    public void displayResult() {
        rootProm.setVisible(true);
        var resultDialog = new Dialog<String>();
        resultDialog.setTitle("Result");
        resultDialog.setContentText("Game ended with " + game.getStatus());
        resultDialog.getDialogPane().getButtonTypes().add(new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        resultDialog.showAndWait();
    }

    public void displayGameOver() {
        apane.setDisable(true);
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        displayResult();

    }

    public void setComputerElo(String computerElo) {
        this.computerElo = computerElo;
    }

    public void setComputerName(String compName) {
        this.computerName = compName;
    }

    public void displayPromoteChoice(boolean isWhite, int col, int row) {
        if (!player1) {
            col = 7 - col;
            row = 7 - row;
        }
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
                    bpane.setStyle(bpane.getStyle() + "-fx-background-image: url(\"" + rootPath + "img/killDot.png\"); -fx-background-size: 100");
                }
                showMoveImgView.add(moveImgView);
            }
        } else {
            for (ImageView imgView : showMoveImgView) {
                if (imgView.getImage() == moveDot) imgView.setImage(null);
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
        if (currentPlayer instanceof ComputerPlayer) {
            return;
        }
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
                    updateBoard();
                    if (!game.getLastMovePlayed().isPromotion()) {
                        updateMoveList(game.getLastMovePlayed());
                    }

                    if (currentPlayer instanceof ComputerPlayer) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                botMove();
                            }
                        }).start();
                    }
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

    private void updateCaptureStack(Move move) {
        var pieceKilled = move.getPieceKilled();
        if (pieceKilled == null) {
            return;
        }
        var pieceUrl = game.pieceToUrl(move.getPieceKilled());
        if (pieceKilled.isWhite()) {
            if (whiteCaptured.contains(pieceUrl)) {
                whiteCaptured.add(whiteCaptured.lastIndexOf(pieceUrl) + 1, pieceUrl);
            } else {
                whiteCaptured.add(whiteCaptured.size(), pieceUrl);
                whiteCaptured.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return pieceOrder.indexOf(o1) - pieceOrder.indexOf(o2);
                    }
                });
            }
        } else {
            if (blackCaptured.contains(pieceUrl)) {
                blackCaptured.add(blackCaptured.lastIndexOf(pieceUrl) + 1, pieceUrl);
            } else {
                blackCaptured.add(blackCaptured.size(), pieceUrl);
                blackCaptured.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return pieceOrder.indexOf(o1) - pieceOrder.indexOf(o2);
                    }
                });
            }
        }
        drawCaptureStack();
    }

    private void drawCaptureStack() {
        String lstUrl = "";
        double lstX = -15.0;
        while (whiteStack.getChildren().size() > whiteCaptured.size()) {
            whiteStack.getChildren().remove(whiteStack.getChildren().size() - 1);
        }
        while (blackStack.getChildren().size() > blackCaptured.size()) {
            blackStack.getChildren().remove(blackStack.getChildren().size() - 1);
        }
        for (int i = 0; i < whiteCaptured.size(); i++) {
            if (i == whiteStack.getChildren().size()) {
                whiteStack.getChildren().add(new ImageView());
            }
            var imgv = (ImageView) whiteStack.getChildren().get(i);
            imgv.setImage(new Image(whiteCaptured.get(i)));
            imgv.setFitHeight(30.0);
            imgv.setFitWidth(30.0);
            if (!whiteCaptured.get(i).equals(lstUrl)) {
                lstX += 25.0;
            } else {
                lstX += 8.0;
            }
            imgv.setLayoutX(lstX);
            lstUrl = whiteCaptured.get(i);
        }
        lstUrl = "";
        lstX = -15.0;
        for (int i = 0; i < blackCaptured.size(); i++) {
            if (i == blackStack.getChildren().size()) {
                blackStack.getChildren().add(new ImageView());
            }
            var imgv = (ImageView) blackStack.getChildren().get(i);
            imgv.setImage(new Image(blackCaptured.get(i)));
            imgv.setFitHeight(30.0);
            imgv.setFitWidth(30.0);
            if (!blackCaptured.get(i).equals(lstUrl)) {
                lstX += 25.0;
            } else {
                lstX += 8.0;
            }
            imgv.setLayoutX(lstX);
            lstUrl = blackCaptured.get(i);
        }
    }

    public void updateMoveList(Move move) {
        int size = whiteList.getItems().size();
        if (size == 0) {
            Node n1 = whiteList.lookup(".scroll-bar");
            if (n1 instanceof ScrollBar whiteBar) {;
                Node n2 = blackList.lookup(".scroll-bar");
                if (n2 instanceof ScrollBar blackBar) {
                    Node n3 = numList.lookup(".scroll-bar"); {
                        if (n3 instanceof ScrollBar numBar) {
                            whiteBar.valueProperty().bindBidirectional(blackBar.valueProperty());
                            numBar.valueProperty().bindBidirectional(whiteBar.valueProperty());
                        }
                    }
                }
            }
        }
        if (move.getPieceMoved().isWhite()) {
            numList.getItems().add(size, String.format("%3d", size + 1) + ".");
            whiteList.getItems().add(size, game.moveToPGN(move));
            blackList.getItems().add(size, "");
        } else {
            blackList.getItems().set(size - 1, game.moveToPGN(move));
        }
        updateCaptureStack(move);
    }

    private void updateBoard() {
        currentPlayer = game.getCurrentTurn();
        if (task != null) {
            task.switchTurn(currentPlayer.isWhiteSide);
        }

        // move piece
        var ltmove = game.getLastMovePlayed();

        // audio
        if (!ltmove.isPromotion()) {
            // promotion will require other audio player
            makeMoveSound(ltmove);
        }

        var startView = (ImageView) spots[ltmove.getStart().getX()][ltmove.getStart().getY()].getCenter();
        var endView = (ImageView) spots[ltmove.getEnd().getX()][ltmove.getEnd().getY()].getCenter();

        endView.setImage(new Image(game.pieceToUrl(ltmove.getPieceMoved())));
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
            if (ltmove.getPiecePromote() != null) {
                endView.setImage(new Image(game.pieceToUrl(ltmove.getPiecePromote())));
            }
            promotingPiece = endView;
        }
        highlightMove(true,
                ltmove.getStart().getX(), ltmove.getStart().getY(), ltmove.getEnd().getX(), ltmove.getEnd().getY(),
                (BorderPane) startView.getParent(), (BorderPane) endView.getParent());
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

    private void botMove() {
        // get FEN based on spots
        if (!(currentPlayer instanceof ComputerPlayer)) {
            return;
        }

        if (!game.getMovesPlayed().isEmpty() && game.getLastMovePlayed().isPromotion()) {
            var lastMove = game.getLastMovePlayed();
            while (lastMove.getPiecePromote() == null) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String fen = game.boardToFen();
        if (currentPlayer.isWhiteSide) {
            fen += " w";
        }
        String move = sfClient.getBestMove(fen, ((ComputerPlayer) currentPlayer).getWaitTime());


        try {
            while (move == null) {
                Thread.sleep(((ComputerPlayer) currentPlayer).getWaitTime() + 30);
            }
            if (!highlightMovePane.isEmpty()) {
                highlightMove(false, -1, -1, -1, -1, highlightMovePane.keySet().stream().toList().get(0),
                        highlightMovePane.keySet().stream().toList().get(1));
            }
            var coords = filterInput(move.substring(0, 2), move.substring(2, 4));
            game.playerMove(currentPlayer, coords[0], coords[1], coords[2], coords[3]);

            Platform.runLater(() -> {
                if (game.getLastMovePlayed().isPromotion()) {
                    if (move.length() == 5) {
                        var promoteTo = String.format("%c", move.charAt(4));
                        if (!player1) {
                            promoteTo = promoteTo.toUpperCase();
                        }
                        game.setPromote(promoteTo);
                    }
                    makeMoveSound(game.getLastMovePlayed());
                } else {
                    updateMoveList(game.getLastMovePlayed());
                }
                updateBoard();
                if (game.isOver()) {
                    displayGameOver();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reverseMove() {
        if (game.getMovesPlayed().isEmpty() || game.isOver()) {
            return;
        }
        var lastMove = game.getLastMovePlayed();

        var startView = (ImageView) spots[lastMove.getStart().getX()][lastMove.getStart().getY()].getCenter();
        var endView = (ImageView) spots[lastMove.getEnd().getX()][lastMove.getEnd().getY()].getCenter();
        startView.setImage(endView.getImage());

        if (lastMove.isCastlingMove()) {
            var rookSpots = lastMove.getRookCastled();
            ((ImageView) spots[rookSpots[0].getX()][rookSpots[0].getY()].getCenter()).setImage(
                    new Image(game.pieceToUrl(lastMove.getPieceKilled()))
            );
            ((ImageView) spots[rookSpots[1].getX()][rookSpots[1].getY()].getCenter()).setImage(null);
            endView.setImage(null);
        } else if (lastMove.isEnpassant()) {
            startView.setImage(new Image(game.pieceToUrl(lastMove.getPieceMoved())));
            endView.setImage(null);
            var spotKillView = spots[lastMove.getSpotKilled().getX()][lastMove.getSpotKilled().getY()];
            ((ImageView)spotKillView.getCenter()).setImage(new Image(game.pieceToUrl(lastMove.getPieceKilled())));
        } else if (lastMove.isPromotion()) {
            startView.setImage(new Image(game.pieceToUrl(lastMove.getPieceMoved())));
            endView.setImage(
                    lastMove.getPieceKilled() == null ? null : new Image(game.pieceToUrl(lastMove.getPieceKilled()))
            );
        } else {
            endView.setImage(
                    lastMove.getPieceKilled() == null ? null : new Image(game.pieceToUrl(lastMove.getPieceKilled()))
            );
        }

        whiteCaptured.remove(game.pieceToUrl(lastMove.getPieceKilled()));
        blackCaptured.remove(game.pieceToUrl(lastMove.getPieceKilled()));
        drawCaptureStack();
        game.reverseLastMove();
    }

    public void setMatch(Player p1, Player p2) {
        game.init(p1, p2);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String[] tmp = url.toString().split("/");
        tmp[tmp.length-1] = "";
        rootPath = String.join("/", tmp);

        showMoveImgView = new ArrayList<>();
        canMovePane = new HashMap<>();

        moveDot = new Image(Objects.requireNonNull(getClass().getResourceAsStream("img/moveDot.png")));

        ChessAudio.setup(rootPath);

        highlightMovePane = new HashMap<>();

        whiteCaptured = new ArrayList<>();
        blackCaptured = new ArrayList<>();
    }

    public void initMatch(Player p1, Player p2) {
        game = new Game();
        game.init(p1, p2);
        game.setPlayController(this);
        game.setRootPath(rootPath);
        currentPlayer = game.getCurrentTurn();

        playSound(SoundEffect.START_GAME);

        player1 = p1.isWhiteSide;

        pieceOrder = game.getPieceUrlSet();

        if (p2 instanceof ComputerPlayer) {
            eloLabel.setText(computerElo);
            opName.setText(computerName);
            sfClient = new Stockfish();
            sfClient.startEngine();
            sfClient.sendCommand("uci");
            sfClient.sendCommand("setoption name UCI_LimitStrength value true");
            sfClient.sendCommand("setoption name UCI_Elo value " + computerElo);
        } else {
            Platform.runLater(() -> {
                p1clock.setVisible(true);
                p2clock.setVisible(true);
            });
            timer = new Timer();
            task = new ChessTimer(600,this);
            timer.schedule(task, 500, 1000 );
        }

        if (p1.isWhiteSide) {
            int idx = 0;
            for (int j = 7; j >= 0; j--) {
                for (int i = 0; i < 8; i++) {
                    spots[i][j] = (BorderPane) gpane.getChildren().get(idx++);
                }
            }
        } else {
            int idx = 0;
            for (int j = 0; j < 8; j++) {
                for (int i = 7; i >= 0; i--) {
                    spots[i][j] = (BorderPane) gpane.getChildren().get(idx++);
                }
            }
            Platform.runLater(() -> {
                botMove();
            });
        }
    }
}