package particle;

import vector.Vector;

public class Particle {
    private final double mass;
    private final double charge;
    private final double radius;
    private final double totalSpin;
    private int spinDirection;
    private Vector position;
    private Vector velocity;
    private Vector acceleration;

    public Particle(double mass, double charge, double radius, double totalSpin, int spinDirection, Vector position,
            Vector velocity, Vector acceleration) {
        this.mass = mass;
        this.charge = charge;
        this.radius = radius;
        this.totalSpin = totalSpin;
        this.spinDirection = spinDirection;
        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
    }

    public void update(double deltaTime) {
        velocity.add(acceleration.multiply(deltaTime));
        position.add(velocity.multiply(deltaTime));
    }

    public void 

}
