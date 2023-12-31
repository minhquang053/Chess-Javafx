package com.yelaco.piece;

import com.yelaco.common.Board;
import com.yelaco.common.Spot;

import java.util.ArrayList;

public class King extends Piece {
    private boolean castlingDone = false;
    private boolean inCheck = false;

    public King(boolean isWhite) {
       super(isWhite);
    }
    public boolean inCheck() {
        return this.inCheck;
    }

    public void setInCheck(boolean inCheck) {
        this.inCheck = inCheck;
    }

    public boolean isCastlingDone() {
        return this.castlingDone;
    }

    public void setCastlingDone(boolean castlingDone) {
        this.castlingDone = castlingDone;
    }

    @Override
    public boolean canMove(Board board, Spot start, Spot end) {
        // check same location
        if (start == end) {
            return false;
        }

        // check different color
        if (end.getPiece() != null
                && (start.getPiece().isWhite() == end.getPiece().isWhite())
                && !isCastlingMove(start, end)) {
            return false;
        }

        // check valid move
        if (Math.abs(start.getX() - end.getX()) <= 1 && Math.abs(start.getY() - end.getY()) <= 1) {
            // if the king can kill the opponent's king, it's already a win, no need to check if being attacked after
            if (end.getPiece() instanceof King) {
                return true;
            }
            // check if move results in King being attacked
            var supposedPiece = end.getPiece();
            end.setPiece(start.getPiece());
            start.setPiece(null);
            try {
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        var box = board.getBox(i, j);
                        var piece = box.getPiece();
                        if (piece == null) {
                            continue;
                        }
                        if (piece.isWhite() == end.getPiece().isWhite()) {
                            continue;
                        }
                        if (piece.canMove(board, box, end)) {
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                start.setPiece(end.getPiece());
                end.setPiece(supposedPiece);
            }
            return true;
        }

        // if not normal valid move, the only option left is castling
        return this.isValidCastling(board, start, end);
    }

    private boolean isValidCastling(Board board, Spot start, Spot end) {
        if (this.isCastlingDone()) {
            return false;
        }

        if (!(end.getPiece() instanceof Rook)) {
            return false;
        }

        if (end.getY() != start.getY()) {
            return false;
        }

        if (inCheck) {
            return false;
        }

        if (!isCastlingMove(start, end)) {
            return false;
        }

        ArrayList<Spot> inBetweens = new ArrayList<>();
        inBetweens.add(start);

        int i = start.getX();
        int j = start.getY();
        int bex = end.getX();
        if (i < bex) {
            bex--;
        } else if (i > bex){
            bex++;
        }

        while (i != bex) {
            if (i < bex) {
                i++;
            } else {
                i--;
            }

            try {
                if (board.getBox(i, j).getPiece() != null) {
                    return false;
                } else {
                    inBetweens.add(board.getBox(i, j));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        // Check valid castling
        try {
            for (Spot theSpot : inBetweens) {
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        var box = board.getBox(x, y);
                        var piece = box.getPiece();
                        if (piece == null) {
                            continue;
                        }
                        if (piece.isWhite() == start.getPiece().isWhite()) {
                            continue;
                        }
                        if (piece.canMove(board, box, theSpot)) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean isCastlingMove(Spot start, Spot end) {
        if (start.getY() != 0 && start.getY() != 7) {
            return false;
        }

        if ( start.getX() != 4 || (end.getX() != 0 && end.getX() != 7) ) {
            return false;
        }

        return true;
    }
}
