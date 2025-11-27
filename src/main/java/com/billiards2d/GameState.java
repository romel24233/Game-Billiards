// src/main/java/com/billiards2d/GameState.java
package com.billiards2d;

public enum GameState {
    OPEN_TABLE,     // Belum ada yang memasukkan bola (bebas pilih)
    SOLIDS_TURN,    // Giliran pemain bola Solid (1-7)
    STRIPES_TURN,   // Giliran pemain bola Stripe (9-15)
    GAME_OVER       // Permainan selesai
}