package com.billiards2d;

import javafx.scene.paint.Color;

public class ObjectBall extends Ball {

    public ObjectBall(Vector2D position, int number) {
        super(position, getColorForNumber(number), 10.0, number);
    }

    private static Color getColorForNumber(int number) {
        switch (number) {
            case 1: case 9: return Color.YELLOW;
            case 2: case 10: return Color.BLUE;
            case 3: case 11: return Color.RED;
            case 4: case 12: return Color.PURPLE;
            case 5: case 13: return Color.ORANGE;
            case 6: case 14: return Color.GREEN;
            case 7: case 15: return Color.MAROON;
            case 8: return Color.BLACK;
            default: return Color.GRAY;
        }
    }
}