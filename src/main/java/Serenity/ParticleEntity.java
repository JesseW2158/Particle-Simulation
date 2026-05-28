package Serenity;

import org.joml.Vector3f;

public class ParticleEntity {
    private Particle particle;
    private Vector3f position, rotation;
    private float scale;

    public ParticleEntity(Particle particle, Vector3f position, Vector3f rotation, float scale) {
        this.particle = particle;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    public void increasePosition(float offsetX, float offsetY, float offsetZ) {
        this.position.x += offsetX;
        this.position.y += offsetY;
        this.position.z += offsetZ;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public void increaseRotation(float offsetX, float offsetY, float offsetZ) {
        this.rotation.x += offsetX;
        this.rotation.y += offsetY;
        this.rotation.z += offsetZ;
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.set(x, y, z);
    }

    public Particle getParticle() {
        return particle;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public float getScale() {
        return scale;
    }
}
