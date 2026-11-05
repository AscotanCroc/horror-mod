# Workflows

## First launch

1. Install a JDK 25 and set `JAVA_HOME` if `java` is not already available. Loom will not configure the project on an older runtime.
2. Open `horror-mod` as the Cursor workspace.
3. Run `gradlew.bat genSources` once. This decompiles Minecraft into a form the editor can navigate.
4. Run `gradlew.bat runClient`.

The Gradle configuration cache is on. After a `build.gradle` change, the next run may reconfigure. That is expected.

`genSources`, `runClient`, and `build` are the tasks to use from the Gradle tool window. Loom also writes run configurations the first time the project is imported.

## Where a change goes

| Change | Place |
| --- | --- |
| Mod id, logger, something both sides must share | `src/main/java/com/horrormod` |
| World rules, the abstract space | `src/main/java/com/horrormod/world` and `src/main/resources/data/horror_mod` |
| A screen or the front end | `src/client/java/com/horrormod/client/screen` |
| Code that turns a post chain on | `src/client/java/com/horrormod/client/render` |
| A shader or post chain | `src/client/resources/assets/horror_mod` |
| A common mixin | `com.horrormod.mixin`, then list it in `horror_mod.mixins.json` |
| A client mixin | `com.horrormod.client.mixin`, then list it in `horror_mod.client.mixins.json` |

After adding a mixin class, put its simple name in the matching JSON array. `defaultRequire` is 1. If the target method is wrong, the game fails at startup instead of silently skipping the injection.

## Shader loop

Edit the `.fsh` or `pipeline.json`, save, then press F3+T in a running client. Apply `horror_mod:pipeline` again with `/posteffect` if it was not already active. The chain is not selected on its own.

Read [shaders.md](shaders.md) before adding a pass. The 26.3 file format is not the older `program` JSON format.

## Server check

`gradlew.bat runServer` is how a client-only mistake gets caught. The first start writes `run/eula.txt`. Set `eula=true` there. Then set `online-mode=false` in `run/server.properties` so a development account can join. The common entry should log. The client entry should not.

## Build

`gradlew.bat build` produces `build/libs/horror-mod-0.1.0.jar`. That jar is what a normal Fabric 26.3 client loads. Development runs do not need you to copy it anywhere.

GitHub Actions, when this repo has a remote, runs the same `build` on Java 25. The workflow file is `.github/workflows/build.yml`. Nothing is pushed from this setup.

## Versions

Bump Minecraft, Loader, Loom, or Fabric API together in `gradle.properties`, using the set published at <https://fabricmc.net/develop>. `fabric.mod.json` depends on `minecraft` `~26.3` and Java 25. A one-sided bump will not load.

## Rename

The display name can change without a migration. The mod id, package, asset folder, and mixin packages cannot drift apart. The checklist is in the README.
