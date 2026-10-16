package eternity;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import websocket.events.IncomingAttackEvent;

/**
 * Opens when the loaded character is the target of a Roll20 attack card.
 */
public class FrameAttacked extends JFrame {
	private static final long serialVersionUID = 1L;

	private final FrameSheet sheetFrame;
	private final StoreCharData character;

	private IncomingAttackEvent attack;
	private Boolean hit;

	private final JLabel statusLabel = new JLabel();
	private final JLabel incomingValue = new JLabel();
	private final JLabel typeValue = new JLabel();
	private final JLabel crushValue = new JLabel();
	private final JLabel mitigatedValue = new JLabel();
	private final JLabel shieldedValue = new JLabel();
	private final JLabel healthValue = new JLabel();
	private final JLabel shieldNote = new JLabel();
	private final JCheckBox evasionSaveCheckBox = new JCheckBox("Successful damage-reducing save");
	private final JPanel interruptList = new JPanel();
	private final JLabel emptyInterrupts = new JLabel("No receiving-attack interrupts available.");

	FrameAttacked(FrameSheet sheetFrame, StoreCharData character, IncomingAttackEvent attack, Boolean hit) {
		super("Attacked");
		this.sheetFrame = sheetFrame;
		this.character = character;
		this.attack = attack;
		this.hit = hit;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout(10, 10));
		setSize(520, 520);
		setLocationRelativeTo(sheetFrame);
		setResizable(false);

		add(buildInfoPanel(), BorderLayout.NORTH);
		add(buildInterruptPanel(), BorderLayout.CENTER);
		add(buildFooter(), BorderLayout.SOUTH);

		addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				refreshDisplay();
			}
		});

		refreshDisplay();
	}

	void updateIncomingAttack(IncomingAttackEvent attack, Boolean hit) {
		this.attack = attack;
		this.hit = hit;
		refreshDisplay();
	}

	private JPanel buildInfoPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));

		statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 16f));
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, 10, 0);
		c.weightx = 1.0;
		panel.add(statusLabel, c);

		addInfoRow(panel, 1, "Incoming Damage", incomingValue);
		addInfoRow(panel, 2, "Damage Type", typeValue);
		addInfoRow(panel, 3, "Crush", crushValue);
		addInfoRow(panel, 4, "After Mitigation", mitigatedValue);
		addInfoRow(panel, 5, "After Shielding", shieldedValue);
		addInfoRow(panel, 6, "Resulting Health", healthValue);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 7;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(2, 0, 4, 0);
		shieldNote.setFont(shieldNote.getFont().deriveFont(Font.ITALIC, 11f));
		panel.add(shieldNote, c);

		if (hasEvasionSpecialty()) {
			evasionSaveCheckBox.setToolTipText(
					"If this effect allowed a save to reduce damage and the save succeeded, Evasion reduces the damage to 0.");
			evasionSaveCheckBox.addActionListener(e -> {
				refreshMitigatedDamage();
				refreshResultingHealth();
			});
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 8;
			c.gridwidth = 2;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(4, 0, 0, 0);
			panel.add(evasionSaveCheckBox, c);
		}

		return panel;
	}

	private void addInfoRow(JPanel panel, int row, String labelText, JLabel valueLabel) {
		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridx = 0;
		labelConstraints.gridy = row;
		labelConstraints.anchor = GridBagConstraints.WEST;
		labelConstraints.insets = new Insets(3, 0, 3, 12);
		JLabel label = new JLabel(labelText);
		label.setFont(label.getFont().deriveFont(Font.PLAIN, 13f));
		panel.add(label, labelConstraints);

		GridBagConstraints valueConstraints = new GridBagConstraints();
		valueConstraints.gridx = 1;
		valueConstraints.gridy = row;
		valueConstraints.anchor = GridBagConstraints.WEST;
		valueConstraints.weightx = 1.0;
		valueConstraints.fill = GridBagConstraints.HORIZONTAL;
		valueConstraints.insets = new Insets(3, 0, 3, 0);
		valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 13f));
		panel.add(valueLabel, valueConstraints);
	}

	private JPanel buildInterruptPanel() {
		JPanel panel = new JPanel(new BorderLayout(0, 6));
		panel.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));

		JLabel header = new JLabel("Receiving Attack Interrupts");
		header.setFont(header.getFont().deriveFont(Font.BOLD, 13f));
		panel.add(header, BorderLayout.NORTH);

		interruptList.setLayout(new BoxLayout(interruptList, BoxLayout.Y_AXIS));
		JScrollPane scroll = new JScrollPane(interruptList);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(scroll, BorderLayout.CENTER);
		return panel;
	}

	private JPanel buildFooter() {
		JPanel footer = new JPanel();
		footer.setBorder(BorderFactory.createEmptyBorder(4, 16, 12, 16));
		JButton confirmButton = new JButton("Confirm");
		confirmButton.addActionListener(e -> confirmPressed());
		footer.add(confirmButton);
		return footer;
	}

	private void confirmPressed() {
		if (!Boolean.FALSE.equals(hit) && character != null && character.getResources() != null) {
			Integer beforeShield = computeMitigatedBeforeShield();
			Integer afterShield = computeAfterMitigation();
			if (beforeShield != null) {
				double absorbed = Math.min(currentShield(), beforeShield.intValue());
				if (absorbed > 0.0) {
					character.getResources().setShield(Math.max(0.0, currentShield() - absorbed));
				}
			}
			if (afterShield != null && afterShield.intValue() > 0) {
				character.applyIncomingHealthDamage(afterShield.intValue());
				if (character.getCombat() != null && character.getCombat().getIncapacitateTokens() > 0) {
					character.getCombat().consumeIncapacitateToken();
				}
			}
			if (sheetFrame != null) {
				sheetFrame.refreshMainPanel();
			}
		}
		setVisible(false);
		dispose();
	}

	private void refreshDisplay() {
		Integer critCount = resolveCritCount();
		if (Boolean.FALSE.equals(hit)) {
			statusLabel.setText("Miss");
		} else if (Boolean.TRUE.equals(hit) && critCount != null && critCount.intValue() > 0) {
			statusLabel.setText("CRITICAL HIT x" + critCount.intValue());
		} else if (Boolean.TRUE.equals(hit)) {
			statusLabel.setText("Hit");
		} else {
			statusLabel.setText("Incoming Attack");
		}

		incomingValue.setText(formatIncomingDamage());
		typeValue.setText(resolveDamageType().isBlank() ? "—" : resolveDamageType());
		double crush = resolveCrush();
		crushValue.setText(crush > 0.0 ? trimNumber(crush) : "—");
		refreshMitigatedDamage();
		refreshResultingHealth();
		refreshInterruptList();
	}

	String getChatOutcome() {
		Integer critCount = resolveCritCount();
		if (Boolean.FALSE.equals(hit)) {
			return "Miss";
		}
		if (Boolean.TRUE.equals(hit) && critCount != null && critCount.intValue() > 0) {
			return "Critical Hit x" + critCount.intValue();
		}
		if (Boolean.TRUE.equals(hit)) {
			return "Hit";
		}
		return null;
	}

	Integer getChatMitigatedDamage() {
		if (Boolean.FALSE.equals(hit)) {
			return 0;
		}
		return computeMitigatedBeforeShield();
	}

	Integer getChatAfterShielding() {
		return computeAfterMitigation();
	}

	String getChatResultingHealth() {
		return formatResultingHealth(computeResultingHealth());
	}

	String getChatVsDefense() {
		return resolveVsDefense();
	}

	private Integer resolveCritCount() {
		Double attackValue = attack == null ? null : attack.attackValue;
		if (attackValue == null || attackValue.doubleValue() <= 0.0) {
			return null;
		}
		String vs = resolveVsDefense();
		if (!isComparedDefense(vs)) {
			return null;
		}
		double defense = sheetFrame == null ? 0.0 : sheetFrame.getDisplayedDefenseAgainst(vs);
		if (defense <= 0.0) {
			return null;
		}
		return (int) Math.floor(attackValue.doubleValue() / defense) - 1;
	}

	private String resolveVsDefense() {
		if (attack == null || attack.vsDefense == null) {
			return "";
		}
		String vs = attack.vsDefense.trim();
		if (vs.regionMatches(true, 0, "vs ", 0, 3)) {
			vs = vs.substring(3).trim();
		}
		return vs;
	}

	private static boolean isComparedDefense(String vs) {
		return "AC".equalsIgnoreCase(vs)
				|| "DODGE".equalsIgnoreCase(vs)
				|| "ARMOR".equalsIgnoreCase(vs);
	}

	private void refreshMitigatedDamage() {
		if (Boolean.FALSE.equals(hit)) {
			mitigatedValue.setText("0");
			shieldedValue.setText("0");
			shieldNote.setText("");
			return;
		}
		Double incoming = resolveIncomingAmount();
		if (incoming == null) {
			mitigatedValue.setText("—");
			shieldedValue.setText("—");
			shieldNote.setText("Incoming damage has not resolved yet.");
			return;
		}
		Integer beforeShield = computeMitigatedBeforeShield();
		Integer afterShield = computeAfterMitigation();
		if (beforeShield == null || afterShield == null) {
			mitigatedValue.setText("—");
			shieldedValue.setText("—");
			shieldNote.setText("");
			return;
		}
		mitigatedValue.setText(Integer.toString(beforeShield.intValue()));
		shieldedValue.setText(Integer.toString(afterShield.intValue()));
		double shield = currentShield();
		if (shield > 0.0) {
			double absorbed = Math.min(shield, beforeShield.intValue());
			shieldNote.setText(trimNumber(absorbed) + " absorbed by current Shield.");
		} else {
			shieldNote.setText("");
		}
	}

	private void refreshResultingHealth() {
		String resulting = formatResultingHealth(computeResultingHealth());
		healthValue.setText(resulting == null ? "—" : resulting);
	}

	private Integer computeResultingHealth() {
		Integer mitigated = computeAfterMitigation();
		if (mitigated == null || character == null || character.getResources() == null) {
			return null;
		}
		return character.getResources().calcCurrentHP() - mitigated.intValue();
	}

	private String formatResultingHealth(Integer resulting) {
		if (resulting == null) {
			return null;
		}
		String text = Integer.toString(resulting.intValue());
		if (resulting.intValue() < 0
				&& character != null
				&& character.getResources() != null
				&& Math.abs(resulting.intValue()) >= character.getResources().calcMaxHP()) {
			text += " (Dead)";
		}
		return text;
	}

	private Integer computeAfterMitigation() {
		if (Boolean.FALSE.equals(hit)) {
			return 0;
		}
		Integer mitigated = computeMitigatedBeforeShield();
		if (mitigated == null) {
			return null;
		}
		return (int) Math.max(0.0, mitigated.intValue() - currentShield());
	}

	private Integer computeMitigatedBeforeShield() {
		Double incoming = resolveIncomingAmount();
		if (incoming == null) {
			return null;
		}
		boolean ignoreResist = "DIVINE".equalsIgnoreCase(resolveDamageType()) || "TRUE".equalsIgnoreCase(resolveDamageType());
		return FrameCombatDamage.computeMitigatedDamage(
				character,
				incoming.doubleValue(),
				resolveDamageType(),
				resolveCrush(),
				ignoreResist,
				hasEvasionSpecialty() && evasionSaveCheckBox.isSelected());
	}

	private double currentShield() {
		if (character == null || character.getResources() == null) {
			return 0.0;
		}
		return Math.max(0.0, character.getResources().getShield());
	}

	private void refreshInterruptList() {
		interruptList.removeAll();
		FrameCombat combat = sheetFrame == null ? null : sheetFrame.ensureCombatHelperFrame();
		List<DataAction> interrupts = combat == null ? List.of() : combat.getReceivingAttackInterrupts();
		if (interrupts.isEmpty()) {
			emptyInterrupts.setAlignmentX(LEFT_ALIGNMENT);
			interruptList.add(emptyInterrupts);
		} else {
			for (DataAction action : interrupts) {
				if (action == null) continue;
				JButton button = new JButton(action.getName() == null ? "Interrupt" : action.getName());
				button.setAlignmentX(LEFT_ALIGNMENT);
				button.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 36));
				button.addActionListener(e -> activateInterrupt(action));
				interruptList.add(button);
				interruptList.add(Box.createVerticalStrut(6));
			}
		}
		interruptList.revalidate();
		interruptList.repaint();
	}

	private void activateInterrupt(DataAction action) {
		FrameCombat combat = sheetFrame == null ? null : sheetFrame.ensureCombatHelperFrame();
		if (combat == null || action == null) {
			return;
		}
		combat.activateReceivingAttackInterrupt(action);
		refreshDisplay();
	}

	private String formatIncomingDamage() {
		Double incoming = resolveIncomingAmount();
		return incoming == null ? "—" : trimNumber(incoming.doubleValue());
	}

	private Double resolveIncomingAmount() {
		if (attack == null || attack.damageAmount == null) {
			return null;
		}
		double incoming = attack.damageAmount.doubleValue();
		Integer critCount = resolveCritCount();
		if (critCount == null || critCount.intValue() <= 0) {
			return incoming;
		}
		double criticalDamage = attack.criticalDamage == null ? 0.0 : attack.criticalDamage.doubleValue();
		double multiplier = 1.0 + ((1.0 + criticalDamage) * critCount.intValue());
		return incoming * multiplier;
	}

	private String resolveDamageType() {
		if (attack == null || attack.damageType == null) {
			return "";
		}
		return attack.damageType.trim().toUpperCase(java.util.Locale.ROOT);
	}

	private double resolveCrush() {
		if (attack == null || attack.crush == null) {
			return 0.0;
		}
		return Math.max(0.0, attack.crush.doubleValue());
	}

	private boolean hasEvasionSpecialty() {
		return character != null
				&& character.getSpecials() != null
				&& character.getSpecials().hasSpecialty("Evasion");
	}

	private String trimNumber(double value) {
		if (Math.abs(value - Math.rint(value)) < 0.0001) {
			return Integer.toString((int) Math.rint(value));
		}
		return Double.toString(Math.round(value * 100.0) / 100.0);
	}
}
