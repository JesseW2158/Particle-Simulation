package Serenity;

public class Particle {
    private static int idCounter = 0;
    private final int id;
    private int vertexCount;
    private final double mass;
    private final double charge;
    private final double radius;
    private final double totalSpin;
    private int spinDirection;
    private Vector position;
    private Vector velocity;
    private Vector acceleration;

    public Particle(int vertexCount, double mass, double charge, double radius, double totalSpin, int spinDirection,
                    Vector position) {
        this.id = idCounter++;
        this.vertexCount = vertexCount;
        this.mass = mass;
        this.charge = charge;
        this.radius = radius;
        this.totalSpin = totalSpin;
        this.spinDirection = spinDirection;
        this.position = position;
        this.velocity = Vector.zero();
        this.acceleration = Vector.zero();
    }

    // ==========<< Getters >>==========
    public int getId() {
        return id;
    }

    public double mass() {
        return mass;
    }

    public Vector position() {
        return position;
    }

    public Vector addForce(Vector force) {
        this.acceleration.add(force.multiply(1 / mass));
        return this.acceleration;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
