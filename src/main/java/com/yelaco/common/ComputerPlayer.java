package com.yelaco.common;

public class ComputerPlayer extends Player {
    private int waitTime;
    private int depth;
    public ComputerPlayer(boolean isWhiteSide, int waitTime) {
        this.isHumanPlayer = false;
        setWaitTime(waitTime);
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public int getWaitTime() {
        return this.waitTime;
    }
}
