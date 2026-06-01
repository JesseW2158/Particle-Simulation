package Test;

import java.util.Arrays;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import Serenity.Camera;
import Serenity.MouseInput;
import Serenity.PointRenderer;
import Serenity.Util.Constants;
import Serenity.Util.ILogic;
import Serenity.Util.SimulationSettings;
import Serenity.WindowManager;
import particle.Particle;
import particle.ParticleDefinition;
import particle.ParticleElectrostatics;
import particle.ParticleGravity;
import particle.ParticleSpawner;
import particle.ParticleStrongForce;
import particle.ParticleWeakForce;

public class ParticleSimulation implements ILogic {
    private final PointRenderer pointRenderer;
    private final WindowManager window;
    private final SimulationSettings settings;
    private final List<ParticleDefinition> particleDefinitions;

    private List<Particle> particles;
    private final Camera camera = new Camera(new Vector3f(0, 0, 30), new Vector3f(0, 0, 0));
    private final MouseInput mouseInput = new MouseInput();
    private long lastFrameTime = System.nanoTime();
    private long lastUpdateTime = System.nanoTime();
    private float updateAccumulator;

    public ParticleSimulation(SimulationSettings settings, List<ParticleDefinition> particleDefinitions) {
        this.settings = settings;
        this.particleDefinitions = particleDefinitions;
        this.pointRenderer = new PointRenderer();
        this.window = Launcher.getWindow();
    }

    @Override
    public void init() throws Exception {
        particles = ParticleSpawner.randomCloud(settings.particleCount(), settings.spawnHalfExtent(), 1L,
            particleDefinitions);

        pointRenderer.init();
        pointRenderer.buildBuffer(particles);
        pointRenderer.updatePositions(particles);

        mouseInput.init(window);
        lastUpdateTime = System.nanoTime();
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
        long now = System.nanoTime();
        float frameDelta = (float) ((now - lastUpdateTime) / 1_000_000_000.0);
        lastUpdateTime = now;

        frameDelta = Math.min(frameDelta, 0.25f);
        updateAccumulator += frameDelta * settings.simulationSpeed();

        float fixedStep = settings.fixedPhysicsStepSeconds();
        int count = particles.size();
        float[] ax = new float[count];
        float[] ay = new float[count];
        float[] az = new float[count];

        while (updateAccumulator >= fixedStep) {
            Arrays.fill(ax, 0f);
            Arrays.fill(ay, 0f);
            Arrays.fill(az, 0f);

            ParticleGravity.accumulateAccelerations(particles, ax, ay, az, settings.metersPerWorldUnit(),
                settings.softeningLength(), settings.gravityMultiplier());
            ParticleElectrostatics.accumulateAccelerations(particles, ax, ay, az, settings.metersPerWorldUnit(),
                settings.softeningLength(), settings.electrostaticMultiplier());

            if (settings.strongForceEnabled()) {
                ParticleStrongForce.accumulateAccelerations(particles, ax, ay, az, settings.metersPerWorldUnit(),
                        settings.softeningLength(), settings.strongAttractionMultiplier(),
                        settings.strongRepulsionMultiplier(), settings.strongRangeMultiplier());
            }

            if (settings.weakForceEnabled()) {
                ParticleWeakForce.accumulateAccelerations(particles, ax, ay, az, settings.metersPerWorldUnit(),
                        settings.softeningLength(), settings.weakMultiplier(), settings.weakRangeMultiplier());
            }

            for (int i = 0; i < count; i++) {
            particles.get(i).integrate(ax[i], ay[i], az[i], fixedStep);
            particles.get(i).bounceWithinCube(settings.boundaryHalfExtent());
            }

            updateAccumulator -= fixedStep;
        }

        pointRenderer.updatePositions(particles);
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
