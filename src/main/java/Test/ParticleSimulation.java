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
