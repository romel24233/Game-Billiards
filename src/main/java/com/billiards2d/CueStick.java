package com.billiards2d;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class CueStick implements GameObject {

    private CueBall cueBall;

    // Aiming Property
    private boolean isAiming = false;
    private Vector2D aimStart;
    private Vector2D aimCurrent;

    private static final double POWER_MULTIPLIER = 8.0; // tuning
    private static final double MAX_FORCE = 500;         // clamp limit

    public CueStick(CueBall cueBall) {
        this.cueBall = cueBall;
    }

    @Override
    public void update(double deltaTime) {
        // TODO: Implement in Week 4
    }

    @Override
    public void draw(GraphicsContext gc) {
        // TODO: Implement in Week 4
        if (!isAiming) return;

        if (cueBall.getVelocity().length() > 0) return;

        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(2);
        gc.strokeLine(
                cueBall.getPosition().getX(),
                cueBall.getPosition().getY(),
                aimCurrent.getX(),
                aimCurrent.getY()
        );

        drawPredictedPath(gc);
    }

    private void drawPredictedPath(GraphicsContext gc) {
        if (!isAiming) return;
        if (cueBall.getVelocity().length() > 0) return;

        Vector2D dragDirection = aimStart.subtract(aimCurrent).normalize();

        double lineLength = 300; // panjang garis ramalan
        double segment = 15;     // panjang titik-titik
        double gap = 8;          // jarak antar titik

        double x = cueBall.getPosition().getX();
        double y = cueBall.getPosition().getY();

        gc.setStroke(Color.rgb(255, 255, 0, 0.6)); // kuning transparan
        gc.setLineWidth(2);

        double dist = 0;
        while (dist < lineLength) {
            double x2 = x + dragDirection.getX() * segment;
            double y2 = y + dragDirection.getY() * segment;

            gc.strokeLine(x, y, x2, y2);

            // geser ke segmen berikutnya
            x += dragDirection.getX() * (segment + gap);
            y += dragDirection.getY() * (segment + gap);
            dist += segment + gap;
        }
    }

    // izin buat event handler masing masing king
    public void handleMouseInput(MouseEvent event) {
        // TODO: Implement in Week 4
    }

    // saat mouse ditekan
    public void handleMousePressed(MouseEvent e) {
        if (cueBall.getVelocity().length() > 0) {
            return; // ignore click
        }

        double dx = e.getX() - cueBall.getPosition().getX();
        double dy = e.getY() - cueBall.getPosition().getY();
        double distance = Math.sqrt(dx*dx + dy*dy);

        if (distance <= 40) {
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
        double dragLength = drag.length();

        // Scale power smooth (exponential easing)
        double scaledPower = Math.pow(dragLength, 1.25);

        // Limit agar tidak chaos
        if (scaledPower > 3000)
            scaledPower = 3000;

        Vector2D direction = drag.normalize();
        Vector2D hitForce = direction.multiply(scaledPower);

        cueBall.hit(hitForce);
        isAiming = false;
    }
}