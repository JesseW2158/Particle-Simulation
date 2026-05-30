package Test;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import Serenity.Camera;
import Serenity.MouseInput;
import Serenity.Particle;
import Serenity.ParticleLoader;
import Serenity.RenderManager;
import Serenity.WindowManager;
import Serenity.Util.Constants;
import Serenity.Util.ILogic;

public class ParticleSimulation implements ILogic {
    private final RenderManager renderer;
    private final ParticleLoader loader;
    private final WindowManager window;

    private Particle particle;
    private final Camera camera = new Camera(new Vector3f(0, 0, 2), new Vector3f(0, 0, 0));
    private final MouseInput mouseInput = new MouseInput();
    private long lastFrameTime = System.nanoTime();

    public ParticleSimulation() {
        this.renderer = new RenderManager();
        this.window = Launcher.getWindow();
        this.loader = new ParticleLoader();
    }

    @Override
    public void init() throws Exception {
        renderer.init();

        float[] vertices = {
                -0.5f, 0.5f, 0f,   // 0 top-left
                -0.5f, -0.5f, 0f,  // 1 bottom-left
                0.5f, -0.5f, 0f,   // 2 bottom-right
                0.5f, 0.5f, 0f     // 3 top-right
        };

        int[] indices = {
                0, 1, 3,
                3, 1, 2
        };

        particle = loader.loadParticle(vertices, indices);

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

        window.setClearColor(1, 1, 1, 0);
        renderer.render(particle, camera);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        loader.cleanup();
    }
}
