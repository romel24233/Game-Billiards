package com.billiards2d;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

public class CueStick implements GameObject {

    private CueBall cueBall;

    public CueStick(CueBall cueBall) {
        this.cueBall = cueBall;
    }

    @Override
    public void update(double deltaTime) {
        // TODO: Implement in Week 4
    }

    @Override
    public void draw(GraphicsContext gc) {
        // TODO: Implement in Week 4
    }

    public void handleMouseInput(MouseEvent event) {
        // TODO: Implement in Week 4
    }
}