package com.yelaco.chessgui;

import com.yelaco.common.Game;
import com.yelaco.common.GameStatus;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.util.TimerTask;

public class ChessTimer extends TimerTask {
    private int tw = 0;
    private int tb = 0;
    public boolean isWhite = true;
    private ChessController cs;
    private Label pwclock;
    private Label pbclock;

    public ChessTimer(int totalTime, ChessController cs) {
        this.tw = totalTime;
        this.tb = totalTime;
        this.cs = cs;
        this.pwclock = cs.p1clock;
        this.pbclock = cs.p2clock;
        pbclock.setStyle("-fx-background-color: grey; -fx-border-color: black");
        setClock();
    }

    @Override
    public void run() {
        Platform.runLater(() -> {
            setClock();
            if (tw == 0) {
                cs.game.setStatus(GameStatus.BLACK_WIN);
                cs.displayGameOver();
            } else if (tb == 0){
                cs.game.setStatus(GameStatus.WHITE_WIN);
                cs.displayGameOver();
            }
            if (isWhite) {
                tw--;
            } else {
                tb--;
            }
        });
    }

    public void switchTurn(boolean color) {
        this.isWhite = color;
        if (isWhite) {
            pwclock.setStyle("-fx-background-color: white; -fx-border-color: black");
            pbclock.setStyle("-fx-background-color: grey; -fx-border-color: black");
        } else {
            pwclock.setStyle("-fx-background-color: grey; -fx-border-color: black");
            pbclock.setStyle("-fx-background-color: white; -fx-border-color: black");
        }
    }

    private void setClock() {
        pwclock.setText(secondToMinute(tw));
        pbclock.setText(secondToMinute(tb));
    }

    private String secondToMinute(int sec) {
        int minute = Math.floorDiv(sec, 60);
        int second = sec - minute * 60;
        if (second == 0) {
            return String.format("%d:00", minute);
        }
        return String.format("%d:%d", minute, second);
    }
}