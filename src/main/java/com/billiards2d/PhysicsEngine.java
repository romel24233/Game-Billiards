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
        for (GameObject obj1 : gameObjects) {
            if (!(obj1 instanceof Ball)) continue;
            Ball b1 =  (Ball) obj1;

            checkWallCollision(b1);

            for (GameObject obj2 : gameObjects) {
                if (!(obj2 instanceof Ball)) continue;
                Ball b2 =  (Ball) obj2;

                if (b1 == b2) continue;

                resolveBallCollision(b1, b2);
            }
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        // No rendering needed
    }

    private void checkWallCollision(Ball ball) {
        double x = ball.getPosition().getX();
        double y = ball.getPosition().getY();
        double r = ball.getRadius();

        double vx = ball.getVelocity().getX();
        double vy = ball.getVelocity().getY();

        boolean collided = false;

        // left wall
        if (x - r < 0) {
            x = r;
            vx = -vx;
            collided = true;
        }
        // right wall
        else if (x + r > table.getWidth()) {
            x = table.getWidth() - r;
            vx = -vx;
            collided = true;
        }

        // top wall
        if (y - r < 0) {
            y = r;
            vy = -vy;
            collided = true;
        }
        // bottom wall
        else if (y + r > table.getHeight()) {
            y = table.getHeight() - r;
            vy = -vy;
            collided = true;
        }

        if (collided) {
            ball.setPosition(new Vector2D(x, y));
            ball.setVelocity(new Vector2D(vx, vy));
        }
    }

    private void resolveBallCollision(Ball b1, Ball b2)
    {
        Vector2D posDiff = b1.getPosition().subtract(b2.getPosition());
        double dist = posDiff.length();

        if (dist == 0 || dist > b1.getRadius() + b2.getRadius()) return;

        Vector2D normalVector = posDiff.normalize();

        Vector2D relativeVel = b1.getVelocity().subtract(b2.getVelocity());
        double speed = relativeVel.dot(normalVector);

        if (speed >= 0) return;

        double impulse = 2 * speed / (b1.getMass() + b2.getMass());

        double restitution = 0.9; // sedikit hilang energi (smoother than perfect bounce)

        b1.setVelocity(
                b1.getVelocity().subtract(normalVector.multiply(impulse * b2.getMass()))
                        .multiply(restitution)
        );

        b2.setVelocity(
                b2.getVelocity().add(normalVector.multiply(impulse * b1.getMass()))
                        .multiply(restitution)
        );
    }
}