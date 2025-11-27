package com.billiards2d.rules;

import com.billiards2d.Ball;
import com.billiards2d.CueBall;
import com.billiards2d.core.GameBus;
import java.util.ArrayList;
import java.util.List;

public class RuleEngine {
    private enum TableState { OPEN, CLOSED }

    private TableState tableState = TableState.OPEN;
    private int currentPlayer = 1;
    private boolean player1IsSolids = true;

    private final List<Ball> ballsPottedThisTurn = new ArrayList<>();
    private boolean foulCommitted = false;
    private boolean cueBallPotted = false;
    private boolean ballInHand = false;

    private int solidsRemaining = 7;
    private int stripesRemaining = 7;

    private boolean isGameEnded = false;
    private String winMessage = "";

    public RuleEngine() {
        GameBus.subscribe(GameBus.EventType.BALL_POTTED, this::onBallPotted);

        GameBus.subscribe(GameBus.EventType.SHOT_TAKEN, o -> {
            foulCommitted = false;
            cueBallPotted = false;
            ballsPottedThisTurn.clear();
            if (ballInHand) setBallInHand(false);
        });

        GameBus.subscribe(GameBus.EventType.TURN_ENDED, o -> evaluateTurn());
    }

    private void onBallPotted(Object payload) {
        Ball ball = (Ball) payload;
        if (ball instanceof CueBall) {
            cueBallPotted = true;
            foulCommitted = true;
        } else {
            ballsPottedThisTurn.add(ball);
            if (ball.getNumber() >= 1 && ball.getNumber() <= 7) solidsRemaining--;
            if (ball.getNumber() >= 9 && ball.getNumber() <= 15) stripesRemaining--;
        }
    }

    private void evaluateTurn() {
        if (isGameEnded) return;

        boolean switchTurn = true;
        String statusMsg;
        boolean eightBallPotted = ballsPottedThisTurn.stream().anyMatch(b -> b.getNumber() == 8);

        if (eightBallPotted) {
            isGameEnded = true;
            if (foulCommitted) {
                winMessage = "GAME OVER! P" + currentPlayer + " LOSE (Scratch on 8-Ball)";
            } else if (tableState == TableState.OPEN) {
                winMessage = "GAME OVER! P" + currentPlayer + " LOSE (Early 8-Ball)";
            } else {
                boolean isSolid = (currentPlayer == 1) ? player1IsSolids : !player1IsSolids;
                int remaining = isSolid ? solidsRemaining : stripesRemaining;

                if (remaining > 0) winMessage = "GAME OVER! P" + currentPlayer + " LOSE (Early 8-Ball)";
                else winMessage = "GAME OVER! P" + currentPlayer + " WINS!";
            }
            GameBus.publish(GameBus.EventType.GAME_STATE_CHANGE, winMessage);
            return;
        }

        if (foulCommitted) {
            switchTurn = true;
            ballInHand = true;
            statusMsg = "FOUL! P" + currentPlayer + " Scratch. Ball-in-Hand.";
        } else if (ballsPottedThisTurn.isEmpty()) {
            switchTurn = true;
            statusMsg = "MISS. Turn Ended.";
        } else {
            Ball firstBall = ballsPottedThisTurn.get(0);
            if (tableState == TableState.OPEN) {
                player1IsSolids = (currentPlayer == 1) == (firstBall.getNumber() <= 7);
                tableState = TableState.CLOSED;
                switchTurn = false;
                statusMsg = "Table Closed! P1 is " + (player1IsSolids ? "SOLIDS" : "STRIPES");
            } else {
                boolean isCurPlayerSolid = (currentPlayer == 1) ? player1IsSolids : !player1IsSolids;
                boolean pottedOwn = false;

                for (Ball b : ballsPottedThisTurn) {
                    if ((b.getNumber() <= 7) == isCurPlayerSolid) pottedOwn = true;
                }

                switchTurn = !pottedOwn;
                statusMsg = pottedOwn ? "Nice Shot! Continue." : "Opponent Ball. Turn Lost.";
            }
        }

        if (switchTurn) {
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
        }

        GameBus.publish(GameBus.EventType.GAME_STATE_CHANGE, statusMsg);
        ballsPottedThisTurn.clear();
        foulCommitted = false;
        cueBallPotted = false;
    }

    public int getCurrentPlayer() { return currentPlayer; }
    public boolean isGameOver() { return isGameEnded; }
    public String getWinMessage() { return winMessage; }
    public boolean isBallInHand() { return ballInHand; }
    public void setBallInHand(boolean active) { this.ballInHand = active; }
    public boolean isCurrentPlayerSolids() { return tableState == TableState.OPEN || (currentPlayer == 1 ? player1IsSolids : !player1IsSolids); }
    public boolean isTableOpen() { return tableState == TableState.OPEN; }
    public boolean isPlayer1Solids() { return player1IsSolids; }
}