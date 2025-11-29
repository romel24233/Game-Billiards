package com.billiards2d;

import javafx.scene.paint.Color;

public class ObjectBall extends Ball {
    public ObjectBall(Vector2D position, int number) {
        super(position, getColorForNumber(number), 15.0, number);
    }

    public static Color getColorForNumber(int number) {
        return switch (number) {
            case 1, 9 -> Color.YELLOW;
            case 2, 10 -> Color.BLUE;
            case 3, 11 -> Color.RED;
            case 4, 12 -> Color.PURPLE;
            case 5, 13 -> Color.ORANGE;
            case 6, 14 -> Color.GREEN;
            case 7, 15 -> Color.MAROON;
            case 8 -> Color.BLACK;
            default -> Color.GRAY;
        };
    }
}