package eternity;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PanelCharList extends PanelCharBase {
	private static final long serialVersionUID = 1L;
	
	private ArrayList<JLabel> titles ;
	private ArrayList<ArrayList<JTextField>> lists;
	private String cachedListSignature = "";
	private final JButton removeItemButton;

	/*
	 * PARAMETERIZED CONSTRUCTOR
	 */
	PanelCharList (StoreRuleManager dataQuery, FrameSheet sheetFrame){
		super (dataQuery, sheetFrame);
		setBackground(new Color(169, 169, 169));
		
		titles = new ArrayList<JLabel>();
		lists = new ArrayList<ArrayList<JTextField>>();
		removeItemButton = buildButton("Remove Item");
		removeItemButton.addActionListener(e -> promptRemoveItem());
		removeItemButton.setToolTipText("Remove a list entry manually while GM mode is active.");
	} //END OF PARAMETERIZED CONSTRUCTOR
	
	/*
	 * 
	 * UPDATERS
	 * 
	 */
	/*
	 * updateMain - updates the main panel
	 */
	public void updateAll() {
		boolean wasButtonVisible = removeItemButton != null && removeItemButton.isVisible();
		boolean structureChanged = updateList();
		updateRemoveItemButtonVisibility();
		boolean buttonVisibilityChanged = removeItemButton != null && removeItemButton.isVisible() != wasButtonVisible;
		if (structureChanged || buttonVisibilityChanged) {
			resizeSheet();
		}
	}
	public boolean updateList() {
		List<List<DataList>> tempLists = character != null && character.getLists() != null ? character.getLists() : List.of();
		String signature = buildListSignature(tempLists);
		boolean structureChanged = !signature.equals(cachedListSignature);
		cachedListSignature = signature;

		int usedSections = 0;
		for (List<DataList> currList : tempLists) {
			if (currList == null || currList.isEmpty()) continue;

			ensureSectionCapacity(usedSections);
			String title = resolveListTitle(currList);
			JLabel label = titles.get(usedSections);
			label.setText(title);
			label.setVisible(true);

			ArrayList<JTextField> fields = lists.get(usedSections);
			ensureFieldCapacity(fields, currList.size());
			int usedFields = 0;
			for (DataList data : currList) {
				if (data == null) continue;
				if (data.getName() == null || data.getName().trim().isBlank()) continue;
				JTextField field = fields.get(usedFields);
				field.setText(formatListEntryText(data));
				field.setToolTipText(data.getDescription() != null && !data.getDescription().isBlank() ? data.getDescription() : null);
				field.setVisible(true);
				usedFields++;
			}
			hideUnusedFields(fields, usedFields);
			usedSections++;
		}

		hideUnusedSections(usedSections);
		return structureChanged;
	}
	
	public void resizeSheet() {
		pageHeight = resizeHeader();

		
		for (int i = 0; i < titles.size(); i++ ) {
			if (!titles.get(i).isVisible()) continue;
			titles.get(i).setBounds(5,pageHeight,570,20);
			pageHeight += 25;
		
			for (int j = 0; j < lists.get(i).size(); j++) {
				if (!lists.get(i).get(j).isVisible()) continue;
				if (j % 2 == 1)
					pageHeight -= 25;
					
				lists.get(i).get(j).setBounds(5 + 285*(j%2),pageHeight,280,20);
				pageHeight += 25;
			}
		}
		
		if (removeItemButton != null && removeItemButton.isVisible()) {
			removeItemButton.setBounds(225, pageHeight, 130, 30);
			pageHeight += 40;
		}

		pageHeight += 10;
		this.setPreferredSize(new Dimension(580, pageHeight));
	}

	private String buildListSignature(List<List<DataList>> tempLists) {
		StringBuilder signature = new StringBuilder();
		for (List<DataList> currList : tempLists) {
			if (currList == null || currList.isEmpty()) continue;
			signature.append(currList.size()).append(':');
			for (DataList data : currList) {
				if (data == null) continue;
				signature.append(data.getList()).append('|').append(data.getName()).append(';');
			}
			signature.append('#');
		}
		return signature.toString();
	}

	private String resolveListTitle(List<DataList> currList) {
		for (DataList data : currList) {
			if (data != null && data.getList() != null && !data.getList().isBlank()) {
				return data.getList();
			}
		}
		return "List";
	}

	private String formatListEntryText(DataList data) {
		if (data == null) return "";
		String name = data.getName() == null ? "" : data.getName();
		String description = data.getDescription() == null ? "" : data.getDescription().trim();
		if (description.matches("(?i)Rank\\s+\\d+")) {
			return name + " (" + description + ")";
		}
		return name;
	}

	private void ensureSectionCapacity(int size) {
		while (titles.size() <= size) {
			titles.add(buildLabel("", null));
			lists.add(new ArrayList<JTextField>());
		}
	}

	private void ensureFieldCapacity(ArrayList<JTextField> fields, int size) {
		while (fields.size() < size) {
			JTextField field = buildTextField("");
			field.setEditable(false);
			fields.add(field);
		}
	}

	private void hideUnusedFields(ArrayList<JTextField> fields, int usedCount) {
		for (int i = usedCount; i < fields.size(); i++) {
			fields.get(i).setVisible(false);
			fields.get(i).setToolTipText(null);
		}
	}

	private void hideUnusedSections(int usedSections) {
		for (int i = usedSections; i < titles.size(); i++) {
			titles.get(i).setVisible(false);
			hideUnusedFields(lists.get(i), 0);
		}
	}

	private void updateRemoveItemButtonVisibility() {
		if (removeItemButton == null) return;
		removeItemButton.setVisible(sheetFrame != null && sheetFrame.isGmModeActive());
	}

	private void promptRemoveItem() {
		if (sheetFrame == null || !sheetFrame.isGmModeActive() || character == null) return;
		ArrayList<ListGroupOption> groups = collectListGroups();
		if (groups.isEmpty()) {
			JOptionPane.showMessageDialog(this, "This character has no list entries to remove.", "No List Entries", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		LinkedHashMap<String, ListGroupOption> groupsByLabel = new LinkedHashMap<>();
		for (ListGroupOption option : groups) {
			groupsByLabel.put(option.label, option);
		}

		JComboBox<String> listBox = new JComboBox<>(groupsByLabel.keySet().toArray(new String[0]));
		JComboBox<String> itemBox = new JComboBox<>();
		populateItemBox(itemBox, groupsByLabel.get((String) listBox.getSelectedItem()));
		listBox.addActionListener(e -> populateItemBox(itemBox, groupsByLabel.get((String) listBox.getSelectedItem())));

		JPanel panel = new JPanel(null);
		JLabel listLabel = new JLabel("List");
		listLabel.setBounds(10, 10, 80, 20);
		panel.add(listLabel);
		listBox.setBounds(10, 35, 260, 24);
		panel.add(listBox);
		JLabel itemLabel = new JLabel("Item");
		itemLabel.setBounds(10, 70, 80, 20);
		panel.add(itemLabel);
		itemBox.setBounds(10, 95, 260, 24);
		panel.add(itemBox);
		panel.setPreferredSize(new Dimension(280, 130));

		int choice = JOptionPane.showConfirmDialog(
				this,
				panel,
				"Remove Item",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (choice != JOptionPane.OK_OPTION) return;

		ListGroupOption selectedGroup = groupsByLabel.get((String) listBox.getSelectedItem());
		ListItemOption selectedItem = selectedGroup == null ? null : selectedGroup.itemsByLabel.get((String) itemBox.getSelectedItem());
		if (selectedGroup == null || selectedItem == null || selectedItem.item == null) return;

		int confirm = JOptionPane.showConfirmDialog(
				this,
				"Remove " + selectedItem.label + " from " + selectedGroup.listName + "?",
				"Confirm Remove Item",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (confirm != JOptionPane.OK_OPTION) return;

		selectedGroup.group.remove(selectedItem.item);
		if (selectedGroup.group.isEmpty()) {
			character.getLists().remove(selectedGroup.group);
		}
		character.updateAll();
		sheetFrame.loadCharacter(character, sheetFrame.isGmModeActive());
	}

	private ArrayList<ListGroupOption> collectListGroups() {
		ArrayList<ListGroupOption> options = new ArrayList<>();
		if (character == null || character.getLists() == null) return options;
		for (List<DataList> group : character.getLists()) {
			if (group == null || group.isEmpty()) continue;
			String listName = resolveListTitle(group);
			LinkedHashMap<String, ListItemOption> itemsByLabel = new LinkedHashMap<>();
			for (DataList item : group) {
				if (item == null || item.getName() == null || item.getName().trim().isBlank()) continue;
				String label = buildListItemLabel(item, itemsByLabel);
				itemsByLabel.put(label, new ListItemOption(label, item));
			}
			if (!itemsByLabel.isEmpty()) {
				options.add(new ListGroupOption(buildListGroupLabel(listName, options), listName, group, itemsByLabel));
			}
		}
		return options;
	}

	private String buildListGroupLabel(String listName, List<ListGroupOption> existing) {
		String baseLabel = (listName == null || listName.isBlank()) ? "List" : listName.trim();
		String label = baseLabel;
		int suffix = 2;
		while (containsGroupLabel(existing, label)) {
			label = baseLabel + " #" + suffix;
			suffix++;
		}
		return label;
	}

	private boolean containsGroupLabel(List<ListGroupOption> groups, String label) {
		for (ListGroupOption group : groups) {
			if (group != null && label.equals(group.label)) return true;
		}
		return false;
	}

	private String buildListItemLabel(DataList item, LinkedHashMap<String, ListItemOption> existing) {
		String baseLabel = formatListEntryText(item);
		if (baseLabel == null || baseLabel.isBlank()) {
			baseLabel = item.getName() == null ? "Item" : item.getName().trim();
		}
		String label = baseLabel;
		int suffix = 2;
		while (existing.containsKey(label)) {
			label = baseLabel + " #" + suffix;
			suffix++;
		}
		return label;
	}

	private void populateItemBox(JComboBox<String> itemBox, ListGroupOption selectedGroup) {
		if (itemBox == null) return;
		itemBox.removeAllItems();
		if (selectedGroup == null || selectedGroup.itemsByLabel.isEmpty()) return;
		for (String label : selectedGroup.itemsByLabel.keySet()) {
			itemBox.addItem(label);
		}
		if (itemBox.getItemCount() > 0) {
			itemBox.setSelectedIndex(0);
		}
	}

	private static final class ListGroupOption {
		private final String label;
		private final String listName;
		private final List<DataList> group;
		private final LinkedHashMap<String, ListItemOption> itemsByLabel;

		private ListGroupOption(String label, String listName, List<DataList> group, LinkedHashMap<String, ListItemOption> itemsByLabel) {
			this.label = label;
			this.listName = listName == null ? "List" : listName;
			this.group = group;
			this.itemsByLabel = itemsByLabel;
		}
	}

	private static final class ListItemOption {
		private final String label;
		private final DataList item;

		private ListItemOption(String label, DataList item) {
			this.label = label;
			this.item = item;
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	

	


	

	
}

