package com.billiards2d;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class BilliardApp extends Application {

    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 600;

    private GraphicsContext gc;
    private final List<GameObject> gameObjects = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        primaryStage.setTitle("Billiard Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();

        initializeGameObjects();

        GameLoop gameLoop = new GameLoop();
        gameLoop.start();
    }

    private void initializeGameObjects() {
        Table table = new Table(CANVAS_WIDTH, CANVAS_HEIGHT);
        CueBall cueBall = new CueBall(new Vector2D(600, 300));
        ObjectBall ball1 = new ObjectBall(new Vector2D(200, 300), "RED");
        ObjectBall ball2 = new ObjectBall(new Vector2D(220, 290), "BLUE");

        gameObjects.add(table);
        gameObjects.add(cueBall);
        gameObjects.add(ball1);
        gameObjects.add(ball2);
    }

    private class GameLoop extends AnimationTimer {
        private long lastNanoTime = System.nanoTime();

        @Override
        public void handle(long currentNanoTime) {
            double deltaTime = (currentNanoTime - lastNanoTime) / 1_000_000_000.0;
            lastNanoTime = currentNanoTime;

            for (GameObject obj : gameObjects) {
                obj.update(deltaTime);
            }

            gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            for (GameObject obj : gameObjects) {
                obj.draw(gc);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}