package driver;

import particle.Particle;
import space.Space;
import vector.Vector;

public class Driver {
    public static void main(String[] args) {
        Particle p1 = new Particle(1.0, 1.0, 1.0, 0.5, 1, new Vector(0d, 0d, 0d));
        Particle p2 = new Particle(1.0, -1.0, 1.0, 0.5, -1, new Vector(1d, 0d, 0d));
        Space space = new Space();
        space.addParticle(p1);
        space.addParticle(p2);
        space.display();
    }
}
