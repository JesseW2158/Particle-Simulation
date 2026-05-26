package Test;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import Serenity.RenderManager;
import Serenity.WindowManager;
import Serenity.Util.ILogic;

public class ParticleSimulation implements ILogic {
    private final RenderManager renderer;
    private final WindowManager window;

    public ParticleSimulation() {
        this.renderer = new RenderManager();
        this.window = Launcher.getWindow();
    }

    @Override
    public void init() throws Exception {
        renderer.init();
    }

    @Override
    public void input() {

    }

    @Override
    public void update() {

    }

    @Override
    public void render() {
        if(window.isResize()) {
            GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResize(true);
        }

        renderer.clear();
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
    }
}