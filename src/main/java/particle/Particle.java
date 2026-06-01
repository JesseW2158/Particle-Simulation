package particle;

import org.joml.Vector3f;

import Serenity.Util.PhysicsConstants;

public class Particle {
    private final Vector3f position;
    private final Vector3f velocity;
    private final ParticleDefinition type;

    public Particle(Vector3f position, ParticleDefinition type) {
        this.position = position;
        this.velocity = new Vector3f();
        this.type = type;
    }

    public Vector3f position() {
        return position;
    }

    public ParticleDefinition type() {
        return type;
    }

    public Vector3f velocity() {
        return velocity;
    }

    public double massKg() {
        return type.mass();
    }

    public float cbrtMass() {
        return (float) Math.cbrt(type.mass() / PhysicsConstants.ELECTRON_MASS_KG);
    }

    public float renderRadius() {
        return type.renderStyle().radiusFor(cbrtMass());
    }

    public float glowStrength() {
        return type.renderStyle().glowStrength();
    }

    public float glowRadiusScale() {
        return type.renderStyle().glowRadiusScale();
    }

    public void integrate(float ax, float ay, float az, float dt) {
        if (!Float.isFinite(ax)) {
            ax = 0f;
        }
        if (!Float.isFinite(ay)) {
            ay = 0f;
        }
        if (!Float.isFinite(az)) {
            az = 0f;
        }

        velocity.x += ax * dt;
        velocity.y += ay * dt;
        velocity.z += az * dt;

        if (!Float.isFinite(velocity.x)) {
            velocity.x = 0f;
        }
        if (!Float.isFinite(velocity.y)) {
            velocity.y = 0f;
        }
        if (!Float.isFinite(velocity.z)) {
            velocity.z = 0f;
        }

        position.x += velocity.x * dt;
        position.y += velocity.y * dt;
        position.z += velocity.z * dt;

        if (!Float.isFinite(position.x)) {
            position.x = 0f;
        }
        if (!Float.isFinite(position.y)) {
            position.y = 0f;
        }
        if (!Float.isFinite(position.z)) {
            position.z = 0f;
        }
    }

    public void bounceWithinCube(float halfExtent) {
        if (position.x > halfExtent) {
            position.x = halfExtent;
            velocity.x = -Math.abs(velocity.x);
        } else if (position.x < -halfExtent) {
            position.x = -halfExtent;
            velocity.x = Math.abs(velocity.x);
        }

        if (position.y > halfExtent) {
            position.y = halfExtent;
            velocity.y = -Math.abs(velocity.y);
        } else if (position.y < -halfExtent) {
            position.y = -halfExtent;
            velocity.y = Math.abs(velocity.y);
        }

        if (position.z > halfExtent) {
            position.z = halfExtent;
            velocity.z = -Math.abs(velocity.z);
        } else if (position.z < -halfExtent) {
            position.z = -halfExtent;
            velocity.z = Math.abs(velocity.z);
        }
    }
}
