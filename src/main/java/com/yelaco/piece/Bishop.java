package com.yelaco.piece;

import com.yelaco.common.Board;
import com.yelaco.common.Spot;

public class Bishop extends Piece {
    public Bishop(boolean isWhite) {
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
        if (Math.abs(start.getX() - end.getX()) != Math.abs(start.getY() - end.getY())) {
            return false;
        }

        // check if there are any pieces in between
        int i = start.getX();
        int j = start.getY();
        int bex = end.getX();
        int bey = end.getY();
        if (i < bex) {
            bex--;
        } else {
            bex++;
        }
        if (j < bey) {
            bey--;
        } else {
            bey++;
        }

        while (i != bex && j != bey) {
            if (i < bex) {
                i++;
            } else {
                i--;
            }
            if (j < bey) {
                j++;
            } else {
                j--;
            }

            try {
                if (board.getBox(i, j).getPiece() != null) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }

        return true;
    }
}
