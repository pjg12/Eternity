package eternity;

import java.util.ArrayList;
import java.util.List;

public class StatBlock {

    private final List<DataStatus> status = new ArrayList<>();
    private final List<DataStatus> multi = new ArrayList<>();

    public List<DataStatus> getStatus() { return status; }
    public List<DataStatus> getMulti() { return multi; }

    public void addStatus(DataStatus s) { if (status.stream().noneMatch(x -> x.getName().equals(s.getName()))) status.add(s); }

    public void addMulti(DataStatus s) { if (multi.stream().noneMatch(x -> x.getName().equals(s.getName()))) multi.add(s); }

    public void removeStatus(String name) { status.removeIf(s -> s.getName().equals(name)); }

    public void removeMulti(String name) { multi.removeIf(s -> s.getName().equals(name)); }

    public int computeValue() {
        double sum = status.stream().mapToDouble(DataStatus::getSeverity).sum();
        double mul = multi.stream().mapToDouble(DataStatus::getSeverity).sum();
        if (mul == 0) mul = 1;   // Avoid 0 multiplier
        return (int)(sum * mul);
    }
}