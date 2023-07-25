package com.yelaco.common;

import com.yelaco.piece.*;

public class Board {
    Spot[][] boxes;

    public Board() {
        boxes = new Spot[8][8];
        this.setBoard();
    }

    public Spot getBox(int x, int y) throws Exception {
        if (x < 0 || x > 7 || y < 0 || y > 7) {
            throw new Exception("Index out of bound");
        }
        return boxes[x][y];
    }

    public void setBoard() {
        boxes[0][0] = new Spot(0, 0, new Rook(true));
        boxes[1][0] = new Spot(1, 0, new Knight(true));
        boxes[2][0] = new Spot(2, 0, new Bishop(true));
        boxes[3][0] = new Spot(3, 0, new Queen(true));
        boxes[4][0] = new Spot(4, 0, new King(true));
        boxes[5][0] = new Spot(5, 0, new Bishop(true));
        boxes[6][0] = new Spot(6, 0, new Knight(true));
        boxes[7][0] = new Spot(7, 0, new Rook(true));

        boxes[0][7] = new Spot(0, 7, new Rook(false));
        boxes[1][7] = new Spot(1, 7, new Knight(false));
        boxes[2][7] = new Spot(2, 7, new Bishop(false));
        boxes[3][7] = new Spot(3, 7, new Queen(false));
        boxes[4][7] = new Spot(4, 7, new King(false));
        boxes[5][7] = new Spot(5, 7, new Bishop(false));
        boxes[6][7] = new Spot(6, 7, new Knight(false));
        boxes[7][7] = new Spot(7, 7, new Rook(false));

        for (int i = 0; i < 8; i++) {
            boxes[i][1] = new Spot(i, 1, new Pawn(true));
            boxes[i][6] = new Spot(i, 6, new Pawn(false));
        }

        for (int i = 0; i < 8; i++) {
            for (int j = 2; j < 6; j++) {
                boxes[i][j] = new Spot(i, j, null);
            }
        }
    }
}
