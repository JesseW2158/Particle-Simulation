package Serenity;

import java.util.HashSet;


public class Space {
    private HashSet<Particle> particles;

    public Space() {
        particles = new HashSet<>();
    }

    public void addParticle(Particle particle) {
        particles.add(particle);
    }

    public void display() {
        for (Particle particle : particles) {
            System.out.println(particle);
        }
    }

    public void applyGravity() {
        for (Particle p1 : particles) {
            for (Particle p2 : particles) {
                if (p1 != p2) {
                    Gravity.gravitationalForce(p1, p2);
                }
            }
        }
    }
}
