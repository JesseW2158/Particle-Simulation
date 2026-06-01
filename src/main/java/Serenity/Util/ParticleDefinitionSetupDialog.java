package Serenity.Util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;

import org.joml.Vector3f;

import particle.CustomParticleDefinition;
import particle.ParticleDefinition;
import particle.ParticleRenderStyle;

public final class ParticleDefinitionSetupDialog {
    private static final int MIN_PARTICLES = 2;
    private static final int MAX_PARTICLES = 8;

    private ParticleDefinitionSetupDialog() {
    }

    public static List<ParticleDefinition> promptOrDefault() {
        if (GraphicsEnvironment.isHeadless()) {
            return CustomParticleDefinition.defaultDefinitions();
        }

        List<ParticleCardPanel> cards = new ArrayList<>();
        for (ParticleDefinition definition : CustomParticleDefinition.defaultDefinitions()) {
            cards.add(new ParticleCardPanel(definition));
        }

        JPanel root = new JPanel(new BorderLayout(8, 8));

        JPanel topControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton minusButton = new JButton("-");
        JButton plusButton = new JButton("+");
        JButton randomButton = new JButton("Random");
        topControls.add(new JLabel("Particles"));
        topControls.add(minusButton);
        topControls.add(plusButton);
        topControls.add(randomButton);
        root.add(topControls, BorderLayout.NORTH);

        JPanel cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(cardsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(780, 500));
        root.add(scrollPane, BorderLayout.CENTER);

        JButton bottomPlusButton = new JButton("Add Particle +");
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.add(bottomPlusButton);
        root.add(footer, BorderLayout.SOUTH);

        JLabel stabilityWarningLabel = new JLabel();
        stabilityWarningLabel.setForeground(new Color(170, 80, 0));
        topControls.add(stabilityWarningLabel);

        Runnable refresh = () -> rebuildCards(cards, cardsContainer, minusButton, plusButton, bottomPlusButton);
        refresh.run();
        stabilityWarningLabel.setText(buildDangerWarning(cards));

        minusButton.addActionListener(event -> {
            if (cards.size() > MIN_PARTICLES) {
                cards.remove(cards.size() - 1);
                refresh.run();
            }
        });

        Runnable addAction = () -> {
            if (cards.size() < MAX_PARTICLES) {
                cards.add(ParticleCardPanel.newTemplate(cards.size() + 1));
                refresh.run();
            }
        };
        plusButton.addActionListener(event -> addAction.run());
        bottomPlusButton.addActionListener(event -> addAction.run());

        randomButton.addActionListener(event -> {
            Random rng = new Random();
            for (ParticleCardPanel card : cards) {
                card.randomize(rng);
            }
            stabilityWarningLabel.setText(buildDangerWarning(cards));
        });

        while (true) {
            int result = JOptionPane.showConfirmDialog(null, root, "Particle Definitions",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            ValidationResult validation = validate(cards);
            if (!validation.valid) {
                JOptionPane.showMessageDialog(null, validation.message, "Invalid Particle Configuration",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }

            String dangerWarning = buildDangerWarning(cards);
            if (!dangerWarning.isEmpty()) {
                int warningChoice = JOptionPane.showConfirmDialog(null,
                        dangerWarning + " Continue anyway?",
                        "Potentially Unstable Values",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (warningChoice != JOptionPane.YES_OPTION) {
                    continue;
                }
            }

            List<ParticleDefinition> definitions = new ArrayList<>();
            for (ParticleCardPanel card : cards) {
                definitions.add(card.toDefinition());
            }
            return definitions;
        }
    }

    private static ValidationResult validate(List<ParticleCardPanel> cards) {
        if (cards.size() < MIN_PARTICLES || cards.size() > MAX_PARTICLES) {
            return ValidationResult.invalid("Particle count must be between 2 and 8.");
        }

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < cards.size(); i++) {
            ParticleCardPanel card = cards.get(i);
            String id = card.idField.getText().trim();
            if (id.isEmpty()) {
                return ValidationResult.invalid("Particle " + (i + 1) + " must have a non-empty id.");
            }
            String normalized = id.toLowerCase();
            if (!ids.add(normalized)) {
                return ValidationResult.invalid("Particle ids must be unique.");
            }

            double mass;
            try {
                mass = Double.parseDouble(card.massField.getText().trim());
            } catch (NumberFormatException exception) {
                return ValidationResult.invalid("Particle " + id + " mass is invalid.");
            }
            if (!Double.isFinite(mass) || mass <= 0.0) {
                return ValidationResult.invalid("Particle " + id + " mass must be a positive finite value.");
            }
        }

        return ValidationResult.valid();
    }

    private static String buildDangerWarning(List<ParticleCardPanel> cards) {
        int dangerousChargeCount = 0;
        int tinyMassCount = 0;

        for (ParticleCardPanel card : cards) {
            int charge = ((Number) card.chargeSpinner.getValue()).intValue();
            if (Math.abs(charge) > 2) {
                dangerousChargeCount++;
            }

            double mass;
            try {
                mass = Double.parseDouble(card.massField.getText().trim());
            } catch (NumberFormatException exception) {
                continue;
            }

            if (mass < 1.0e-33) {
                tinyMassCount++;
            }
        }

        if (dangerousChargeCount == 0 && tinyMassCount == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder("Warning: unstable value region detected");
        if (dangerousChargeCount > 0) {
            builder.append("; ").append(dangerousChargeCount)
                    .append(" particle(s) have |charge| > 2e");
        }
        if (tinyMassCount > 0) {
            builder.append("; ").append(tinyMassCount)
                    .append(" particle(s) have mass < 1e-33 kg");
        }
        builder.append(". Inverse-square forces can spike and explicit-Euler integration may blow up.");
        return builder.toString();
    }

    private static void rebuildCards(List<ParticleCardPanel> cards, JPanel cardsContainer, JButton minusButton,
            JButton plusButton, JButton bottomPlusButton) {
        cardsContainer.removeAll();

        boolean canRemove = cards.size() > MIN_PARTICLES;
        boolean canAdd = cards.size() < MAX_PARTICLES;
        minusButton.setEnabled(canRemove);
        plusButton.setEnabled(canAdd);
        bottomPlusButton.setEnabled(canAdd);

        for (int i = 0; i < cards.size(); i++) {
            ParticleCardPanel card = cards.get(i);
            int index = i;
            card.removeButton.setEnabled(canRemove);
            for (java.awt.event.ActionListener listener : card.removeButton.getActionListeners()) {
                card.removeButton.removeActionListener(listener);
            }
            card.removeButton.addActionListener(event -> {
                if (cards.size() > MIN_PARTICLES) {
                    cards.remove(index);
                    rebuildCards(cards, cardsContainer, minusButton, plusButton, bottomPlusButton);
                }
            });

            cardsContainer.add(card.panel);
        }

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private static final class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        static ValidationResult valid() {
            return new ValidationResult(true, "");
        }

        static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
    }

    private static final class ParticleCardPanel {
        private final JPanel panel;
        private final JButton removeButton;
        private final JTextField idField;
        private final JTextField massField;
        private final JSpinner chargeSpinner;
        private final JSpinner redSpinner;
        private final JSpinner greenSpinner;
        private final JSpinner blueSpinner;
        private final JSpinner renderSizeSpinner;
        private final JSpinner glowStrengthSpinner;
        private final JSpinner glowRadiusSpinner;
        private final JCheckBox scaleWithMassCheckbox;
        private final JCheckBox nucleonCheckbox;

        private ParticleCardPanel(ParticleDefinition definition) {
            panel = new JPanel(new BorderLayout(8, 8));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)));

            JPanel header = new JPanel(new BorderLayout());
            JLabel title = new JLabel("Particle");
            removeButton = new JButton("X");
            header.add(title, BorderLayout.WEST);
            header.add(removeButton, BorderLayout.EAST);
            panel.add(header, BorderLayout.NORTH);

            idField = new JTextField(definition.id(), 12);
            massField = new JTextField(String.format("%.8e", definition.mass()), 12);
            chargeSpinner = new JSpinner(new SpinnerNumberModel(definition.charge(), -4, 4, 1));

            Vector3f color = definition.color();
            redSpinner = new JSpinner(new SpinnerNumberModel((double) color.x, 0.0, 1.0, 0.05));
            greenSpinner = new JSpinner(new SpinnerNumberModel((double) color.y, 0.0, 1.0, 0.05));
            blueSpinner = new JSpinner(new SpinnerNumberModel((double) color.z, 0.0, 1.0, 0.05));

            ParticleRenderStyle style = definition.renderStyle();
            renderSizeSpinner = new JSpinner(new SpinnerNumberModel((double) style.sphereRadiusScale(), 0.1, 8.0, 0.1));
            glowStrengthSpinner = new JSpinner(new SpinnerNumberModel((double) style.glowStrength(), 0.0, 2.0, 0.05));
            glowRadiusSpinner = new JSpinner(new SpinnerNumberModel((double) style.glowRadiusScale(), 0.0, 4.0, 0.05));
            scaleWithMassCheckbox = new JCheckBox("Scale with mass", style.scaleWithCbrtMass());
            nucleonCheckbox = new JCheckBox("Nucleon", definition.isNucleon());

            JPanel fields = new JPanel(new GridLayout(0, 2, 6, 6));
            fields.add(new JLabel("Id"));
            fields.add(idField);
            fields.add(new JLabel("Mass (kg)"));
            fields.add(massField);
            fields.add(new JLabel("Charge (e)"));
            fields.add(chargeSpinner);
            fields.add(new JLabel("Color R"));
            fields.add(redSpinner);
            fields.add(new JLabel("Color G"));
            fields.add(greenSpinner);
            fields.add(new JLabel("Color B"));
            fields.add(blueSpinner);
            fields.add(new JLabel("Render size"));
            fields.add(renderSizeSpinner);
            fields.add(new JLabel("Glow strength"));
            fields.add(glowStrengthSpinner);
            fields.add(new JLabel("Glow radius"));
            fields.add(glowRadiusSpinner);
            fields.add(scaleWithMassCheckbox);
            fields.add(nucleonCheckbox);
            panel.add(fields, BorderLayout.CENTER);
        }

        static ParticleCardPanel newTemplate(int index) {
            return new ParticleCardPanel(new CustomParticleDefinition(
                    "particle_" + index,
                    1.0e-30,
                    0,
                    new Vector3f(0.8f, 0.8f, 0.8f),
                    new ParticleRenderStyle(1.2f, true, 0.2f, 1.0f),
                    false));
        }

        void randomize(Random rng) {
            idField.setText("particle_" + (1 + rng.nextInt(900)));
            double exp = -32.0 + rng.nextDouble() * 5.0;
            double mass = Math.pow(10.0, exp);
            massField.setText(String.format("%.8e", mass));
            chargeSpinner.setValue(rng.nextInt(5) - 2);
            redSpinner.setValue(0.15 + rng.nextDouble() * 0.75);
            greenSpinner.setValue(0.15 + rng.nextDouble() * 0.75);
            blueSpinner.setValue(0.15 + rng.nextDouble() * 0.75);
            renderSizeSpinner.setValue(0.8 + rng.nextDouble() * 2.0);
            glowStrengthSpinner.setValue(rng.nextDouble() * 0.9);
            glowRadiusSpinner.setValue(0.6 + rng.nextDouble() * 1.6);
            scaleWithMassCheckbox.setSelected(rng.nextBoolean());
            nucleonCheckbox.setSelected(rng.nextBoolean());
        }

        ParticleDefinition toDefinition() {
            String id = idField.getText().trim();
            double mass = Double.parseDouble(massField.getText().trim());
            int charge = ((Number) chargeSpinner.getValue()).intValue();

            float r = ((Number) redSpinner.getValue()).floatValue();
            float g = ((Number) greenSpinner.getValue()).floatValue();
            float b = ((Number) blueSpinner.getValue()).floatValue();

            float renderSize = ((Number) renderSizeSpinner.getValue()).floatValue();
            float glowStrength = ((Number) glowStrengthSpinner.getValue()).floatValue();
            float glowRadius = ((Number) glowRadiusSpinner.getValue()).floatValue();
            boolean scaleWithMass = scaleWithMassCheckbox.isSelected();

            return new CustomParticleDefinition(id, mass, charge, new Vector3f(r, g, b),
                    new ParticleRenderStyle(renderSize, scaleWithMass, glowStrength, glowRadius),
                    nucleonCheckbox.isSelected());
        }
    }
}
