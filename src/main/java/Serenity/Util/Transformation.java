package Serenity.Util;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import Serenity.Camera;
import particle.Particle;

public class Transformation {
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f worldMatrix = new Matrix4f();

    public Matrix4f getViewMatrix(Camera camera) {
        Vector3f pos = camera.getPosition();
        Vector3f rot = camera.getRotation();
        viewMatrix.identity()
                .rotate((float) Math.toRadians(rot.x), 1f, 0f, 0f)
                .rotate((float) Math.toRadians(rot.y), 0f, 1f, 0f)
                .translate(-pos.x, -pos.y, -pos.z);
        return viewMatrix;
    }

    public Matrix4f getWorldMatrix(Particle particle) {
        Vector3f pos = particle.position();
        worldMatrix.identity()
            .translate(pos.x, pos.y, pos.z)
                .scale(1f);
        return worldMatrix;
    }
}
