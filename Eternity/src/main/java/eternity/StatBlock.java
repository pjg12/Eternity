package eternity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class StatBlock {

    /*private final TrackedStatusList status = new TrackedStatusList();
    private final TrackedStatusList multi = new TrackedStatusList();

    public List<DataStatus> getStatus() { return status; }
    public List<DataStatus> getMulti() { return multi; }

    public void addStatus(DataStatus s) {
        if (s == null || s.getName() == null) return;
        Integer existingIndex = status.indexByName(s.getName());
        if (existingIndex != null) {
            status.set(existingIndex, s);
            return;
        }
        status.add(s);
    }

    public void addMulti(DataStatus s) {
        if (s == null || s.getName() == null) return;
        Integer existingIndex = multi.indexByName(s.getName());
        if (existingIndex != null) {
            multi.set(existingIndex, s);
            return;
        }
        multi.add(s);
    }

    public void removeStatus(String name) {
        if (name == null) return;
        Integer index = status.indexByName(name);
        if (index != null) {
            status.remove((int) index);
        }
    }

    public void removeMulti(String name) {
        if (name == null) return;
        Integer index = multi.indexByName(name);
        if (index != null) {
            multi.remove((int) index);
        }
    }

    public int computeValue() {
        // Multiplier always includes a base 1 plus any multiplier severities
        return (int)(status.getSeveritySum() * (1.0 + multi.getSeveritySum()));
    }

    /**
     * Computes value without the implicit base +1 multiplier.
     * Use when the base multiplier should be explicitly provided via statuses.
     */
    public int computeValueNoBase() {
        return (int)(status.getSeveritySum() * multi.getSeveritySum());
    }

    @JsonIgnore
    public List<DataStatus> getAllStatuses() {
        List<DataStatus> all = new ArrayList<>(status);
        return all;
    }
    @JsonIgnore
    public List<DataStatus> getAllMultipliers() {
        List<DataStatus> all = new ArrayList<>(multi);
        return all;
    }

    private static final class TrackedStatusList extends ArrayList<DataStatus> {
        private final Map<String, Integer> indexByName = new HashMap<>();
        private double severitySum;

        private double getSeveritySum() {
            return severitySum;
        }

        private Integer indexByName(String name) {
            return normalizeName(name) == null ? null : indexByName.get(normalizeName(name));
        }

        private void indexEntry(int index, DataStatus status) {
            if (status == null) return;
            String normalizedName = normalizeName(status.getName());
            if (normalizedName != null) {
                indexByName.put(normalizedName, index);
            }
            severitySum += status.getSeverity();
        }

        private void deindexEntry(DataStatus status) {
            if (status == null) return;
            String normalizedName = normalizeName(status.getName());
            if (normalizedName != null) {
                indexByName.remove(normalizedName);
            }
            severitySum -= status.getSeverity();
        }

        private void shiftIndices(int startIndex) {
            if (startIndex < 0) startIndex = 0;
            for (int i = startIndex; i < size(); i++) {
                DataStatus status = get(i);
                if (status == null) continue;
                String normalizedName = normalizeName(status.getName());
                if (normalizedName != null) {
                    indexByName.put(normalizedName, i);
                }
            }
        }

        private void rebuildCache() {
            indexByName.clear();
            severitySum = 0;
            shiftIndices(0);
        }

        @Override
        public boolean add(DataStatus dataStatus) {
            boolean changed = super.add(dataStatus);
            if (changed) {
                indexEntry(size() - 1, dataStatus);
            }
            return changed;
        }

        @Override
        public void add(int index, DataStatus element) {
            super.add(index, element);
            indexEntry(index, element);
            shiftIndices(index + 1);
        }

        @Override
        public boolean addAll(java.util.Collection<? extends DataStatus> c) {
            boolean changed = super.addAll(c);
            if (changed) {
                int start = size() - c.size();
                int current = start;
                for (DataStatus status : c) {
                    indexEntry(current++, status);
                }
            }
            return changed;
        }

        @Override
        public boolean addAll(int index, java.util.Collection<? extends DataStatus> c) {
            boolean changed = super.addAll(index, c);
            if (changed) {
                int current = index;
                for (DataStatus status : c) {
                    indexEntry(current++, status);
                }
                shiftIndices(current);
            }
            return changed;
        }

        @Override
        public DataStatus set(int index, DataStatus element) {
            DataStatus previous = super.set(index, element);
            deindexEntry(previous);
            indexEntry(index, element);
            return previous;
        }

        @Override
        public DataStatus remove(int index) {
            DataStatus removed = super.remove(index);
            deindexEntry(removed);
            shiftIndices(index);
            return removed;
        }

        @Override
        public boolean remove(Object o) {
            int index = indexOf(o);
            if (index < 0) return false;
            remove(index);
            return true;
        }

        @Override
        public boolean removeIf(Predicate<? super DataStatus> filter) {
            boolean changed = super.removeIf(filter);
            if (changed) rebuildCache();
            return changed;
        }

        @Override
        public void clear() {
            super.clear();
            indexByName.clear();
            severitySum = 0;
        }

        private static String normalizeName(String name) {
            return name == null ? null : name.toLowerCase(Locale.ROOT);
        }
    }*/
}
