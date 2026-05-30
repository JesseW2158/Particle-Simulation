package Serenity.Util;

import static org.junit.Assert.assertEquals;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.Test;

import Serenity.Camera;
import Serenity.Particle;
import Serenity.Vector;

public class TransformationTest {
    private static final float EPS = 1e-5f;

    @Test
    public void viewMatrixAtOriginIsIdentity() {
        Camera camera = new Camera(); // position (0,0,0), rotation (0,0,0)
        Matrix4f view = new Transformation().getViewMatrix(camera);
        Vector4f p = new Vector4f(1f, 2f, 3f, 1f);
        view.transform(p);
        assertEquals(1f, p.x, EPS);
        assertEquals(2f, p.y, EPS);
        assertEquals(3f, p.z, EPS);
    }

    @Test
    public void viewMatrixTranslatesByNegativeCameraPosition() {
        Camera camera = new Camera(new Vector3f(0f, 0f, 2f), new Vector3f(0f, 0f, 0f));
        Matrix4f view = new Transformation().getViewMatrix(camera);
        Vector4f p = new Vector4f(0f, 0f, 0f, 1f);
        view.transform(p); // world origin -> view space
        assertEquals(0f, p.x, EPS);
        assertEquals(0f, p.y, EPS);
        assertEquals(-2f, p.z, EPS);
    }

    @Test
    public void worldMatrixTranslatesToParticlePosition() {
        Particle particle = new Particle(0, 6, 1d, 1d, 1d, 1d, 1, new Vector(1d, 2d, 3d));
        Matrix4f world = new Transformation().getWorldMatrix(particle);
        Vector4f p = new Vector4f(0f, 0f, 0f, 1f);
        world.transform(p);
        assertEquals(1f, p.x, EPS);
        assertEquals(2f, p.y, EPS);
        assertEquals(3f, p.z, EPS);
    }
}
