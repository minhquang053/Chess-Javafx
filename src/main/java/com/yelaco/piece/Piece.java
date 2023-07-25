package com.yelaco.piece;

import com.yelaco.common.Board;
import com.yelaco.common.Spot;

public abstract class Piece {
    private boolean isWhite = false;

    public Piece(boolean isWhite) {
        this.setWhite(isWhite);
    }

    public boolean isWhite() {
        return this.isWhite;
    }

    public void setWhite(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public abstract boolean canMove(Board board, Spot start, Spot end);

}
