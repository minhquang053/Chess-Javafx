package com.yelaco.common;

import com.yelaco.chessgui.PlayController;
import com.yelaco.piece.*;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private Player[] players;
    private Board board;
    private Player currentTurn;
    private GameStatus status;
    private ArrayList<Move> movesPlayed;
    private PlayController pc;
    private Spot[] twoKings;
    private String rootPath;

    public Game() {
        players = new Player[2];
        board = new Board();
        movesPlayed = new ArrayList<>();

        twoKings = new Spot[2];
        try {
            twoKings[0] = (Spot) board.getBox(4, 0);
            twoKings[1] = (Spot) board.getBox(4, 7);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(Player p1, Player p2) {
        players[0] = p1;
        players[1] = p2;

        board.setBoard();

        if (p1.isWhiteSide()) {
            this.currentTurn = p1;
        } else {
            this.currentTurn = p2;
        }

        setStatus(GameStatus.ACTIVE);

        movesPlayed.clear();
    }

    public void setPlayController(PlayController pc) {
        this.pc = pc;
    }

    public Player[] getPlayers() {
        return this.players;
    }

    public void updateTwokings() {
        try {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    var box = board.getBox(i, j);
                    var piece = box.getPiece();
                    if (piece instanceof King) {
                        if (piece.isWhite()) {
                            twoKings[0] = box;
                        } else {
                            twoKings[1] = box;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isOver() {
        return this.getStatus() != GameStatus.ACTIVE;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public GameStatus getStatus() {
        return this.status;
    }

    public void setRootPath(String rp) {
        rootPath = rp;
    }

    public void setPromote(String pieceName) {
        var lastMove = getLastMovePlayed();
        if (lastMove.isPromotion()) {
            switch (pieceName) {
                case "wq.png", "Q" -> {
                    lastMove.getEnd().setPiece(new Queen(true));
                }
                case "wn.png", "N" -> {
                    lastMove.getEnd().setPiece(new Knight(true));
                }
                case "wb.png", "B" -> {
                    lastMove.getEnd().setPiece(new Bishop(true));
                }
                case "wr.png", "R" -> {
                    lastMove.getEnd().setPiece(new Rook(true));
                }
                case "bq.png", "q" -> {
                    lastMove.getEnd().setPiece(new Queen(false));
                }
                case "bn.png", "n" -> {
                    lastMove.getEnd().setPiece(new Knight(false));
                }
                case "bb.png", "b" -> {
                    lastMove.getEnd().setPiece(new Bishop(false));
                }
                case "br.png", "r" -> {
                    lastMove.getEnd().setPiece(new Rook(false));
                }
            }
            lastMove.setPiecePromote(lastMove.getEnd().getPiece());
            if (kingInDanger(!lastMove.getEnd().getPiece().isWhite())) {
                lastMove.setCheckMove(true);
            }
            pc.updateMoveList(lastMove);
        }
    }

    public ArrayList<String> getAvailiableMove(int mX, int mY) {
        ArrayList<String> availMove = new ArrayList<>();
        try {
            var startSpot = board.getBox(mX, mY);
            var piece = startSpot.getPiece();
            if (piece == null) {
                return availMove;
            }
            Piece oldPiece = null;
            Piece tmpPiece = null;
            Spot tmpBox = null;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    var endSpot = board.getBox(i, j);
                    if (piece.canMove(board, startSpot, endSpot)) {
                        oldPiece = endSpot.getPiece();
                        startSpot.setPiece(null);
                        endSpot.setPiece(piece);
                        if (piece instanceof Pawn && endSpot.getX() != startSpot.getX() && oldPiece == null) {
                            tmpBox = board.getBox(endSpot.getX(), startSpot.getY());
                            tmpPiece = tmpBox.getPiece();
                            tmpBox.setPiece(null);
                        }
                        if (piece instanceof King && ((King) piece).isCastlingMove(startSpot, endSpot)) {
                            endSpot.setPiece(null);
                            if (startSpot.getX() < endSpot.getX()) {
                                board.getBox(endSpot.getX() - 1, mY).setPiece(piece);
                            } else {
                                board.getBox(endSpot.getX() + 2, mY).setPiece(piece);
                            }
                        }
                        if (!kingInDanger(piece.isWhite())) {
                            availMove.add(String.format("%d%d", i, j));
                        }
                        startSpot.setPiece(piece);
                        endSpot.setPiece(oldPiece);
                        if (piece instanceof Pawn && endSpot.getX() != startSpot.getX() && oldPiece == null) {
                            assert tmpBox != null;
                            tmpBox.setPiece(tmpPiece);
                            tmpBox = null;
                            tmpPiece = null;
                        }
                        if (piece instanceof King && ((King) piece).isCastlingMove(startSpot, endSpot)) {
                            if (startSpot.getX() < endSpot.getX()) {
                                board.getBox(endSpot.getX() - 1, mY).setPiece(null);
                            } else {
                                board.getBox(endSpot.getX() + 2, mY).setPiece(null);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return availMove;
    }

    public void reverseLastMove() {
        var lastMove = getLastMovePlayed();
        getMovesPlayed().remove(getMovesPlayed().size() - 1);
        var pieceMoved = lastMove.getPieceMoved();
        var pieceKilled = lastMove.getPieceKilled();
        lastMove.getStart().setPiece(pieceMoved);
        if (lastMove.isEnpassant()) {
            lastMove.getEnd().setPiece(null);
            lastMove.getStart().setPiece(lastMove.getPieceMoved());
            lastMove.getSpotKilled().setPiece(pieceKilled);
            ((Pawn) pieceKilled).setCanEnpassant(true);
        } else if (lastMove.isCastlingMove()) {
            lastMove.getEnd().setPiece(null);
            var rookMove = lastMove.getRookCastled();
            rookMove[0].setPiece(pieceKilled);
            rookMove[1].setPiece(null);
        } else {
            lastMove.getEnd().setPiece(pieceKilled);
        }
        if (pieceMoved instanceof Pawn &&
                ((lastMove.getStart().getY() == 1 && pieceMoved.isWhite())
                || (lastMove.getStart().getY() == 6 && !pieceMoved.isWhite()))) {
            ((Pawn) pieceMoved).setInitMoved(false);
        }
        if (!getMovesPlayed().isEmpty() && getLastMovePlayed().getPieceMoved() instanceof Pawn) {
            var llpiece = getLastMovePlayed().getPieceMoved();
            var llstart = getLastMovePlayed().getStart();
            if (llstart.getY() == 1 && llpiece.isWhite() || llstart.getY() == 6 && !llpiece.isWhite()) {
                ((Pawn) llpiece).setCanEnpassant(true);
            }
        }

        if (pieceMoved instanceof King) {
            ((King) pieceMoved).setCastlingDone(false);
            for (Move move: getMovesPlayed()) {
                if (move.getPieceMoved() == pieceMoved) {
                    ((King) pieceMoved).setCastlingDone(true);
                    break;
                }
            }
        }

        if (this.currentTurn == players[0]) {
            this.currentTurn = players[1];
        } else {
            this.currentTurn = players[0];
        }
    }

    public Player getCurrentTurn() {
        if (isStalemate()) {
            setStatus(GameStatus.STALEMATE);
        }
        if (kingInDanger(currentTurn.isWhiteSide)) {
            if (currentTurn.isWhiteSide()) {
                ((King) twoKings[0].getPiece()).setInCheck(true);
            } else {
                ((King) twoKings[1].getPiece()).setInCheck(true);
            }
            if (kingInCheckmate(currentTurn.isWhiteSide())) {
                if (currentTurn.isWhiteSide) {
                    setStatus(GameStatus.BLACK_WIN);
                } else {
                    setStatus(GameStatus.WHITE_WIN);
                }
            }
        } else {
            if (currentTurn.isWhiteSide()) {
                ((King) twoKings[0].getPiece()).setInCheck(false);
            } else {
                ((King) twoKings[1].getPiece()).setInCheck(false);
            }
        }
        return this.currentTurn;
    }

    public ArrayList<Move> getMovesPlayed() {
        return movesPlayed;
    }

    public Move getLastMovePlayed() {
        return movesPlayed.get(movesPlayed.size()-1);
    }

    private boolean isStalemate() {
        ArrayList<String> pieceMoves = null;
        Piece piece = null;
        try {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    piece = board.getBox(i, j).getPiece();
                    if (piece == null) {
                        continue;
                    }
                    if (piece.isWhite() != currentTurn.isWhiteSide()) {
                        continue;
                    }
                    pieceMoves = getAvailiableMove(i, j);
                    if (!pieceMoves.isEmpty()) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }

    private boolean kingInDanger(boolean isWhite) {
        updateTwokings();
        Spot spotKing;
        if (isWhite) {
            spotKing = twoKings[0];
        } else {
            spotKing = twoKings[1];
        }
        try {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    var box = board.getBox(i, j);
                    var piece = box.getPiece();
                    if (piece == null) {
                        continue;
                    }
                    if (piece.isWhite() == isWhite) {
                        continue;
                    }
                    if (piece.canMove(board, box, spotKing)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    private boolean kingInCheckmate(boolean isWhite) {
        ArrayList<Spot> threatPieceSpot = new ArrayList<>();
        Spot spotKing;
        if (isWhite) {
            spotKing = twoKings[0];
        } else {
            spotKing = twoKings[1];
        }
        try {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    var box = board.getBox(i, j);
                    var piece = box.getPiece();
                    if (piece == null) {
                        continue;
                    }
                    if (piece.isWhite() == isWhite) {
                        continue;
                    }
                    if (piece.canMove(board, box, spotKing)) {
                        threatPieceSpot.add(box);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // if there are multiple threats, the King must run (can't block attack)
        // we check if the King can run or not with any number of threats
        var king = spotKing.getPiece();
        int xKing = spotKing.getX();
        int yKing = spotKing.getY();
        try {
            for (int i = xKing - 1; i <= xKing + 1; i++) {
                for (int j = yKing - 1; j <= yKing + 1; j++) {
                    if (i < 0 || i > 7 || j < 0 || j > 7) {
                        continue;
                    }
                    if (king.canMove(board, spotKing, board.getBox(i, j))) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (threatPieceSpot.size() > 1) {
            return true;
        }

        // if there is single threat and the King can't run, the threatening piece must be stopped
        // first by eliminate the threatening piece
        try {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    var box = board.getBox(i, j);
                    var piece = box.getPiece();
                    if (piece == null) {
                        continue;
                    }
                    if (piece.isWhite() != isWhite) {
                        continue;
                    }
                    if (piece.canMove(board, box, threatPieceSpot.get(0))) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // second by block the attack
        // with knight or pawn checking, there aren't any spots in between to block the attack
        var threatSpot = threatPieceSpot.get(0);
        var threatPiece = threatSpot.getPiece();
        if (threatPiece instanceof Knight || threatPiece instanceof Pawn) {
            return true;
        }

        // get pieces in between the threatPiece and the King
        ArrayList<Spot> inBetweens = new ArrayList<>();
        int i = threatSpot.getX();
        int j = threatSpot.getY();
        int bex = xKing;
        int bey = yKing;
        if (i < bex) {
            bex--;
        } else if (i > bex){
            bex++;
        }
        if (j < bey) {
            bey--;
        } else if (j > bey){
            bey++;
        }

        while (i != bex || j != bey) {
            if (i < bex) {
                i++;
            } else if (i > bex){
                i--;
            }
            if (j < bey) {
                j++;
            } else if (j > bey){
                j--;
            }

            try {
                inBetweens.add(board.getBox(i, j));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }

        // Check chockablock
        try {
            for (Spot theSpot : inBetweens) {
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        var box = board.getBox(x, y);
                        var piece = box.getPiece();
                        if (piece == null) {
                            continue;
                        }
                        if (piece.isWhite() != king.isWhite()) {
                            continue;
                        }
                        if (piece.canMove(board, box, theSpot)) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // if none of these work, your king dead as hell
        return true;
    }

    public MoveStatus playerMove(Player player, int startX, int startY, int endX, int endY) throws Exception {
        Spot startBox = board.getBox(startX, startY);
        Spot endBox = board.getBox(endX, endY);
        Move move = new Move(player, startBox, endBox);
        return this.makeMove(move, player);
    }

    private MoveStatus makeMove(Move move, Player player) {
        Piece srcPiece = move.getStart().getPiece();
        if (srcPiece == null) {
            return MoveStatus.NULL_PIECE;
        }
        Piece dstPiece = move.getEnd().getPiece();

        // check valid player
        if ( player != currentTurn || (srcPiece.isWhite() != player.isWhiteSide()) ) {
            return MoveStatus.WRONG_TURN;
        }

        // check valid move
        if (!srcPiece.canMove(board, move.getStart(), move.getEnd())) {
            if (dstPiece != null && srcPiece.isWhite() == dstPiece.isWhite()) {
                return MoveStatus.SAME_SIDE;
            }
            return MoveStatus.CANT_MOVE;
        }

        move.setPieceMoved(srcPiece);

        if (dstPiece != null) {
            move.setPieceKilled(dstPiece);
        }

        if (srcPiece instanceof King && ((King) srcPiece).isCastlingMove(move.getStart(), move.getEnd())) {
            move.setCastlingMove(true);
        }
        //

        if (srcPiece instanceof Pawn) {
            // Pawn init move
            if (!((Pawn) srcPiece).getInitMoved()) {
                if (Math.abs(move.getEnd().getY() - move.getStart().getY()) == 2) {
                    ((Pawn) srcPiece).setCanEnpassant(true);
                }
                ((Pawn) srcPiece).setInitMoved(true);
            }
            // Pawn promote
            if (move.getEnd().getY() == 7 || move.getEnd().getY() == 0) {
                if (currentTurn instanceof HumanPlayer) {
                    pc.displayPromoteChoice(srcPiece.isWhite(), move.getEnd().getX(), move.getEnd().getY());
                }
                move.setPromotion(true);
            }
            // En passant
            if (move.getEnd().getX() != move.getStart().getX() && move.getEnd().getPiece() == null) {
                try {
                    var spotKilled = board.getBox(move.getEnd().getX(), move.getStart().getY());
                    move.setPieceKilled(spotKilled.getPiece());
                    spotKilled.setPiece(null);
                    move.setEnpassant(true);
                    move.setSpotKilled(spotKilled);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Castling
        if (move.isCastlingMove()) {
            try {
                move.getEnd().setPiece(null);
                move.getStart().setPiece(null);
                if (move.getEnd().getX() == 0) {
                    board.getBox(2, move.getEnd().getY()).setPiece(srcPiece);
                    board.getBox(3, move.getEnd().getY()).setPiece(dstPiece);
                    move.setRookCastled(board.getBox(0, move.getEnd().getY()), board.getBox(3, move.getEnd().getY()));
                    move.setEnd(board.getBox(2, move.getEnd().getY()));
                } else if (move.getEnd().getX() == 7) {
                    board.getBox(6, move.getEnd().getY()).setPiece(srcPiece);
                    board.getBox(5, move.getEnd().getY()).setPiece(dstPiece);
                    move.setRookCastled(board.getBox(7, move.getEnd().getY()), board.getBox(5, move.getEnd().getY()));
                    move.setEnd(board.getBox(6, move.getEnd().getY()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return MoveStatus.CANT_MOVE;
            }
        } else {
            move.getEnd().setPiece(srcPiece);
            move.getStart().setPiece(null);
            if (kingInDanger(srcPiece.isWhite())) {
                move.getStart().setPiece(srcPiece);
                move.getEnd().setPiece(dstPiece);
                return MoveStatus.CANT_MOVE;
            }
        }

        // Turn off en passant for the last pawn move
        if (!movesPlayed.isEmpty()) {
            Piece lastPieceMove = movesPlayed.get(movesPlayed.size() - 1).getPieceMoved();
            if (lastPieceMove instanceof Pawn && ((Pawn) lastPieceMove).getCanEnpassant()) {
                ((Pawn) lastPieceMove).setCanEnpassant(false);
            }
        }

        if (kingInDanger(!srcPiece.isWhite())) {
            move.setCheckMove(true);
        }

        movesPlayed.add(move);

        // Game winning
        if (dstPiece instanceof King) {
            if (player.isWhiteSide()) {
                this.setStatus(GameStatus.WHITE_WIN);
            }
            else {
                this.setStatus(GameStatus.BLACK_WIN);
            }
        }

        if (this.currentTurn == players[0]) {
            this.currentTurn = players[1];
        } else {
            this.currentTurn = players[0];
        }

        if (srcPiece instanceof King && !((King) srcPiece).isCastlingDone()) {
                ((King) srcPiece).setCastlingDone(true);
        }

        return MoveStatus.SUCCESS;
    }

    public String pieceToUrl(Piece piece) {
        if (piece instanceof Pawn) {
            if (piece.isWhite()) {
                return rootPath + "img/wp.png";
            }
            return rootPath + "img/bp.png";
        } else if (piece instanceof Knight) {
            if (piece.isWhite()) {
                return rootPath + "img/wn.png";
            }
            return rootPath + "img/bn.png";
        } else if (piece instanceof Rook) {
            if (piece.isWhite()) {
                return rootPath + "img/wr.png";
            }
            return rootPath + "img/br.png";
        } else if (piece instanceof Bishop) {
            if (piece.isWhite()) {
                return rootPath + "img/wb.png";
            }
            return rootPath + "img/bb.png";
        } else if (piece instanceof King) {
            if (piece.isWhite()) {
                return rootPath + "img/wk.png";
            }
            return rootPath + "img/bk.png";
        } else if (piece instanceof Queen) {
            if (piece.isWhite()) {
                return rootPath + "img/wq.png";
            }
            return rootPath + "img/bq.png";
        }
        return null;
    }

    public String boardToFen() {
        int nls;
        StringBuilder fen = new StringBuilder();
        try {
            for (int j = 7; j >= 0; j--) {
                nls = 0;
                for (int i = 0; i < 8; i++) {
                    var piece = board.getBox(i, j).getPiece();
                    if (piece != null && nls > 0) {
                        fen.append(String.format("%d", nls));
                        nls = 0;
                    }
                    if (piece instanceof Pawn) {
                        if (piece.isWhite()) {
                            fen.append("P");
                        } else {
                            fen.append("p");
                        }
                    } else if (piece instanceof Knight) {
                        if (piece.isWhite()) {
                            fen.append("N");
                        } else {
                            fen.append("n");
                        }
                    } else if (piece instanceof Rook) {
                        if (piece.isWhite()) {
                            fen.append("R");
                        } else {
                            fen.append("r");
                        }
                    } else if (piece instanceof Bishop) {
                        if (piece.isWhite()) {
                            fen.append("B");
                        } else {
                            fen.append("b");
                        }
                    } else if (piece instanceof King) {
                        if (piece.isWhite()) {
                            fen.append("K");
                        } else {
                            fen.append("k");
                        }
                    } else if (piece instanceof Queen) {
                        if (piece.isWhite()) {
                            fen.append("Q");
                        } else {
                            fen.append("q");
                        }
                    } else {
                        nls++;
                    }
                }
                if (nls > 0) {
                    fen.append(String.format("%d", nls));
                    nls = 0;
                }
                fen.append("/");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fen.toString();
    }

    public Character pieceToUnicode(Piece piece) {
        if (piece instanceof Pawn) {
            return ' ';
        } else if (piece instanceof Knight) {
            if (piece.isWhite()) {
                return '♞';
            }
            return '♘';
        } else if (piece instanceof Rook) {
            if (piece.isWhite()) {
                return '♜';
            }
            return '♖';
        } else if (piece instanceof Bishop) {
            if (piece.isWhite()) {
                return '♝';
            }
            return '♗';
        } else if (piece instanceof King) {
            if (piece.isWhite()) {
                return '♚';
            }
            return '♔';
        } else if (piece instanceof Queen) {
            if (piece.isWhite()) {
                return '♛';
            }
            return '♕';
        }
        return null;
    }

    public String moveToPGN(Move move) {
        if (move.isCastlingMove()) {
            String ret = "O-O";
            if (move.getEnd().getX() == 2) {
                ret += "-O";
            }
            if (move.isCheckMove()) {
                ret += "+";
            }
            return ret;
        }
        var pieceMoved = move.getPieceMoved();
        var pieceKilled = move.getPieceKilled();
        var start = move.getStart();
        var end = move.getEnd();
        String fromX = "";
        String fromY = "";
        String fromPiece = String.format("%c", pieceToUnicode(pieceMoved));
        String to = String.format("%c%d", 'a' + end.getX(), end.getY() + 1);
        if (pieceKilled != null) {
            to = "x" + to;
        }
        if (move.isPromotion()) {
            fromPiece = "";
            to += "=" + String.format("%c", pieceToUnicode(move.getPiecePromote()));
        }
        if (move.isCheckMove()) {
            to += "+";
        }
        try {
            end.setPiece(pieceKilled);
            start.setPiece(pieceMoved);
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    var box = board.getBox(i, j);
                    var piece = box.getPiece();
                    if (piece == null || piece == pieceMoved) {
                        continue;
                    }
                    if (piece.isWhite() != pieceMoved.isWhite()) {
                        continue;
                    }
                    if (!piece.getClass().equals(pieceMoved.getClass())) {
                        continue;
                    }
                    if (piece.canMove(board, box, end)) {
                        if (i == start.getX() && fromY.isEmpty()) {
                            fromY = String.format("%d", start.getY() + 1);
                        }
                        if (j == start.getY() && fromX.isEmpty()) {
                            fromX = String.format("%c", 'a' + start.getX());
                        }
                        if (i != start.getX() && j != start.getY()) {
                            return fromPiece
                                    + String.format("%c", 'a' + start.getX())
                                    + to;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "err";
        } finally {
            if (move.isPromotion()) {
                end.setPiece(move.getPiecePromote());
            } else {
                end.setPiece(pieceMoved);
            }
            start.setPiece(null);
        }
        return fromPiece + fromX + fromY + to;
    }

    public ArrayList<String> getPieceUrlSet() {
        var res = new ArrayList<String>();
        res.add(pieceToUrl(new Pawn(true)));
        res.add(pieceToUrl(new Pawn(false)));
        res.add(pieceToUrl(new Bishop(true)));
        res.add(pieceToUrl(new Bishop(false)));
        res.add(pieceToUrl(new Knight(true)));
        res.add(pieceToUrl(new Knight(false)));
        res.add(pieceToUrl(new Rook(true)));
        res.add(pieceToUrl(new Rook(false)));
        res.add(pieceToUrl(new Queen(true)));
        res.add(pieceToUrl(new Queen(false)));
        return res;
    }
}