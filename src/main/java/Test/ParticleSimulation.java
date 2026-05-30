package Test;

import org.lwjgl.opengl.GL11;

import Serenity.Particle;
import Serenity.ParticleLoader;
import Serenity.RenderManager;
import Serenity.WindowManager;
import Serenity.Util.ILogic;

public class ParticleSimulation implements ILogic {
    private final RenderManager renderer;
    private final ParticleLoader loader;
    private final WindowManager window;

    private Particle particle;

    public ParticleSimulation() {
        this.renderer = new RenderManager();
        this.window = Launcher.getWindow();
        this.loader = new ParticleLoader();
    }

    @Override
    public void init() throws Exception {
        renderer.init();

        float[] vertices = {
                -0.5f, 0.5f, 0f,
                -0.5f, -0.5f, 0f,
                0.5f, -0.5f, 0f,
                0.5f, -0.5f, 0f,
                0.5f, 0.5f, 0f,
                -0.5f, 0.5f, 0f
        };

        int[] indices = {
                0, 1, 3,
                3, 1, 2
        };

        particle = loader.loadParticle(vertices, indices);
    }

    @Override
    public void input() {

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
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        loader.cleanup();
    }
}
