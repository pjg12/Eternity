package eternity;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.text.JTextComponent;

public class PanelCharMinion extends PanelCharBase {
	private static final long serialVersionUID = 1L;
	private static final int MINION_TAB_HEIGHT = 560;

	private final JTabbedPane minionTabs;
	private final ArrayList<MinionSlotPanel> slotPanels;
	private String cachedTabSignature = "";

	PanelCharMinion(StoreRuleManager dataQuery, FrameSheet sheetFrame) {
		super(dataQuery, sheetFrame);
		setBackground(new Color(169, 169, 169));

		slotPanels = new ArrayList<>();
		minionTabs = new JTabbedPane();
		add(minionTabs);
	}

	public void updateAll() {
		syncMinionTabs();
		for (MinionSlotPanel slotPanel : slotPanels) {
			slotPanel.updateCharacter(character);
		}
		resizeSheet();
	}

	public void resizeSheet() {
		pageHeight = resizeHeader();
		minionTabs.setBounds(5, pageHeight, 555, MINION_TAB_HEIGHT);
		pageHeight += MINION_TAB_HEIGHT + 10;
		setPreferredSize(new Dimension(580, pageHeight));
	}

	public List<MinionInitiativeInfo> getSummonedMinionInitiativeInfo() {
		ArrayList<MinionInitiativeInfo> info = new ArrayList<>();
		for (MinionSlotPanel slotPanel : slotPanels) {
			if (slotPanel == null || !slotPanel.isSummoned()) continue;
			info.add(new MinionInitiativeInfo(slotPanel.getSlotTitle(), slotPanel.getInitiativeOffset()));
		}
		return info;
	}

	private void syncMinionTabs() {
		List<String> requiredTitles = buildRequiredMinionTitles();
		String newSignature = String.join("|", requiredTitles);
		if (newSignature.equals(cachedTabSignature)) {
			return;
		}

		cachedTabSignature = newSignature;
		minionTabs.removeAll();
		slotPanels.clear();

		for (String title : requiredTitles) {
			MinionSlotPanel slotPanel = new MinionSlotPanel(dataQuery, sheetFrame, title);
			slotPanels.add(slotPanel);
			minionTabs.addTab(title, wrapSlotPanel(slotPanel));
		}
	}

	private JScrollPane wrapSlotPanel(MinionSlotPanel panel) {
		JScrollPane scroll = new JScrollPane(panel);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(15);
		return scroll;
	}

	private List<String> buildRequiredMinionTitles() {
		ArrayList<String> titles = new ArrayList<>();
		int level = character == null ? 1 : Math.max(1, character.getLevel());
		titles.add("Minor 1");
		if (level >= 6) titles.add("Minor 2");
		if (level >= 9) titles.add("Medium 1");
		if (level >= 12) titles.add("Minor 3");
		if (level >= 14) titles.add("Medium 2");
		if (level >= 16) titles.add("Major");
		return titles;
	}

	public static record MinionInitiativeInfo(String slotTitle, int initiativeOffset) {}

	private static class MinionSlotPanel extends PanelCharMain {
		private static final long serialVersionUID = 1L;
		private static final String[] MINION_ATTRIBUTE_KEYS = { "STR", "DEX", "CON", "FOC", "CTL", "CAP", "KNOW", "MECH", "PERC", "INT", "CHA", "SUB" };
		private static final String MINION_FORM_LIST = "Minion Form";
		private static final String MINION_TYPE_LIST = "Minion Type";
		private static final int SELECTOR_X = 205;
		private static final int SELECTOR_WIDTH = 150;
		private static final int SELECTOR_HEIGHT = 20;
		private static final int SELECTOR_GAP = 5;
		private static final int SELECTOR_BLOCK_HEIGHT = (SELECTOR_HEIGHT * 2) + SELECTOR_GAP;
		private static final int SELECTOR_BOTTOM_PADDING = 10;
		private final String slotTitle;
		private String summonState = "Not Summoned";
		private final JComboBox<String> minionFormBox;
		private final JComboBox<String> minionTypeBox;
		private String selectedMinionForm = "";
		private String selectedMinionType = "";

		MinionSlotPanel(StoreRuleManager dataQuery, FrameSheet sheetFrame, String slotTitle) {
			super(dataQuery, sheetFrame);
			this.slotTitle = slotTitle == null ? "" : slotTitle;
			minionFormBox = buildComboBox();
			minionFormBox.addActionListener(e -> {
				Object selected = minionFormBox.getSelectedItem();
				selectedMinionForm = selected == null ? "" : selected.toString();
				refreshMinionStatDisplay();
			});
			minionTypeBox = buildComboBox();
			minionTypeBox.addActionListener(e -> {
				Object selected = minionTypeBox.getSelectedItem();
				selectedMinionType = selected == null ? "" : selected.toString();
				refreshMinionStatDisplay();
			});
		}

		@Override
		public void updateAll() {
			refreshAttributesOnly();
			refreshStatisticsOnly();
			refreshSelectors();
			super.resizeSheet();
			trimUnusedSections();
		}

		@Override
		protected boolean shouldShowLeaderPower() {
			return false;
		}

		@Override
		protected boolean useHeaderToggle() {
			return true;
		}

		@Override
		protected String[] getHeaderToggleOptions() {
			return new String[] { "Not Summoned", "Summoned" };
		}

		@Override
		protected int getAdditionalHeaderControlsHeight() {
			if (minionFormBox == null || minionTypeBox == null) return 0;
			return SELECTOR_BOTTOM_PADDING;
		}

		@Override
		protected void layoutAdditionalHeaderControls(int startY) {
			if (minionFormBox == null || minionTypeBox == null) return;
			int topY = Math.max(0, startY - SELECTOR_BLOCK_HEIGHT + SELECTOR_GAP);
			minionFormBox.setBounds(SELECTOR_X, topY, SELECTOR_WIDTH, SELECTOR_HEIGHT);
			minionFormBox.setVisible(true);
			minionTypeBox.setBounds(SELECTOR_X, topY + SELECTOR_HEIGHT + SELECTOR_GAP, SELECTOR_WIDTH, SELECTOR_HEIGHT);
			minionTypeBox.setVisible(true);
		}

		@Override
		protected String getHeaderToggleSelection() {
			return summonState;
		}

		@Override
		protected void onHeaderToggleChanged(String selection) {
			if (selection == null || selection.isBlank()) {
				summonState = "Not Summoned";
				return;
			}
			summonState = selection;
		}

		@Override
		protected boolean useCombinedPowerHpHeader() {
			return true;
		}

		@Override
		protected String getPowerHeaderTitle() {
			return "Power";
		}

		@Override
		protected String getPowerHeaderLabel() {
			return "Provided Value";
		}

		@Override
		protected double getPowerHeaderValue() {
			return resolveMinionPowerValue();
		}

		@Override
		protected boolean showCombinedHpFields() {
			return "Summoned".equalsIgnoreCase(summonState);
		}

		@Override
		protected int getCombinedHeaderCurrentHp() {
			return getCombinedHeaderMaxHp();
		}

		@Override
		protected int getCombinedHeaderMaxHp() {
			if (character == null || dataQuery == null) return 0;
			DataLevel levelData = dataQuery.getLevel(Math.max(1, character.getLevel()));
			DataClass effectiveClass = resolveEffectiveClass();
			if (levelData == null || effectiveClass == null) return 0;
			double baseHp = Math.max(0.0, levelData.getBaseHP() * effectiveClass.getHpScaling());
			double conValue = Math.max(0.0, getPowerHeaderValue());
			double hpMultiplier = 1.0 + (Math.round(conValue * 0.5) / 10.0);
			return (int) Math.max(0.0, baseHp * hpMultiplier);
		}

		@Override
		protected CharAttributes getDisplayedAttributes() {
			double leaderPower = resolveMinionPowerValue();
			CharAttributes minionAttributes = new CharAttributes();
			for (String attributeKey : MINION_ATTRIBUTE_KEYS) {
				setBaseAttributeValue(minionAttributes, attributeKey, leaderPower);
			}
			minionAttributes.refreshLinkedAttributeStatuses();
			applyFormBonuses(minionAttributes, leaderPower);
			applyTypeBonuses(minionAttributes, leaderPower);
			return minionAttributes;
		}

		@Override
		protected double resolveDisplayedAttributeValue(CharAttributes attrs, String key) {
			for (String attributeKey : MINION_ATTRIBUTE_KEYS) {
				if (attributeKey.equalsIgnoreCase(key)) {
					return getPowerHeaderValue();
				}
			}
			return super.resolveDisplayedAttributeValue(attrs, key);
		}

		@Override
		protected double resolveDisplayedInitiativeValue(CharAttributes attrs) {
			return switch (slotTitle.trim()) {
				case "Minor 1" -> -4.0;
				case "Minor 2" -> -8.0;
				case "Minor 3" -> -2.0;
				case "Medium 1" -> -3.0;
				case "Medium 2" -> -7.0;
				case "Major" -> -6.0;
				default -> 0.0;
			};
		}

		@Override
		protected boolean shouldShowInitiativeRollButton() {
			return false;
		}

		private void setBaseAttributeValue(CharAttributes attributes, String attributeKey, double value) {
			if (attributes == null || attributeKey == null || attributeKey.isBlank()) return;
			String normalized = "B" + attributeKey.trim().toUpperCase();
			for (ArrayList<DataStatus>[] block : attributes.getBAttributes()) {
				if (block == null || block.length == 0 || block[0] == null || block[0].isEmpty()) continue;
				DataStatus baseStatus = block[0].get(0);
				if (baseStatus == null || !normalized.equalsIgnoreCase(baseStatus.getAttribute())) continue;
				baseStatus.setSeverity(value);
				baseStatus.setDescription("Derived from leader POWER");
				return;
			}
		}

		private void applyFormBonuses(CharAttributes attributes, double powerValue) {
			if (attributes == null || !isGeneralizedFormSelected()) return;
			addPassiveBonus(attributes.getBDefense(), "BARMOR", powerValue, "Generalized Form");
			addPassiveBonus(attributes.getBDefense(), "BFORT", powerValue, "Generalized Form");
			addPassiveBonus(attributes.getBDefense(), "BREF", powerValue, "Generalized Form");
			addPassiveBonus(attributes.getBDefense(), "BWILL", powerValue, "Generalized Form");
			addPassiveBonus(attributes.getBCombat(), "BATK", powerValue, "Generalized Form");
			addPassiveBonus(attributes.getBCombat(), "BAPP", powerValue, "Generalized Form");
			addPassiveBonus(attributes.getBResist(), "BALL", powerValue, "Generalized Form");
		}

		private void applyTypeBonuses(CharAttributes attributes, double primaryAttributeValue) {
			if (attributes == null) return;
			if ("Melee".equalsIgnoreCase(selectedMinionType == null ? "" : selectedMinionType.trim())) {
				applyPrimaryAttributeBonuses(attributes, primaryAttributeValue, "Melee Type");
			}
		}

		private void applyPrimaryAttributeBonuses(CharAttributes attributes, double primaryAttributeValue, String sourceName) {
			if (attributes == null || sourceName == null || sourceName.isBlank()) return;
			double severity = Math.round((primaryAttributeValue * 0.25) * 10.0) / 10.0;
			addPassiveBonus(attributes.getBDamage(), "BTDMG", severity, sourceName);
			addPassiveBonus(attributes.getBDamage(), "BBDMG", severity, sourceName);
			addPassiveBonus(attributes.getBCombat(), "BATK", severity, sourceName);
			addPassiveBonus(attributes.getBDamage(), "BTHEAL", severity, sourceName);
			addPassiveBonus(attributes.getBDamage(), "BBHEAL", severity, sourceName);
			addPassiveBonus(attributes.getBCombat(), "BAPP", severity, sourceName);
		}

		private boolean isGeneralizedFormSelected() {
			if (selectedMinionForm == null) return false;
			String normalized = selectedMinionForm.trim();
			return "Generalized".equalsIgnoreCase(normalized) || "Generalize".equalsIgnoreCase(normalized);
		}

		private void addPassiveBonus(ArrayList<DataStatus>[][] category, String attribute, double severity, String sourceName) {
			if (category == null || attribute == null || sourceName == null) return;
			for (ArrayList<DataStatus>[] block : category) {
				if (block == null || block.length == 0 || block[0] == null || block[0].isEmpty()) continue;
				DataStatus baseStatus = block[0].get(0);
				if (baseStatus == null || !attribute.equalsIgnoreCase(baseStatus.getAttribute())) continue;
				DataStatus bonusStatus = new DataStatus();
				bonusStatus.setName(sourceName);
				bonusStatus.setAttribute(attribute);
				bonusStatus.setAffinity("None");
				bonusStatus.setDescription("Derived from minion form");
				bonusStatus.setDurationType("Passive");
				bonusStatus.setSeverity(severity);
				block[0].add(bonusStatus);
				return;
			}
		}

		private DataClass resolveEffectiveClass() {
			if (character == null || character.getIdentity() == null || dataQuery == null) return null;
			String subclassName = character.getIdentity().getCharSubclass();
			if (subclassName != null && !subclassName.isBlank() && !"***".equals(subclassName.trim()) && !"?".equals(subclassName.trim())) {
				DataClass subclass = dataQuery.getClassByName(subclassName);
				if (subclass != null) return subclass;
			}
			String className = character.getIdentity().getCharClass();
			if (className == null || className.isBlank() || "?".equals(className.trim())) return null;
			return dataQuery.getClassByName(className);
		}

		private double resolveMinionPowerValue() {
			if (character == null || character.getAttributes() == null) return 0.0;
			DataClass effectiveClass = resolveEffectiveClass();
			if (effectiveClass == null || effectiveClass.getPrimaryAtt() == null || effectiveClass.getPrimaryAtt().isBlank()) {
				return 0.0;
			}
			double primaryValue = Math.max(0.0, character.getAttributes().calcStatusValue(effectiveClass.getPrimaryAtt().trim().toUpperCase()));
			double levelValue = character.getIdentity() == null ? 0.0 : Math.max(1, character.getIdentity().getLevel());
			return Math.round(((primaryValue * 0.5) + levelValue) * 10.0) / 10.0;
		}

		private void refreshMinionStatDisplay() {
			if (character == null) return;
			refreshAttributesOnly();
			refreshStatisticsOnly();
			refreshHPAuraOnly();
			revalidate();
			repaint();
		}

		private boolean isSummoned() {
			return "Summoned".equalsIgnoreCase(summonState);
		}

		private String getSlotTitle() {
			return slotTitle;
		}

		private int getInitiativeOffset() {
			return (int) Math.round(resolveDisplayedInitiativeValue(null));
		}

		private void refreshSelectors() {
			refreshSelector(minionFormBox, collectListEntries(MINION_FORM_LIST), selectedMinionForm, true);
			Object selectedForm = minionFormBox.getSelectedItem();
			selectedMinionForm = selectedForm == null ? "" : selectedForm.toString();

			refreshSelector(minionTypeBox, collectListEntries(MINION_TYPE_LIST), selectedMinionType, false);
			Object selectedType = minionTypeBox.getSelectedItem();
			selectedMinionType = selectedType == null ? "" : selectedType.toString();
		}

		private void refreshSelector(JComboBox<String> box, List<String> options, String selectedValue, boolean fallbackToFirst) {
			if (box == null) return;
			box.removeAllItems();
			if (options.isEmpty()) {
				box.addItem("*** Empty ***");
			} else {
				for (String option : options) {
					box.addItem(option);
				}
			}
			if (selectedValue != null && !selectedValue.isBlank()) {
				box.setSelectedItem(selectedValue);
			}
			if (box.getSelectedIndex() < 0 && fallbackToFirst && box.getItemCount() > 0) {
				box.setSelectedIndex(0);
			}
			if (box.getSelectedIndex() < 0 && box.getItemCount() > 0) {
				box.setSelectedIndex(0);
			}
			box.setVisible(true);
		}

		private List<String> collectListEntries(String listName) {
			LinkedHashSet<String> entries = new LinkedHashSet<>();
			if (character == null || character.getLists() == null || listName == null || listName.isBlank()) {
				return new ArrayList<>(entries);
			}
			for (List<DataList> group : character.getLists()) {
				if (group == null) continue;
				for (DataList entry : group) {
					if (entry == null || entry.getList() == null || entry.getName() == null) continue;
					if (!listName.equalsIgnoreCase(entry.getList().trim())) continue;
					String name = entry.getName().trim();
					if (name.isBlank()) continue;
					entries.add(name);
				}
			}
			return new ArrayList<>(entries);
		}

		private void trimUnusedSections() {
			int detailsStartY = findComponentYByText("Character Name");
			int attributesStartY = findComponentYByText("Core Attribute");
			int skillsStartY = findComponentYByText("Skills");
			if (detailsStartY < 0 || attributesStartY < 0 || skillsStartY < 0) {
				revalidate();
				repaint();
				return;
			}

			int shiftedAttributeTop = detailsStartY;
			int shiftUp = attributesStartY - shiftedAttributeTop;
			int maxBottom = 0;
			for (Component component : getComponents()) {
				if (component == null || !component.isVisible()) continue;
				int y = component.getY();
				if (y >= detailsStartY && y < attributesStartY) {
					component.setVisible(false);
					continue;
				}
				if (y >= skillsStartY) {
					component.setVisible(false);
					continue;
				}
				if (y >= attributesStartY && y < skillsStartY) {
					component.setBounds(component.getX(), y - shiftUp, component.getWidth(), component.getHeight());
				}
				maxBottom = Math.max(maxBottom, component.getY() + component.getHeight());
			}

			setPreferredSize(new Dimension(580, maxBottom + 10));
			revalidate();
			repaint();
		}

		private int findComponentYByText(String text) {
			if (text == null || text.isBlank()) return -1;
			for (Component component : getComponents()) {
				if (!component.isVisible()) continue;
				String componentText = extractText(component);
				if (text.equals(componentText)) {
					return component.getY();
				}
			}
			return -1;
		}

		private String extractText(Component component) {
			if (component instanceof JLabel label) {
				return label.getText();
			}
			if (component instanceof AbstractButton button) {
				return button.getText();
			}
			if (component instanceof JTextComponent textComponent) {
				return textComponent.getText();
			}
			return null;
		}
	}
}
