![Repository Header](./public/banner.png)

# Flaggi 🚩

**A Multiplayer Strategy Game**

Flaggi is a multiplayer strategy game developed by [Samuel](https://github.com/Snapshot20) and [Matěj](https://github.com/kireiiiiiiii) as a submission for the **RHS Videogame Development Club Tournament**. It is built with **Java 8** and uses **Gradle 8.10** for dependency management and builds. This project uses Gradle featured planned to be removed in Gradle 9, and can't be used with Gradle 9. The project can be used with higher versions of Java 8 or higher.

## 📖 Game Overview

Flaggi is a turn-based multiplayer game where two players take turns placing flags on a grid. The goal is to be the **first player to collect 3 flags** and win the match!

## 🚀 Installation

Download the latest release of the **server** (`Server.jar`) and **client app** from the [releases page](https://github.com/kireiiiiiiii/Flaggi/releases/latest).

You can choose from the following options for the client:

-   `.dmg` for macOS
-   `.exe` for Windows
-   `.jar` (universal) for any platform with Java installed

> [!NOTE]
> This is a multiplayer game that requires a server to run, which is then accesible from all devices in the same network.

### 🎮 Running the Client

You can run the universal `.jar` client on any platform with Java 8 or higher installed using:

```bash
java -jar Flaggi.jar
```

Alternatively, use the platform-specific executable (`.exe` or `.dmg`) for Windows or macOS.

### 🖥️ Running the Server

You can run the server using one of the following methods:

#### **1. Using Java**

```bash
java -jar Server.jar
```

Once the server starts, it logs the **IP address** it's running on. Use this IP to connect clients to the server.

#### **2. Using [Docker](docker.com)**

Clone this repository and execute the following command in the project root directory:

```bash
./scripts/run.sh docker
```

## 🛠️ Scripts and Packaging

### **[run.sh](./scripts/run.sh) script**

The `run.sh` script provides multiple ways to execute the project.

-   Run the following command for available options:

```bash
./run.sh --help
```

### **[package.sh](./scripts/package.sh) script**

To package the project for specific operating systems, use the `package.sh` script:

```bash
./scripts/package.sh
```

> [!NOTE]
>
> -   The script builds platform-specific executables (.exe for Windows, .dmg for macOS).
> -   To generate an universal `.jar`, run `gradle shadowjar`.
>
> -   Build files will be available in:
>     -   **Client:** `client/app/build/libs|win|mac`
>     -   **Server:** `server/app/build/libs`

---

## 📚 Resources

-   **Font used in banners:** [Ultra Google font](https://fonts.google.com/specimen/Ultra)
-   **Prompt and specifications:** [Game Rules (PDF)](./public/TTT-game-rules.pdf)

## 💬 Contact

Have questions or suggestions? Feel free to:

-   **Create issues** or **submit pull requests** on the [GitHub repository](https://github.com/kireiiiiiiii/Flaggi).
-   Reach out to [@\_kireiiiiiiii](https://www.instagram.com/_kireiiiiiiii) on Instagram.
