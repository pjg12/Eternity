package eternity;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class StatBlock {

    private final List<DataStatus> status = new ArrayList<>();
    private final List<DataStatus> multi = new ArrayList<>();

    public List<DataStatus> getStatus() { return status; }
    public List<DataStatus> getMulti() { return multi; }

    public void addStatus(DataStatus s) {
        if (s == null || s.getName() == null) return;
        for (int i = 0; i < status.size(); i++) {
            DataStatus existing = status.get(i);
            if (existing.getName().equals(s.getName())) {
                // Only prompt when the incoming status is not permanent
                if (!"Permanent".equalsIgnoreCase(s.getDurationType())) {
                    int choice = JOptionPane.showConfirmDialog(
                            null,
                            "A status named \"" + s.getName() + "\" already exists.\nReplace it with the new one?",
                            "Duplicate Status",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        status.set(i, s);
                    }
                }
                // If permanent or user chose No, keep existing
                return;
            }
        }
        status.add(s);
    }

    public void addMulti(DataStatus s) {
        if (s == null || s.getName() == null) return;
        for (int i = 0; i < multi.size(); i++) {
            DataStatus existing = multi.get(i);
            if (existing.getName().equals(s.getName())) {
                if (!"Permanent".equalsIgnoreCase(s.getDurationType())) {
                    int choice = JOptionPane.showConfirmDialog(
                            null,
                            "A multiplier named \"" + s.getName() + "\" already exists.\nReplace it with the new one?",
                            "Duplicate Multiplier",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        multi.set(i, s);
                    }
                }
                return;
            }
        }
        multi.add(s);
    }

    public void removeStatus(String name) { status.removeIf(s -> s.getName().equals(name)); }

    public void removeMulti(String name) { multi.removeIf(s -> s.getName().equals(name)); }

    public int computeValue() {
        double sum = status.stream().mapToDouble(DataStatus::getSeverity).sum();
        // Multiplier always includes a base 1 plus any multiplier severities
        double mul = 1.0 + multi.stream().mapToDouble(DataStatus::getSeverity).sum();
        return (int)(sum * mul);
    }

    public List<DataStatus> getAllStatuses() {
        List<DataStatus> all = new ArrayList<>(status);
        return all;
    }
    public List<DataStatus> getAllMultipliers() {
        List<DataStatus> all = new ArrayList<>(multi);
        return all;
    }
}
