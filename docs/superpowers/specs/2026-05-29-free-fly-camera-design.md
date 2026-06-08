# Free-Fly Mouse Camera — Design

**Date:** 2026-05-29
**Status:** Approved (design), pending implementation plan

## Goal

Let the user look around the 3D scene by **holding left-click and dragging the mouse**, and **zoom in/out with the scroll wheel**. The camera is free-fly (first-person look): it rotates its own view direction in place.

## Settled decisions

| Decision | Choice |
|----------|--------|
| Camera style | Free-fly / look-around (camera stays put, rotates view direction) |
| Activation | Mouse-look only while **left mouse button is held** |
| Yaw (horizontal) | Wraps a full 360° |
| Pitch (vertical) | Clamped to ±89° (no upside-down flip) |
| Zoom | **Dolly** — move camera forward/back along its look direction; driven by scroll wheel |
| Back-face culling | **Disabled** so the flat one-sided quad stays visible from any angle |
| Keyboard fly (WASD) | **Out of scope** — mouse-look + scroll-zoom only |

## Why this needs a full pipeline change

The renderer currently performs **no transformation**: [vertex.vs](../../../src/main/resources/Shaders/vertex.vs) does `gl_Position = vec4(position, 1.0)`, putting raw mesh coordinates straight into clip space. A camera is only meaningful once there is a model → view → projection (MVP) chain. Most of this work is building that chain; the mouse/camera layer sits on top of it.

Existing scaffolding we will use: [WindowManager.updateProjectionMatrix()](../../../src/main/java/Serenity/WindowManager.java) (already computes a perspective matrix, currently unused), the FPS-shaped [Camera](../../../src/main/java/Serenity/Camera.java) class, the empty [Transformation](../../../src/main/java/Serenity/Util/Transformation.java) class, and [ShaderManager](../../../src/main/java/Serenity/ShaderManager.java)'s `createUniform` / `setUniform(Matrix4f)`.

## Architecture

Four isolated units, each with one responsibility:

1. **Transform pipeline** — turns mesh coordinates into screen coordinates through the camera.
2. **Camera** — holds position + orientation (yaw/pitch); applies look and dolly deltas with clamping.
3. **MouseInput** — owns all GLFW mouse callbacks; exposes per-frame mouse displacement, left-button state, and scroll delta.
4. **Wiring** — `ParticleSimulation` reads `MouseInput` each frame, drives the `Camera`, and feeds matrices to the renderer.

### Data flow (per frame)

```
EngineManager.run() loop
  ├─ input()   → gameLogic.input()
  │               ├─ mouseInput.input()            // compute displacement vs last frame
  │               ├─ if leftButtonHeld: camera.moveRotation(dPitch, dYaw, 0); clampPitch()
  │               └─ if scrollDelta != 0: camera.moveForward(scrollDelta * ZOOM_SPEED); reset scroll
  ├─ update()  → (physics later; no-op now)
  └─ render()  → gameLogic.render()
                  ├─ if resize: glViewport(...) + window.updateProjectionMatrix()
                  └─ renderer.render(particle, camera)
                        ├─ clear()
                        ├─ shader.bind()
                        ├─ setUniform("projectionMatrix", window projection)
                        ├─ setUniform("viewMatrix", Transformation.getViewMatrix(camera))
                        ├─ setUniform("worldMatrix", Transformation.getWorldMatrix(particle))
                        ├─ bind VAO + glDrawElements
                        └─ unbind
                 window.update()  // swap + poll
```

## Component details

### 1. Vertex shader — [vertex.vs](../../../src/main/resources/Shaders/vertex.vs)

```glsl
#version 400 core
layout(location = 0) in vec3 position;
out vec3 color;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 worldMatrix;
void main() {
    gl_Position = projectionMatrix * viewMatrix * worldMatrix * vec4(position, 1.0);
    color = vec3(position.x + 0.25, 0.17, position.y + 0.25);
}
```

The fragment shader is unchanged.

### 2. Transformation — [Transformation.java](../../../src/main/java/Serenity/Util/Transformation.java)

JOML-based (the project already depends on `org.joml`). Holds reusable `Matrix4f` instances to avoid per-frame allocation.

- `getViewMatrix(Camera camera)`:
  ```
  view.identity()
      .rotate(toRadians(pitch), 1,0,0)
      .rotate(toRadians(yaw),   0,1,0)
      .translate(-pos.x, -pos.y, -pos.z)
  ```
- `getWorldMatrix(Particle particle)`: `world.identity().translate(px, py, pz).scale(scale)` where `(px,py,pz)` come from `particle.position().x()/y()/z()` cast to float. Scale defaults to `1.0f`. (The particle currently sits at the origin via `Vector.zero()`.)

### 3. Camera — [Camera.java](../../../src/main/java/Serenity/Camera.java)

Reuse the existing class. Conventions: `rotation.y` = yaw, `rotation.x` = pitch (degrees).

- **Initial state:** position `(0, 0, 2)`, rotation `(0, 0, 0)` — placed 2 units back on +Z, looking down −Z toward the quad at the origin.
- **Look:** `moveRotation(dPitch, dYaw, 0)` (existing method). After applying, the caller clamps pitch to `[-89, 89]` and wraps yaw to `[0, 360)`.
- **Dolly (new method) `moveForward(float amount)`:** translate position along the world-space look vector
  `L = ( sin(yaw)·cos(pitch), −sin(pitch), −cos(yaw)·cos(pitch) )` (yaw, pitch in radians):
  `position += L * amount`. At yaw=0, pitch=0 this is `(0,0,−1)` — scrolling forward moves toward the scene. Exact axis signs to be verified by running (see Testing).

### 4. MouseInput — `Serenity/MouseInput.java` (new)

Standard GLFW input-accumulator pattern. Registered against the window handle after the window is created.

State: `Vector2d previousPos, currentPos`; `Vector2f displVec`; `boolean inWindow, leftButtonPressed`; `double scrollDelta`.

- `init(WindowManager window)` registers:
  - `glfwSetCursorPosCallback` → update `currentPos`
  - `glfwSetCursorEnterCallback` → set `inWindow`
  - `glfwSetMouseButtonCallback` → `leftButtonPressed = (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS)`
  - `glfwSetScrollCallback` → `scrollDelta += yoffset`
- `input()` computes `displVec` from `currentPos − previousPos` (only when `inWindow`), then sets `previousPos = currentPos`.
- Getters: `getDisplVec()`, `isLeftButtonPressed()`, `getScrollDelta()`, `resetScroll()`.

`displVec.x` = vertical mouse movement → pitch; `displVec.y` = horizontal mouse movement → yaw. Sign of each is tuned during implementation so dragging right looks right and dragging up looks up.

### 5. Wiring

- **[Launcher](../../../src/main/java/Test/Launcher.java) / [ParticleSimulation](../../../src/main/java/Test/ParticleSimulation.java):** own a `Camera` and a `MouseInput`. `MouseInput.init(window)` is called after the window is initialized (in `ParticleSimulation.init()` or alongside window init).
- **[ParticleSimulation.input()](../../../src/main/java/Test/ParticleSimulation.java):** call `mouseInput.input()`; if `isLeftButtonPressed()`, apply displacement to the camera and clamp; if `getScrollDelta() != 0`, dolly and `resetScroll()`.
- **[ParticleSimulation.render()](../../../src/main/java/Test/ParticleSimulation.java):** on resize, also recompute the projection via `window.updateProjectionMatrix()`; call `renderer.render(particle, camera)`.
- **[RenderManager](../../../src/main/java/Serenity/RenderManager.java):** `init()` creates the three uniforms; `render(Particle, Camera)` sets projection/view/world uniforms before the draw call. (Signature changes from `render(Particle)`.)
- **[WindowManager.init()](../../../src/main/java/Serenity/WindowManager.java):** remove `glEnable(GL_CULL_FACE)` / `glCullFace(GL_BACK)` so the quad renders from both sides. Ensure the projection matrix is initialized once at startup.

### Constants

Add to [Constants](../../../src/main/java/Serenity/Util/Constants.java) (or `Camera`): `MOUSE_SENSITIVITY` (e.g. `0.1f`), `ZOOM_SPEED` (e.g. `0.4f`), `PITCH_LIMIT = 89f`.

## Error handling / edge cases

- **Aspect ratio on resize:** `updateProjectionMatrix()` divides width/height. A minimized window can report height 0 → division by zero / NaN matrix. Guard by skipping the projection update when width or height is 0.
- **First frame:** `previousPos` is initialized to the first cursor position so the opening frame produces zero displacement (no view jump).
- **Quad seen edge-on:** a flat quad viewed exactly edge-on is ~invisible; expected and acceptable for a single test particle.
- **Pitch clamp** prevents gimbal flip; yaw wrap prevents unbounded float growth.

## Testing

Unit-testable pure logic (JUnit 4.12 is already a dependency):

- `Transformation.getViewMatrix` — known camera pose → expected matrix values (e.g. identity pose, a 90° yaw).
- Camera pitch clamping and yaw wrap — feed out-of-range deltas, assert clamped/wrapped result.
- `Camera.moveForward` look-vector — at yaw=0/pitch=0, forward ≈ `(0,0,−1)`.

GLFW/OpenGL interaction (callbacks, actual rendering) is verified **manually**, since there is no `gradle` on the CLI and this is a windowed GL app:
1. Build via the VSCode Java extension and run `Test.Launcher`.
2. Hold left-click and drag — view rotates; releasing stops it.
3. Drag a full circle horizontally (yaw wraps); drag up/down (pitch stops short of vertical).
4. Scroll up/down — camera moves toward/away from the quad.
5. Maximize/restore — quad stays correctly proportioned (projection recomputed) and visible.

## Out of scope

WASD/keyboard movement, multiple particles, billboarding, orbit-around-target mode, configurable key bindings. Each can be added later on top of this pipeline.
