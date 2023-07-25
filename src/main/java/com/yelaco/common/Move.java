package com.yelaco.common;

import com.yelaco.piece.Piece;

public class Move {
    private Player player;
    private Spot start;
    private Spot end;
    private Piece pieceMoved;
    private Piece pieceKilled;
    private boolean isCastlingMove = false;

    public Move(Player player, Spot start, Spot end) {
        this.player = player;
        this.start = start;
        this.end = end;
        this.pieceMoved = start.getPiece();
    }

    public Spot getStart() {
        return this.start;
    }

    public Spot getEnd() {
        return this.end;
    }

    public Piece getPieceMoved() {
        return this.pieceMoved;
    }

    public void setPieceMoved(Piece pieceMoved) {
        this.pieceMoved = pieceMoved;
    }

    public void setPieceKilled(Piece pieceKilled) {
        this.pieceKilled = pieceKilled;
    }

    public boolean isCastlingMove() {
        return this.isCastlingMove;
    }

    public void setCastlingMove(boolean isCastlingMove) {
        this.isCastlingMove = isCastlingMove;
    }
}
