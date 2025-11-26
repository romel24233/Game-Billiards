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
        for (GameObject obj1 : gameObjects) {
            if (!(obj1 instanceof Ball)) continue;
            Ball b1 = (Ball) obj1;
            if (!b1.isActive()) continue;

            if (checkPocketCollision(b1)) continue;
            checkWallCollision(b1);

            for (GameObject obj2 : gameObjects) {
                if (!(obj2 instanceof Ball)) continue;
                Ball b2 = (Ball) obj2;
                if (b1 == b2 || !b2.isActive()) continue;
                resolveBallCollision(b1, b2);
            }
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
    }

    private boolean checkPocketCollision(Ball ball) {
        double pocketSensitiveRadius = table.getPocketRadius() * 0.9;

        for (Vector2D pocketPos : table.getPockets()) {
            double dist = ball.getPosition().subtract(pocketPos).length();

            if (dist < pocketSensitiveRadius) {
                ball.setVelocity(new Vector2D(0, 0));
                if (ball instanceof CueBall) {
                    ((CueBall) ball).reset(); // Scratch
                } else {
                    ball.setActive(false); // Ball potted
                }
                return true;
            }
        }
        return false;
    }

    private void checkWallCollision(Ball ball) {
        double x = ball.getPosition().getX();
        double y = ball.getPosition().getY();
        double r = ball.getRadius();
        double vx = ball.getVelocity().getX();
        double vy = ball.getVelocity().getY();

        double rail = table.getRailSize();
        double width = table.getWidth();
        double height = table.getHeight();

        // High restitution for realistic bank shots
        double wallBounciness = 0.95;

        boolean collided = false;

        if (x - r < rail) {
            x = rail + r;
            vx = -vx * wallBounciness;
            collided = true;
        } else if (x + r > width - rail) {
            x = width - rail - r;
            vx = -vx * wallBounciness;
            collided = true;
        }

        if (y - r < rail) {
            y = rail + r;
            vy = -vy * wallBounciness;
            collided = true;
        } else if (y + r > height - rail) {
            y = height - rail - r;
            vy = -vy * wallBounciness;
            collided = true;
        }

        if (collided) {
            ball.setPosition(new Vector2D(x, y));
            ball.setVelocity(new Vector2D(vx, vy));
        }
    }

    private void resolveBallCollision(Ball b1, Ball b2) {
        Vector2D posDiff = b1.getPosition().subtract(b2.getPosition());
        double dist = posDiff.length();

        if (dist == 0 || dist > b1.getRadius() + b2.getRadius()) return;

        Vector2D normal = posDiff.normalize();
        Vector2D relVel = b1.getVelocity().subtract(b2.getVelocity());
        double speed = relVel.dot(normal);

        if (speed >= 0) return;

        // Elastic collision physics
        double restitution = 0.92;

        double impulse = (1 + restitution) * speed / (b1.getMass() + b2.getMass());
        b1.setVelocity(b1.getVelocity().subtract(normal.multiply(impulse * b2.getMass())));
        b2.setVelocity(b2.getVelocity().add(normal.multiply(impulse * b1.getMass())));

        // Anti-overlap correction
        double overlap = (b1.getRadius() + b2.getRadius() - dist) / 2;
        Vector2D correction = normal.multiply(overlap);
        b1.setPosition(b1.getPosition().add(correction));
        b2.setPosition(b2.getPosition().subtract(correction));
    }
}