package Serenity;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import Serenity.Util.ILogic;
import Test.Launcher;

public class EngineManager {
    public static final long NANOSECONDS_IN_SECOND = 1_000_000_000L;
    public static final float FRAMERATE = 1000;

    private static int fps;
    private static float frametime = 1.0f / FRAMERATE;

    private boolean isRunning;

    private WindowManager window;
    private GLFWErrorCallback errorCallback;
    private ILogic gameLogic;

    private void init() throws Exception {
        GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

        window = Launcher.getWindow();
        gameLogic = Launcher.getSimulation();

        window.init();
        gameLogic.init();
    }

    public void start() throws Exception {
        init();

        if (isRunning) {
            return;
        }

        run();
    }

    public void run() {
        this.isRunning = true;
        int frames = 0;
        long frameCounter = 0;
        long lastTime = System.nanoTime();
        double unprocessedTime = 0;

        while (isRunning) {
            boolean render = false;
            long startTime = System.nanoTime();
            long passedTime = startTime - lastTime;
            lastTime = startTime;

            unprocessedTime += passedTime / (double) NANOSECONDS_IN_SECOND;
            frameCounter += passedTime;

            input();

            while (unprocessedTime > frametime) {
                render = true;
                unprocessedTime -= frametime;

                if (window.windowShouldClose()) {
                    stop();
                }

                if (frameCounter >= NANOSECONDS_IN_SECOND) {
                    setFps(frames);
                    window.setTitle("Particle Simulation | " + getFps() + " FPS");
                    frames = 0;
                    frameCounter = 0;
                }
            }

            if (render) {
                update();
                render();
                frames++;
            }
        }

        cleanup();
    }

    private void stop() {
        if (!isRunning) {
            return;
        }

        isRunning = false;
    }

    private void input() {
        gameLogic.input();
    }

    private void render() {
        gameLogic.render();
        window.update();
    }

    private void update() {
        gameLogic.update();
    }

    private void cleanup() {
        window.cleanup();
        gameLogic.cleanup();
        errorCallback.free();
        GLFW.glfwTerminate();
    }

    public static long getNanosecondsInSecond() {
        return NANOSECONDS_IN_SECOND;
    }

    public static float getFramerate() {
        return FRAMERATE;
    }

    public static int getFps() {
        return fps;
    }

    public static void setFps(int fps) {
        EngineManager.fps = fps;
    }

    public static float getFrametime() {
        return frametime;
    }

    public static void setFrametime(float frametime) {
        EngineManager.frametime = frametime;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public WindowManager getWindow() {
        return window;
    }

    public void setWindow(WindowManager window) {
        this.window = window;
    }

    public GLFWErrorCallback getErrorCallback() {
        return errorCallback;
    }

    public void setErrorCallback(GLFWErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
    }
}
