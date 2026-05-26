package space;

import java.util.HashSet;
import particle.Particle;

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
}
