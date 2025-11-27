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
    private static final double BALL_RADIUS = 10;

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

            // Raycasting
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

            // Visual Guides (White Solid)
            gc.save();
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.setLineDashes(null);

            if (target != null && bestDist < wallDist) {
                Vector2D impact = cueBall.getPosition().add(direction.multiply(bestDist));

                // Aim Line
                gc.strokeLine(cueBall.getPosition().getX(), cueBall.getPosition().getY(), impact.getX(), impact.getY());

                // Ghost Ball
                gc.strokeOval(impact.getX() - BALL_RADIUS, impact.getY() - BALL_RADIUS, BALL_RADIUS * 2, BALL_RADIUS * 2);
                gc.setFill(Color.WHITE);
                gc.fillOval(impact.getX() - 2, impact.getY() - 2, 4, 4);

                // Branching Lines
                Vector2D normal = target.getPosition().subtract(impact).normalize();
                double dot = direction.dot(normal);
                Vector2D tangent = direction.subtract(normal.multiply(dot)).normalize();
                double guideLen = 45.0;

                // Cue Path
                Vector2D cueEnd = impact.add(tangent.multiply(guideLen));
                gc.strokeLine(impact.getX(), impact.getY(), cueEnd.getX(), cueEnd.getY());

                // Target Path
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
        double right = 800 - 30 - 10, left = 30 + 10, bottom = 400 - 30 - 10, top = 30 + 10;
        double tMin = Double.MAX_VALUE;

        if (dir.getX() > 0) tMin = Math.min(tMin, (right - pos.getX()) / dir.getX());
        else if (dir.getX() < 0) tMin = Math.min(tMin, (left - pos.getX()) / dir.getX());

        if (dir.getY() > 0) tMin = Math.min(tMin, (bottom - pos.getY()) / dir.getY());
        else if (dir.getY() < 0) tMin = Math.min(tMin, (top - pos.getY()) / dir.getY());

        return tMin == Double.MAX_VALUE ? 0 : tMin;
    }

    private void drawVisualStick(GraphicsContext gc, Vector2D dir) {
        Vector2D start = cueBall.getPosition().add(dir.multiply(-20));
        start = start.subtract(dir.multiply(15 + currentPower * 0.04));
        Vector2D end = start.add(dir.multiply(-300));

        gc.setStroke(Color.rgb(100, 60, 20));
        gc.setLineWidth(7);
        gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());

        gc.setStroke(Color.CYAN);
        gc.setLineWidth(7);
        Vector2D tip = start.add(dir.multiply(-8));
        gc.strokeLine(start.getX(), start.getY(), tip.getX(), tip.getY());
    }

    public void handleMousePressed(MouseEvent e) {
        if (cueBall.getVelocity().length() > 0.1 || e.getY() < 0) return;
        double dist = Math.sqrt(Math.pow(e.getX() - cueBall.getPosition().getX(), 2) + Math.pow(e.getY() - cueBall.getPosition().getY(), 2));
        if (dist <= BALL_RADIUS * 8) {
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