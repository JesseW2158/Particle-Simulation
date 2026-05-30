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
