package Test;
import Serenity.EngineManager;
import Serenity.WindowManager;
import Serenity.Util.Constants;

public class Launcher {
	private static WindowManager window;
	private static ParticleSimulation simulation;
    
	public static void main(String[] args) {
		System.out.println("LWJGL Version: " + org.lwjgl.Version.getVersion());

		window = new WindowManager(Constants.TITLE, Constants.WIDTH, Constants.HEIGHT, false);
		simulation = new ParticleSimulation();
		EngineManager engine = new EngineManager();
		
		try {
			engine.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
	public static WindowManager getWindow() {
		return window;
	}

	public static ParticleSimulation getSimulation() {
		return simulation;
	}
}
