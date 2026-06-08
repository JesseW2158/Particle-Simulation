# High-Scale Particle "Points of Light" Renderer — Design

**Date:** 2026-05-30
**Status:** Approved (design), pending implementation plan

## Goal

Render up to ~10,000 particles efficiently (no major lag) as glowing **points of light** — no geometric radius. Each particle's on-screen size is `k · cbrt(mass)`, where `k` is a tunable constant living in the renderer. Particles are typed (proton / neutron / electron) and colored by type. This is the rendering foundation for a future proton/neutron/electron atom-formation simulation.

## Settled decisions

| Decision | Choice |
|----------|--------|
| Scope | Renderer + data model + demo spawning only. **No physics/motion** (separate later project). |
| Technique | `GL_POINTS` point sprites, one static VBO, **single draw call**, additive glow. |
| Size | `gl_PointSize = k · cbrt(mass) / clipW` (perspective: nearer = bigger). No physical radius. |
| `k` | `public static final float K` in `PointRenderer`, pushed as a uniform — tweak + rerun, no rebuild. |
| Particle types | ELECTRON / PROTON / NEUTRON with real relative masses, charge, and a glow color. |
| Color | By type/charge — electron blue, proton red, neutron white. |
| Count | `Constants.RENDERED_PARTICLES = 10000` (configurable). |
| Background | Black, so additive glow reads. |

## Why a new render path

The current renderer binds one VAO and issues one draw call **per particle** (the single quad). At 10k particles that is ~10k draw calls per frame — unworkable. The new path uploads all particles into **one** buffer and draws them with a single `glDrawArrays(GL_POINTS, …)`. The camera/MVP work already built is reused (projection + view matrices).

## Architecture

Domain model lives in a `particle` package (separate from rendering in `Serenity`). The renderer reads particle position + type to build a static GPU buffer once, then draws it each frame through the camera.

```
ParticleSimulation
  ├─ ParticleSpawner.randomCloud(count, halfExtent, seed) -> List<particle.Particle>
  ├─ PointRenderer.buildBuffer(particles)        // once, in init()
  └─ each frame: PointRenderer.render(camera)      // one draw call
                    ├─ uses WindowManager.updateProjectionMatrix()
                    └─ uses Transformation.getViewMatrix(camera)
```

## Components

### 1. `particle.ParticleType` (new enum)

Each constant carries mass (electron-mass units), charge, and a glow color (`org.joml.Vector3f`):

| Type | mass | charge | color (r,g,b) | `cbrt(mass)` |
|------|------|--------|---------------|--------------|
| ELECTRON | 1.0 | −1 | (0.4, 0.6, 1.0) blue | 1.00 |
| PROTON | 1836.0 | +1 | (1.0, 0.35, 0.35) red | 12.245 |
| NEUTRON | 1839.0 | 0 | (0.9, 0.9, 0.95) white | 12.252 |

Accessors: `mass()` (double), `charge()` (int), `color()` (Vector3f).

### 2. `particle.Particle` (new)

- Fields: `Vector3f position`, `ParticleType type`.
- `position()`, `type()`.
- `cbrtMass()` → `(float) Math.cbrt(type.mass())`.
- No radius, no velocity (renderer-only scope).

### 3. `particle.ParticleSpawner` (new)

- `static List<Particle> randomCloud(int count, float halfExtent, long seed)`:
  seeded `Random`; for each of `count`, a position uniformly in the cube `[-halfExtent, halfExtent]³` and a random `ParticleType`. Seeded so it's unit-testable and reproducible.

### 4. `Serenity.PointRenderer` (new)

- `public static final float K = 25f;` — the tunable constant `k`.
- Owns a `ShaderManager` (point shaders), a VAO id, the particle count, and its VBO ids.
- `init()`: load/link `point_vertex.vs` + `point_fragment.fs`; create uniforms `projectionMatrix`, `viewMatrix`, `k`; set GL state: `glEnable(GL32.GL_PROGRAM_POINT_SIZE)`, `glEnable(GL_BLEND)`, `glBlendFunc(GL_SRC_ALPHA, GL_ONE)` (additive).
- `buildBuffer(List<particle.Particle>)`: build three float arrays — position (vec3), `cbrtMass` (float), color (vec3) — and upload as three static attributes (locations 0/1/2) into one VAO. Store `count`. Called once.
- `render(Camera camera)`: `glClear`; `glDepthMask(false)`; bind shader; set `projectionMatrix` (`window.updateProjectionMatrix()`), `viewMatrix` (`transformation.getViewMatrix(camera)`), `k` (`K`); bind VAO; enable attrib arrays 0/1/2; `glDrawArrays(GL_POINTS, 0, count)`; disable arrays; unbind; `glDepthMask(true)`.
- `cleanup()`: delete shader, VAO, VBOs.

### 5. `Serenity.ShaderManager` (modify)

Add a float-uniform overload:

```java
public void setUniform(String uniformName, float value) {
    GL20.glUniform1f(uniforms.get(uniformName), value);
}
```

### 6. Shaders (new)

`src/main/resources/Shaders/point_vertex.vs`:

```glsl
#version 400 core
layout(location = 0) in vec3 position;
layout(location = 1) in float cbrtMass;
layout(location = 2) in vec3 color;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform float k;

out vec3 glowColor;

void main() {
    vec4 clip = projectionMatrix * viewMatrix * vec4(position, 1.0);
    gl_Position = clip;
    gl_PointSize = max(k * cbrtMass / clip.w, 1.0);
    glowColor = color;
}
```

`src/main/resources/Shaders/point_fragment.fs`:

```glsl
#version 400 core
in vec3 glowColor;
out vec4 fragColor;

void main() {
    vec2 d = gl_PointCoord - vec2(0.5);
    float r = length(d) * 2.0;       // 0 at center, 1 at edge
    if (r > 1.0) {
        discard;                     // round point, not square
    }
    float intensity = 1.0 - r;
    intensity = intensity * intensity; // soft glow falloff
    fragColor = vec4(glowColor * intensity, intensity);
}
```

With additive blending each particle adds `glowColor · intensity²`, so overlapping particles brighten — the "light" look.

### 7. `Test.ParticleSimulation` (modify)

- Replace the single-quad fields/usage (`RenderManager`, `ParticleLoader`, `Serenity.Particle particle`) with `PointRenderer` and `List<particle.Particle>`.
- Keep `Camera`, `MouseInput`, and all input handling unchanged.
- Camera initial position moves to `(0, 0, 30)` so the spawned cloud (half-extent ~10) is in frame on launch.
- `init()`: `particles = ParticleSpawner.randomCloud(Constants.RENDERED_PARTICLES, 10f, 1L)`; `pointRenderer.init()`; `pointRenderer.buildBuffer(particles)`; `mouseInput.init(window)`.
- `render()`: viewport-on-resize (unchanged); `window.setClearColor(0, 0, 0, 1)` (black); `pointRenderer.render(camera)`.
- `cleanup()`: `pointRenderer.cleanup()`.

### 8. `Serenity.Util.Constants` (modify)

`RENDERED_PARTICLES` → `10000`.

## Retired (left in place, unused)

`RenderManager`, `ParticleLoader`, the quad geometry, and `Serenity.Particle`'s render fields are no longer used by the running app. They are left in the repo to minimize churn (the camera unit tests still construct `Serenity.Particle`, and `Transformation.getViewMatrix` remains in use by `PointRenderer`). They can be deleted in a later cleanup.

## Performance

- One static VBO + one `glDrawArrays` call per frame; zero per-frame CPU work beyond setting 3 uniforms. 10k points is trivial (the technique scales to 100k+).
- The only real cost is glow fill-rate (large overlapping sprites). Controlled by `K`; reduce it if fill-bound.
- FPS is already shown in the window title — the lag check is "fly through 10k and watch it stay high."

## Edge cases / notes

- **Black background is required** — additive glow on a white clear is invisible. `setClearColor(0,0,0,1)`.
- **`gl_PointSize` cap:** GPUs clamp point size; a particle very close to the camera clamps to the max instead of filling the screen. Acceptable for points of light.
- **Empty particle list:** `count == 0` → `glDrawArrays(…, 0, 0)` is a harmless no-op.
- **Depth:** depth-write is disabled during the points pass so additive blending is order-independent; depth-test stays enabled (no opaque geometry, so all points pass).
- `GL_PROGRAM_POINT_SIZE` is core in the 3.2 context already requested by `WindowManager`.

## Testing

Unit-testable pure logic (JUnit 4.12, run via VSCode Test Explorer — no Gradle CLI):
- `ParticleTest`: `ELECTRON.mass()==1`, `PROTON.charge()==+1`, `NEUTRON.charge()==0`; `cbrtMass()` ≈ `cbrt(1836)=12.23` for a proton, `1.0` for an electron.
- `ParticleSpawnerTest`: `randomCloud(N, h, seed)` returns exactly `N` particles, every coordinate within `[-h, h]`, and is reproducible for a fixed seed.

GL/visual verification is manual: run `Test.Launcher`, see a cloud of red/white/blue glowing points, fly through it with the camera, and confirm the FPS counter stays high with `RENDERED_PARTICLES = 10000`.

## Out of scope

Physics (forces, motion, atom formation), spatial partitioning, picking/selection, HDR/bloom post-processing, removing the legacy quad classes, fixing the unrelated broken `space.Space`.