package particle;

import vector.Vector;

public class Particle {
    private static int idCounter = 0;
    private final int id;
    private final double mass;
    private final double charge;
    private final double radius;
    private final double totalSpin;
    private int spinDirection;
    private Vector position;
    private Vector velocity;
    private Vector acceleration;

    public Particle(double mass, double charge, double radius, double totalSpin, int spinDirection, Vector position) {
        this.id = idCounter++;
        this.mass = mass;
        this.charge = charge;
        this.radius = radius;
        this.totalSpin = totalSpin;
        this.spinDirection = spinDirection;
        this.position = position;
        this.velocity = new Vector(0d, 0d, 0d);
        this.acceleration = new Vector(0d, 0d, 0d);
    }

    public void update(double deltaTime) {
        velocity.add(acceleration.multiply(deltaTime));
        position.add(velocity.multiply(deltaTime));
    }

    public double mass() {
        return mass;
    }

    public Vector position() {
        return position;
    }

    public void addForce(Vector force) {
        acceleration.add(force.multiply(1 / mass));
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(": ").append(velocity);
        return sb.toString();
    }
}
