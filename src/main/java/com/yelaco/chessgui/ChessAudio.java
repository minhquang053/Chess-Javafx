package com.yelaco.chessgui;

import javafx.scene.media.AudioClip;

public class ChessAudio {
    private static AudioClip move = null;
    private static AudioClip start = null;
    private static AudioClip capture = null;
    private static AudioClip castling = null;
    private static AudioClip check = null;
    private static AudioClip gameover = null;
    private static AudioClip stallmate = null;
    private static AudioClip checkmate = null;

    public static void setup(String rootPath) {
        String path = String.join("/", rootPath.split("\\\\")) + "/sfx";
        move = new AudioClip(path + "/move.mp3");
        start = new AudioClip(path + "/start.mp3");
        capture = new AudioClip(path + "/capture.mp3");
        castling = new AudioClip(path + "/castling.mp3");
        check = new AudioClip(path + "/check.mp3");
        gameover = new AudioClip(path + "/gameover.mp3");
        stallmate = new AudioClip(path + "/stalemate.mp3");
        checkmate = new AudioClip(path + "/checkmate.mp3");
    }

    public static void playSound(SoundEffect sfx) {
        switch (sfx) {
            case MAKE_MOVE -> {
                move.play();
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
