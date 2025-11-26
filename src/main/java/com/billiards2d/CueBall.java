package com.billiards2d;

import javafx.scene.paint.Color;

public class CueBall extends Ball {

    private Vector2D startPosition;

    public CueBall(Vector2D position) {
        super(position, Color.WHITE, 10.0, 0);
        this.startPosition = position;
    }

    public void hit(Vector2D force) {
        this.velocity = force;
    }

    public void reset() {
        this.position = startPosition;
        this.velocity = new Vector2D(0, 0);
        this.active = true;
    }
}