# 🎱 Billiards-2D: Ultimate Edition

**A high-fidelity billiard simulation engine built from scratch using JavaFX.**

This repository is an advanced fork of the original *Billiards-2D* academic project. It features accurate 2D physics, procedural 3D-like rendering, an intelligent AI opponent, and network multiplayer capability using Playit.gg.

---

## 🚀 Key Features

### 1. Advanced Physics Engine
* **Elastic Collisions:** Implements 2D momentum conservation (0.92 restitution).
* **High-Energy Rails:** "Live" table cushions (0.98 restitution) for realistic bank shots.
* **Sub-Stepping:** High-frequency physics updates (20 steps/frame) for precision at high speeds.
* **Pocketing System:** Accurate distance-based detection.

### 2. Intelligent Aiming & Visuals
* **Visual Guides:** 8-Ball Pool style trajectory lines (Solid White).
* **Ghost Ball:** Visual indicator of the impact position.
* **Procedural Graphics:** Asset-free rendering using `RadialGradient` and JavaFX Canvas.

---

## 🛠️ Tech Stack

| Component | Technology | Details |
| :--- | :--- | :--- |
| **Language** | **Java 17+** | Core Logic |
| **Renderer** | **JavaFX 21** | 2D Canvas |
| **Build** | **Gradle 8.x** | Dependency Management |

---

## 🌐 Multiplayer Guide (Playit.gg)

To play with a friend over the internet without configuring router ports:

### 1. Host Setup (Player 1)
1.  **Install Playit:**
    * **Arch Linux:** `yay -S playit`
    * **Others:** Download from [playit.gg](https://playit.gg)
2.  **Run Playit:** Open a terminal and run `playit`.
3.  **Claim & Configure:**
    * Click the claim link in the terminal.
    * In the browser, click **Add Tunnel**.
    * Select **Custom TCP**.
    * Set Local Port to **5000**.
    * Click Add.
4.  **Copy Address:** Copy the generated address (e.g., `cool-cat.playit.gg:12345`).
5.  **Run Game:** Open the game and click **HOST**.

### 2. Client Setup (Player 2)
1.  Run the game.
2.  Click **JOIN**.
3.  Paste the address provided by the Host.
4.  Click OK to connect.

---

## 🎮 Controls

* **Aim:** Move mouse cursor.
* **Power:** Click and drag backward.
* **Shoot:** Release mouse.
* **Ball-in-Hand:** Drag the white ball when permitted.

---

## ▶️ Execution

**Linux / macOS:**
```bash
./gradlew run
````

**Windows:**

```cmd
gradlew.bat run
```