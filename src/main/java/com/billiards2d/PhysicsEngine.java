package com.billiards2d;

import javafx.scene.canvas.GraphicsContext;
import java.util.List;

public class PhysicsEngine implements GameObject {

    private Table table;
    private List<GameObject> gameObjects;

    public PhysicsEngine(Table table, List<GameObject> gameObjects) {
        this.table = table;
        this.gameObjects = gameObjects;
    }

    @Override
    public void update(double deltaTime) {
        // TODO: Implement collision detection in Week 4-5
    }

    @Override
    public void draw(GraphicsContext gc) {
        // No rendering needed
    }
}