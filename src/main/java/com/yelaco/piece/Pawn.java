package com.yelaco.piece;

import com.yelaco.common.Board;
import com.yelaco.common.Spot;

public class Pawn extends Piece {
    private boolean initMoved = false;
    private boolean canEnpassant = false;

    public Pawn(boolean isWhite) {
        super(isWhite);
    }

    public boolean getInitMoved() {
        return this.initMoved;
    }

    public void setInitMoved(boolean initMoved) {
        this.initMoved = initMoved;
    }

    public boolean getCanEnpassant() {
        return this.canEnpassant;
    }

    public void setCanEnpassant(boolean canEnpassant) {
        this.canEnpassant = canEnpassant;
    }

    private boolean enpassantCheck(Board board, Spot start, Spot end) {
        try {
            Piece anotherPiece = board.getBox(end.getX(), start.getY()).getPiece();
            if (anotherPiece instanceof Pawn && ((Pawn) anotherPiece).getCanEnpassant()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    public boolean canMove(Board board, Spot start, Spot end) {
        // check same location
        if (start == end) {
            return false;
        }

        // check different color
        if (end.getPiece() != null && start.getPiece().isWhite() == end.getPiece().isWhite()) {
            return false;
        }

        // check valid move
        if (Math.abs(end.getX() - start.getX()) > 1
                || Math.abs(end.getY() - start.getY()) > 2
                || Math.abs(end.getY() - start.getY()) == 0) {
            return false;
        }

        // check if there are any pieces in between
        // (Not including enpassant yet)
        if (end.getX() == start.getX()) {
            // so this pawn won't be able to move 2 step up anymore
            if (Math.abs(end.getY() - start.getY()) == 2 && !initMoved) {
                return true;
            }
            return end.getPiece() == null;
        } else {
            if (Math.abs(end.getY() - start.getY()) == 2) {
                return false;
            }
            return end.getPiece() != null || enpassantCheck(board, start, end);
        }
    }
}
