# Particle-Simulation

A Java + LWJGL particle visualization project with a small custom engine loop, camera controls, OpenGL rendering, and randomized particle generation.

## 1) Project Purpose

This project renders a large cloud of particles in 3D space and lets you move a camera around the scene. It currently focuses on rendering and interaction, with some physics-oriented classes present for future expansion.

The codebase has been consolidated to one canonical particle model and one rendering path.

## 2) Tech Stack

- Language: Java 21
- Build: Gradle (`java`, `application` plugins)
- Graphics/API: LWJGL 3 (OpenGL + GLFW)
- Math: JOML (`org.joml`)
- Tests: JUnit 4

## 3) External Libraries and What They Do

Defined in `build.gradle`:

- `org.lwjgl:lwjgl-bom`
  - Aligns LWJGL module versions through a BOM.
- `org.lwjgl:lwjgl`
  - Core LWJGL bindings.
- `org.lwjgl:lwjgl-glfw`
  - Windowing/input/events via GLFW.
- `org.lwjgl:lwjgl-opengl`
  - OpenGL function bindings.
- `org.lwjgl:lwjgl-assimp`
  - Asset import bindings (currently not used by main code).
- `org.lwjgl:lwjgl-nfd`
  - Native file dialogs (currently not used).
- `org.lwjgl:lwjgl-openal`
  - Audio bindings (currently not used).
- `org.lwjgl:lwjgl-stb`
  - STB utilities (currently not used).
- `runtimeOnly ... ::natives-windows`
  - Native DLLs required at runtime on Windows.
- `org.joml:joml`
  - Vector and matrix math types (`Vector2f`, `Vector3f`, `Matrix4f`, etc.).
- `junit:junit:4.12`
  - Unit testing framework for `src/test/java`.

## 4) Package-by-Package Guide

### `Test`

Startup and top-level simulation implementation.

- `Launcher.java`
  - Program entry point (`main`).
  - Creates `WindowManager`, `ParticleSimulation`, then starts `EngineManager`.
  - Prints LWJGL version on launch.
- `ParticleSimulation.java`
  - Implements the engine lifecycle interface (`ILogic`).
  - Creates random particles using `particle.ParticleSpawner`.
  - Initializes and calls `Serenity.PointRenderer`.
  - Handles camera movement and mouse input each frame.

### `Serenity`

Core engine/window/rendering/input utilities.

- `EngineManager.java`
  - Main game loop with fixed frame timing (`FRAMERATE = 1000`).
  - Calls `input()`, `update()`, `render()` through `ILogic`.
  - Tracks FPS and updates window title.
- `WindowManager.java`
  - Creates and manages GLFW window and OpenGL context.
  - Handles resize callback, ESC-to-close key callback, swap/poll loop.
  - Maintains projection matrix and perspective update.
- `Camera.java`
  - Stores camera position/rotation.
  - Supports movement (W/S, strafe, vertical), forward motion, and look controls.
  - Clamps pitch and wraps yaw.
- `MouseInput.java`
  - Captures cursor movement, left-click state, and scroll wheel.
  - Produces displacement vector used for camera look.
- `PointRenderer.java`
  - Main active renderer for this simulation.
  - Builds GPU buffers for particle positions, sphere radius, color, and glow parameters.
  - Renders particles as shader-based sphere impostors: solid proton/neutron bodies plus a separate electron glow pass.
- `ShaderManager.java`
  - Wraps shader program lifecycle: compile, link, validate, bind/unbind, cleanup.
  - Stores and updates uniform locations/values.

### `Serenity.Util`

Engine utility abstractions/constants.

- `ILogic.java`
  - Lifecycle interface required by `EngineManager`.
  - Methods: `init`, `input`, `update`, `render`, `cleanup`.
- `Constants.java`
  - Shared constants for title, window size, particle count, camera sensitivity/speed.
- `Transformation.java`
  - Builds view matrix from camera and world matrix from a `particle.Particle` position.
  - Used by renderers needing matrix transforms.
- `Utils.java`
  - Buffer allocation helpers for float/int arrays.
  - Loads shader text resources from classpath.

### `particle`

Current lightweight particle model used by `ParticleSimulation` + `PointRenderer`.

- `Particle.java` (in `particle` package)
  - Immutable particle data: `Vector3f position`, `ParticleType type`.
  - Exposes `cbrtMass()` plus render helpers like sphere radius and glow settings.
- `ParticleType.java`
  - Enum with mass, charge, color, and per-type render style presets:
    - `ELECTRON`, `PROTON`, `NEUTRON`.
- `ParticleRenderStyle.java`
  - Centralized visual settings for each particle type.
  - Controls sphere radius scaling, whether size follows cube-root mass, glow strength, and glow radius.
- `ParticleSpawner.java`
  - Creates randomized particle clouds inside a cube volume.
  - Deterministic with fixed random seed.

## 5) Resource Files

Located in `src/main/resources/Shaders`:

- `point_vertex.vs`
  - Vertex shader for point-sphere rendering.
  - Sets `gl_PointSize` from per-particle sphere radius and glow radius scale.
- `point_fragment.fs`
  - Fragment shader for shaded sphere impostors.
  - Draws solid proton/neutron spheres in the body pass and a subtle halo around electrons in the glow pass.

## 6) Test Suite (`src/test/java`)

- `Serenity/CameraTest.java`
  - Verifies camera forward movement, pitch clamp, yaw wrapping.
- `Serenity/Util/TransformationTest.java`
  - Verifies view/world matrix behavior.
- `particle/ParticleTest.java`
  - Verifies particle type masses/charges and cube-root mass scaling.
- `particle/ParticleSpawnerTest.java`
  - Verifies cloud size, bounds, and deterministic seeding.

## 7) Runtime Flow (How It Works End-to-End)

1. `Test.Launcher.main` creates window + simulation and starts engine.
2. `Serenity.EngineManager` initializes GLFW error callback, window, and game logic.
3. `ParticleSimulation.init` creates particles (`ParticleSpawner`), initializes `PointRenderer`, and mouse callbacks.
4. Main loop repeatedly executes:
   - `input`: camera mouse look, wheel zoom, keyboard movement.
   - `update`: currently empty hook.
   - `render`: resize viewport handling, clear color, draw particles with `PointRenderer`.
5. `PointRenderer.render` binds shader, sets projection/view uniforms, draws points.
6. On shutdown: renderer cleanup, window destroy, GLFW terminate.

## 8) Important Design Notes

- There is one canonical particle model: `particle.Particle`.
- Active rendering uses GL points as sphere impostors via `PointRenderer`.
- Protons and neutrons render as solid shaded spheres.
- Electrons render as smaller spheres with a slight glow halo for visibility.
- Physics integration for active particles is not implemented yet in `ParticleSimulation.update`.

## 9) Build and Run

From project root:

- Build:
  - `gradle build`
- Force dependency refresh:
  - `gradle build --refresh-dependencies`
- Run app:
  - `gradle run`

If you add Gradle wrapper later:

- Windows:
  - `./gradlew.bat build`
  - `./gradlew.bat run`

## 10) Common Issues and Quick Fixes

- `package org.joml does not exist`
  - Ensure Gradle dependencies are resolved and Java extension reload is done in VS Code.
- `Unsupported class file major version 69`
  - Caused by old Gradle process/runtime. Use Gradle 8.4+ for Java 21 (you have 9.5.1).
- `Missing mandatory Classpath entries`
  - Usually IDE model mismatch. Refresh/reimport Gradle project and clean Java language server workspace.

## 11) File Inventory (Main Source)

- `src/main/java/Test/Launcher.java`
- `src/main/java/Test/ParticleSimulation.java`
- `src/main/java/Serenity/EngineManager.java`
- `src/main/java/Serenity/WindowManager.java`
- `src/main/java/Serenity/Camera.java`
- `src/main/java/Serenity/MouseInput.java`
- `src/main/java/Serenity/PointRenderer.java`
- `src/main/java/Serenity/ShaderManager.java`
- `src/main/java/Serenity/Util/ILogic.java`
- `src/main/java/Serenity/Util/Constants.java`
- `src/main/java/Serenity/Util/Transformation.java`
- `src/main/java/Serenity/Util/Utils.java`
- `src/main/java/particle/Particle.java`
- `src/main/java/particle/ParticleRenderStyle.java`
- `src/main/java/particle/ParticleType.java`
- `src/main/java/particle/ParticleSpawner.java`

## 12) Configuration Files

- `build.gradle`
  - Dependency and application entry configuration.
- `.gitignore`
  - Ignores Gradle/build/editor generated directories.
