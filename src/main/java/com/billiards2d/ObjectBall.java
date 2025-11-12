package com.billiards2d;

import javafx.scene.paint.Color;

public class ObjectBall extends Ball {

    private String type;

    public ObjectBall(Vector2D position, String type) {
        super(position, Color.valueOf(type), 10.0);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}