package com.billiards2d;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Table implements GameObject {

    private double width;
    private double height;

    public Table(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(double deltaTime) {
        // Static object, no updates
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 0, width, height);
    }

    public double getWidth() { return width; }
    public double getHeight() { return height; }
}