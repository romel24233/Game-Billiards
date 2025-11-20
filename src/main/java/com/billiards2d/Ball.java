package com.billiards2d;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Ball implements GameObject {

    protected Vector2D position;
    protected Vector2D velocity;
    protected double radius;
    protected double mass;
    protected Color color;

    public Ball(Vector2D position, Color color, double radius) {
        this.position = position;
        this.velocity = new Vector2D(0, 0);
        this.color = color;
        this.radius = radius;
        this.mass = 1.0;
    }

    @Override
    public void update(double deltaTime) {
        // TODO: Add physics in Week 4
        // Rumus untuk velocity : posisi baru = posisi lama + velocity * dt(waktu per frame cmiiw)
        position = position.add(velocity.multiply(deltaTime));

        // value untuk gesekan bola biar stop, atur aja king
        double friction = 0.992;
        velocity = velocity.multiply(friction);
        if (velocity.length() < 15)
            velocity = new Vector2D(0, 0); // biar ga gerak ketika sudah tidak ada gaya
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(this.color);
        gc.fillOval(
                this.position.getX() - this.radius,
                this.position.getY() - this.radius,
                this.radius * 2,
                this.radius * 2
        );
    }

    public Vector2D getPosition() { return position; }
    public void setPosition(Vector2D position) { this.position = position; }
    public Vector2D getVelocity() { return velocity; }
    public void setVelocity(Vector2D velocity) { this.velocity = velocity; }
    public double getRadius() { return radius; }
    public double getMass() { return mass; }
}