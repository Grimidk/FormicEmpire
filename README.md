Manage a colony of ants, then a whole dynasty, and eventually the world. Unlock more upgrades by researching or defeating enemy species or other bugs. Think of this game like an unholy combination of games like Stellaris, Rimworld, and Spore with an Ant coat of paint.

A game about ants developed solely on Java, autism, energy drinks (or coffee), and cigarettes. 
I refuse to use an engine or learn proper UI/UX.
Cursor is being used to help me debug and test, all pixelart made using Aseprite with my own trackpad. 
I plan to make everything from scratch including the music, assets, fonts and more.

Please enjoy it, and send feedback to: thegrimidk@gmail.com .
Check the roadmap to see planned features and known bugs and credits for acknowledgments. 

The game will eventually be published, but I don't care about profits. Donations are always welcome.
I aim to publish the game around October 2026, maybe, until then betas are available at itch.io or github.
After that expect regular but sparse updates including a Core Engine Package.

Use 'production' branch for a stable version and 'development' branch for unstable beta features.

Requirements:
    Java 17 (for development on macOS/Linux)
    64-bit system for the Windows release bundle
    Electricity (optional)

Running the game with Java installed (bash):

    ./scripts/run.sh

Uses your system JDK and saves in `./saves/`.

Generating a release bundle (bash):

    ./scripts/package.sh

`scripts/package.sh` calls `./scripts/setup_jre.sh` first to verify `jre/` is present.

Produces **`outputs/`** with separate bundles per platform:

| Path | Contents |
|------|----------|
| `outputs/FormicEmpire.jar.zip` | JAR + `FormicEmpire.sh` + `saves/` — **JAR download** (needs Java 17+ installed) |
| `outputs/FormicEmpire.windows.zip` | `FormicEmpire.exe` (game embedded) + `jre/` — **Windows download** |
| `outputs/FormicEmpire.linux.zip` | jpackage app-image (`FormicEmpire/bin/FormicEmpire` + embedded runtime) — **Linux download** |
| `outputs/FormicEmpire.app` | macOS app bundle (built on macOS via `jpackage`) |

### Bundled runtimes (not committed to git)

| Folder | Platform | Required? |
|--------|----------|-----------|
| `jre/` | **Windows x64** Temurin 17 JRE | Yes on macOS, for Windows zip |
| `jre-linux/` or `jdk-*-jre/` | **Linux x64** Temurin 17 JRE | Yes on Linux, for Linux zip |
| System JDK 17 | **macOS** dev + `.app` build via `jpackage` | Yes on Mac (already installed) |
| System JDK 17 | **Linux** dev + `.linux.zip` build via `jpackage` | Yes on Linux |
| `jre-mac/` | Optional custom macOS runtime for `.app` | No — only if you want a specific embedded JRE |

**Windows JRE setup (once):**

1. Download [Temurin 17 JRE — Windows x64](https://adoptium.net/temurin/releases/?version=17&os=windows&arch=x64&package=jre)
2. Extract into the project root as **`jre/`** (must contain `jre/bin/java.exe`)
3. Run `./scripts/package.sh` (validates `jre/` automatically)

**Linux JRE setup (once, on the Linux build machine):**

1. Download [Temurin 17 JRE — Linux x64](https://adoptium.net/temurin/releases/?version=17&os=linux&arch=x64&package=jre)
2. Extract into the project root as **`jre-linux/`** (or leave as `jdk-*-jre/` — `setup_jre_linux.sh` detects both)
3. Run `./scripts/package.sh` on **Linux** (requires `jpackage` from JDK 17+)

**macOS:** No separate JRE download is required. `./scripts/run.sh` and the `.app` use your installed JDK 17. `jpackage` embeds a runtime when building `outputs/FormicEmpire.app`.

**App icons:** `src/main/resources/meta/icon.ico` (Windows) and `icon.icns` (macOS) are committed assets. To refresh `icon.icns` after editing PNGs in `icon.iconset/` on macOS:

    iconutil -c icns src/main/resources/meta/icon.iconset -o src/main/resources/meta/icon.icns

Run on macOS: `open outputs/FormicEmpire.app`  
Run on Linux: ship `outputs/FormicEmpire.linux.zip` (unzip, then run `FormicEmpire/bin/FormicEmpire`). Saves are created beside the app folder at first run.  
Run on Windows: ship `outputs/FormicEmpire.windows.zip` (unzip, then run `FormicEmpire.exe`). Saves are created beside the exe at first run.  
Run anywhere with Java 17+: unzip `outputs/FormicEmpire.jar.zip` and run `./FormicEmpire.sh`

In order to add new ant assets you need to use these colors in order to properly map the species: 
    	head FF0000, torso 00FF00, abdomen 0000FF, wingPrimary FFFF00, wingSecondary 00FFFF, 
        drone FF00FF, droneWingPrimary FF8000, droneWingSecondary 8000FF, honeypot FF0080;
		borders (and appendages) 000000

In case of wanting to edit or add new languages, please look into the translation_sheet.csv, there you can look for the string or add a new column (you still need to declare it in the codegen tool and for the settings).