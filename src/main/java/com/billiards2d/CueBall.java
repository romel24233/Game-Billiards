package com.billiards2d;

import javafx.scene.paint.Color;

public class CueBall extends Ball {
    public CueBall(Vector2D position) {
        super(position, Color.WHITE, 15.0, 0);
    }

    public void hit(Vector2D force) {
        this.velocity = force;
    }
}