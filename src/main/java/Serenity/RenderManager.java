package Serenity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import Test.Launcher;

public class RenderManager {
    private final WindowManager window;

    public RenderManager() {
        this.window = Launcher.getWindow();
    }

    public void init() throws Exception {
        
    }

    public void render(Particle particle) {
        clear();
        GL30.glBindVertexArray(particle.getId());
        GL20.glEnableVertexAttribArray(0);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, particle.getVertexCount());
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
    }

    public void clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void cleanup() {

    }
}
