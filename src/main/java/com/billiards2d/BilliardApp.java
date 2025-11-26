package com.billiards2d;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class BilliardApp extends Application {

    public static final double TABLE_WIDTH = 800;
    public static final double TABLE_HEIGHT = 600;
    public static final double SIDEBAR_WIDTH = 100;
    public static final double BOTTOM_HEIGHT = 100;

    private static final double APP_WIDTH = TABLE_WIDTH + SIDEBAR_WIDTH;
    private static final double APP_HEIGHT = TABLE_HEIGHT + BOTTOM_HEIGHT;

    private GraphicsContext gc;
    private final List<GameObject> gameObjects = new ArrayList<>();
    private CueStick cueStick;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(APP_WIDTH, APP_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        root.setStyle("-fx-background-color: #000000;");

        Scene scene = new Scene(root, APP_WIDTH, APP_HEIGHT);

        scene.setOnMousePressed(e -> cueStick.handleMousePressed(e));
        scene.setOnMouseDragged(e -> cueStick.handleMouseDragged(e));
        scene.setOnMouseReleased(e -> cueStick.handleMouseReleased(e));

        primaryStage.setTitle("Billiard Simulation 2D - Ultimate");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();

        initializeGameObjects();

        new AnimationTimer() {
            private long lastNanoTime = System.nanoTime();
            @Override
            public void handle(long currentNanoTime) {
                double deltaTime = (currentNanoTime - lastNanoTime) / 1_000_000_000.0;
                lastNanoTime = currentNanoTime;
                updateAndDraw(deltaTime);
            }
        }.start();
    }

    private void initializeGameObjects() {
        Table table = new Table(TABLE_WIDTH, TABLE_HEIGHT);
        gameObjects.add(table);

        // 8-Ball rack setup
        double startX = 600;
        double startY = 300;
        double radius = 10;
        int ballNumber = 1;

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col <= row; col++) {
                double x = startX + (row * radius * 2 * 0.866);
                double y = startY - (row * radius) + (col * radius * 2);

                int num = ballNumber++;
                if (row == 2 && col == 1) num = 8;
                else if (num == 8) num = 15;

                gameObjects.add(new ObjectBall(new Vector2D(x, y), num));
            }
        }

        CueBall cueBall = new CueBall(new Vector2D(200, 300));
        gameObjects.add(cueBall);

        this.cueStick = new CueStick(cueBall, gameObjects);
        gameObjects.add(cueStick);

        PhysicsEngine physicsEngine = new PhysicsEngine(table, gameObjects);
        gameObjects.add(physicsEngine);
    }

    private void updateAndDraw(double deltaTime) {
        for (GameObject obj : gameObjects) {
            obj.update(deltaTime);
        }

        gc.clearRect(0, 0, APP_WIDTH, APP_HEIGHT);

        // Clip game area
        gc.save();
        gc.beginPath();
        gc.rect(0, 0, TABLE_WIDTH, TABLE_HEIGHT);
        gc.clip();
        for (GameObject obj : gameObjects) {
            obj.draw(gc);
        }
        gc.restore();

        // Render UI
        drawUIBackground();
        drawUIForeground();
        drawPowerBar();
    }

    private void drawUIBackground() {
        LinearGradient sideGrad = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(20, 20, 20)),
                new Stop(1, Color.rgb(40, 40, 40))
        );
        gc.setFill(sideGrad);
        gc.fillRect(TABLE_WIDTH, 0, SIDEBAR_WIDTH, APP_HEIGHT);

        gc.setStroke(Color.GOLDENROD);
        gc.setLineWidth(4);
        gc.strokeLine(TABLE_WIDTH, 0, TABLE_WIDTH, APP_HEIGHT);

        LinearGradient bottomGrad = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(25, 25, 25)),
                new Stop(1, Color.rgb(10, 10, 10))
        );
        gc.setFill(bottomGrad);
        gc.fillRect(0, TABLE_HEIGHT, TABLE_WIDTH, BOTTOM_HEIGHT);

        gc.strokeLine(0, TABLE_HEIGHT, TABLE_WIDTH, TABLE_HEIGHT);
    }

    private void drawUIForeground() {
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        gc.fillText("POCKETED BALLS", 20, TABLE_HEIGHT + 30);

        double trayX = 20;
        double trayY = TABLE_HEIGHT + 45;
        double trayW = TABLE_WIDTH - 40;

        gc.setFill(Color.rgb(5, 5, 5));
        gc.fillRoundRect(trayX, trayY, trayW, 45, 20, 20);
        gc.setStroke(Color.rgb(60, 60, 60));
        gc.setLineWidth(1);
        gc.strokeRoundRect(trayX, trayY, trayW, 45, 20, 20);

        int drawX = (int) trayX + 25;
        int drawY = (int) trayY + 23;

        for (GameObject obj : gameObjects) {
            if (obj instanceof ObjectBall) {
                ObjectBall ball = (ObjectBall) obj;
                if (!ball.isActive()) {
                    ball.drawBallAt(gc, drawX, drawY);
                    drawX += 32;
                }
            }
        }
    }

    private void drawPowerBar() {
        double barWidth = 30;
        double barHeight = 400;
        double centerX = TABLE_WIDTH + (SIDEBAR_WIDTH / 2);
        double barY = 100;
        double padding = 4;

        // Power Bar Container
        gc.setFill(Color.rgb(5, 5, 5));
        gc.fillRoundRect(centerX - barWidth/2, barY, barWidth, barHeight, 15, 15);
        gc.setStroke(Color.rgb(80, 80, 80));
        gc.setLineWidth(2);
        gc.strokeRoundRect(centerX - barWidth/2, barY, barWidth, barHeight, 15, 15);

        // Power Fill Calculation
        double maxPower = cueStick.getMaxPower();
        double current = cueStick.getCurrentPower();
        double ratio = current / maxPower;

        if (ratio > 0.99) ratio = 1.0;

        double maxFillHeight = barHeight - (padding * 2);
        double currentFillHeight = ratio * maxFillHeight;

        if (currentFillHeight > 0) {
            LinearGradient powerGrad = new LinearGradient(
                    0, 1, 0, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.LIME),
                    new Stop(0.5, Color.YELLOW),
                    new Stop(1.0, Color.RED)
            );
            gc.setFill(powerGrad);

            gc.fillRoundRect(
                    centerX - barWidth/2 + padding,
                    (barY + barHeight) - padding - currentFillHeight,
                    barWidth - (padding * 2),
                    currentFillHeight,
                    10, 10
            );
        }

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("FORCE", centerX, barY + barHeight + 25);

        String percent = String.format("%d%%", (int)(ratio * 100));
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        gc.fillText(percent, centerX, barY - 15);
    }

    public static void main(String[] args) {
        launch(args);
    }
}