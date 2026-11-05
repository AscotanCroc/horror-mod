# Architecture

Horror Mod is a total conversion. Vanilla survival is not the game. The target is an abstract space with almost no recognizable Minecraft architecture, seen through a shader pipeline that ships inside the mod and is always on once that work starts.

This scaffold does not create that space. It only fixes the boundaries so the later work has somewhere to go.

## Source sets

Fabric Loom splits the mod into two source sets.

`src/main` is common. It loads on a client and on a dedicated server. `HorrorMod` holds the mod id, the logger, and `HorrorMod.id`. Future world rules, the abstract space, and anything the server must agree on go in `com.horrormod.world`.

`src/client` loads on the client only. `HorrorModClient` is the client entry. Future screens go in `com.horrormod.client.screen`. Future code that binds the shader pipeline goes in `com.horrormod.client.render`. Shader files are client resources, under `src/client/resources`.

A dedicated server can exist later without carrying menus or shader code in its entrypoints. The client source set is how that stays true. Do not import `net.minecraft.client` from `src/main`.

## Sessions

The first version is singleplayer. The split above is the whole of the multiplayer preparation. There is no dedicated-server gameplay, no sync protocol, and no integrated-server feature yet.

## The look

The picture is owned by this mod's post passes. Players do not install a shader pack, and the project does not depend on Iris. Iris replaces the game's shader programs while a pack is active, so a mod-authored pipeline and a player-installed pack are two different projects.

Post effects in 26.3 run after the world and before the HUD. The chain that will eventually own the picture is a resource at `assets/minecraft/post_effect/end_of_frame.json`. When that file exists, the game applies it every frame and the `/posteffect` command cannot turn it off. Leave it absent until the look is real. Until then the only chain is `horror_mod:pipeline`, and nothing selects it.

Core-shader replacements under `assets/minecraft/shaders/core` rewrite how the world itself is drawn. That is a later, narrower tool. The default place for a new effect is a post pass.

## Data

Future data-driven world files belong in `src/main/resources/data/horror_mod/`. Add them when the space exists. Do not invent dimension JSON ahead of that.

## Mixins

Both mixin configs are empty on purpose. A common mixin is a class in `com.horrormod.mixin`, listed in `src/main/resources/horror_mod.mixins.json`. A client mixin is a class in `com.horrormod.client.mixin`, listed in `src/client/resources/horror_mod.client.mixins.json`. `defaultRequire` is 1, so a missed injection fails the game instead of being skipped.
