package Serenity;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class MouseInput {
    private final Vector2d previousPos = new Vector2d(-1, -1);
    private final Vector2d currentPos = new Vector2d(0, 0);
    private final Vector2f displVec = new Vector2f();

    private boolean inWindow = false;
    private boolean leftButtonPressed = false;
    private double scrollDelta = 0;

    public void init(WindowManager window) {
        long handle = window.getWindowHandle();

        GLFW.glfwSetCursorPosCallback(handle, (win, xpos, ypos) -> {
            currentPos.x = xpos;
            currentPos.y = ypos;
        });

        GLFW.glfwSetCursorEnterCallback(handle, (win, entered) -> inWindow = entered);

        GLFW.glfwSetMouseButtonCallback(handle, (win, button, action, mods) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                leftButtonPressed = (action == GLFW.GLFW_PRESS);
            }
        });

        GLFW.glfwSetScrollCallback(handle, (win, xoffset, yoffset) -> scrollDelta += yoffset);
    }

    public void input() {
        displVec.set(0, 0);
        if (previousPos.x >= 0 && previousPos.y >= 0 && inWindow) {
            double dx = currentPos.x - previousPos.x;
            double dy = currentPos.y - previousPos.y;
            displVec.x = (float) dy; // vertical mouse movement -> pitch
            displVec.y = (float) dx; // horizontal mouse movement -> yaw
        }
        previousPos.set(currentPos.x, currentPos.y);
    }

    public Vector2f getDisplVec() {
        return displVec;
    }

    public boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    public double getScrollDelta() {
        return scrollDelta;
    }

    public void resetScroll() {
        scrollDelta = 0;
    }
}
