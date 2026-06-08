# Free-Fly Mouse Camera Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a free-fly camera so holding left-click + dragging rotates the view and the scroll wheel zooms, by building the model→view→projection pipeline the renderer currently lacks.

**Architecture:** Four isolated units — (1) `Transformation` builds view/world matrices, (2) `Camera` holds pose and applies clamped look + dolly, (3) `MouseInput` owns GLFW mouse callbacks, (4) `ParticleSimulation` wires mouse → camera → renderer each frame. The vertex shader gains projection/view/world uniforms; `RenderManager` sets them per frame.

**Tech Stack:** Java 21, LWJGL 3.4.1 (GLFW + OpenGL), JOML 1.10.8, JUnit 4.12.

---

## Testing note (read first)

This project has **no Gradle wrapper and no `gradle` on PATH** — it builds/runs through the **VSCode Java extension**. So:

- **Unit tests (Tasks 1–2):** run via the VSCode **Testing** sidebar (or the "Run Test" CodeLens above each `@Test`). If a `gradle` binary is later available, the equivalent is `gradle test --tests "<FQCN>"`.
- **GL/GLFW behavior (Tasks 3–6):** verified by **running `Test.Launcher`** from VSCode and observing the window. There is no headless way to assert this.

JUnit 4.12 is already declared in [build.gradle](../../../build.gradle); test sources go under `src/test/java/...` (created in Task 1).

**Branch/commit:** confirm with the user whether to work on a branch before starting (they currently develop on `main`). Commit steps below assume a working branch is in place.

---

### Task 1: `Transformation` view & world matrices

**Files:**
- Modify: `src/main/java/Serenity/Util/Transformation.java` (currently an empty class)
- Test (create): `src/test/java/Serenity/Util/TransformationTest.java`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/Serenity/Util/TransformationTest.java`:

```java
package Serenity.Util;

import static org.junit.Assert.assertEquals;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.Test;

import Serenity.Camera;
import Serenity.Particle;
import Serenity.Vector;

public class TransformationTest {
    private static final float EPS = 1e-5f;

    @Test
    public void viewMatrixAtOriginIsIdentity() {
        Camera camera = new Camera(); // position (0,0,0), rotation (0,0,0)
        Matrix4f view = new Transformation().getViewMatrix(camera);
        Vector4f p = new Vector4f(1f, 2f, 3f, 1f);
        view.transform(p);
        assertEquals(1f, p.x, EPS);
        assertEquals(2f, p.y, EPS);
        assertEquals(3f, p.z, EPS);
    }

    @Test
    public void viewMatrixTranslatesByNegativeCameraPosition() {
        Camera camera = new Camera(new Vector3f(0f, 0f, 2f), new Vector3f(0f, 0f, 0f));
        Matrix4f view = new Transformation().getViewMatrix(camera);
        Vector4f p = new Vector4f(0f, 0f, 0f, 1f);
        view.transform(p); // world origin -> view space
        assertEquals(0f, p.x, EPS);
        assertEquals(0f, p.y, EPS);
        assertEquals(-2f, p.z, EPS);
    }

    @Test
    public void worldMatrixTranslatesToParticlePosition() {
        Particle particle = new Particle(0, 6, 1d, 1d, 1d, 1d, 1, new Vector(1d, 2d, 3d));
        Matrix4f world = new Transformation().getWorldMatrix(particle);
        Vector4f p = new Vector4f(0f, 0f, 0f, 1f);
        world.transform(p);
        assertEquals(1f, p.x, EPS);
        assertEquals(2f, p.y, EPS);
        assertEquals(3f, p.z, EPS);
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

VSCode Testing sidebar → run `TransformationTest`.
Expected: FAIL to compile / "method getViewMatrix(...) is undefined for type Transformation".

- [ ] **Step 3: Implement `Transformation`**

Replace the contents of `src/main/java/Serenity/Util/Transformation.java`:

```java
package Serenity.Util;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import Serenity.Camera;
import Serenity.Particle;

public class Transformation {
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f worldMatrix = new Matrix4f();

    public Matrix4f getViewMatrix(Camera camera) {
        Vector3f pos = camera.getPosition();
        Vector3f rot = camera.getRotation();
        viewMatrix.identity()
                .rotate((float) Math.toRadians(rot.x), 1f, 0f, 0f)
                .rotate((float) Math.toRadians(rot.y), 0f, 1f, 0f)
                .translate(-pos.x, -pos.y, -pos.z);
        return viewMatrix;
    }

    public Matrix4f getWorldMatrix(Particle particle) {
        float x = (float) particle.position().x();
        float y = (float) particle.position().y();
        float z = (float) particle.position().z();
        worldMatrix.identity()
                .translate(x, y, z)
                .scale(1f);
        return worldMatrix;
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

VSCode Testing sidebar → run `TransformationTest`.
Expected: 3 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/Serenity/Util/Transformation.java src/test/java/Serenity/Util/TransformationTest.java
git commit -m "feat: add Transformation view/world matrices with tests"
```

---

### Task 2: `Camera` dolly + clamped look

**Files:**
- Modify: `src/main/java/Serenity/Camera.java`
- Test (create): `src/test/java/Serenity/CameraTest.java`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/Serenity/CameraTest.java`:

```java
package Serenity;

import static org.junit.Assert.assertEquals;

import org.joml.Vector3f;
import org.junit.Test;

public class CameraTest {
    private static final float EPS = 1e-4f;

    @Test
    public void moveForwardAtZeroPoseMovesAlongNegativeZ() {
        Camera camera = new Camera(new Vector3f(0f, 0f, 2f), new Vector3f(0f, 0f, 0f));
        camera.moveForward(1f);
        assertEquals(0f, camera.getPosition().x, EPS);
        assertEquals(0f, camera.getPosition().y, EPS);
        assertEquals(1f, camera.getPosition().z, EPS);
    }

    @Test
    public void lookClampsPitch() {
        Camera camera = new Camera();
        camera.look(200f, 0f);
        assertEquals(89f, camera.getRotation().x, EPS);
        camera.look(-1000f, 0f);
        assertEquals(-89f, camera.getRotation().x, EPS);
    }

    @Test
    public void lookWrapsYawWithin360() {
        Camera camera = new Camera();
        camera.look(0f, 400f);
        assertEquals(40f, camera.getRotation().y, EPS);
        camera.look(0f, -80f);
        assertEquals(320f, camera.getRotation().y, EPS);
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

VSCode Testing sidebar → run `CameraTest`.
Expected: FAIL to compile / "method moveForward(float) is undefined" and "method look(float, float) is undefined".

- [ ] **Step 3: Add the two methods to `Camera`**

In `src/main/java/Serenity/Camera.java`, add these methods inside the class (e.g. after `moveRotation`):

```java
    public void moveForward(float amount) {
        double yaw = Math.toRadians(rotation.y);
        double pitch = Math.toRadians(rotation.x);
        position.x += (float) (Math.sin(yaw) * Math.cos(pitch)) * amount;
        position.y += (float) (-Math.sin(pitch)) * amount;
        position.z += (float) (-Math.cos(yaw) * Math.cos(pitch)) * amount;
    }

    public void look(float deltaPitch, float deltaYaw) {
        rotation.x += deltaPitch;
        if (rotation.x > 89f) {
            rotation.x = 89f;
        } else if (rotation.x < -89f) {
            rotation.x = -89f;
        }

        rotation.y = (rotation.y + deltaYaw) % 360f;
        if (rotation.y < 0f) {
            rotation.y += 360f;
        }
    }
```

- [ ] **Step 4: Run the tests to verify they pass**

VSCode Testing sidebar → run `CameraTest`.
Expected: 3 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/Serenity/Camera.java src/test/java/Serenity/CameraTest.java
git commit -m "feat: add Camera moveForward (dolly) and clamped look"
```

---

### Task 3: MVP pipeline in shader + renderer (static camera renders the quad)

After this task the existing quad renders through a fixed-pose camera in perspective. No mouse yet.

**Files:**
- Modify: `src/main/resources/Shaders/vertex.vs`
- Modify: `src/main/java/Serenity/RenderManager.java`
- Modify: `src/main/java/Serenity/WindowManager.java`
- Modify: `src/main/java/Test/ParticleSimulation.java`

- [ ] **Step 1: Add uniforms to the vertex shader**

Replace `src/main/resources/Shaders/vertex.vs` with:

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

- [ ] **Step 2: Create uniforms and set them in `RenderManager`**

Replace `src/main/java/Serenity/RenderManager.java` with:

```java
package Serenity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import Serenity.Util.Transformation;
import Serenity.Util.Utils;
import Test.Launcher;

public class RenderManager {
    private final WindowManager window;
    private final Transformation transformation;
    private ShaderManager shader;

    public RenderManager() {
        this.window = Launcher.getWindow();
        this.transformation = new Transformation();
    }

    public void init() throws Exception {
        shader = new ShaderManager();
        shader.createVertexShader(Utils.loadResource("Shaders/vertex.vs"));
        shader.createFragmentShader(Utils.loadResource("Shaders/fragment.fs"));
        shader.link();
        shader.createUniform("projectionMatrix");
        shader.createUniform("viewMatrix");
        shader.createUniform("worldMatrix");
    }

    public void render(Particle particle, Camera camera) {
        clear();
        shader.bind();
        shader.setUniform("projectionMatrix", window.updateProjectionMatrix());
        shader.setUniform("viewMatrix", transformation.getViewMatrix(camera));
        shader.setUniform("worldMatrix", transformation.getWorldMatrix(particle));
        GL30.glBindVertexArray(particle.getVaoId());
        GL20.glEnableVertexAttribArray(0);
        GL11.glDrawElements(GL11.GL_TRIANGLES, particle.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        shader.unbind();
    }

    public void clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void cleanup() {
        shader.cleanup();
    }
}
```

- [ ] **Step 3: Disable culling and guard the projection in `WindowManager`**

In `src/main/java/Serenity/WindowManager.java`, in `init()`, delete these two lines:

```java
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
```

Then replace the `updateProjectionMatrix()` method with:

```java
    public Matrix4f updateProjectionMatrix() {
        if (height == 0) {
            return projectionMatrix;
        }
        float aspectRatio = (float) width / height;
        return projectionMatrix.setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
    }
```

- [ ] **Step 4: Give `ParticleSimulation` a camera and draw through it**

In `src/main/java/Test/ParticleSimulation.java`:

Add imports near the top (after the existing imports):

```java
import org.joml.Vector3f;

import Serenity.Camera;
```

Add the camera field (next to the other fields):

```java
    private final Camera camera = new Camera(new Vector3f(0, 0, 2), new Vector3f(0, 0, 0));
```

Replace the `render()` method body's draw call so it reads:

```java
    @Override
    public void render() {
        if (window.isResize()) {
            GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResize(false);
        }

        window.setClearColor(1, 1, 1, 0);
        renderer.render(particle, camera);
    }
```

- [ ] **Step 5: Build and run to verify the quad renders in perspective**

Run `Test.Launcher` from VSCode.
Expected: a coloured quad is visible on the white background, centered, now in a perspective projection (a few units in front of the camera). It stays visible (per the earlier render-loop fix). No mouse interaction yet.

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/Shaders/vertex.vs src/main/java/Serenity/RenderManager.java src/main/java/Serenity/WindowManager.java src/main/java/Test/ParticleSimulation.java
git commit -m "feat: render quad through projection/view/world pipeline"
```

---

### Task 4: `MouseInput` (GLFW mouse callbacks)

**Files:**
- Create: `src/main/java/Serenity/MouseInput.java`

- [ ] **Step 1: Create the `MouseInput` class**

Create `src/main/java/Serenity/MouseInput.java`:

```java
package Serenity;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class MouseInput {
    private final Vector2d previousPos = new Vector2d(-1, -1);
    private final Vector2d currentPos = new Vector2d(0, 0);
    private final Vector2f displVec = new Vector2f();

    private boolean inWindow = false;
    private boolean leftButtonPressed = false;
    private double scrollDelta = 0;

    public void init(WindowManager window) {
        long handle = window.getWindowHandle();

        GLFW.glfwSetCursorPosCallback(handle, (win, xpos, ypos) -> {
            currentPos.x = xpos;
            currentPos.y = ypos;
        });

        GLFW.glfwSetCursorEnterCallback(handle, (win, entered) -> inWindow = entered);

        GLFW.glfwSetMouseButtonCallback(handle, (win, button, action, mods) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                leftButtonPressed = (action == GLFW.GLFW_PRESS);
            }
        });

        GLFW.glfwSetScrollCallback(handle, (win, xoffset, yoffset) -> scrollDelta += yoffset);
    }

    public void input() {
        displVec.set(0, 0);
        if (previousPos.x >= 0 && previousPos.y >= 0 && inWindow) {
            double dx = currentPos.x - previousPos.x;
            double dy = currentPos.y - previousPos.y;
            displVec.x = (float) dy; // vertical mouse movement -> pitch
            displVec.y = (float) dx; // horizontal mouse movement -> yaw
        }
        previousPos.set(currentPos.x, currentPos.y);
    }

    public Vector2f getDisplVec() {
        return displVec;
    }

    public boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    public double getScrollDelta() {
        return scrollDelta;
    }

    public void resetScroll() {
        scrollDelta = 0;
    }
}
```

- [ ] **Step 2: Verify it compiles**

VSCode shows no errors in `MouseInput.java` (the IDE compiles on save). No runtime behavior to test yet — it is wired up in Task 5.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/Serenity/MouseInput.java
git commit -m "feat: add MouseInput for cursor, button, and scroll state"
```

---

### Task 5: Wire mouse → camera (look + zoom)

**Files:**
- Modify: `src/main/java/Serenity/Util/Constants.java`
- Modify: `src/main/java/Test/ParticleSimulation.java`

- [ ] **Step 1: Add sensitivity constants**

In `src/main/java/Serenity/Util/Constants.java`, add inside the class:

```java
    public static final float MOUSE_SENSITIVITY = 0.1f;
    public static final float ZOOM_SPEED = 0.4f;
```

- [ ] **Step 2: Add the `MouseInput`, init it, and drive the camera**

In `src/main/java/Test/ParticleSimulation.java`:

Add imports (after the existing imports):

```java
import org.joml.Vector2f;

import Serenity.MouseInput;
import Serenity.Util.Constants;
```

Add the field (next to the `camera` field):

```java
    private final MouseInput mouseInput = new MouseInput();
```

At the end of the `init()` method, after the particle is loaded, add:

```java
        mouseInput.init(window);
```

Replace the `input()` method with:

```java
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
    }
```

- [ ] **Step 3: Build and run to verify camera control**

Run `Test.Launcher` from VSCode and confirm:
- Hold left-click + drag → view rotates; release → rotation stops.
- Drag horizontally all the way around → yaw keeps turning (360° wrap).
- Drag up/down → pitch stops short of straight up/down (no flip).
- Scroll up → quad grows (camera moves toward it); scroll down → shrinks.

If drag direction feels inverted, flip the sign on the relevant axis: in `MouseInput.input()` negate `displVec.x` (pitch) and/or `displVec.y` (yaw); for zoom, negate the `moveForward` amount.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/Serenity/Util/Constants.java src/main/java/Test/ParticleSimulation.java
git commit -m "feat: drive free-fly camera from mouse look and scroll zoom"
```

---

### Task 6: Full manual verification pass

**Files:** none (verification only)

- [ ] **Step 1: Run `Test.Launcher` and walk the checklist**

- [ ] Quad is visible on launch.
- [ ] Hold left-click + drag left/right → smooth horizontal rotation, wraps fully around.
- [ ] Hold left-click + drag up/down → vertical look, clamps near vertical (never upside-down).
- [ ] Releasing left-click stops camera movement; moving the mouse without holding does nothing.
- [ ] Scroll wheel zooms in and out along the look direction.
- [ ] Maximize the window, then restore it → quad stays correctly proportioned (not stretched) and visible throughout.
- [ ] Press ESC → window closes cleanly (existing behavior still works).

- [ ] **Step 2: If everything passes, finalize**

No code change. If sign/sensitivity tweaks were needed during Task 5, ensure they are committed.

---

## Self-Review

**Spec coverage:**
- Free-fly look while left button held → Task 5 Step 2 (`isLeftButtonPressed` gates `camera.look`). ✓
- Yaw 360° wrap, pitch ±89° clamp → Task 2 `look()` + test. ✓
- Dolly zoom on scroll → Task 2 `moveForward()` + Task 5 scroll handling. ✓
- MVP pipeline (shader uniforms, view/world/projection) → Task 1 + Task 3. ✓
- Back-face culling disabled → Task 3 Step 3. ✓
- Projection recompute / aspect on resize + height-0 guard → Task 3 (`updateProjectionMatrix` called per frame, guarded). ✓
- MouseInput callbacks (cursor/button/scroll/enter), first-frame no-jump → Task 4 (`previousPos` starts at -1). ✓
- Constants for sensitivity/zoom → Task 5 Step 1. ✓
- WASD/multi-particle out of scope → not included. ✓
- Unit tests for matrices + camera math → Tasks 1–2. ✓

**Placeholder scan:** No TBD/TODO; every code step shows complete code. Sign-tuning in Task 5 Step 3 is a concrete, reversible run-time adjustment, not a gap.

**Type consistency:**
- `Camera.getPosition()/getRotation()` return `org.joml.Vector3f` (existing) — used in `Transformation` and tests. ✓
- `Particle(int vaoId, int vertexCount, double, double, double, double, int, Vector)` — matches the current constructor; used in `TransformationTest`. ✓
- `Particle.position()` returns `Serenity.Vector` with `x()/y()/z()` — used in `getWorldMatrix`. ✓
- `RenderManager.render(Particle, Camera)` — new signature; the only caller (`ParticleSimulation.render`) is updated in Task 3 Step 4. ✓
- `MouseInput` methods (`getDisplVec`, `isLeftButtonPressed`, `getScrollDelta`, `resetScroll`) — defined in Task 4, consumed in Task 5. ✓
- `Constants.MOUSE_SENSITIVITY` / `Constants.ZOOM_SPEED` — defined Task 5 Step 1, used Step 2. ✓
