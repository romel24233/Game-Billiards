package com.billiards2d;

import com.billiards2d.ai.AdvancedBot;
import com.billiards2d.core.GameBus;
import com.billiards2d.net.NetworkManager;
import com.billiards2d.rules.RuleEngine;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BilliardApp extends Application {

    public static final double TOP_HEADER_HEIGHT = 140;
    public static final double TABLE_WIDTH = 800;
    public static final double TABLE_HEIGHT = 400;
    public static final double SIDEBAR_WIDTH = 60;
    public static final double BOTTOM_HEIGHT = 40;

    private static final double APP_WIDTH = TABLE_WIDTH + SIDEBAR_WIDTH;
    private static final double APP_HEIGHT = TOP_HEADER_HEIGHT + TABLE_HEIGHT + BOTTOM_HEIGHT;
    private final double MENU_X_CENTER = APP_WIDTH / 2;

    private GraphicsContext gc;
    private final List<GameObject> gameObjects = new ArrayList<>();
    private CueStick cueStick;
    private CueBall cueBall;

    private RuleEngine ruleEngine;
    private AdvancedBot bot;
    private NetworkManager networkManager;

    private boolean ballsMoving = false;
    private boolean aiThinking = false;
    private boolean isDraggingCueBall = false;
    private String lastLogMessage = "Welcome to 8-Ball Pro";

    private static String modeArgs = "ai";
    private static String clientHost = "localhost";
    private static int clientPort = 5000;
    private boolean isMultiplayer = false;
    private boolean isVsAI = true;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(APP_WIDTH, APP_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        StackPane root = new StackPane(canvas);
        root.setStyle("-fx-background-color: #1a1a1a;");
        Scene scene = new Scene(root, APP_WIDTH, APP_HEIGHT);

        initializeGameObjects();
        initializeSystems();
        setupInputHandlers(scene);

        primaryStage.setTitle("Billiards-2D: Ultimate Edition");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();

        startGameLoop();
    }

    private void setupInputHandlers(Scene scene) {
        scene.setOnMouseMoved(e -> {
            MouseEvent shifted = shiftEvent(e);
            if (canPlayerMoveBall() && isHoveringCueBall(shifted)) {
                scene.setCursor(Cursor.HAND);
            } else {
                scene.setCursor(Cursor.DEFAULT);
            }
        });

        scene.setOnMousePressed(e -> {
            if (e.getY() < TOP_HEADER_HEIGHT) {
                handleMenuClick(e.getX(), e.getY());
                return;
            }
            if (canPlayerShoot()) {
                MouseEvent shifted = shiftEvent(e);
                if (canPlayerMoveBall() && isHoveringCueBall(shifted)) {
                    isDraggingCueBall = true;
                    cueBall.setVelocity(new Vector2D(0, 0));
                } else if (!isDraggingCueBall) {
                    cueStick.handleMousePressed(shifted);
                }
            }
        });

        scene.setOnMouseDragged(e -> {
            if (e.getY() >= TOP_HEADER_HEIGHT && canPlayerShoot()) {
                MouseEvent shifted = shiftEvent(e);
                if (isDraggingCueBall) {
                    moveCueBallTo(shifted.getX(), shifted.getY());
                } else {
                    cueStick.handleMouseDragged(shifted);
                }
            }
        });

        scene.setOnMouseReleased(e -> {
            MouseEvent shifted = shiftEvent(e);
            if (isDraggingCueBall) {
                isDraggingCueBall = false;
            } else if (e.getY() >= TOP_HEADER_HEIGHT && canPlayerShoot()) {
                cueStick.handleMouseReleased(shifted);
            }
        });
    }

    private MouseEvent shiftEvent(MouseEvent e) {
        return new MouseEvent(
                e.getSource(), e.getTarget(), e.getEventType(),
                e.getX(), e.getY() - TOP_HEADER_HEIGHT,
                e.getScreenX(), e.getScreenY(),
                e.getButton(), e.getClickCount(), e.isShiftDown(), e.isControlDown(),
                e.isAltDown(), e.isMetaDown(), e.isPrimaryButtonDown(), e.isMiddleButtonDown(),
                e.isSecondaryButtonDown(), e.isSynthesized(), e.isPopupTrigger(), e.isStillSincePress(), null
        );
    }

    private void initializeGameObjects() {
        gameObjects.clear();
        Table table = new Table(TABLE_WIDTH, TABLE_HEIGHT);
        gameObjects.add(table);

        double startX = TABLE_WIDTH * 0.75;
        double startY = TABLE_HEIGHT / 2;

        // Rack setup
        gameObjects.add(new ObjectBall(new Vector2D(startX, startY), 1));
        gameObjects.add(new ObjectBall(new Vector2D(startX + 18, startY - 10), 9));
        gameObjects.add(new ObjectBall(new Vector2D(startX + 18, startY + 10), 2));
        gameObjects.add(new ObjectBall(new Vector2D(startX + 36, startY - 20), 10));
        gameObjects.add(new ObjectBall(new Vector2D(startX + 36, startY), 8));
        gameObjects.add(new ObjectBall(new Vector2D(startX + 36, startY + 20), 3));
        gameObjects.add(new ObjectBall(new Vector2D(startX + 54, startY - 30), 11));
        gameObjects.add(new ObjectBall(new Vector2D(startX + 54, startY - 10), 7));
        gameObjects.add(new ObjectBall(new Vector2D(startX + 54, startY + 10), 14));
        gameObjects.add(new ObjectBall(new Vector2D(startX + 54, startY + 30), 4));
        gameObjects.add(new ObjectBall(new Vector2D(startX + 72, startY - 40), 5));
        gameObjects.add(new ObjectBall(new Vector2D(startX + 72, startY - 20), 13));
        gameObjects.add(new ObjectBall(new Vector2D(startX + 72, startY), 15));
        gameObjects.add(new ObjectBall(new Vector2D(startX + 72, startY + 20), 6));
        gameObjects.add(new ObjectBall(new Vector2D(startX + 72, startY + 40), 12));

        cueBall = new CueBall(new Vector2D(TABLE_WIDTH * 0.25, startY));
        gameObjects.add(cueBall);

        cueStick = new CueStick(cueBall, gameObjects);
        gameObjects.add(cueStick);
        gameObjects.add(new PhysicsEngine(table, gameObjects));
    }

    private void initializeSystems() {
        ruleEngine = new RuleEngine();

        GameBus.subscribe(GameBus.EventType.GAME_STATE_CHANGE, msg -> {
            lastLogMessage = (String) msg;
            Platform.runLater(this::triggerAiIfNecessary);
        });

        GameBus.subscribe(GameBus.EventType.SHOT_TAKEN, p -> aiThinking = false);

        GameBus.subscribe(GameBus.EventType.REMOTE_SHOT, payload -> {
            Vector2D force = (Vector2D) payload;
            cueBall.hit(force);
        });

        if (modeArgs.equalsIgnoreCase("server")) {
            isMultiplayer = true;
            isVsAI = false;
            lastLogMessage = "Server Mode: Waiting...";
            networkManager = new NetworkManager();
            networkManager.startServer(5000);
        } else if (modeArgs.equalsIgnoreCase("client")) {
            isMultiplayer = true;
            isVsAI = false;
            lastLogMessage = "Connecting to " + clientHost + ":" + clientPort + "...";
            networkManager = new NetworkManager();
            networkManager.connectClient(clientHost, clientPort);
        } else {
            isMultiplayer = false;
            isVsAI = true;
            bot = new AdvancedBot((Table) gameObjects.get(0), gameObjects);
        }
    }

    private void startGameLoop() {
        new AnimationTimer() {
            private long lastNanoTime = System.nanoTime();

            @Override
            public void handle(long currentNanoTime) {
                double deltaTime = (currentNanoTime - lastNanoTime) / 1_000_000_000.0;
                lastNanoTime = currentNanoTime;
                if (deltaTime > 0.05) deltaTime = 0.05;

                updateAndDraw(deltaTime);
                checkTurnEnd();
            }
        }.start();
    }

    private void updateAndDraw(double deltaTime) {
        if (!cueBall.isActive() && ruleEngine.isBallInHand()) {
            cueBall.setPosition(new Vector2D(TABLE_WIDTH * 0.25, TABLE_HEIGHT / 2));
            cueBall.setVelocity(new Vector2D(0, 0));
            cueBall.setActive(true);
        }

        for (GameObject obj : gameObjects) obj.update(deltaTime);

        gc.setFill(Color.rgb(30, 30, 35));
        gc.fillRect(0, 0, APP_WIDTH, APP_HEIGHT);

        gc.save();
        gc.translate(0, TOP_HEADER_HEIGHT);
        gc.beginPath();
        gc.rect(0, 0, TABLE_WIDTH, TABLE_HEIGHT);
        gc.clip();

        for (GameObject obj : gameObjects) {
            if (obj instanceof CueStick) {
                if (!isDraggingCueBall && !ballsMoving && canPlayerShoot()) {
                    obj.draw(gc);
                }
            } else {
                obj.draw(gc);
            }
        }
        gc.restore();

        drawTopHeader();
        drawSidebar();
        drawFooter();
    }

    private void checkTurnEnd() {
        boolean anyMoving = gameObjects.stream()
                .filter(o -> o instanceof Ball)
                .anyMatch(b -> ((Ball) b).getVelocity().length() > 0.1);

        if (ballsMoving && !anyMoving) {
            ballsMoving = false;
            GameBus.publish(GameBus.EventType.TURN_ENDED, null);
        } else {
            ballsMoving = anyMoving;
        }
    }

    private boolean canPlayerShoot() {
        if (ballsMoving || aiThinking) return false;
        if (ruleEngine.isGameOver()) return false;
        if (isMultiplayer) {
            boolean isServer = modeArgs.equalsIgnoreCase("server");
            int myPlayerId = isServer ? 1 : 2;
            if (ruleEngine.getCurrentPlayer() != myPlayerId) return false;
        }
        if (isVsAI && ruleEngine.getCurrentPlayer() != 1) return false;
        return true;
    }

    private boolean canPlayerMoveBall() {
        return canPlayerShoot() && ruleEngine.isBallInHand() && ruleEngine.getCurrentPlayer() == 1;
    }

    private void triggerAiIfNecessary() {
        if (ruleEngine.getCurrentPlayer() != 2) return;
        if (ruleEngine.isGameOver() || aiThinking || ballsMoving) return;

        if (isVsAI) {
            aiThinking = true;
            boolean ballInHand = ruleEngine.isBallInHand();
            if (ballInHand && !cueBall.isActive()) cueBall.setActive(true);
            bot.playTurn(cueBall, !ruleEngine.isPlayer1Solids(), ballInHand);
        }
    }

    private void drawTopHeader() {
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(50, 50, 60)), new Stop(1, Color.rgb(30, 30, 35)));
        gc.setFill(bg);
        gc.fillRect(0, 0, APP_WIDTH, TOP_HEADER_HEIGHT);

        gc.setStroke(Color.GOLD);
        gc.setLineWidth(2);
        gc.strokeLine(0, TOP_HEADER_HEIGHT, APP_WIDTH, TOP_HEADER_HEIGHT);

        drawButton(MENU_X_CENTER - 130, 15, 80, 25, "RESET AI", isVsAI);
        boolean isServer = modeArgs.equalsIgnoreCase("server");
        drawButton(MENU_X_CENTER - 40, 15, 80, 25, "HOST", isServer);
        boolean isClient = modeArgs.equalsIgnoreCase("client");
        drawButton(MENU_X_CENTER + 50, 15, 80, 25, "JOIN", isClient);

        boolean p1Solids = true;
        String p1Type = "OPEN";
        String p2Type = "OPEN";

        if (!ruleEngine.isTableOpen()) {
            p1Solids = ruleEngine.isPlayer1Solids();
            p1Type = p1Solids ? "SOLIDS" : "STRIPES";
            p2Type = !p1Solids ? "SOLIDS" : "STRIPES";
        }

        String p1Name = "PLAYER 1";
        String p2Name = isVsAI ? "BOT" : (isServer ? "OPPONENT" : (isClient ? "YOU (CLIENT)" : "PLAYER 2"));
        if (isServer) p1Name = "YOU (HOST)";
        else if (isClient) p1Name = "OPPONENT";

        drawPlayerBar(20, 55, p1Name, p1Type, p1Solids, ruleEngine.getCurrentPlayer() == 1);
        drawPlayerBar(APP_WIDTH - 420, 55, p2Name, p2Type, !p1Solids, ruleEngine.getCurrentPlayer() == 2);
    }

    private void drawPlayerBar(double x, double y, String name, String typeStr, boolean isSolid, boolean isTurn) {
        double w = 400;
        double h = 70;

        if (isTurn) {
            gc.setEffect(new javafx.scene.effect.DropShadow(15, Color.rgb(0, 255, 0, 0.6)));
            gc.setStroke(Color.LIME);
            gc.setLineWidth(2);
        } else {
            gc.setEffect(null);
            gc.setStroke(Color.rgb(80, 80, 80));
            gc.setLineWidth(1);
        }

        LinearGradient barGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(40, 40, 45)), new Stop(1, Color.rgb(25, 25, 30)));
        gc.setFill(barGrad);
        gc.fillRoundRect(x, y, w, h, 15, 15);
        gc.strokeRoundRect(x, y, w, h, 15, 15);
        gc.setEffect(null);

        gc.setFill(isTurn ? Color.LIME : Color.GRAY);
        gc.fillOval(x + 10, y + 10, 50, 50);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(name, x + 70, y + 10);
        gc.setFont(Font.font("Arial", 12));
        gc.setFill(Color.rgb(180, 180, 180));
        gc.fillText(typeStr, x + 70, y + 32);

        double ballStartY = y + 35;
        double ballStartX = x + 140;
        double miniRadius = 9;
        double spacing = 22;

        if (ruleEngine.isTableOpen()) {
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Arial", 12));
            gc.fillText("POCKET ANY BALL", ballStartX, ballStartY + 5);
        } else {
            int startNum = isSolid ? 1 : 9;
            int endNum = isSolid ? 7 : 15;
            for (int i = startNum; i <= endNum; i++) {
                boolean onTable = isBallOnTable(i);
                Color c = ObjectBall.getColorForNumber(i);
                Ball.renderVisual(gc, ballStartX + ((i - startNum) * spacing), ballStartY + 10, miniRadius, i, c, onTable);
            }
            boolean groupCleared = areAllTargetBallsPocketed(startNum, endNum);
            double eightX = ballStartX + (7 * spacing) + 10;
            if (groupCleared) gc.setEffect(new javafx.scene.effect.DropShadow(10, Color.GOLD));
            Ball.renderVisual(gc, eightX, ballStartY + 10, miniRadius, 8, Color.BLACK, isBallOnTable(8));
            gc.setEffect(null);
        }
    }

    private void drawSidebar() {
        double startX = TABLE_WIDTH;
        gc.setFill(Color.rgb(25, 25, 25));
        gc.fillRect(startX, TOP_HEADER_HEIGHT, SIDEBAR_WIDTH, TABLE_HEIGHT);

        double barW = 15;
        double barH = TABLE_HEIGHT - 60;
        double barX = startX + (SIDEBAR_WIDTH - barW) / 2;
        double barY = TOP_HEADER_HEIGHT + 30;

        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barW, barH);

        double powerRatio = cueStick.getCurrentPower() / cueStick.getMaxPower();
        if (powerRatio > 0) {
            double fillH = powerRatio * barH;
            LinearGradient powerGrad = new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.LIME), new Stop(0.5, Color.YELLOW), new Stop(1, Color.RED));
            gc.setFill(powerGrad);
            gc.fillRect(barX + 1, barY + barH - fillH, barW - 2, fillH);
        }

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("PWR", startX + SIDEBAR_WIDTH / 2, barY + barH + 15);
    }

    private void drawFooter() {
        double y = TOP_HEADER_HEIGHT + TABLE_HEIGHT;
        gc.setFill(Color.rgb(15, 15, 15));
        gc.fillRect(0, y, APP_WIDTH, BOTTOM_HEIGHT);
        gc.setStroke(Color.rgb(50, 50, 50));
        gc.strokeLine(0, y, APP_WIDTH, y);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("Consolas", 14));

        if (ruleEngine.isGameOver()) {
            gc.setFill(Color.RED);
            gc.fillText(ruleEngine.getWinMessage(), 20, y + BOTTOM_HEIGHT / 2);
        } else {
            gc.setFill(Color.CYAN);
            gc.fillText("> " + lastLogMessage, 20, y + BOTTOM_HEIGHT / 2);

            if (ruleEngine.isBallInHand()) {
                if (canPlayerMoveBall()) {
                    gc.setFill(Color.YELLOW);
                    gc.fillText("[BALL IN HAND] Drag Cue Ball", APP_WIDTH - 250, y + BOTTOM_HEIGHT / 2);
                } else if (aiThinking) {
                    gc.setFill(Color.LIGHTGREEN);
                    gc.fillText("AI Thinking...", APP_WIDTH - 200, y + BOTTOM_HEIGHT / 2);
                }
            }
        }
    }

    private void drawButton(double x, double y, double w, double h, String text, boolean active) {
        gc.setFill(active ? Color.rgb(0, 120, 215) : Color.rgb(60, 60, 60));
        gc.fillRoundRect(x, y, w, h, 5, 5);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, w, h, 5, 5);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, x + w / 2, y + h / 2);
    }

    private boolean isBallOnTable(int number) {
        for (GameObject obj : gameObjects) {
            if (obj instanceof ObjectBall) {
                if (((ObjectBall) obj).getNumber() == number) return ((ObjectBall) obj).isActive();
            }
        }
        return false;
    }

    private boolean areAllTargetBallsPocketed(int start, int end) {
        for (int i = start; i <= end; i++) {
            if (isBallOnTable(i)) return false;
        }
        return true;
    }

    private boolean isHoveringCueBall(MouseEvent e) {
        if (!cueBall.isActive()) return false;
        double dist = cueBall.getPosition().subtract(new Vector2D(e.getX(), e.getY())).length();
        return dist < cueBall.getRadius() * 2.5;
    }

    private void moveCueBallTo(double x, double y) {
        double r = cueBall.getRadius();
        double rail = 35;
        x = Math.max(rail + r, Math.min(TABLE_WIDTH - rail - r, x));
        y = Math.max(rail + r, Math.min(TABLE_HEIGHT - rail - r, y));

        boolean collision = false;
        Vector2D newPos = new Vector2D(x, y);

        for (GameObject obj : gameObjects) {
            if (obj instanceof ObjectBall && ((ObjectBall) obj).isActive()) {
                if (newPos.subtract(((ObjectBall) obj).getPosition()).length() < r * 2) {
                    collision = true;
                    break;
                }
            }
        }

        if (!collision) {
            cueBall.setPosition(newPos);
            cueBall.setVelocity(new Vector2D(0, 0));
        }
    }

    private void handleMenuClick(double x, double y) {
        if (y >= 15 && y <= 40) {
            if (x >= MENU_X_CENTER - 130 && x <= MENU_X_CENTER - 50) {
                modeArgs = "ai";
                resetGame();
            } else if (x >= MENU_X_CENTER - 40 && x <= MENU_X_CENTER + 40) {
                modeArgs = "server";
                resetGame();
            } else if (x >= MENU_X_CENTER + 50 && x <= MENU_X_CENTER + 130) {
                showJoinDialog();
            }
        }
    }

    private void showJoinDialog() {
        TextInputDialog dialog = new TextInputDialog("0.tcp.ap.ngrok.io:12345");
        dialog.setTitle("Join Multiplayer");
        dialog.setHeaderText("Enter Host Address");
        dialog.setContentText("Address (e.g., traffic.playit.gg:12345):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(address -> {
            try {
                String[] parts = address.split(":");
                if (parts.length == 2) {
                    clientHost = parts[0];
                    clientPort = Integer.parseInt(parts[1]);
                    modeArgs = "client";
                    resetGame();
                } else {
                    System.out.println("Invalid format! Use host:port");
                }
            } catch (Exception e) {
                System.out.println("Error parsing: " + e.getMessage());
            }
        });
    }

    private void resetGame() {
        initializeGameObjects();
        initializeSystems();
        ballsMoving = false;
        aiThinking = false;
        isDraggingCueBall = false;
        lastLogMessage = "Game Reset: " + modeArgs.toUpperCase();
    }

    public static void main(String[] args) {
        if (args.length > 0) modeArgs = args[0];
        launch(args);
    }
}