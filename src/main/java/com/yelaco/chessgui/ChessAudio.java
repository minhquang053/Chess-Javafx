package com.yelaco.chessgui;

import javafx.scene.media.AudioClip;

public class ChessAudio {
    private static AudioClip selfmove = null;
    private static AudioClip opmove = null;
    private static AudioClip start = null;
    private static AudioClip capture = null;
    private static AudioClip castling = null;
    private static AudioClip promotion = null;
    private static AudioClip check = null;
    private static AudioClip gameover = null;
    private static AudioClip stallmate = null;
    private static AudioClip checkmate = null;

    public static void setup(String path) {
        selfmove = new AudioClip(path + "sfx/move-self.mp3");
        opmove = new AudioClip(path + "sfx/move-opponent.mp3");
        start = new AudioClip(path + "sfx/start.mp3");
        capture = new AudioClip(path + "sfx/capture.mp3");
        castling = new AudioClip(path + "sfx/castling.mp3");
        promotion = new AudioClip(path + "sfx/promote.mp3");
        check = new AudioClip(path + "sfx/check.mp3");
        gameover = new AudioClip(path + "sfx/gameover.mp3");
        stallmate = new AudioClip(path + "sfx/stalemate.mp3");
        checkmate = new AudioClip(path + "sfx/checkmate.mp3");
    }

    public static void playSound(SoundEffect sfx) {
        switch (sfx) {
            case OPPONENT_MOVE -> {
                opmove.play();
            }
            case SELF_MOVE -> {
                selfmove.play();
            }
            case START_GAME -> {
                start.play();
            }
            case CHECK -> {
                check.play();
            }
            case CAPTURE_PIECE -> {
                capture.play();
            }
            case CASTLING -> {
                castling.play();
            }
            case PROMOTION -> {
                promotion.play();
            }
            case CHECKMATE -> {
                checkmate.play();
            }
            case GAME_OVER -> {
                gameover.play();
            }
            case STALEMATE -> {
                stallmate.play();
            }
            default -> {
                return;
            }
        }
    }
}
