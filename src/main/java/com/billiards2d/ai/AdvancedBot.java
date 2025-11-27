package com.billiards2d.ai;

import com.billiards2d.*;
import com.billiards2d.core.GameBus;
import javafx.application.Platform;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdvancedBot {
    private final Table table;
    private final List<GameObject> objects;

    private static class BotShot {
        Vector2D force;
        Vector2D placement;
        BotShot(Vector2D f, Vector2D p) { force = f; placement = p; }
    }

    public AdvancedBot(Table table, List<GameObject> objects) {
        this.table = table;
        this.objects = objects;
    }

    public void playTurn(CueBall cueBall, boolean targetSolids, boolean ballInHand) {
        CompletableFuture.runAsync(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            BotShot result = calculateBestShot(cueBall, targetSolids, ballInHand);

            Platform.runLater(() -> {
                if (!cueBall.isActive()) cueBall.setActive(true);

                if (ballInHand && result.placement != null) {
                    cueBall.setPosition(result.placement);
                    cueBall.setVelocity(new Vector2D(0, 0));
                    CompletableFuture.runAsync(() -> {
                        try { Thread.sleep(700); } catch (InterruptedException e) {}
                        Platform.runLater(() -> executeShot(cueBall, result.force));
                    });
                } else {
                    executeShot(cueBall, result.force);
                }
            });
        });
    }

    private void executeShot(CueBall cueBall, Vector2D force) {
        GameBus.publish(GameBus.EventType.SHOT_TAKEN, force);
        cueBall.hit(force);
    }

    private BotShot calculateBestShot(CueBall cue, boolean targetSolids, boolean ballInHand) {
        double bestScore = Double.NEGATIVE_INFINITY;
        Vector2D bestForce = new Vector2D(Math.random() - 0.5, Math.random() - 0.5).normalize().multiply(500);
        Vector2D bestPlacement = null;

        for (GameObject obj : objects) {
            if (!(obj instanceof ObjectBall)) continue;
            ObjectBall target = (ObjectBall) obj;
            if (!target.isActive()) continue;

            boolean isSolid = target.getNumber() <= 7;
            boolean isEight = target.getNumber() == 8;
            if (isSolid != targetSolids && !isEight) continue;

            for (Vector2D pocket : table.getPockets()) {
                Vector2D toPocket = pocket.subtract(target.getPosition()).normalize();
                Vector2D ghost = target.getPosition().subtract(toPocket.multiply(target.getRadius() * 2));
                Vector2D shootPos = cue.getPosition();
                Vector2D candidatePlace = null;

                if (ballInHand) {
                    candidatePlace = ghost.subtract(toPocket.multiply(target.getRadius() * 6));
                    if (isValidPosition(candidatePlace)) {
                        shootPos = candidatePlace;
                    }
                }

                Vector2D shotDir = ghost.subtract(shootPos);
                if (shotDir.normalize().dot(toPocket) < 0.3) continue;

                double score = 1000 / (shotDir.length() + 10);
                if (isEight) score -= 500;
                if (ballInHand && candidatePlace != null) score += 2000;

                if (score > bestScore) {
                    bestScore = score;
                    bestPlacement = candidatePlace;
                    double power = Math.min(3500, shotDir.length() * 20 + 800);
                    bestForce = shotDir.normalize().multiply(power);
                }
            }
        }
        return new BotShot(bestForce, bestPlacement);
    }

    private boolean isValidPosition(Vector2D pos) {
        double r = 10, rail = 30;
        if (pos.getX() < rail + r || pos.getX() > 800 - rail - r) return false;
        if (pos.getY() < rail + r || pos.getY() > 400 - rail - r) return false;
        for (GameObject obj : objects) {
            if (obj instanceof Ball && ((Ball) obj).isActive()) {
                if (pos.subtract(((Ball) obj).getPosition()).length() < r * 2.1) return false;
            }
        }
        return true;
    }
}