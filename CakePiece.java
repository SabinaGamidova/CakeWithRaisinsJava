package org.example;

public class CakePiece {
    String[][] piece;
    double area;

    public CakePiece() {}

    public CakePiece(String[][] piece) {
        this.piece = piece;
    }

    public CakePiece(String[][] piece, double area) {
        this.piece = piece;
        this.area = area;
    }
}