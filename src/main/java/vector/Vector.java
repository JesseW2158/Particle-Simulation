package vector;

public class Vector {
    private double x;
    private double y;
    private double z;

    public Vector(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    // ==========<< Instance methods >>==========

    public Vector add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector add(Vector other) {
        this.x += other.x();
        this.y += other.y();
        this.z += other.z();
        return this;
    }

    public Vector subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vector subtract(Vector other) {
        this.x -= other.x();
        this.y -= other.y();
        this.z -= other.z();
        return this;
    }

    public Vector multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        return this;
    }

    public double dot(Vector other) {
        return this.x * other.x() + this.y * other.y() + this.z * other.z();
    }

    public Vector cross(Vector other) {
        double newX = this.y * other.z() - this.z * other.y();
        double newY = this.z * other.x() - this.x * other.z();
        double newZ = this.x * other.y() - this.y * other.x();
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        return this;
    }

    public Vector divide(double scalar) {
        this.x /= scalar;
        this.y /= scalar;
        this.z /= scalar;
        return this;
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector normalize() {
        double mag = magnitude();
        if (mag != 0) {
            this.x /= mag;
            this.y /= mag;
            this.z /= mag;
        }
        return this;
    }

    public Vector normalized() {
        double mag = magnitude();
        if (mag != 0) {
            return new Vector(this.x / mag, this.y / mag, this.z / mag);
        }
        return new Vector(0d, 0d, 0d);
    }

    // ==========<< Static methods >>==========
    public static Vector add(Vector a, Vector b) {
        return new Vector(a.x() + b.x(), a.y() + b.y(), a.z() + b.z());
    }

    public static Vector subtract(Vector a, Vector b) {
        return new Vector(a.x() - b.x(), a.y() - b.y(), a.z() - b.z());
    }

    public static double dot(Vector a, Vector b) {
        return a.x() * b.x() + a.y() * b.y() + a.z() * b.z();
    }

    public static Vector cross(Vector a, Vector b) {
        return new Vector(a.y() * b.z() - a.z() * b.y(), a.z() * b.x() - a.x() * b.z(), a.x() * b.y() - a.y() * b.x());
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}
