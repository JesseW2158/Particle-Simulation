package Serenity;

public class Particle {
    private int id;
    private int vertexCount;

    public Particle(int id, int vertexCount) {
        this.id = id;
        this.vertexCount = vertexCount;
    }

    public int getId() {
        return id;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
