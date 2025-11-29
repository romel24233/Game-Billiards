package com.billiards2d;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
        // Managed by PhysicsEngine to support sub-stepping
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (!active) return;
        renderVisual(gc, position.getX(), position.getY(), radius, number, color, true);
    }

    public static void renderVisual(GraphicsContext gc, double x, double y, double r, int num, Color c, boolean isActive) {
        if (!isActive) {
            gc.setGlobalAlpha(0.3);
            gc.setFill(Color.BLACK);
            gc.fillOval(x - r, y - r, r * 2, r * 2);
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(1);
            gc.strokeOval(x - r, y - r, r * 2, r * 2);
            gc.setGlobalAlpha(1.0);
            return;
        }

        // Drop Shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillOval(x - r + (r * 0.15), y - r + (r * 0.15), r * 2, r * 2);

        // Base Sphere
        boolean isStripe = num >= 9 && num <= 15;
        Color baseColor = (num == 0 || isStripe) ? Color.WHITE : c;

        RadialGradient baseGrad = new RadialGradient(
                0, 0, x - (r * 0.2), y - (r * 0.2), r * 1.4,
                false, CycleMethod.NO_CYCLE,
                new Stop(0.0, baseColor.brighter()),
                new Stop(1.0, baseColor.darker().darker())
        );
        gc.setFill(baseGrad);
        gc.fillOval(x - r, y - r, r * 2, r * 2);

        // Stripe Pattern
        if (isStripe) {
            gc.save();
            gc.beginPath();
            gc.arc(x, y, r, r, 0, 360);
            gc.clip();
            gc.setFill(c);
            gc.fillRect(x - r, y - (r * 0.65), r * 2, r * 1.3);
            gc.restore();
        }

        // Specular Highlight
        RadialGradient glare = new RadialGradient(
                0, 0, x - (r * 0.35), y - (r * 0.35), r * 0.7,
                false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(255, 255, 255, 0.9)),
                new Stop(1.0, Color.TRANSPARENT)
        );
        gc.setFill(glare);
        gc.fillOval(x - r * 0.8, y - r * 0.8, r * 1.2, r * 1.2);

        // Number Badge
        if (num > 0) {
            double badgeSize = r * 1.1;
            gc.setFill(Color.WHITE);
            gc.fillOval(x - badgeSize / 2, y - badgeSize / 2, badgeSize, badgeSize);

            gc.setFill(Color.BLACK);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, r * 0.7));
            gc.fillText(String.valueOf(num), x, y);
        } else if (num == 0) {
            // Cue Ball Dot
            gc.setFill(Color.RED);
            double dotSize = r * 0.2;
            gc.fillOval(x - dotSize / 2, y - dotSize / 2, dotSize, dotSize);
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