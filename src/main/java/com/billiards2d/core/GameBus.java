package com.billiards2d.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GameBus {
    public enum EventType {
        BALL_POTTED,
        TURN_ENDED,
        SHOT_TAKEN,
        GAME_STATE_CHANGE,
        REMOTE_SHOT
    }

    private static final Map<EventType, List<Consumer<Object>>> listeners = new HashMap<>();

    public static void subscribe(EventType type, Consumer<Object> listener) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
    }

    public static void publish(EventType type, Object payload) {
        if (listeners.containsKey(type)) {
            List<Consumer<Object>> currentListeners = new ArrayList<>(listeners.get(type));
            for (Consumer<Object> consumer : currentListeners) {
                consumer.accept(payload);
            }
        }
    }
}