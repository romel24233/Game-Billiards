package com.billiards2d;

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
    private static final double PREDICTION_SCALE = 0.15;

    private static final double TABLE_WIDTH = 800;
    private static final double TABLE_HEIGHT = 600;
    private static final double RAIL_SIZE = 30;
    private static final double BALL_RADIUS = 10;

    public CueStick(CueBall cueBall, List<GameObject> allObjects) {
        this.cueBall = cueBall;
        this.allObjects = allObjects;
    }

    @Override
    public void update(double deltaTime) {
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (isAiming && cueBall.getVelocity().length() == 0) {
            Vector2D dragVector = aimStart.subtract(aimCurrent);
            Vector2D direction = dragVector.normalize();

            double dragDist = dragVector.length();
            currentPower = Math.min(dragDist * FORCE_MULTIPLIER, MAX_POWER);

            // --- Raycasting ---
            double distToBall = Double.MAX_VALUE;
            Ball targetBall = null;

            for (GameObject obj : allObjects) {
                if (obj instanceof ObjectBall) {
                    Ball b = (Ball) obj;
                    if (!b.isActive()) continue;

                    Vector2D toBall = b.getPosition().subtract(cueBall.getPosition());
                    double dot = toBall.dot(direction);

                    if (dot <= 0) continue;

                    double distToLine = Math.sqrt(toBall.length() * toBall.length() - dot * dot);

                    if (distToLine < b.getRadius() * 2 - 0.1) {
                        double distToImpact = dot - Math.sqrt(Math.pow(b.getRadius() * 2, 2) - distToLine * distToLine);
                        if (distToImpact < distToBall) {
                            distToBall = distToImpact;
                            targetBall = b;
                        }
                    }
                }
            }

            double distToWall = getDistanceToWall(cueBall.getPosition(), direction);

            // --- Render Trajectory ---
            gc.save();

            if (targetBall != null && distToBall < distToWall) {
                // Impact Point Calculation
                Vector2D impactPoint = cueBall.getPosition().add(direction.multiply(distToBall));

                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2);
                gc.strokeLine(cueBall.getPosition().getX(), cueBall.getPosition().getY(), impactPoint.getX(), impactPoint.getY());

                // Ghost Ball Visual
                gc.setStroke(Color.WHITESMOKE);
                gc.setLineWidth(1);
                gc.setLineDashes(4);
                gc.strokeOval(impactPoint.getX() - 10, impactPoint.getY() - 10, 20, 20);
                gc.setLineDashes(null);

                Vector2D normal = targetBall.getPosition().subtract(impactPoint).normalize();
                double impactRatio = direction.dot(normal);

                // Prediction Line 1: Object Ball
                double targetLen = currentPower * impactRatio * PREDICTION_SCALE;
                double wallDistTarget = getDistanceToWall(targetBall.getPosition(), normal);
                double finalTargetLen = Math.min(targetLen, wallDistTarget);

                if (finalTargetLen > 5) {
                    Vector2D targetEnd = targetBall.getPosition().add(normal.multiply(finalTargetLen));
                    gc.setStroke(Color.CYAN);
                    gc.setLineWidth(3);
                    gc.strokeLine(targetBall.getPosition().getX(), targetBall.getPosition().getY(), targetEnd.getX(), targetEnd.getY());
                    gc.setFill(Color.CYAN);
                    gc.fillOval(targetEnd.getX() - 3, targetEnd.getY() - 3, 6, 6);
                }

                // Prediction Line 2: Cue Ball Deflection
                Vector2D tangent = direction.subtract(normal.multiply(impactRatio)).normalize();
                double cueLen = currentPower * (1.0 - impactRatio) * PREDICTION_SCALE;
                double wallDistCue = getDistanceToWall(impactPoint, tangent);
                double finalCueLen = Math.min(cueLen, wallDistCue);

                if (finalCueLen > 5) {
                    Vector2D deflectionEnd = impactPoint.add(tangent.multiply(finalCueLen));
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(1.5);
                    gc.strokeLine(impactPoint.getX(), impactPoint.getY(), deflectionEnd.getX(), deflectionEnd.getY());
                    gc.setFill(Color.WHITE);
                    gc.fillOval(deflectionEnd.getX() - 2, deflectionEnd.getY() - 2, 4, 4);
                }

            } else {
                // Wall Impact
                Vector2D endPoint = cueBall.getPosition().add(direction.multiply(distToWall));
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2);
                gc.strokeLine(cueBall.getPosition().getX(), cueBall.getPosition().getY(), endPoint.getX(), endPoint.getY());
                gc.setFill(Color.WHITE);
                gc.fillOval(endPoint.getX() - 3, endPoint.getY() - 3, 6, 6);
            }
            gc.restore();

            drawVisualStick(gc, direction);
        }
    }

    private double getDistanceToWall(Vector2D startPos, Vector2D dir) {
        double x = startPos.getX();
        double y = startPos.getY();
        double dx = dir.getX();
        double dy = dir.getY();

        double tMin = Double.MAX_VALUE;

        // Collision boundaries accounting for rail and radius
        double rightLimit = TABLE_WIDTH - RAIL_SIZE - BALL_RADIUS;
        double leftLimit = RAIL_SIZE + BALL_RADIUS;
        double bottomLimit = TABLE_HEIGHT - RAIL_SIZE - BALL_RADIUS;
        double topLimit = RAIL_SIZE + BALL_RADIUS;

        if (dx > 0.001) {
            double t = (rightLimit - x) / dx;
            if (t >= 0 && t < tMin) tMin = t;
        } else if (dx < -0.001) {
            double t = (leftLimit - x) / dx;
            if (t >= 0 && t < tMin) tMin = t;
        }

        if (dy > 0.001) {
            double t = (bottomLimit - y) / dy;
            if (t >= 0 && t < tMin) tMin = t;
        } else if (dy < -0.001) {
            double t = (topLimit - y) / dy;
            if (t >= 0 && t < tMin) tMin = t;
        }

        return (tMin == Double.MAX_VALUE) ? 0 : tMin;
    }

    private void drawVisualStick(GraphicsContext gc, Vector2D direction) {
        Vector2D stickStart = cueBall.getPosition().add(direction.multiply(-20));
        Vector2D stickEnd = cueBall.getPosition().add(direction.multiply(-300));

        gc.setStroke(Color.SADDLEBROWN.darker());
        gc.setLineWidth(8);
        gc.strokeLine(stickStart.getX(), stickStart.getY(), stickEnd.getX(), stickEnd.getY());

        gc.setStroke(Color.SADDLEBROWN);
        gc.setLineWidth(4);
        gc.strokeLine(stickStart.getX(), stickStart.getY(), stickEnd.getX(), stickEnd.getY());

        Vector2D tipEnd = cueBall.getPosition().add(direction.multiply(-25));
        gc.setStroke(Color.IVORY);
        gc.setLineWidth(8);
        gc.strokeLine(stickStart.getX(), stickStart.getY(), tipEnd.getX(), tipEnd.getY());
    }

    public void handleMousePressed(MouseEvent e) {
        if (cueBall.getVelocity().length() > 0) return;
        if (e.getX() > TABLE_WIDTH || e.getY() > TABLE_HEIGHT) return;

        double dx = e.getX() - cueBall.getPosition().getX();
        double dy = e.getY() - cueBall.getPosition().getY();
        if (Math.sqrt(dx * dx + dy * dy) <= 60) {
            isAiming = true;
            aimStart = cueBall.getPosition();
            aimCurrent = new Vector2D(e.getX(), e.getY());
        }
    }

    public void handleMouseDragged(MouseEvent e) {
        if (!isAiming) return;
        aimCurrent = new Vector2D(e.getX(), e.getY());
    }

    public void handleMouseReleased(MouseEvent e) {
        if (!isAiming) return;
        Vector2D drag = aimStart.subtract(aimCurrent);
        double power = Math.min(drag.length() * FORCE_MULTIPLIER, MAX_POWER);
        Vector2D hitForce = drag.normalize().multiply(power);
        cueBall.hit(hitForce);
        isAiming = false;
        currentPower = 0;
    }

    public double getCurrentPower() { return currentPower; }
    public double getMaxPower() { return MAX_POWER; }
}