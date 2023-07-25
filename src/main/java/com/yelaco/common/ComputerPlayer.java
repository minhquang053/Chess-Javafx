package com.yelaco.common;

public class ComputerPlayer extends Player {
    public ComputerPlayer(boolean isWhiteSide) {
        this.isWhiteSide = isWhiteSide;
        this.isHumanPlayer = false;
    }
}
