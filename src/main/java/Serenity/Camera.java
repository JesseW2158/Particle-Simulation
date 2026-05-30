package Serenity;

import org.joml.Vector3f;

public class Camera {
    private Vector3f position, rotation;

    public Camera() {
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
    }

    public Camera(Vector3f position, Vector3f rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public void movePosition(float offsetX, float offsetY, float offsetZ) {
        if (offsetZ != 0) {
            position.x += (float) Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
            position.z += (float) Math.cos(Math.toRadians(rotation.y)) * offsetZ;
        }

        if (offsetX != 0) {
            position.x += (float) Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
            position.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
        }
        
        position.y += offsetY;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.set(x, y, z);
    }

    public void moveRotation(float offsetX, float offsetY, float offsetZ) {
        this.rotation.x += offsetX;
        this.rotation.y += offsetY;
        this.rotation.z += offsetZ;
    }

    public void moveForward(float amount) {
        double yaw = Math.toRadians(rotation.y);
        double pitch = Math.toRadians(rotation.x);
        position.x += (float) (Math.sin(yaw) * Math.cos(pitch)) * amount;
        position.y += (float) (-Math.sin(pitch)) * amount;
        position.z += (float) (-Math.cos(yaw) * Math.cos(pitch)) * amount;
    }

    public void look(float deltaPitch, float deltaYaw) {
        rotation.x += deltaPitch;
        if (rotation.x > 89f) {
            rotation.x = 89f;
        } else if (rotation.x < -89f) {
            rotation.x = -89f;
        }

        rotation.y = (rotation.y + deltaYaw) % 360f;
        if (rotation.y < 0f) {
            rotation.y += 360f;
        }
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }
}
