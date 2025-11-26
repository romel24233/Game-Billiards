package com.billiards2d;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public abstract class Ball implements GameObject {

    protected Vector2D position;
    protected Vector2D velocity;
    protected double radius;
    protected double mass;
    protected Color color;
    protected int number;
    protected boolean active = true;

    public Ball(Vector2D position, Color color, double radius, int number) {
        this.position = position;
        this.velocity = new Vector2D(0, 0);
        this.color = color;
        this.radius = radius;
        this.mass = 1.0;
        this.number = number;
    }

    @Override
    public void update(double deltaTime) {
        if (!active) return;

        position = position.add(velocity.multiply(deltaTime));

        // Apply cloth friction
        double friction = 0.992;
        velocity = velocity.multiply(friction);

        // Stop threshold to prevent infinite micro-movement
        if (velocity.length() < 1.5) {
            velocity = new Vector2D(0, 0);
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (!active) return;
        drawBallAt(gc, position.getX(), position.getY());
    }

    public void drawBallAt(GraphicsContext gc, double x, double y) {
        // Drop shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillOval(x - radius + 2, y - radius + 2, radius * 2, radius * 2);

        // 3D Sphere effect
        RadialGradient gradient = new RadialGradient(
                0, 0, x - (radius * 0.3), y - (radius * 0.3), radius,
                false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.WHITE),
                new Stop(0.4, color),
                new Stop(1.0, color.darker())
        );
        gc.setFill(gradient);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // Render number
        if (number > 0) {
            gc.setFill(Color.WHITE);
            gc.fillOval(x - radius/2, y - radius/2, radius, radius);

            gc.setFill(Color.BLACK);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(new Font("Arial", 10));
            gc.fillText(String.valueOf(number), x, y + 4);
        }
    }

    public Vector2D getPosition() { return position; }
    public void setPosition(Vector2D position) { this.position = position; }
    public Vector2D getVelocity() { return velocity; }
    public void setVelocity(Vector2D velocity) { this.velocity = velocity; }
    public double getRadius() { return radius; }
    public double getMass() { return mass; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getNumber() { return number; }
}