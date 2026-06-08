# High-Scale Particle "Points of Light" Renderer — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Render up to 10,000 typed particles as glowing points of light, sized by `k·cbrt(mass)`, in a single draw call.

**Architecture:** A `particle` domain package (`ParticleType`, `Particle`, `ParticleSpawner`) holds the data; `Serenity.PointRenderer` uploads all particles into one static VBO and draws them as `GL_POINTS` with additive-blended glow shaders, reusing the existing camera's projection/view matrices.

**Tech Stack:** Java 21, LWJGL 3.4.1 (GLFW + OpenGL `GL_POINTS`, `GL_PROGRAM_POINT_SIZE`, additive blending), JOML 1.10.8, JUnit 4.12.

---

## Testing note (read first)

No Gradle wrapper / no `gradle` on PATH — this project builds and runs through the **VSCode Java extension**:
- **Unit tests (Tasks 1–2):** run via the VSCode **Testing** sidebar / "Run Test" CodeLens.
- **GL/visual behavior (Tasks 3–5):** verified by running `Test.Launcher` and looking at the window + FPS counter in the title bar.

Test sources live under `src/test/java/...`. **Commits:** the user has opted to handle commits themselves — the commit steps below are the natural boundaries; skip them if the user is not committing.

---

### Task 1: `ParticleType` + `Particle` domain model

**Files:**
- Create: `src/main/java/particle/ParticleType.java`
- Create: `src/main/java/particle/Particle.java`
- Test (create): `src/test/java/particle/ParticleTest.java`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/particle/ParticleTest.java`:

```java
package particle;

import static org.junit.Assert.assertEquals;

import org.joml.Vector3f;
import org.junit.Test;

public class ParticleTest {
    private static final float EPS = 1e-4f;

    @Test
    public void typeMassesAndCharges() {
        assertEquals(1.0, ParticleType.ELECTRON.mass(), EPS);
        assertEquals(1836.0, ParticleType.PROTON.mass(), EPS);
        assertEquals(-1, ParticleType.ELECTRON.charge());
        assertEquals(1, ParticleType.PROTON.charge());
        assertEquals(0, ParticleType.NEUTRON.charge());
    }

    @Test
    public void cbrtMassScalesWithType() {
        Particle electron = new Particle(new Vector3f(0, 0, 0), ParticleType.ELECTRON);
        Particle proton = new Particle(new Vector3f(0, 0, 0), ParticleType.PROTON);
        assertEquals(1.0f, electron.cbrtMass(), EPS);
        assertEquals(12.245f, proton.cbrtMass(), 0.005f); // cbrt(1836) ~= 12.245
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

VSCode Testing sidebar → run `ParticleTest`.
Expected: FAIL to compile / "ParticleType cannot be resolved", "Particle cannot be resolved".

- [ ] **Step 3: Create `ParticleType`**

Create `src/main/java/particle/ParticleType.java`:

```java
package particle;

import org.joml.Vector3f;

public enum ParticleType {
    ELECTRON(1.0, -1, new Vector3f(0.4f, 0.6f, 1.0f)),
    PROTON(1836.0, 1, new Vector3f(1.0f, 0.35f, 0.35f)),
    NEUTRON(1839.0, 0, new Vector3f(0.9f, 0.9f, 0.95f));

    private final double mass;
    private final int charge;
    private final Vector3f color;

    ParticleType(double mass, int charge, Vector3f color) {
        this.mass = mass;
        this.charge = charge;
        this.color = color;
    }

    public double mass() {
        return mass;
    }

    public int charge() {
        return charge;
    }

    public Vector3f color() {
        return color;
    }
}
```

- [ ] **Step 4: Create `Particle`**

Create `src/main/java/particle/Particle.java`:

```java
package particle;

import org.joml.Vector3f;

public class Particle {
    private final Vector3f position;
    private final ParticleType type;

    public Particle(Vector3f position, ParticleType type) {
        this.position = position;
        this.type = type;
    }

    public Vector3f position() {
        return position;
    }

    public ParticleType type() {
        return type;
    }

    public float cbrtMass() {
        return (float) Math.cbrt(type.mass());
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

VSCode Testing sidebar → run `ParticleTest`.
Expected: 2 tests PASS.

- [ ] **Step 6: Commit** (skip if not committing)

```bash
git add src/main/java/particle/ParticleType.java src/main/java/particle/Particle.java src/test/java/particle/ParticleTest.java
git commit -m "feat: add ParticleType and Particle domain model"
```

---

### Task 2: `ParticleSpawner`

**Files:**
- Create: `src/main/java/particle/ParticleSpawner.java`
- Test (create): `src/test/java/particle/ParticleSpawnerTest.java`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/particle/ParticleSpawnerTest.java`:

```java
package particle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class ParticleSpawnerTest {
    @Test
    public void producesRequestedCount() {
        List<Particle> particles = ParticleSpawner.randomCloud(5000, 10f, 1L);
        assertEquals(5000, particles.size());
    }

    @Test
    public void allPositionsWithinBounds() {
        float h = 10f;
        List<Particle> particles = ParticleSpawner.randomCloud(2000, h, 1L);
        for (Particle p : particles) {
            assertTrue(p.position().x >= -h && p.position().x <= h);
            assertTrue(p.position().y >= -h && p.position().y <= h);
            assertTrue(p.position().z >= -h && p.position().z <= h);
        }
    }

    @Test
    public void reproducibleForSameSeed() {
        List<Particle> a = ParticleSpawner.randomCloud(100, 10f, 42L);
        List<Particle> b = ParticleSpawner.randomCloud(100, 10f, 42L);
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i).position().x, b.get(i).position().x, 0f);
            assertEquals(a.get(i).type(), b.get(i).type());
        }
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

VSCode Testing sidebar → run `ParticleSpawnerTest`.
Expected: FAIL to compile / "ParticleSpawner cannot be resolved".

- [ ] **Step 3: Create `ParticleSpawner`**

Create `src/main/java/particle/ParticleSpawner.java`:

```java
package particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joml.Vector3f;

public class ParticleSpawner {
    private ParticleSpawner() {
    }

    public static List<Particle> randomCloud(int count, float halfExtent, long seed) {
        Random rng = new Random(seed);
        ParticleType[] types = ParticleType.values();
        List<Particle> particles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            float x = (rng.nextFloat() * 2f - 1f) * halfExtent;
            float y = (rng.nextFloat() * 2f - 1f) * halfExtent;
            float z = (rng.nextFloat() * 2f - 1f) * halfExtent;
            ParticleType type = types[rng.nextInt(types.length)];
            particles.add(new Particle(new Vector3f(x, y, z), type));
        }
        return particles;
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

VSCode Testing sidebar → run `ParticleSpawnerTest`.
Expected: 3 tests PASS.

- [ ] **Step 5: Commit** (skip if not committing)

```bash
git add src/main/java/particle/ParticleSpawner.java src/test/java/particle/ParticleSpawnerTest.java
git commit -m "feat: add seeded ParticleSpawner.randomCloud"
```

---

### Task 3: Float uniform setter + point shaders

**Files:**
- Modify: `src/main/java/Serenity/ShaderManager.java`
- Create: `src/main/resources/Shaders/point_vertex.vs`
- Create: `src/main/resources/Shaders/point_fragment.fs`

- [ ] **Step 1: Add a float-uniform setter to `ShaderManager`**

In `src/main/java/Serenity/ShaderManager.java`, add this method next to the other `setUniform` overloads:

```java
    public void setUniform(String uniformName, float value) {
        GL20.glUniform1f(uniforms.get(uniformName), value);
    }
```

(`GL20` is already imported in that file.)

- [ ] **Step 2: Create the point vertex shader**

Create `src/main/resources/Shaders/point_vertex.vs`:

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

- [ ] **Step 3: Create the point fragment shader**

Create `src/main/resources/Shaders/point_fragment.fs`:

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

- [ ] **Step 4: Verify it compiles**

VSCode shows no errors in `ShaderManager.java`. (The shaders are GLSL assets — they are loaded and compiled at runtime, validated in Task 5.)

- [ ] **Step 5: Commit** (skip if not committing)

```bash
git add src/main/java/Serenity/ShaderManager.java src/main/resources/Shaders/point_vertex.vs src/main/resources/Shaders/point_fragment.fs
git commit -m "feat: add float uniform setter and point-of-light shaders"
```

---

### Task 4: `PointRenderer`

**Files:**
- Create: `src/main/java/Serenity/PointRenderer.java`

- [ ] **Step 1: Create `PointRenderer`**

Create `src/main/java/Serenity/PointRenderer.java`:

```java
package Serenity;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import Serenity.Util.Transformation;
import Serenity.Util.Utils;
import Test.Launcher;
import particle.Particle;

public class PointRenderer {
    public static final float K = 25f;

    private final WindowManager window;
    private final Transformation transformation;
    private final List<Integer> vbos = new ArrayList<>();

    private ShaderManager shader;
    private int vaoId;
    private int count;

    public PointRenderer() {
        this.window = Launcher.getWindow();
        this.transformation = new Transformation();
    }

    public void init() throws Exception {
        shader = new ShaderManager();
        shader.createVertexShader(Utils.loadResource("Shaders/point_vertex.vs"));
        shader.createFragmentShader(Utils.loadResource("Shaders/point_fragment.fs"));
        shader.link();
        shader.createUniform("projectionMatrix");
        shader.createUniform("viewMatrix");
        shader.createUniform("k");

        GL11.glEnable(GL32.GL_PROGRAM_POINT_SIZE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
    }

    public void buildBuffer(List<Particle> particles) {
        count = particles.size();
        float[] positions = new float[count * 3];
        float[] sizes = new float[count];
        float[] colors = new float[count * 3];

        for (int i = 0; i < count; i++) {
            Particle p = particles.get(i);
            positions[i * 3] = p.position().x;
            positions[i * 3 + 1] = p.position().y;
            positions[i * 3 + 2] = p.position().z;
            sizes[i] = p.cbrtMass();
            colors[i * 3] = p.type().color().x;
            colors[i * 3 + 1] = p.type().color().y;
            colors[i * 3 + 2] = p.type().color().z;
        }

        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);
        storeAttribute(0, 3, positions);
        storeAttribute(1, 1, sizes);
        storeAttribute(2, 3, colors);
        GL30.glBindVertexArray(0);
    }

    private void storeAttribute(int location, int size, float[] data) {
        int vbo = GL15.glGenBuffers();
        vbos.add(vbo);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer buffer = Utils.storeDataInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(location, size, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void render(Camera camera) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDepthMask(false);

        shader.bind();
        shader.setUniform("projectionMatrix", window.updateProjectionMatrix());
        shader.setUniform("viewMatrix", transformation.getViewMatrix(camera));
        shader.setUniform("k", K);

        GL30.glBindVertexArray(vaoId);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL11.glDrawArrays(GL11.GL_POINTS, 0, count);
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
        shader.unbind();

        GL11.glDepthMask(true);
    }

    public void cleanup() {
        if (shader != null) {
            shader.cleanup();
        }
        GL30.glDeleteVertexArrays(vaoId);
        for (int vbo : vbos) {
            GL15.glDeleteBuffers(vbo);
        }
    }
}
```

- [ ] **Step 2: Verify it compiles**

VSCode shows no errors in `PointRenderer.java`. (No runtime behavior yet — wired up in Task 5.)

- [ ] **Step 3: Commit** (skip if not committing)

```bash
git add src/main/java/Serenity/PointRenderer.java
git commit -m "feat: add PointRenderer (single-draw-call point cloud)"
```

---

### Task 5: Wire into `ParticleSimulation` + bump count, then run

**Files:**
- Modify: `src/main/java/Serenity/Util/Constants.java`
- Modify: `src/main/java/Test/ParticleSimulation.java`

- [ ] **Step 1: Bump the particle count**

In `src/main/java/Serenity/Util/Constants.java`, change:

```java
    public static final int RENDERED_PARTICLES = 1000;
```

to:

```java
    public static final int RENDERED_PARTICLES = 10000;
```

- [ ] **Step 2: Replace `ParticleSimulation` with the point-cloud version**

Replace the entire contents of `src/main/java/Test/ParticleSimulation.java` with:

```java
package Test;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import Serenity.Camera;
import Serenity.MouseInput;
import Serenity.PointRenderer;
import Serenity.WindowManager;
import Serenity.Util.Constants;
import Serenity.Util.ILogic;
import particle.Particle;
import particle.ParticleSpawner;

public class ParticleSimulation implements ILogic {
    private final PointRenderer pointRenderer;
    private final WindowManager window;

    private List<Particle> particles;
    private final Camera camera = new Camera(new Vector3f(0, 0, 30), new Vector3f(0, 0, 0));
    private final MouseInput mouseInput = new MouseInput();
    private long lastFrameTime = System.nanoTime();

    public ParticleSimulation() {
        this.pointRenderer = new PointRenderer();
        this.window = Launcher.getWindow();
    }

    @Override
    public void init() throws Exception {
        particles = ParticleSpawner.randomCloud(Constants.RENDERED_PARTICLES, 10f, 1L);

        pointRenderer.init();
        pointRenderer.buildBuffer(particles);

        mouseInput.init(window);
    }

    @Override
    public void input() {
        mouseInput.input();

        if (mouseInput.isLeftButtonPressed()) {
            Vector2f displ = mouseInput.getDisplVec();
            camera.look(displ.x * Constants.MOUSE_SENSITIVITY, displ.y * Constants.MOUSE_SENSITIVITY);
        }

        double scroll = mouseInput.getScrollDelta();
        if (scroll != 0) {
            camera.moveForward((float) (scroll * Constants.ZOOM_SPEED));
            mouseInput.resetScroll();
        }

        long now = System.nanoTime();
        float deltaSeconds = (float) ((now - lastFrameTime) / 1_000_000_000.0);
        lastFrameTime = now;
        float step = Constants.CAMERA_MOVE_SPEED * deltaSeconds;

        if (window.isKeyPressed(GLFW.GLFW_KEY_W)) {
            camera.moveForward(step);
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_S)) {
            camera.moveForward(-step);
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_D)) {
            camera.movePosition(step, 0, 0);
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_A)) {
            camera.movePosition(-step, 0, 0);
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_E)) {
            camera.movePosition(0, step, 0);
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_Q)) {
            camera.movePosition(0, -step, 0);
        }
    }

    @Override
    public void update() {
    }

    @Override
    public void render() {
        if (window.isResize()) {
            GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResize(false);
        }

        window.setClearColor(0, 0, 0, 1);
        pointRenderer.render(camera);
    }

    @Override
    public void cleanup() {
        pointRenderer.cleanup();
    }
}
```

- [ ] **Step 3: Build and run to verify the cloud**

Run `Test.Launcher` from VSCode and confirm:
- A cloud of glowing points appears on a **black** background — red (protons), white (neutrons), blue (electrons), with protons/neutrons clearly larger than electrons.
- Hold left-click + drag to look around; WASD/QE to fly; scroll to zoom — you can move through the cloud.
- The FPS counter in the title bar stays high (no major lag) with 10,000 particles.

If the points look too big/small, adjust `PointRenderer.K`. If too dim/bright, tweak the glow in `point_fragment.fs`. If you see square points instead of round, the `discard` in the fragment shader isn't running — check the shader compiled (console will print a shader error on launch).

- [ ] **Step 4: Commit** (skip if not committing)

```bash
git add src/main/java/Serenity/Util/Constants.java src/main/java/Test/ParticleSimulation.java
git commit -m "feat: render 10k particles as points of light via PointRenderer"
```

---

## Self-Review

**Spec coverage:**
- Up to 10k particles, one draw call, no lag → Task 4 (`PointRenderer`, single `glDrawArrays`) + Task 5 (count 10000, FPS check). ✓
- Size = `k·cbrt(mass)`, `k` a tunable renderer constant → Task 1 (`cbrtMass`), Task 3 (`k` uniform in vertex shader), Task 4 (`PointRenderer.K`). ✓
- No radius / points of light → Tasks 3–4 (`GL_POINTS` + glow fragment shader, additive blend). ✓
- Typed particles (proton/neutron/electron), real relative masses, charge, color → Task 1 (`ParticleType`). ✓
- Color by type → Task 1 colors + Task 3/4 color attribute passed to fragment. ✓
- Black background → Task 5 (`setClearColor(0,0,0,1)`). ✓
- Demo spawning → Task 2 (`ParticleSpawner`), Task 5 (`randomCloud(..., 10f, 1L)`). ✓
- Reuse camera matrices → Task 4 (`updateProjectionMatrix`, `getViewMatrix`). ✓
- Unit tests for data → Tasks 1–2. ✓
- Retire single-quad path → Task 5 (ParticleSimulation no longer references `RenderManager`/`ParticleLoader`/quad). ✓

**Placeholder scan:** No TBD/TODO; every code step is complete. `cbrt(1836)≈12.245` is verified, not a placeholder.

**Type consistency:**
- `ParticleType.mass()` returns `double`, `charge()` `int`, `color()` `Vector3f` — used consistently in `Particle`, `PointRenderer.buildBuffer`, and tests. ✓
- `Particle.cbrtMass()` returns `float` — stored into the `sizes` float array. ✓
- `ParticleSpawner.randomCloud(int, float, long)` — signature matches all call sites (tests + `ParticleSimulation.init`). ✓
- `PointRenderer.render(Camera)` — matches the call in `ParticleSimulation.render`. ✓
- `ShaderManager.setUniform(String, float)` (new, Task 3) — used by `PointRenderer` for `"k"`; `setUniform(String, Matrix4f)` (existing) used for the two matrices. No overload ambiguity (`K` is `float`). ✓
- `Constants.RENDERED_PARTICLES` (Task 5) — used in `ParticleSimulation.init`. ✓
- `particle.Particle` (new) vs `Serenity.Particle` (legacy) — `PointRenderer` and `ParticleSimulation` import `particle.Particle`; no `Serenity.Particle` import remains in `ParticleSimulation`, so no clash. ✓
