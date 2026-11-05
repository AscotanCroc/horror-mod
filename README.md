# Horror Mod

Placeholder name for a standalone Minecraft horror mod. It is a Fabric total conversion aimed at an abstract, non-Euclidean space, with the look owned by the mod's own shaders. The name, the space, and the effects are not built yet. This repository is the toolchain.

| | |
| --- | --- |
| Minecraft | 26.3 |
| Loader | Fabric Loader 0.20.1, Fabric API 0.165.0+26.3, Loom 1.19.3 |
| Java | 25 |
| Mod id | `horror_mod` |
| Package | `com.horrormod` |
| Gradle project | `horror-mod` |

Iris shader packs are out of scope. The mod will not try to coexist with them.

## Requirements

Gradle has to be started by JDK 25. Loom 1.19.3 will not configure on an older runtime. If `java` is not on your PATH, install [Microsoft Build of OpenJDK 25](https://learn.microsoft.com/en-us/java/openjdk/download) or another JDK 25, set `JAVA_HOME` to that install, and open a new terminal.

The first build downloads Minecraft and the Fabric toolchain. That takes a while.

## Everyday commands

From this folder:

```bat
gradlew.bat genSources
gradlew.bat runClient
gradlew.bat runServer
gradlew.bat build
```

`runClient` launches a development client with the mod loaded. The log line `Post chain horror_mod:pipeline is present and not applied` means the client entry ran. `runServer` asks you to accept the Minecraft EULA in `run/eula.txt`, then set `online-mode=false` in `run/server.properties` before a dev client can join.

`build` writes the mod jar to `build/libs`.

In Cursor, open this folder as the workspace, then use the Gradle tasks above. Client code lives in `src/client` and is a separate source set from `src/main`.

## What is here

```text
src/main/java/com/horrormod/          common entry and future world rules
src/client/java/com/horrormod/client/ client entry, future menu, future shader binding
src/client/resources/assets/horror_mod/post_effect/pipeline.json
src/client/resources/assets/horror_mod/shaders/
```

`horror_mod:pipeline` copies the frame and writes it back. Nothing selects it, so launching the game does not change the picture. In a world with cheats, `/posteffect add @s horror_mod:pipeline` compiles that chain. A clean log means the shader fixture loaded. There is still no horror look.

## What is not here

No custom menu, no dimension, no entities, no applied post effect. Those are later, on top of this layout.

## Docs

- [Architecture](docs/architecture.md)
- [Shaders](docs/shaders.md)
- [Workflows](docs/workflows.md)

## Rename this project

The display name is the only soft part. These are the hard identifiers:

1. `gradle.properties`: `archives_base_name`, `group`, and `version`
2. `settings.gradle`: `rootProject.name`
3. `src/main/resources/fabric.mod.json`: `id`, `name`, `description`, entrypoints
4. `HorrorMod.MOD_ID` and the Java package `com.horrormod`
5. Mixin config file names and the `"package"` field inside each one
6. `build.gradle`: the name inside the `loom.mods` block
7. Asset folder `src/client/resources/assets/horror_mod/`
8. Shader include `<horror_mod:horror.glsl>` and the post-chain id `pipeline`

Do that in one pass. A half-renamed mod id will fail to load.
