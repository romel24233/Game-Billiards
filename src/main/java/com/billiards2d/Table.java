package com.billiards2d;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import java.util.ArrayList;
import java.util.List;

public class Table implements GameObject {

    private final double width;
    private final double height;
    private final double railSize = 30;
    private final double pocketRadius = 20;
    private List<Vector2D> pockets;

    public Table(double width, double height) {
        this.width = width;
        this.height = height;
        initializePockets();
    }

    private void initializePockets() {
        pockets = new ArrayList<>();
        pockets.add(new Vector2D(railSize, railSize));
        pockets.add(new Vector2D(width - railSize, railSize));
        pockets.add(new Vector2D(railSize, height - railSize));
        pockets.add(new Vector2D(width - railSize, height - railSize));
        pockets.add(new Vector2D(width / 2, railSize));
        pockets.add(new Vector2D(width / 2, height - railSize));
    }

    @Override
    public void update(double deltaTime) {}

    @Override
    public void draw(GraphicsContext gc) {
        // Frame
        gc.setFill(Color.rgb(101, 67, 33));
        gc.fillRect(0, 0, width, height);
        gc.setStroke(Color.rgb(139, 69, 19));
        gc.setLineWidth(5);
        gc.strokeRect(5, 5, width - 10, height - 10);

        // Cloth
        gc.setFill(Color.rgb(0, 100, 0));
        gc.fillRect(railSize, railSize, width - (railSize * 2), height - (railSize * 2));

        // Pockets
        for (Vector2D pocket : pockets) {
            RadialGradient holeGrad = new RadialGradient(
                    0, 0, pocket.getX(), pocket.getY(), pocketRadius,
                    false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.BLACK),
                    new Stop(1.0, Color.rgb(50, 50, 50))
            );
            gc.setFill(holeGrad);
            gc.fillOval(pocket.getX() - pocketRadius, pocket.getY() - pocketRadius, pocketRadius * 2, pocketRadius * 2);
        }

        // Aiming Diamonds
        gc.setFill(Color.WHITESMOKE);
        double markerSize = 5;
        for (int i = 1; i < 8; i++) {
            if (i == 4) continue;
            double x = railSize + (i * (width - 2 * railSize) / 8);
            gc.fillOval(x - markerSize / 2, railSize / 2 - markerSize / 2, markerSize, markerSize);
            gc.fillOval(x - markerSize / 2, height - railSize / 2 - markerSize / 2, markerSize, markerSize);
        }
        for (int i = 1; i < 4; i++) {
            double y = railSize + (i * (height - 2 * railSize) / 4);
            gc.fillOval(railSize / 2 - markerSize / 2, y - markerSize / 2, markerSize, markerSize);
            gc.fillOval(width - railSize / 2 - markerSize / 2, y - markerSize / 2, markerSize, markerSize);
        }

        // Head String
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        double headStringX = width * 0.25;
        gc.strokeLine(headStringX, railSize, headStringX, height - railSize);
    }

    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getRailSize() { return railSize; }
    public double getPocketRadius() { return pocketRadius; }
    public List<Vector2D> getPockets() { return pockets; }
}