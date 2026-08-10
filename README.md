GAME DESCRIPTION:

Formic Empire is an open source, ant managment and simulation game completely made in Java by a single developer. This game is equal parts grand strategy, colony managment, and incremental. Grow your ant dynasty from a few ants to millions and billions by conquering more territory and assimilating new abilities that allow you to expand further. Think of it as an unholy cross between rimworld, stellaris and spore. 

All the assets from images, music, ui, font and code was made by me and so I give it freely to whoever wants to use it. Including to a package of core reusable classes with code-gen tools so its easier to make more games in the future, read the license for details but in general just don't sell my stuff without permission. Tools use include: Aseprite to make pixelart, Cursor to code, LMMS to make audio, GitHub to host the project, Java as the sole programming language with the relevant development tools. 

Formic Empire will eventually release on steam for a few dollars at most, I'm aiming for Q4 of 2026, while the itch.io version will always stay at donations based price. Please be on the lookout for a demo version and wishlist on Steam soon. After the 1.0 release there will be regular but not common major updates for as long as I have ideas, these may include community ideas or even forks. The project does not have a budget other than my own time and money spent on my vices, so all donations and steam income will go to pay the fees and fuel the further development of Formic Empire and other projects. 

Please enjoy Formice Empire, and send feedback to: thegrimidk@gmail.com or leave a comment here. I'll answer every single inquiry I get, but please do be kind since this is my very first game. 

Check the roadmap to see planned features and known bugs and credits for acknowledgments, if you want to help out let me know and I'll add you to the credits. 

DEVELOPMENT INSTRUCTIONS:

This are instructions to modify or test the game, if you are instered in just playing please go to: https://grimidk.itch.io/formic-empire 

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

You can add new .mp3 to the audio/music/base in order to add them to the random track selection, the compile will add them as unknown artists, you will have to add that manually in the MusicTracks.java repository