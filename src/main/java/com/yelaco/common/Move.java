package com.yelaco.common;

import com.yelaco.piece.Piece;
import com.yelaco.piece.Rook;

public class Move {
    private Player player;
    private Spot start;
    private Spot end;
    private Piece pieceMoved;
    private Piece pieceKilled;
    private Spot spotKilled;
    private Spot[] rookCastle;
    private boolean isCastlingMove = false;
    private boolean isEnpassant = false;
    private boolean isPromotion = false;

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

    public void setEnd(Spot end) {
        this.end = end;
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

    public boolean isEnpassant() {
        return this.isEnpassant;
    }

    public void setEnpassant(boolean isEnpassant) {
        this.isEnpassant = isEnpassant;
    }

    public boolean isPromotion() {
        return this.isPromotion;
    }

    public void setPromotion(boolean isPromotion) {
        this.isPromotion = isPromotion;
    }

    public Spot[] getRookCastled() {
        return this.rookCastle;
    }

    public void setRookCastled(Spot start, Spot end) {
        this.rookCastle = new Spot[2];
        this.rookCastle[0] = start;
        this.rookCastle[1] = end;
    }

    public Spot getSpotKilled() {
        return this.spotKilled;
    }

    public void setSpotKilled(Spot spotKilled) {
        this.spotKilled = spotKilled;
    }
}
