package com.billiards2d;

import javafx.scene.canvas.GraphicsContext;

public interface GameObject {
    void update(double deltaTime);
    void draw(GraphicsContext gc);
}