# 🎱 Billiards-2D: Ultimate Edition

**A high-fidelity billiard simulation engine built from scratch using JavaFX.**

This repository is an advanced fork of the original *Billiards-2D* academic project. It serves as an R&D sandbox dedicated to exploring complex game mechanics, vector mathematics, and procedural graphics rendering without relying on external game engines.

> **Current Status:** Stable Release (Physics & UI Overhaul)

---

## 🚀 Key Features

### 1. Advanced Physics Engine (`PhysicsEngine`)
* **Elastic Collisions:** Implements accurate 2D momentum conservation for ball-to-ball interactions with a restitution coefficient of 0.92.
* **High-Energy Rails:** Features "live" table cushions (0.98 restitution) allowing for realistic bank shots and multi-rail kicks.
* **Dynamic Friction:** Simulates cloth resistance using continuous velocity damping for natural ball deceleration.
* **Pocketing System:** Precise distance-based detection for potting balls and handling scratch fouls.

### 2. Intelligent Aiming Assistant (`CueStick`)
* **Raycasting Trajectory:** Real-time prediction line that calculates the exact point of impact.
* **Branching Paths:** Visualizes post-collision paths for both the target ball (impact line) and the cue ball (tangent line).
* **Wall Clipping:** Prediction lines respect table boundaries using vector math to determine exact intersection points.
* **Angle Snapping:** Automatically snaps aiming vector to the ball's center for precise straight shots.

### 3. Procedural Rendering & Modern UI
* **Asset-Free 3D Visuals:** All graphics (balls, lighting, shadows) are generated programmatically using `RadialGradient` and JavaFX Canvas API. No external image files are used.
* **HD Table Design:** Features a detailed wood-grain bezel, bevel effects, and standard aiming diamond markers.
* **Professional HUD:** A separate dashboard layout containing a gradient Power Meter and a recessed "Ball Tray" for tracking pocketed balls.

---

## 🛠️ Tech Stack

| Component | Technology | Details |
| :--- | :--- | :--- |
| **Language** | **Java 17+** | Core logic & Object-Oriented Architecture |
| **Renderer** | **JavaFX 21** | Hardware-accelerated 2D Canvas rendering |
| **Build System** | **Gradle 8.x** | Dependency management & build automation |
| **Format** | **Kotlin DSL** | Used for `build.gradle.kts` configuration |

---

## 🎮 Controls

* **Aim:** Move mouse cursor around the cue ball.
* **Power:** Click and drag the mouse backward (away from the ball).
    * *The Power Bar on the right indicates shot strength.*
    * *Trajectory lines extend based on predicted force.*
* **Shoot:** Release the mouse button.

---

## ▶️ How to Run

### Prerequisites
Ensure you have **Java JDK 17** (or higher) installed on your machine.

### Execution
This project includes the Gradle Wrapper, so no manual Gradle installation is required.

**Linux / macOS:**
```bash
./gradlew run
````

**Windows:**

```cmd
gradlew.bat run
```

-----

## 📂 Project Structure

The codebase follows a strict **Responsibility-Driven Design (RDD)**:

```
src/main/java/com/billiards2d/
├── BilliardApp.java      # Entry point, Game Loop, and UI Rendering
├── PhysicsEngine.java    # Centralized physics logic (Collisions, Movement)
├── CueStick.java         # Input handling and Raycasting (Aim prediction)
├── Table.java            # Environment rendering (Rails, Pockets, Markers)
├── Ball.java             # Abstract base entity with Physics properties
├── CueBall.java          # Player-controlled ball entity
├── ObjectBall.java       # Target balls (Numbered & Colored)
├── GameObject.java       # Polymorphic interface for game entities
└── Vector2D.java         # Math utility for vector operations
```

-----

## 🔮 Roadmap

* [ ] **Game Rules:** Implementation of standard 8-Ball rules (Solids vs. Stripes).
* [ ] **AI Opponent:** Simple bot using trajectory evaluation to find optimal shots.
* [ ] **Network Play:** Socket-based multiplayer support.