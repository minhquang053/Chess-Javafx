package com.yelaco.piece;

import com.yelaco.common.Board;
import com.yelaco.common.Spot;

public class Knight extends Piece {
    public Knight(boolean isWhite) {
        super(isWhite);
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
        return ( Math.abs(start.getX() - end.getX()) * Math.abs(start.getY() - end.getY()) ) == 2;
    }
}
