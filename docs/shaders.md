# Shaders

The pipeline is a normal 26.3 post effect. There is no extra shader library. Files live in the client resources so a dedicated server never needs them.

```text
src/client/resources/assets/horror_mod/
  post_effect/pipeline.json
  shaders/post/passthrough.fsh
  shaders/include/horror.glsl
```

`pipeline.json` is the chain `horror_mod:pipeline`. Its first pass samples `minecraft:main`, runs `horror_mod:post/passthrough`, and writes a target named `swap`. The second pass is vanilla `minecraft:post/blit` back onto `minecraft:main`. A pass cannot read and write `minecraft:main` in the same step.

`passthrough.fsh` multiplies the frame by white. `horror.glsl` only exports `HORROR_MOD_PASS`, which is `1.0`. Applying the chain should not change the picture. That is the fixture.

## See that it compiles

Launch a client, open a world with cheats, and run:

```text
/posteffect add @s horror_mod:pipeline
```

Shader errors show up in the game log. `/posteffect remove @s horror_mod:pipeline` takes it off. This does not turn the effect on for a normal launch.

When an effect should be on every frame, add `src/client/resources/assets/minecraft/post_effect/end_of_frame.json` and point its passes at the horror shaders. Do that only once the look is intentional. While that file is absent, the slot stays empty and the game skips it.

## Rules for a new pass

Write the fragment shader at `assets/horror_mod/shaders/post/<name>.fsh`. The post-effect id `horror_mod:post/<name>` maps to that file.

The first two lines, with nothing before them:

```glsl
#version 330
#extension GL_ARB_separate_shader_objects : require
```

Every `in` and `out` needs an explicit location. Fullscreen passes use the vertex shader `minecraft:core/screenquad`, which writes `texCoord` at location 0. Match that location in the fragment shader. 26.4 renames this vertex shader to `screentriangle`. This project is 26.3, so the name stays `screenquad`.

Shared helpers go in `shaders/include/`, wrapped in `#ifndef` / `#define` / `#endif`. Include them as:

```glsl
#include <horror_mod:horror.glsl>
```

That path loads `assets/horror_mod/shaders/include/horror.glsl`. Do not use `#moj_import`.

Uniform blocks use `std140`. The block name in the shader and the key under `uniforms` in the JSON are the same string. The game ignores the `name` field inside a uniform entry; it still belongs there so the JSON stays readable. If the shader declares a block, the JSON must supply it.

`layout(location = 0) out vec4 fragColor` is the color output. Keep includes free of samplers and uniform blocks so two passes can share a helper without fighting over block layout.

## Adding a second effect

1. Add the `.fsh` file.
2. Add a pass to `pipeline.json`, or add a new file under `post_effect/` if it should be selectable on its own.
3. Give the pass its own uniform block if it needs parameters.
4. Write to `swap` when the input is `minecraft:main`, then blit back.
5. Reload resources with F3+T, then `/posteffect add` the chain again.

Core replacements, if one is ever required, would be files under `assets/minecraft/shaders/core/` that use the same filename as a vanilla program. That replaces the program for the whole game. Prefer a post pass.

## What this version will not do

GLSL here is compiled by ShaderC, including on OpenGL. A shader that compiled on an older Minecraft version can fail here because locations are required, `#version` must be the first line, and includes are `#include`. Check the log rather than a driver-specific tool.
