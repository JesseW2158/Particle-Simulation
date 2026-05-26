import serenity.WindowManager;

public class ParticleSimulation {
	private static WindowManager window;

	public static void main(String[] args) {
		System.out.println("LWJGL Version: " + org.lwjgl.Version.getVersion());

		WindowManager window = new WindowManager("Particle Simulation", 1600, 900, false);
		window.init();

		while(!window.windowShouldClose()) {
			window.update();
		}

		window.cleanup();
	}

	public static WindowManager getWindow() {
		return window;
	}
}