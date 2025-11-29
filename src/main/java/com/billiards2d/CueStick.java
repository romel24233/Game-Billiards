package com.billiards2d;

import com.billiards2d.core.GameBus;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import java.util.List;

public class CueStick implements GameObject {

    private CueBall cueBall;
    private List<GameObject> allObjects;

    private boolean isAiming = false;
    private Vector2D aimStart;
    private Vector2D aimCurrent;
    private double currentPower = 0;

    private static final double MAX_POWER = 4000;
    private static final double FORCE_MULTIPLIER = 20.0;
    private static final double TABLE_WIDTH = 1100;
    private static final double TABLE_HEIGHT = 550;
    private static final double RAIL_SIZE = 55;
    private static final double BALL_RADIUS = 15;

    public CueStick(CueBall cueBall, List<GameObject> allObjects) {
        this.cueBall = cueBall;
        this.allObjects = allObjects;
    }

    @Override
    public void update(double deltaTime) {}

    @Override
    public void draw(GraphicsContext gc) {
        if (isAiming && cueBall.getVelocity().length() < 0.1) {
            Vector2D drag = aimStart.subtract(aimCurrent);
            Vector2D direction = drag.normalize();
            currentPower = Math.min(drag.length() * FORCE_MULTIPLIER, MAX_POWER);

            double bestDist = Double.MAX_VALUE;
            Ball target = null;

            for (GameObject obj : allObjects) {
                if (obj instanceof ObjectBall) {
                    Ball b = (Ball) obj;
                    if (!b.isActive()) continue;

                    Vector2D toBall = b.getPosition().subtract(cueBall.getPosition());
                    double dot = toBall.dot(direction);
                    if (dot <= 0) continue;

                    double distLine = Math.sqrt(toBall.length() * toBall.length() - dot * dot);
                    if (distLine < b.getRadius() * 2 - 0.1) {
                        double distImpact = dot - Math.sqrt(Math.pow(b.getRadius() * 2, 2) - distLine * distLine);
                        if (distImpact < bestDist) {
                            bestDist = distImpact;
                            target = b;
                        }
                    }
                }
            }

            double wallDist = getDistanceToWall(cueBall.getPosition(), direction);

            gc.save();
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.setLineDashes(null);

            if (target != null && bestDist < wallDist) {
                Vector2D impact = cueBall.getPosition().add(direction.multiply(bestDist));
                gc.strokeLine(cueBall.getPosition().getX(), cueBall.getPosition().getY(), impact.getX(), impact.getY());

                gc.strokeOval(impact.getX() - BALL_RADIUS, impact.getY() - BALL_RADIUS, BALL_RADIUS * 2, BALL_RADIUS * 2);
                gc.setFill(Color.WHITE);
                gc.fillOval(impact.getX() - 2, impact.getY() - 2, 4, 4);

                Vector2D normal = target.getPosition().subtract(impact).normalize();
                double dot = direction.dot(normal);
                Vector2D tangent = direction.subtract(normal.multiply(dot)).normalize();
                double guideLen = 45.0;

                Vector2D cueEnd = impact.add(tangent.multiply(guideLen));
                gc.strokeLine(impact.getX(), impact.getY(), cueEnd.getX(), cueEnd.getY());
                Vector2D targetEnd = target.getPosition().add(normal.multiply(guideLen));
                gc.strokeLine(target.getPosition().getX(), target.getPosition().getY(), targetEnd.getX(), targetEnd.getY());
            } else {
                Vector2D end = cueBall.getPosition().add(direction.multiply(wallDist));
                gc.strokeLine(cueBall.getPosition().getX(), cueBall.getPosition().getY(), end.getX(), end.getY());
                gc.setFill(Color.WHITE);
                gc.fillOval(end.getX() - 3, end.getY() - 3, 6, 6);
            }
            gc.restore();

            drawVisualStick(gc, direction);
        }
    }

    private double getDistanceToWall(Vector2D pos, Vector2D dir) {
        double right = TABLE_WIDTH - RAIL_SIZE - BALL_RADIUS;
        double left = RAIL_SIZE + BALL_RADIUS;
        double bottom = TABLE_HEIGHT - RAIL_SIZE - BALL_RADIUS;
        double top = RAIL_SIZE + BALL_RADIUS;
        double tMin = Double.MAX_VALUE;

        if (dir.getX() > 0) tMin = Math.min(tMin, (right - pos.getX()) / dir.getX());
        else if (dir.getX() < 0) tMin = Math.min(tMin, (left - pos.getX()) / dir.getX());

        if (dir.getY() > 0) tMin = Math.min(tMin, (bottom - pos.getY()) / dir.getY());
        else if (dir.getY() < 0) tMin = Math.min(tMin, (top - pos.getY()) / dir.getY());

        return tMin == Double.MAX_VALUE ? 0 : tMin;
    }

    private void drawVisualStick(GraphicsContext gc, Vector2D direction) {
        Vector2D stickStart = cueBall.getPosition().add(direction.multiply(-20));
        double pullBack = 15 + (currentPower * 0.04);
        stickStart = stickStart.subtract(direction.multiply(pullBack));
        Vector2D stickEnd = stickStart.add(direction.multiply(-300));

        gc.setStroke(Color.rgb(100, 60, 20));
        gc.setLineWidth(7);
        gc.strokeLine(stickStart.getX(), stickStart.getY(), stickEnd.getX(), stickEnd.getY());

        gc.setStroke(Color.rgb(140, 90, 40));
        gc.setLineWidth(3);
        gc.strokeLine(stickStart.getX(), stickStart.getY(), stickEnd.getX(), stickEnd.getY());

        Vector2D tipEnd = stickStart.add(direction.multiply(-8));
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(7);
        gc.strokeLine(stickStart.getX(), stickStart.getY(), tipEnd.getX(), tipEnd.getY());
    }

    public void handleMousePressed(MouseEvent e) {
        if (cueBall.getVelocity().length() > 0.1) return;
        double dx = e.getX() - cueBall.getPosition().getX();
        double dy = e.getY() - cueBall.getPosition().getY();
        if (Math.sqrt(dx * dx + dy * dy) <= BALL_RADIUS * 8) {
            isAiming = true;
            aimStart = cueBall.getPosition();
            aimCurrent = new Vector2D(e.getX(), e.getY());
        }
    }

    public void handleMouseDragged(MouseEvent e) {
        if (isAiming) aimCurrent = new Vector2D(e.getX(), e.getY());
    }

    public void handleMouseReleased(MouseEvent e) {
        if (!isAiming) return;
        Vector2D drag = aimStart.subtract(aimCurrent);
        double power = Math.min(drag.length() * FORCE_MULTIPLIER, MAX_POWER);

        if (power > 50) {
            Vector2D force = drag.normalize().multiply(power);
            cueBall.hit(force);
            GameBus.publish(GameBus.EventType.SHOT_TAKEN, force);
        }
        isAiming = false;
        currentPower = 0;
    }

    public double getCurrentPower() { return currentPower; }
    public double getMaxPower() { return MAX_POWER; }
}