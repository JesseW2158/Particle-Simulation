package Serenity.Util;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class SimulationSetupDialog {
    private SimulationSetupDialog() {
    }

    public static SimulationSettings promptOrDefault() {
        if (GraphicsEnvironment.isHeadless()) {
            return SimulationSettings.defaults();
        }

        SimulationSettings defaults = SimulationSettings.defaults();
        JSlider particleSlider = new JSlider(50, 1200, defaults.particleCount());
        JSlider speedSlider = new JSlider(1, 200, Math.max(1, Math.round(defaults.simulationSpeed() * 100f)));
        JSlider gravitySlider = new JSlider(1, 400, Math.max(1, (int) Math.round(defaults.gravityMultiplier() * 100.0)));
        JSlider electrostaticSlider = new JSlider(1, 400,
            Math.max(1, (int) Math.round(defaults.electrostaticMultiplier() * 100.0)));
        JSlider strongAttractionSlider = new JSlider(1, 400,
            Math.max(1, (int) Math.round(defaults.strongAttractionMultiplier() * 100.0)));
        JSlider strongRepulsionSlider = new JSlider(1, 400,
            Math.max(1, (int) Math.round(defaults.strongRepulsionMultiplier() * 100.0)));
        JSlider strongRangeSlider = new JSlider(10, 300,
            Math.max(10, (int) Math.round(defaults.strongRangeMultiplier() * 100.0)));
        JSlider weakStrengthSlider = new JSlider(0, 300,
            Math.max(0, (int) Math.round(defaults.weakMultiplier() * 100.0)));
        JSlider weakRangeSlider = new JSlider(10, 300,
            Math.max(10, (int) Math.round(defaults.weakRangeMultiplier() * 100.0)));
        JCheckBox strongEnabled = new JCheckBox("Enable strong force", defaults.strongForceEnabled());
        JCheckBox weakEnabled = new JCheckBox("Enable weak force", defaults.weakForceEnabled());
        JButton randomButton = new JButton("Random");
        JButton funButton = new JButton("Fun");
        FunPreset[] funPresets = createFunPresets();

        particleSlider.setMajorTickSpacing(250);
        particleSlider.setMinorTickSpacing(50);
        particleSlider.setPaintTicks(true);

        speedSlider.setMajorTickSpacing(25);
        speedSlider.setMinorTickSpacing(5);
        speedSlider.setPaintTicks(true);

        gravitySlider.setMajorTickSpacing(50);
        gravitySlider.setMinorTickSpacing(10);
        gravitySlider.setPaintTicks(true);

        electrostaticSlider.setMajorTickSpacing(50);
        electrostaticSlider.setMinorTickSpacing(10);
        electrostaticSlider.setPaintTicks(true);

        strongAttractionSlider.setMajorTickSpacing(50);
        strongAttractionSlider.setMinorTickSpacing(10);
        strongAttractionSlider.setPaintTicks(true);

        strongRepulsionSlider.setMajorTickSpacing(50);
        strongRepulsionSlider.setMinorTickSpacing(10);
        strongRepulsionSlider.setPaintTicks(true);

        strongRangeSlider.setMajorTickSpacing(50);
        strongRangeSlider.setMinorTickSpacing(10);
        strongRangeSlider.setPaintTicks(true);

        weakStrengthSlider.setMajorTickSpacing(50);
        weakStrengthSlider.setMinorTickSpacing(10);
        weakStrengthSlider.setPaintTicks(true);

        weakRangeSlider.setMajorTickSpacing(50);
        weakRangeSlider.setMinorTickSpacing(10);
        weakRangeSlider.setPaintTicks(true);

        JLabel particleLabel = new JLabel();
        JLabel speedLabel = new JLabel();
        JLabel gravityLabel = new JLabel();
        JLabel electrostaticLabel = new JLabel();
        JLabel strongAttractionLabel = new JLabel();
        JLabel strongRepulsionLabel = new JLabel();
        JLabel strongRangeLabel = new JLabel();
        JLabel weakStrengthLabel = new JLabel();
        JLabel weakRangeLabel = new JLabel();
        JLabel stabilityWarningLabel = new JLabel();
        stabilityWarningLabel.setForeground(new Color(170, 80, 0));

        ChangeListener updater = (ChangeEvent event) -> {
            particleLabel.setText("Particles: " + particleSlider.getValue());
            speedLabel.setText(String.format("Simulation speed: %.2fx", speedSlider.getValue() / 100.0));
            gravityLabel.setText(String.format("Gravity (G) multiplier: %.2fx", gravitySlider.getValue() / 100.0));
            electrostaticLabel.setText(
                String.format("Electrostatic multiplier: %.2fx", electrostaticSlider.getValue() / 100.0));
            strongAttractionLabel
                .setText(String.format("Strong attraction multiplier: %.2fx", strongAttractionSlider.getValue() / 100.0));
            strongRepulsionLabel
                .setText(String.format("Strong repulsion multiplier: %.2fx", strongRepulsionSlider.getValue() / 100.0));
            strongRangeLabel.setText(String.format("Strong range multiplier: %.2fx", strongRangeSlider.getValue() / 100.0));
            weakStrengthLabel.setText(String.format("Weak force multiplier: %.2fx", weakStrengthSlider.getValue() / 100.0));
            weakRangeLabel.setText(String.format("Weak range multiplier: %.2fx", weakRangeSlider.getValue() / 100.0));

            String warning = buildStabilityWarning(speedSlider.getValue() / 100.0,
                    gravitySlider.getValue() / 100.0,
                    electrostaticSlider.getValue() / 100.0,
                    strongAttractionSlider.getValue() / 100.0,
                    strongRepulsionSlider.getValue() / 100.0,
                    weakStrengthSlider.getValue() / 100.0);
            stabilityWarningLabel.setText(warning);
        };
        updater.stateChanged(null);
        particleSlider.addChangeListener(updater);
        speedSlider.addChangeListener(updater);
        gravitySlider.addChangeListener(updater);
        electrostaticSlider.addChangeListener(updater);
        strongAttractionSlider.addChangeListener(updater);
        strongRepulsionSlider.addChangeListener(updater);
        strongRangeSlider.addChangeListener(updater);
        weakStrengthSlider.addChangeListener(updater);
        weakRangeSlider.addChangeListener(updater);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Configure before simulation starts"));
        panel.add(particleLabel);
        panel.add(particleSlider);
        panel.add(speedLabel);
        panel.add(speedSlider);
        panel.add(gravityLabel);
        panel.add(gravitySlider);
        panel.add(electrostaticLabel);
        panel.add(electrostaticSlider);
        panel.add(strongEnabled);
        panel.add(strongAttractionLabel);
        panel.add(strongAttractionSlider);
        panel.add(strongRepulsionLabel);
        panel.add(strongRepulsionSlider);
        panel.add(strongRangeLabel);
        panel.add(strongRangeSlider);
        panel.add(weakEnabled);
        panel.add(weakStrengthLabel);
        panel.add(weakStrengthSlider);
        panel.add(weakRangeLabel);
        panel.add(weakRangeSlider);
        panel.add(stabilityWarningLabel);

        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 8, 0));
        buttonRow.add(randomButton);
        buttonRow.add(funButton);
        panel.add(buttonRow);

        funButton.addActionListener(event -> {
            FunPreset selectedPreset = chooseFunPreset(funPresets);
            if (selectedPreset == null) {
                return;
            }

            applyPreset(selectedPreset, particleSlider, speedSlider, gravitySlider, electrostaticSlider,
                strongAttractionSlider, strongRepulsionSlider, strongRangeSlider,
                weakStrengthSlider, weakRangeSlider, strongEnabled, weakEnabled);
        });

        randomButton.addActionListener(event -> randomizeControls(particleSlider, speedSlider, gravitySlider,
            electrostaticSlider, strongAttractionSlider, strongRepulsionSlider, strongRangeSlider,
            weakStrengthSlider, weakRangeSlider, strongEnabled, weakEnabled));

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        int result = JOptionPane.showConfirmDialog(null, scrollPane, "Particle Simulation Setup",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        return new SimulationSettings(particleSlider.getValue(), speedSlider.getValue() / 100.0f,
                defaults.spawnHalfExtent(), defaults.boundaryHalfExtent(), defaults.fixedPhysicsStepSeconds(),
            defaults.metersPerWorldUnit(), defaults.softeningLength(), gravitySlider.getValue() / 100.0,
            electrostaticSlider.getValue() / 100.0, strongEnabled.isSelected(),
            strongAttractionSlider.getValue() / 100.0, strongRepulsionSlider.getValue() / 100.0,
            strongRangeSlider.getValue() / 100.0, weakEnabled.isSelected(), weakStrengthSlider.getValue() / 100.0,
            weakRangeSlider.getValue() / 100.0);
    }

    private static void randomizeControls(JSlider particleSlider, JSlider speedSlider, JSlider gravitySlider,
            JSlider electrostaticSlider, JSlider strongAttractionSlider, JSlider strongRepulsionSlider,
            JSlider strongRangeSlider, JSlider weakStrengthSlider, JSlider weakRangeSlider,
            JCheckBox strongEnabled, JCheckBox weakEnabled) {
        Random rng = new Random();
        randomizeSlider(rng, particleSlider, 120, 800);
        randomizeSlider(rng, speedSlider, 10, 90);
        randomizeSlider(rng, gravitySlider, 50, 180);
        randomizeSlider(rng, electrostaticSlider, 25, 140);
        randomizeSlider(rng, strongAttractionSlider, 40, 160);
        randomizeSlider(rng, strongRepulsionSlider, 40, 160);
        randomizeSlider(rng, strongRangeSlider, 60, 160);
        randomizeSlider(rng, weakStrengthSlider, 0, 110);
        randomizeSlider(rng, weakRangeSlider, 60, 160);
        strongEnabled.setSelected(rng.nextBoolean());
        weakEnabled.setSelected(rng.nextBoolean());
    }

    private static void randomizeSlider(Random rng, JSlider slider, int desiredMin, int desiredMax) {
        int min = Math.max(slider.getMinimum(), desiredMin);
        int max = Math.min(slider.getMaximum(), desiredMax);
        slider.setValue(min + rng.nextInt(max - min + 1));
    }

    private static FunPreset chooseFunPreset(FunPreset[] presets) {
        Object selected = JOptionPane.showInputDialog(
            null,
            "Choose a fun force profile",
            "Fun Presets",
            JOptionPane.PLAIN_MESSAGE,
            null,
            presets,
            presets[0]);
        if (selected instanceof FunPreset preset) {
            return preset;
        }
        return null;
    }

    private static void applyPreset(FunPreset preset, JSlider particleSlider, JSlider speedSlider, JSlider gravitySlider,
            JSlider electrostaticSlider, JSlider strongAttractionSlider, JSlider strongRepulsionSlider,
            JSlider strongRangeSlider, JSlider weakStrengthSlider, JSlider weakRangeSlider,
            JCheckBox strongEnabled, JCheckBox weakEnabled) {
        setSliderValue(particleSlider, preset.particleCount);
        setSliderValue(speedSlider, preset.simulationSpeed);
        setSliderValue(gravitySlider, preset.gravityMultiplier);
        setSliderValue(electrostaticSlider, preset.electrostaticMultiplier);
        setSliderValue(strongAttractionSlider, preset.strongAttractionMultiplier);
        setSliderValue(strongRepulsionSlider, preset.strongRepulsionMultiplier);
        setSliderValue(strongRangeSlider, preset.strongRangeMultiplier);
        setSliderValue(weakStrengthSlider, preset.weakMultiplier);
        setSliderValue(weakRangeSlider, preset.weakRangeMultiplier);
        strongEnabled.setSelected(preset.strongForceEnabled);
        weakEnabled.setSelected(preset.weakForceEnabled);
    }

    private static void setSliderValue(JSlider slider, int value) {
        slider.setValue(Math.max(slider.getMinimum(), Math.min(slider.getMaximum(), value)));
    }

    private static FunPreset[] createFunPresets() {
        return new FunPreset[] {
            new FunPreset("Calm Orbit", "gentle gravity and slow drift", 260, 24, 145, 40, true, 85, 75, 120, false, 30,
                100),
            new FunPreset("Balanced Showcase", "balanced force interplay", 420, 45, 120, 95, true, 120, 105, 125, false,
                45, 110),
            new FunPreset("Plasma Dance", "electrostatic-heavy motion", 520, 68, 85, 175, false, 95, 95, 110, true, 70,
                120),
            new FunPreset("Nuclear Jitter", "short-range nucleon bursts", 360, 58, 105, 130, true, 190, 155, 145, true,
                110, 135),
            new FunPreset("Chaotic Storm", "high-energy visual chaos", 640, 90, 175, 185, true, 165, 155, 150, true, 130,
                125)
        };
    }

    private static String buildStabilityWarning(double speed, double gravityMultiplier,
            double electrostaticMultiplier, double strongAttractionMultiplier,
            double strongRepulsionMultiplier, double weakMultiplier) {
        boolean risky = speed > 1.2
                || gravityMultiplier > 2.5
                || electrostaticMultiplier > 2.0
                || strongAttractionMultiplier > 2.0
                || strongRepulsionMultiplier > 2.0
                || weakMultiplier > 2.0;

        if (!risky) {
            return "Stability note: inverse-square forces can spike at close range; these values are in a safer band.";
        }

        return "Warning: high multipliers or speed can destabilize explicit-Euler integration (large force spikes).";
    }

    private static final class FunPreset {
        private final String name;
        private final String description;
        private final int particleCount;
        private final int simulationSpeed;
        private final int gravityMultiplier;
        private final int electrostaticMultiplier;
        private final boolean strongForceEnabled;
        private final int strongAttractionMultiplier;
        private final int strongRepulsionMultiplier;
        private final int strongRangeMultiplier;
        private final boolean weakForceEnabled;
        private final int weakMultiplier;
        private final int weakRangeMultiplier;

        private FunPreset(String name, String description, int particleCount, int simulationSpeed,
                int gravityMultiplier, int electrostaticMultiplier, boolean strongForceEnabled,
                int strongAttractionMultiplier, int strongRepulsionMultiplier, int strongRangeMultiplier,
                boolean weakForceEnabled, int weakMultiplier, int weakRangeMultiplier) {
            this.name = name;
            this.description = description;
            this.particleCount = particleCount;
            this.simulationSpeed = simulationSpeed;
            this.gravityMultiplier = gravityMultiplier;
            this.electrostaticMultiplier = electrostaticMultiplier;
            this.strongForceEnabled = strongForceEnabled;
            this.strongAttractionMultiplier = strongAttractionMultiplier;
            this.strongRepulsionMultiplier = strongRepulsionMultiplier;
            this.strongRangeMultiplier = strongRangeMultiplier;
            this.weakForceEnabled = weakForceEnabled;
            this.weakMultiplier = weakMultiplier;
            this.weakRangeMultiplier = weakRangeMultiplier;
        }

        @Override
        public String toString() {
            return name + " - " + description;
        }
    }
}