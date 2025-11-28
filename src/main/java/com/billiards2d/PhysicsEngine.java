package com.billiards2d;

import com.billiards2d.core.GameBus;
import javafx.scene.canvas.GraphicsContext;
import java.util.List;

public class PhysicsEngine implements GameObject {

    private final Table table;
    private final List<GameObject> gameObjects;

    // Physics Constants
    private static final int SUB_STEPS = 10;
    private static final double FRICTION = 0.990;
    private static final double RESTITUTION = 0.92;
    private static final double STOP_THRESHOLD = 1.5;

    public PhysicsEngine(Table table, List<GameObject> gameObjects) {
        this.table = table;
        this.gameObjects = gameObjects;
    }

    @Override
    public void update(double deltaTime) {
        // Apply friction once per frame
        applyFriction();

        // Sub-stepping for accurate collision detection
        double stepTime = deltaTime / SUB_STEPS;
        for (int i = 0; i < SUB_STEPS; i++) {
            moveBalls(stepTime);
            resolveCollisions();
        }
    }

    private void applyFriction() {
        for (GameObject obj : gameObjects) {
            if (obj instanceof Ball b && b.isActive()) {
                b.setVelocity(b.getVelocity().multiply(FRICTION));
                if (b.getVelocity().length() < STOP_THRESHOLD) {
                    b.setVelocity(new Vector2D(0, 0));
                }
            }
        }
    }

    private void moveBalls(double stepTime) {
        for (GameObject obj : gameObjects) {
            if (obj instanceof Ball b && b.isActive()) {
                b.setPosition(b.getPosition().add(b.getVelocity().multiply(stepTime)));
            }
        }
    }

    private void resolveCollisions() {
        for (int i = 0; i < gameObjects.size(); i++) {
            if (!(gameObjects.get(i) instanceof Ball b1) || !b1.isActive()) continue;

            if (checkPocketCollision(b1)) continue;
            checkWallCollision(b1);

            for (int j = i + 1; j < gameObjects.size(); j++) {
                if (!(gameObjects.get(j) instanceof Ball b2) || !b2.isActive()) continue;
                resolveBallBall(b1, b2);
            }
        }
    }

    @Override
    public void draw(GraphicsContext gc) {}

    private boolean checkPocketCollision(Ball ball) {
        double sensitiveRadius = table.getPocketRadius() * 1.0;
        for (Vector2D pocket : table.getPockets()) {
            if (ball.getPosition().subtract(pocket).length() < sensitiveRadius) {
                ball.setVelocity(new Vector2D(0, 0));
                ball.setActive(false);
                ball.setPosition(pocket); // Center visual
                GameBus.publish(GameBus.EventType.BALL_POTTED, ball);
                return true;
            }
        }
        return false;
    }

    private void checkWallCollision(Ball ball) {
        double x = ball.getPosition().getX();
        double y = ball.getPosition().getY();
        double r = ball.getRadius();
        double rail = table.getRailSize();
        double w = table.getWidth();
        double h = table.getHeight();
        double bounce = 0.85;

        double vx = ball.getVelocity().getX();
        double vy = ball.getVelocity().getY();
        boolean collided = false;

        // Horizontal Walls
        if (x - r < rail) {
            x = rail + r;
            vx = Math.abs(vx) * bounce;
            collided = true;
        } else if (x + r > w - rail) {
            x = w - rail - r;
            vx = -Math.abs(vx) * bounce;
            collided = true;
        }

        // Vertical Walls
        if (y - r < rail) {
            y = rail + r;
            vy = Math.abs(vy) * bounce;
            collided = true;
        } else if (y + r > h - rail) {
            y = h - rail - r;
            vy = -Math.abs(vy) * bounce;
            collided = true;
        }

        if (collided) {
            ball.setPosition(new Vector2D(x, y));
            ball.setVelocity(new Vector2D(vx, vy));
        }
    }

    private void resolveBallBall(Ball b1, Ball b2) {
        Vector2D delta = b1.getPosition().subtract(b2.getPosition());
        double dist = delta.length();
        double minDist = b1.getRadius() + b2.getRadius();

        if (dist >= minDist || dist == 0) return;

        // Position Correction (Anti-overlap)
        Vector2D normal = delta.normalize();
        double overlap = minDist - dist;
        Vector2D correction = normal.multiply(overlap * 0.5);

        b1.setPosition(b1.getPosition().add(correction));
        b2.setPosition(b2.getPosition().subtract(correction));

        // Velocity Response
        Vector2D relVel = b1.getVelocity().subtract(b2.getVelocity());
        double velAlongNormal = relVel.dot(normal);

        if (velAlongNormal > 0) return;

        double j = -(1 + RESTITUTION) * velAlongNormal;
        j /= (1 / b1.getMass() + 1 / b2.getMass());

        Vector2D impulse = normal.multiply(j);
        b1.setVelocity(b1.getVelocity().add(impulse.multiply(1 / b1.getMass())));
        b2.setVelocity(b2.getVelocity().subtract(impulse.multiply(1 / b2.getMass())));
    }
}