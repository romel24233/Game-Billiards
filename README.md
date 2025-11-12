# 🎱 Billiards-2D

Repository ini adalah implementasi tugas kelompok untuk mata kuliah **Desain dan Pemrograman Berbasis Objek (OOP)**. Tujuan proyek ini adalah membuat simulasi permainan billiard 2D dari awal.

> **Status:** Tahap Awal - Implementasi kerangka dasar dan GUI

---

## 🛠️ Teknologi

| Komponen | Teknologi | Versi |
|----------|-----------|-------|
| **Bahasa** | Java | 17+ |
| **GUI Framework** | JavaFX | 21 |
| **Build Tool** | Gradle (Kotlin DSL) | 8.14 |
| **IDE** | IntelliJ IDEA | Community Edition |

---

## 📁 Struktur Proyek

```
Billiards-2D/
├── src/
│   └── main/
│       └── java/
│           └── com/billiards2d/
│               ├── BilliardApp.java      # Main class & Game loop
│               ├── GameObject.java        # Interface polimorfisme
│               ├── PhysicsEngine.java     # Engine simulasi fisika
│               ├── Ball.java              # Abstract base class
│               ├── CueBall.java           # Bola putih
│               ├── ObjectBall.java        # Bola target
│               ├── Table.java             # Meja billiard
│               └── Vector2D.java          # Utilitas vektor
├── build.gradle.kts            # Konfigurasi build & dependensi
├── gradlew                     # Gradle wrapper (Linux/Mac)
└── gradlew.bat                 # Gradle wrapper (Windows)
```

---

## ☕ Yang Perlu Disiapkan

Sebelum menjalankan proyek, install dulu:

1. **Git** - Untuk clone repository
2. **Java JDK 17 atau lebih baru**
    - Download dari [Eclipse Temurin](https://adoptium.net/)
    - Windows: Download installer `.msi`
    - Linux: `sudo apt install openjdk-17-jdk` atau `sudo pacman -S jdk17-openjdk`

Cek apakah Java sudah terinstall:
```bash
java -version
```

> **Catatan:** Gradle dan JavaFX akan didownload otomatis, tidak perlu install manual.

---

## ▶️ Cara Menjalankan Program

### Pakai IntelliJ IDEA (Paling Mudah)

1. **Clone repository**
   ```bash
   git clone https://github.com/Billiards-2D/Billiards-2D.git
   ```

2. **Buka IntelliJ IDEA**
    - Pilih **Open** (jangan "New Project")
    - Pilih folder `Billiards-2D`
    - Klik **Trust Project**

3. **Tunggu proses download selesai**
    - Lihat progress bar di pojok kanan bawah
    - Gradle akan download JavaFX dan dependencies lainnya
    - Biasanya 1-5 menit (tergantung internet)

4. **Jalankan program**
    - Klik tab **Gradle** di sisi kanan
    - Buka: **Billiards-2D → Tasks → application → run**
    - Double-click **run**

### Pakai Terminal/Command Line

```bash
# Clone repository
git clone https://github.com/Billiards-2D/Billiards-2D.git
cd Billiards-2D

# Jalankan
./gradlew run              # Linux/Mac
gradlew.bat run            # Windows
```

---

## ⚠️ Kalau Ada Masalah

**Program tidak jalan / Error JavaFX:**
- Jangan run langsung dari `BilliardApp.java`
- Gunakan Gradle panel: **Tasks → application → run**

**Gradle sync error:**
- Pastikan internet stabil
- Coba: **File → Invalidate Caches → Invalidate and Restart**