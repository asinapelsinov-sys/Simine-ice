# Snowstorm — Fabric 1.21.1

A first implementation of a blizzard event for Minecraft Java Edition 1.21.1.

## Requirements
- Java 21
- Minecraft 1.21.1
- Fabric Loader 0.16.0 or newer
- Fabric API 0.115.6+1.21.1

## Build
Open this folder in IntelliJ IDEA as a Gradle project and let Gradle sync. In the Gradle tool window, run **Tasks → build → build**. If you have Gradle installed globally, you can also run `gradle build` from this folder. The built mod jar will be in `build/libs/`.

## Commands
- `/snowstorm start` — start a 5-minute storm
- `/snowstorm start <seconds>` — start a storm for 30–3600 seconds
- `/snowstorm stop` — stop the storm
- `/snowstorm status` — show whether a storm is active

Random storms are enabled. The first random storm is scheduled after roughly 8 minutes; after each storm, the next one is scheduled after a random cooldown.

## Notes
This is an initial source project, not a tested release jar. Build it with Java 21 and run it in a Fabric 1.21.1 development client/server. The client overlay currently darkens the full HUD layer slightly; a later rendering pass can move the effect into a dedicated world post-processing overlay. Server-side cold exposure is authoritative, so multiplayer players are affected consistently.

## Build on GitHub without installing an IDE
1. Create a new empty repository on GitHub.
2. Upload the contents of this `snowstorm` folder to the repository root (including the hidden `.github` folder).
3. Open the repository's **Actions** tab and select **Build Snowstorm mod**.
4. Click **Run workflow**. You can also trigger a build by pushing a commit to `main` or `master`.
5. When the run finishes successfully, open that run and download the `snowstorm-fabric-1.21.1` artifact. The artifact ZIP contains the mod JAR(s).

The workflow provisions Java 21 and Gradle 8.10.2 on GitHub-hosted runners and uploads the JAR from `build/libs`.
