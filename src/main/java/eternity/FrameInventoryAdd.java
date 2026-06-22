package eternity;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Inventory add helper frame. Lets the user search item templates and add one to a character inventory.
 */
public class FrameInventoryAdd extends JFrame {
    private static final long serialVersionUID = 1L;

    private final FrameSheet sheetFrame;
    private final StoreRuleManager dataQuery;

    private StoreCharData character;

    private final JTextField searchField;
    private final JComboBox<String> itemBox;
    private final JLabel searchL;
    private final JLabel itemL;
    private final JLabel quantityL;
    private final JComboBox<String> destinationBox;
    private final JTextField quantityField;
    private final JTextField customNameField;
    private final JTextField customNoteField;

    private final JLabel selectedMetaL;

    private final ArrayList<DataItemEquipment> resultItems;

    FrameInventoryAdd(FrameSheet sheetFrame, StoreRuleManager dataQuery) {
        super("Add Inventory");
        this.sheetFrame = sheetFrame;
        this.dataQuery = dataQuery;
        this.resultItems = new ArrayList<>();

        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(null);
        form.setPreferredSize(new Dimension(520, 250));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(form, BorderLayout.CENTER);

        JLabel headerL = new JLabel("Select an item to add", SwingConstants.CENTER);
        headerL.setBounds(10, 5, 490, 25);
        form.add(headerL);

        JLabel destinationL = new JLabel("Destination");
        destinationL.setBounds(10, 40, 100, 20);
        form.add(destinationL);

        destinationBox = new JComboBox<>(new String[] {"Equipment", "Consumables", "Goods", "Items"});
        destinationBox.setBounds(110, 40, 190, 22);
        form.add(destinationBox);

        quantityL = new JLabel("Quantity");
        quantityL.setBounds(310, 40, 60, 20);
        form.add(quantityL);

        quantityField = new JTextField("1");
        quantityField.setBounds(380, 40, 120, 22);
        form.add(quantityField);

        searchL = new JLabel("Search");
        searchL.setBounds(10, 70, 100, 20);
        form.add(searchL);

        searchField = new JTextField();
        searchField.setBounds(110, 70, 390, 22);
        form.add(searchField);

        itemL = new JLabel("Item");
        itemL.setBounds(10, 100, 100, 20);
        form.add(itemL);

        itemBox = new JComboBox<>();
        itemBox.setBounds(110, 100, 390, 22);
        form.add(itemBox);

        JLabel customNameL = new JLabel("Custom Name");
        customNameL.setBounds(10, 130, 100, 20);
        form.add(customNameL);

        customNameField = new JTextField();
        customNameField.setBounds(110, 130, 390, 22);
        form.add(customNameField);

        JLabel customNoteL = new JLabel("Custom Note");
        customNoteL.setBounds(10, 160, 100, 20);
        form.add(customNoteL);

        customNoteField = new JTextField();
        customNoteField.setBounds(110, 160, 390, 22);
        form.add(customNoteField);

        selectedMetaL = new JLabel("-", SwingConstants.CENTER);
        selectedMetaL.setBounds(10, 192, 490, 45);
        selectedMetaL.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY));
        form.add(selectedMetaL);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancelB = new JButton("Cancel");
        JButton addB = new JButton("Add Item");
        buttons.add(cancelB);
        buttons.add(addB);
        add(buttons, BorderLayout.SOUTH);

        searchField.addActionListener(e -> refreshItemList());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshItemList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshItemList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshItemList();
            }
        });
        itemBox.addActionListener(e -> updateSelectedMeta());
        destinationBox.addActionListener(e -> updateSearchItemVisibility());
        cancelB.addActionListener(e -> setVisible(false));
        addB.addActionListener(e -> addSelectedItem());

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(540, 340);
        setLocationRelativeTo(sheetFrame);

        refreshItemList();
        updateSearchItemVisibility();
    }

    void updateCharacter(StoreCharData character) {
        this.character = character;
    }

    private void refreshItemList() {
        DataItemEquipment previousSelection = getSelectedTemplate();
        Integer preferredDid = previousSelection != null ? previousSelection.getDid() : null;
        itemBox.removeAllItems();
        resultItems.clear();

        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        List<DataItemEquipment> found = dataQuery.getItemEquipmentData();

        int selectedIndex = -1;
        for (DataItemEquipment item : found) {
            if (item == null) continue;
            if (!matchesSearch(item, search)) continue;
            if (selectedIndex < 0 && preferredDid != null && item.getDid() == preferredDid) {
                selectedIndex = resultItems.size();
            }
            resultItems.add(item);
            itemBox.addItem(display(item));
        }

        if (itemBox.getItemCount() == 0) {
            selectedMetaL.setText("No matching items found.");
        } else {
            itemBox.setSelectedIndex(selectedIndex >= 0 ? selectedIndex : 0);
            updateSelectedMeta();
        }
    }

    private String display(DataItemEquipment item) {
        String name = item.getDname() == null ? "-" : item.getDname();
        String slot = item.getSlot() == null ? "-" : item.getSlot();
        String cat = item.getCategory() == null ? "-" : item.getCategory();
        return name + " (Tier " + item.getTier() + ", " + cat + ", " + slot + ", DID " + item.getDid() + ")";
    }

    private void updateSelectedMeta() {
        DataItemEquipment item = getSelectedTemplate();
        if (item == null) {
            selectedMetaL.setText("No item selected.");
            return;
        }
        String text = "<html><center>Category: " + safe(item.getCategory()) +
                " | Slot: " + safe(item.getSlot()) +
                " | Tier: " + item.getTier() +
                "<br>Value: " + item.getValue() + " | Bonus: " + safe(item.getBonusAtt()) + " " + item.getBonusAmount() +
                "</center></html>";
        selectedMetaL.setText(text);
    }

    private void addSelectedItem() {
        if (character == null || character.getInventory() == null) {
            JOptionPane.showMessageDialog(this, "No character loaded.", "Add Inventory", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DataItemEquipment template = getSelectedTemplate();
        if (template == null) {
            JOptionPane.showMessageDialog(this, "Choose an item first.", "Add Inventory", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double qty = parseQuantity();
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.", "Add Inventory", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String destination = String.valueOf(destinationBox.getSelectedItem());
        String target = resolveTarget(destination, template);
        CharInventory inv = character.getInventory();

        if ("Equipment".equals(target)) {
            DataItemEquipment added = new DataItemEquipment(template);
            added.setEquipped(false);
            added.setQuantity(qty);
            applyCustomFields(added);
            inv.addEquipment(added);
        } else {
            DataItem added = new DataItem(template);
            added.setQuantity(qty);
            applyCustomFields(added);
            if ("Consumables".equals(target)) {
                inv.addConsumable(added);
            } else if ("Goods".equals(target)) {
                inv.addGoods(added);
            } else {
                inv.addItem(added);
            }
        }

        character.updateAll();
        if (sheetFrame != null) {
            sheetFrame.refreshMainPanel();
            sheetFrame.refreshInventoryPanel();
        }

        JOptionPane.showMessageDialog(this, "Added: " + safe(template.getDname()), "Add Inventory", JOptionPane.INFORMATION_MESSAGE);
    }

    private void applyCustomFields(DataItem item) {
        String customName = customNameField.getText();
        if (customName != null && !customName.isBlank()) {
            item.setIname(customName.trim());
        }

        String customNote = customNoteField.getText();
        if (customNote != null && !customNote.isBlank()) {
            item.setInote(customNote.trim());
        }
    }

    private double parseQuantity() {
        try {
            return Double.parseDouble(quantityField.getText().trim());
        } catch (Exception e) {
            return -1.0;
        }
    }

    private String resolveTarget(String destination, DataItemEquipment template) {
        if (destination != null && !"Auto".equals(destination)) {
            return destination;
        }

        String cat = safe(template.getCategory()).toLowerCase();
        String slot = safe(template.getSlot()).toLowerCase();
        if (cat.contains("weapon") || cat.contains("armor") || cat.contains("accessory")) return "Equipment";
        if (cat.contains("consum")) return "Consumables";
        if (cat.contains("good") || cat.contains("material")) return "Goods";
        if (!slot.isBlank() && !"-".equals(slot)) return "Equipment";
        return "Items";
    }

    private DataItemEquipment getSelectedTemplate() {
        int idx = itemBox.getSelectedIndex();
        if (idx < 0 || idx >= resultItems.size()) return null;
        return resultItems.get(idx);
    }

    private void updateSearchItemVisibility() {
        boolean equipment = "Equipment".equals(String.valueOf(destinationBox.getSelectedItem()));
        searchL.setVisible(equipment);
        searchField.setVisible(equipment);
        itemL.setVisible(equipment);
        itemBox.setVisible(equipment);
        quantityL.setVisible(!equipment);
        quantityField.setVisible(!equipment);
    }

    private boolean matchesSearch(DataItemEquipment item, String search) {
        if (item == null) return false;
        if (search == null || search.isBlank()) return true;
        return safe(item.getDname()).toLowerCase(Locale.ROOT).contains(search) ||
                safe(item.getIname()).toLowerCase(Locale.ROOT).contains(search) ||
                safe(item.getSlot()).toLowerCase(Locale.ROOT).contains(search);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}

