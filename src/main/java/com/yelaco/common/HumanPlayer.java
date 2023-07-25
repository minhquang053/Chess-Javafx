package com.yelaco.common;

public class HumanPlayer extends Player {
    public HumanPlayer(boolean isWhiteSide) {
        this.isWhiteSide = isWhiteSide;
        this.isHumanPlayer = true;
    }
}
