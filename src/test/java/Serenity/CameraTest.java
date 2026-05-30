package Serenity;

import static org.junit.Assert.assertEquals;

import org.joml.Vector3f;
import org.junit.Test;

public class CameraTest {
    private static final float EPS = 1e-4f;

    @Test
    public void moveForwardAtZeroPoseMovesAlongNegativeZ() {
        Camera camera = new Camera(new Vector3f(0f, 0f, 2f), new Vector3f(0f, 0f, 0f));
        camera.moveForward(1f);
        assertEquals(0f, camera.getPosition().x, EPS);
        assertEquals(0f, camera.getPosition().y, EPS);
        assertEquals(1f, camera.getPosition().z, EPS);
    }

    @Test
    public void lookClampsPitch() {
        Camera camera = new Camera();
        camera.look(200f, 0f);
        assertEquals(89f, camera.getRotation().x, EPS);
        camera.look(-1000f, 0f);
        assertEquals(-89f, camera.getRotation().x, EPS);
    }

    @Test
    public void lookWrapsYawWithin360() {
        Camera camera = new Camera();
        camera.look(0f, 400f);
        assertEquals(40f, camera.getRotation().y, EPS);
        camera.look(0f, -80f);
        assertEquals(320f, camera.getRotation().y, EPS);
    }
}
