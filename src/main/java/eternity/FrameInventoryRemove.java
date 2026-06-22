package eternity;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Inventory remove helper frame. Lets the user remove item quantities from a character inventory.
 */
public class FrameInventoryRemove extends JFrame {
    private static final long serialVersionUID = 1L;

    private final FrameSheet sheetFrame;
    private StoreCharData character;

    private final JComboBox<String> sourceBox;
    private final JComboBox<String> itemBox;
    private final JLabel quantityL;
    private final JTextField quantityField;
    private final JLabel selectedMetaL;

    private final ArrayList<DataItem> resultItems;

    FrameInventoryRemove(FrameSheet sheetFrame, StoreRuleManager dataQuery) {
        super("Remove Inventory");
        this.sheetFrame = sheetFrame;
        this.resultItems = new ArrayList<>();

        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(null);
        form.setPreferredSize(new Dimension(520, 210));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(form, BorderLayout.CENTER);

        JLabel headerL = new JLabel("Select an item to remove", SwingConstants.CENTER);
        headerL.setBounds(10, 5, 490, 25);
        form.add(headerL);

        JLabel sourceL = new JLabel("Source");
        sourceL.setBounds(10, 40, 100, 20);
        form.add(sourceL);

        sourceBox = new JComboBox<>(new String[] {"Equipment", "Consumables", "Goods", "Items"});
        sourceBox.setBounds(110, 40, 190, 22);
        form.add(sourceBox);

        quantityL = new JLabel("Quantity");
        quantityL.setBounds(310, 40, 60, 20);
        form.add(quantityL);

        quantityField = new JTextField("1");
        quantityField.setBounds(380, 40, 120, 22);
        form.add(quantityField);

        JLabel itemL = new JLabel("Item");
        itemL.setBounds(10, 75, 100, 20);
        form.add(itemL);

        itemBox = new JComboBox<>();
        itemBox.setBounds(110, 75, 390, 22);
        form.add(itemBox);

        selectedMetaL = new JLabel("-", SwingConstants.CENTER);
        selectedMetaL.setBounds(10, 110, 490, 55);
        selectedMetaL.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY));
        form.add(selectedMetaL);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancelB = new JButton("Cancel");
        JButton removeB = new JButton("Remove Item");
        buttons.add(cancelB);
        buttons.add(removeB);
        add(buttons, BorderLayout.SOUTH);

        sourceBox.addActionListener(e -> refreshItemList());
        itemBox.addActionListener(e -> updateSelectedMeta());
        cancelB.addActionListener(e -> setVisible(false));
        removeB.addActionListener(e -> removeSelectedItem());

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(540, 305);
        setLocationRelativeTo(sheetFrame);
        updateQuantityVisibility();
    }

    void updateCharacter(StoreCharData character) {
        this.character = character;
        refreshItemList();
    }

    private void refreshItemList() {
        updateQuantityVisibility();
        itemBox.removeAllItems();
        resultItems.clear();

        List<DataItem> found = getSourceItems();
        for (DataItem item : found) {
            if (item == null) continue;
            resultItems.add(item);
            itemBox.addItem(display(item));
        }

        if (itemBox.getItemCount() == 0) {
            selectedMetaL.setText("No items in selected source.");
        } else {
            itemBox.setSelectedIndex(0);
            updateSelectedMeta();
        }
    }

    private List<DataItem> getSourceItems() {
        if (character == null || character.getInventory() == null) return List.of();
        CharInventory inv = character.getInventory();
        String source = String.valueOf(sourceBox.getSelectedItem());
        if ("Equipment".equals(source)) return new ArrayList<>(inv.getEquipment());
        if ("Consumables".equals(source)) return new ArrayList<>(inv.getConsumables());
        if ("Goods".equals(source)) return new ArrayList<>(inv.getGoods());
        return new ArrayList<>(inv.getItems());
    }

    private String display(DataItem item) {
        String name = bestName(item);
        double qty = item.getQuantity();
        if (item instanceof DataItemEquipment equip) {
            String cat = safe(equip.getCategory());
            String slot = safe(equip.getSlot());
            return name + " (" + cat + ", " + slot + ", Qty " + qty + ", DID " + item.getDid() + ")";
        }
        return name + " (Qty " + qty + ", DID " + item.getDid() + ")";
    }

    private String bestName(DataItem item) {
        String custom = safe(item.getIname());
        if (!custom.isBlank()) return custom;
        String display = safe(item.getDname());
        return display.isBlank() ? "-" : display;
    }

    private void updateSelectedMeta() {
        DataItem item = getSelectedItem();
        if (item == null) {
            selectedMetaL.setText("No item selected.");
            return;
        }
        String source = String.valueOf(sourceBox.getSelectedItem());
        String text;
        if (item instanceof DataItemEquipment equip) {
            text = "<html><center>Source: " + source +
                    " | Category: " + safe(equip.getCategory()) +
                    " | Slot: " + safe(equip.getSlot()) +
                    "<br>Qty: " + item.getQuantity() +
                    "</center></html>";
        } else {
            text = "<html><center>Source: " + source +
                    "<br>Qty: " + item.getQuantity() +
                    "</center></html>";
        }
        selectedMetaL.setText(text);
    }

    private void removeSelectedItem() {
        if (character == null || character.getInventory() == null) {
            JOptionPane.showMessageDialog(this, "No character loaded.", "Remove Inventory", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DataItem selected = getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Choose an item first.", "Remove Inventory", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double qty = parseQuantity();
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.", "Remove Inventory", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CharInventory inv = character.getInventory();
        String source = String.valueOf(sourceBox.getSelectedItem());
        double remaining = selected.getQuantity() - qty;

        if ("Equipment".equals(source) && selected instanceof DataItemEquipment equip) {
            if (remaining <= 0) inv.removeEquipment(equip);
            else equip.setQuantity(remaining);
        } else if ("Consumables".equals(source)) {
            if (remaining <= 0) inv.removeConsumable(selected);
            else selected.setQuantity(remaining);
        } else if ("Goods".equals(source)) {
            if (remaining <= 0) inv.removeGoods(selected);
            else selected.setQuantity(remaining);
        } else {
            if (remaining <= 0) inv.removeItem(selected);
            else selected.setQuantity(remaining);
        }

        character.updateAll();
        if (sheetFrame != null) {
            sheetFrame.refreshMainPanel();
            sheetFrame.refreshInventoryPanel();
        }

        JOptionPane.showMessageDialog(this, "Removed: " + bestName(selected), "Remove Inventory", JOptionPane.INFORMATION_MESSAGE);
        refreshItemList();
    }

    private DataItem getSelectedItem() {
        int idx = itemBox.getSelectedIndex();
        if (idx < 0 || idx >= resultItems.size()) return null;
        return resultItems.get(idx);
    }

    private double parseQuantity() {
        try {
            return Double.parseDouble(quantityField.getText().trim());
        } catch (Exception e) {
            return -1.0;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void updateQuantityVisibility() {
        boolean equipment = "Equipment".equals(String.valueOf(sourceBox.getSelectedItem()));
        quantityL.setVisible(!equipment);
        quantityField.setVisible(!equipment);
    }
}

