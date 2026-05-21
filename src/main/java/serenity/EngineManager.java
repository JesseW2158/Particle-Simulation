package serenity;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

public class EngineManager {
    public static final long NANOSECONDS_IN_SECOND = 1_000_000_000L;
    public static final float FRAMERATE = 1000;

    private static int fps;
    private static float frametime = 1.0f / FRAMERATE;

    private boolean isRunning;

    private WindowManager window;
    private GLFWErrorCallback errorCallback;

    private void init() {
        GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        window = ProjectSimulation
        window.init();
    }

}
