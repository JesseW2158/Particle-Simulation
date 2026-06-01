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
    public static final float K = 65f;

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
        shader.createUniform("renderPass");

        GL11.glEnable(GL32.GL_PROGRAM_POINT_SIZE);
    }

    public void buildBuffer(List<Particle> particles) {
        count = particles.size();
        float[] positions = new float[count * 3];
        float[] sizes = new float[count];
        float[] colors = new float[count * 3];
        float[] glowParams = new float[count * 2];

        for (int i = 0; i < count; i++) {
            Particle p = particles.get(i);
            positions[i * 3] = p.position().x;
            positions[i * 3 + 1] = p.position().y;
            positions[i * 3 + 2] = p.position().z;
            sizes[i] = p.renderRadius();
            colors[i * 3] = p.type().color().x;
            colors[i * 3 + 1] = p.type().color().y;
            colors[i * 3 + 2] = p.type().color().z;
            glowParams[i * 2] = p.glowStrength();
            glowParams[i * 2 + 1] = p.glowRadiusScale();
        }

        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);
        storeAttribute(0, 3, positions);
        storeAttribute(1, 1, sizes);
        storeAttribute(2, 3, colors);
        storeAttribute(3, 2, glowParams);
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

        shader.bind();
        shader.setUniform("projectionMatrix", window.updateProjectionMatrix());
        shader.setUniform("viewMatrix", transformation.getViewMatrix(camera));
        shader.setUniform("k", K);

        GL30.glBindVertexArray(vaoId);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glEnableVertexAttribArray(3);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        shader.setUniform("renderPass", 0);
        GL11.glDrawArrays(GL11.GL_POINTS, 0, count);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDepthMask(false);
        shader.setUniform("renderPass", 1);
        GL11.glDrawArrays(GL11.GL_POINTS, 0, count);

        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL20.glDisableVertexAttribArray(3);
        GL30.glBindVertexArray(0);
        shader.unbind();

        GL11.glDisable(GL11.GL_BLEND);
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
