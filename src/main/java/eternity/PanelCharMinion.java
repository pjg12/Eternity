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

import eternity.DataTypes.DataClass;
import eternity.DataTypes.DataList;
import eternity.Minions.CharMinions;
import eternity.Minions.MinionSlotState;

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
		if (character != null && character.getMinions() != null) {
			for (MinionSlotState slot : character.getMinions().getSummonedSlots()) {
				if (slot == null) continue;
				info.add(new MinionInitiativeInfo(slot.getSlotTitle(), slot.getInitiativeOffset()));
			}
			return info;
		}
		for (MinionSlotPanel slotPanel : slotPanels) {
			if (slotPanel == null || !slotPanel.isSummoned()) continue;
			info.add(new MinionInitiativeInfo(slotPanel.getSlotTitle(), slotPanel.getInitiativeOffset()));
		}
		return info;
	}

	public List<SummonedMinionCombatInfo> getSummonedMinionCombatInfo() {
		ArrayList<SummonedMinionCombatInfo> info = new ArrayList<>();
		if (character != null && character.getMinions() != null) {
			for (CharMinions.SummonedMinionActor actor : character.getSummonedMinionActors()) {
				if (actor == null) continue;
				info.add(new SummonedMinionCombatInfo(actor.slotTitle(), actor.displayName(), actor.attackBonus()));
			}
			return info;
		}
		for (MinionSlotPanel slotPanel : slotPanels) {
			if (slotPanel == null || !slotPanel.isSummoned()) continue;
			info.add(slotPanel.getCombatInfo());
		}
		return info;
	}

	private void syncMinionTabs() {
		List<String> requiredTitles = buildRequiredMinionTitles();
		if (character != null && character.getMinions() != null) {
			character.getMinions().syncUnlockedSlots(requiredTitles);
		}
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
		if (character == null) {
			return List.of();
		}
		return character.getUnlockedMinionSlotTitles();
	}

	public static record MinionInitiativeInfo(String slotTitle, int initiativeOffset) {}
	public static record SummonedMinionCombatInfo(String slotTitle, String displayName, double attackBonus) {}

	private static class MinionSlotPanel extends PanelCharMain {
		private static final long serialVersionUID = 1L;
		private static final String MINION_FORM_LIST = "Minion Form";
		private static final String MINION_TYPE_LIST = "Minion Type";
		private static final int SELECTOR_X = 205;
		private static final int SELECTOR_WIDTH = 150;
		private static final int SELECTOR_HEIGHT = 20;
		private static final int SELECTOR_GAP = 5;
		private static final int SELECTOR_BLOCK_HEIGHT = (SELECTOR_HEIGHT * 2) + SELECTOR_GAP;
		private static final int SELECTOR_BOTTOM_PADDING = 10;
		private final String slotTitle;
		private boolean loadingFromStore;
		private final JComboBox<String> minionFormBox;
		private final JComboBox<String> minionTypeBox;

		MinionSlotPanel(StoreRuleManager dataQuery, FrameSheet sheetFrame, String slotTitle) {
			super(dataQuery, sheetFrame);
			this.slotTitle = slotTitle == null ? "" : slotTitle;
			this.loadingFromStore = false;
			minionFormBox = buildComboBox();
			minionFormBox.addActionListener(e -> {
				if (loadingFromStore) return;
				persistSlotState();
				refreshMinionStatDisplay();
			});
			minionTypeBox = buildComboBox();
			minionTypeBox.addActionListener(e -> {
				if (loadingFromStore) return;
				persistSlotState();
				refreshMinionStatDisplay();
			});
		}

		@Override
		public void updateAll() {
			loadFromStore();
			refreshAttributesOnly();
			refreshStatisticsOnly();
			refreshSelectors();
			super.resizeSheet();
			trimUnusedSections();
		}

		@Override
		protected String getDisplayedPanelReminder() {
			return "";
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
			MinionSlotState slot = resolveSlotState();
			return slot != null && slot.isSummoned() ? "Summoned" : "Not Summoned";
		}

		@Override
		protected void onHeaderToggleChanged(String selection) {
			if (loadingFromStore) return;
			boolean summoned = selection != null && "Summoned".equalsIgnoreCase(selection.trim());
			if (character != null && character.getMinions() != null) {
				character.getMinions().updateSlot(slotTitle, summoned, selectedForm(), selectedType());
			}
			refreshMinionStatDisplay();
			if (sheetFrame != null) {
				sheetFrame.refreshCombatActorSelectorIfOpen();
			}
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
			if (character != null && character.getMinions() != null) {
				return character.getMinions().resolveMinionPowerValue(resolveSlotState());
			}
			return 0.0;
		}

		@Override
		protected boolean showCombinedHpFields() {
			return isSummoned();
		}

		@Override
		protected int getCombinedHeaderCurrentHp() {
			if (character != null && character.getMinions() != null) {
				return character.getMinions().getCurrentHp(slotTitle);
			}
			return getCombinedHeaderMaxHp();
		}

		@Override
		protected int getCombinedHeaderMaxHp() {
			if (character != null && character.getMinions() != null) {
				return character.getMinions().calcMaxHp(slotTitle);
			}
			return 0;
		}

		@Override
		protected CharAttributes getDisplayedAttributes() {
			MinionSlotState slot = resolveSlotState();
			if (character != null && character.getMinions() != null && slot != null) {
				return character.getMinions().buildMinionAttributes(slot);
			}
			return new CharAttributes();
		}

		@Override
		protected boolean isPrimaryAttribute(String key, DataClass cls) {
			if (key == null || character == null || character.getMinions() == null) return false;
			String primary = character.getMinions().resolveMinionPrimaryAttributeKey(resolveSlotState());
			return primary != null && primary.equalsIgnoreCase(key);
		}

		@Override
		protected double resolveDisplayedInitiativeValue(CharAttributes attrs) {
			MinionSlotState slot = resolveSlotState();
			return slot == null ? 0.0 : slot.getInitiativeOffset();
		}

		@Override
		protected boolean shouldShowInitiativeRollButton() {
			return false;
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
			MinionSlotState slot = resolveSlotState();
			return slot != null && slot.isSummoned();
		}

		private String getSlotTitle() {
			return slotTitle;
		}

		private int getInitiativeOffset() {
			return (int) Math.round(resolveDisplayedInitiativeValue(null));
		}

		private SummonedMinionCombatInfo getCombatInfo() {
			MinionSlotState slot = resolveSlotState();
			String display = slot == null ? slotTitle : slot.buildDisplayName();
			CharAttributes attrs = getDisplayedAttributes();
			double attack = attrs == null ? 0.0 : Math.max(0.0, attrs.calcStatusValue("ATK"));
			return new SummonedMinionCombatInfo(slotTitle, display, attack);
		}

		private MinionSlotState resolveSlotState() {
			if (character == null || character.getMinions() == null) return null;
			return character.getMinions().getOrCreateSlot(slotTitle);
		}

		private void loadFromStore() {
			MinionSlotState slot = resolveSlotState();
			if (slot == null) return;
			loadingFromStore = true;
			try {
				refreshHeaderState(character);
			} finally {
				loadingFromStore = false;
			}
		}

		private void persistSlotState() {
			if (character == null || character.getMinions() == null) return;
			MinionSlotState slot = resolveSlotState();
			boolean summoned = slot != null && slot.isSummoned();
			character.getMinions().updateSlot(slotTitle, summoned, selectedForm(), selectedType());
			if (sheetFrame != null) {
				sheetFrame.refreshCombatActorSelectorIfOpen();
			}
		}

		private String selectedForm() {
			Object selected = minionFormBox == null ? null : minionFormBox.getSelectedItem();
			return selected == null ? "" : selected.toString();
		}

		private String selectedType() {
			Object selected = minionTypeBox == null ? null : minionTypeBox.getSelectedItem();
			return selected == null ? "" : selected.toString();
		}

		private void refreshSelectors() {
			MinionSlotState slot = resolveSlotState();
			String selectedForm = slot == null ? "" : slot.getForm();
			String selectedType = slot == null ? "" : slot.getType();
			loadingFromStore = true;
			try {
				refreshSelector(minionFormBox, collectListEntries(MINION_FORM_LIST), selectedForm, true);
				refreshSelector(minionTypeBox, collectListEntries(MINION_TYPE_LIST), selectedType, false);
			} finally {
				loadingFromStore = false;
			}
			persistSlotState();
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
