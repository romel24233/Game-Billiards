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
    private final double railSize = 55;
    private final double pocketRadius = 30;
    private List<Vector2D> pockets;

    public Table(double width, double height) {
        this.width = width;
        this.height = height;
        initializePockets();
    }

    private void initializePockets() {
        pockets = new ArrayList<>();
        double cornerOffset = railSize * 0.85;
        pockets.add(new Vector2D(cornerOffset, cornerOffset));
        pockets.add(new Vector2D(width - cornerOffset, cornerOffset));
        pockets.add(new Vector2D(cornerOffset, height - cornerOffset));
        pockets.add(new Vector2D(width - cornerOffset, height - cornerOffset));

        double sideOffset = railSize * 0.6;
        pockets.add(new Vector2D(width / 2, sideOffset));
        pockets.add(new Vector2D(width / 2, height - sideOffset));
    }

    @Override
    public void update(double deltaTime) {}

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.rgb(80, 40, 10));
        gc.fillRect(0, 0, width, height);
        gc.setStroke(Color.rgb(120, 70, 30));
        gc.setLineWidth(4);
        gc.strokeRect(4, 4, width - 8, height - 8);

        gc.setFill(Color.rgb(0, 100, 30));
        gc.fillRoundRect(railSize, railSize, width - (railSize * 2), height - (railSize * 2), 12, 12);

        for (Vector2D pocket : pockets) {
            RadialGradient holeGrad = new RadialGradient(
                    0, 0, pocket.getX(), pocket.getY(), pocketRadius,
                    false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.BLACK),
                    new Stop(0.8, Color.rgb(20, 20, 20)),
                    new Stop(1.0, Color.rgb(60, 40, 20))
            );
            gc.setFill(holeGrad);
            gc.fillOval(pocket.getX() - pocketRadius, pocket.getY() - pocketRadius, pocketRadius * 2, pocketRadius * 2);

            gc.setStroke(Color.rgb(40, 20, 5));
            gc.setLineWidth(2);
            gc.strokeOval(pocket.getX() - pocketRadius, pocket.getY() - pocketRadius, pocketRadius * 2, pocketRadius * 2);
        }

        drawDetails(gc);
    }

    private void drawDetails(GraphicsContext gc) {
        gc.setFill(Color.WHITESMOKE);
        double markerSize = 8;
        double offset = railSize / 2;

        for (int i = 1; i < 8; i++) {
            if (i == 4) continue;
            double x = railSize + (i * (width - 2 * railSize) / 8);
            drawDiamond(gc, x, offset, markerSize);
            drawDiamond(gc, x, height - offset, markerSize);
        }
        for (int i = 1; i < 4; i++) {
            double y = railSize + (i * (height - 2 * railSize) / 4);
            drawDiamond(gc, offset, y, markerSize);
            drawDiamond(gc, width - offset, y, markerSize);
        }

        gc.setStroke(Color.rgb(255, 255, 255, 0.25));
        gc.setLineWidth(1);
        double headStringX = width * 0.25;
        gc.strokeLine(headStringX, railSize, headStringX, height - railSize);
    }

    private void drawDiamond(GraphicsContext gc, double cx, double cy, double size) {
        double[] xPoints = {cx, cx + size/1.5, cx, cx - size/1.5};
        double[] yPoints = {cy - size/1.5, cy, cy + size/1.5, cy};
        gc.fillPolygon(xPoints, yPoints, 4);
    }

    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getRailSize() { return railSize; }
    public double getPocketRadius() { return pocketRadius; }
    public List<Vector2D> getPockets() { return pockets; }
}