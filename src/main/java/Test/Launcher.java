package Test;

import java.util.List;

import Serenity.EngineManager;
import Serenity.Util.Constants;
import Serenity.Util.ParticleDefinitionSetupDialog;
import Serenity.Util.SimulationSettings;
import Serenity.Util.SimulationSetupDialog;
import Serenity.Util.StartupConfiguration;
import Serenity.WindowManager;
import particle.ParticleDefinition;

public class Launcher {
	private static WindowManager window;
	private static ParticleSimulation simulation;
    
	public static void main(String[] args) {
		System.out.println("LWJGL Version: " + org.lwjgl.Version.getVersion());

		StartupConfiguration startupConfiguration = promptStartupConfiguration();
		if (startupConfiguration == null) {
			return;
		}

		window = new WindowManager(Constants.TITLE, Constants.WIDTH, Constants.HEIGHT, false);
		simulation = new ParticleSimulation(startupConfiguration.settings(), startupConfiguration.particleDefinitions());
		EngineManager engine = new EngineManager();
		
		try {
			engine.start();
		} catch (Exception e) {
			throw new RuntimeException("Failed to start simulation", e);
		}
	}
    
	public static WindowManager getWindow() {
		return window;
	}

	public static ParticleSimulation getSimulation() {
		return simulation;
	}

	private static StartupConfiguration promptStartupConfiguration() {
		SimulationSettings settings = SimulationSetupDialog.promptOrDefault();
		if (settings == null) {
			return null;
		}

		List<ParticleDefinition> particleDefinitions = ParticleDefinitionSetupDialog.promptOrDefault();
		if (particleDefinitions == null) {
			return null;
		}

		return new StartupConfiguration(settings, particleDefinitions);
	}
}
