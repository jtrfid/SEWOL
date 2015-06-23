package de.uni.freiburg.iig.telematik.sewol.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

public class LogTrace<E extends LogEntry> {

    private final List<E> logEntries = new ArrayList<>();
    private int caseNumber = -1;
    private final Set<Integer> similarInstances = new HashSet<>();

    public LogTrace() {
    }

    public LogTrace(Integer caseNumber) {
        Validate.notNegative(caseNumber);
        this.caseNumber = caseNumber;
    }

    public boolean addEntry(E entry) {
        if (entry != null) {
            return logEntries.add(entry);
        }
        return false;
    }

    public List<E> getEntries() {
        return Collections.unmodifiableList(logEntries);
    }

    public List<E> getEntriesForActivity(String activity) {
        Validate.notNull(activity);
        List<E> result = new ArrayList<>();
        for (E entry : logEntries) {
            if (entry.getActivity().equals(activity)) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Returns all log entries of this trace whose activities are in the given
     * activity set.
     *
     * @param activities
     * @return
     */
    public List<E> getEntriesForActivities(Set<String> activities) {
        Validate.noNullElements(activities);
        List<E> result = new ArrayList<>();
        for (E entry : logEntries) {
            if (activities.contains(entry.getActivity())) {
                result.add(entry);
            }
        }
        return result;
    }

    public List<E> getEntriesForGroup(String groupID) {
        Validate.notNull(groupID);
        List<E> result = new ArrayList<>();
        for (E entry : logEntries) {
            if (entry.getGroup().equals(groupID)) {
                result.add(entry);
            }
        }
        return result;
    }

    public List<E> getFirstKEntries(Integer k) {
        Validate.notNegative(k);
        if (k > size()) {
            throw new ParameterException("Trace does only contain " + size() + " entries!");
        }
        List<E> result = new ArrayList<>();
        if (k == 0) {
            return result;
        }
        for (int i = 0; i < k; i++) {
            result.add(logEntries.get(i));
        }
        return result;
    }

    public List<E> getSucceedingEntries(E entry) {
        Validate.notNull(entry);
        List<E> result = new ArrayList<>();
        Integer index = null;
        for (E traceEntry : logEntries) {
            if (traceEntry == entry) {
                index = logEntries.indexOf(traceEntry);
                break;
            }
        }
        if (index != null && index < logEntries.size() - 1) {
            for (int i = index + 1; i < logEntries.size(); i++) {
                result.add(logEntries.get(i));
            }
        }
        return result;
    }

    public E getDirectSuccessor(E entry) {
        Validate.notNull(entry);
        Integer index = null;
        for (E traceEntry : logEntries) {
            if (traceEntry == entry) {
                index = logEntries.indexOf(traceEntry);
                break;
            }
        }
        if (index != null && index < logEntries.size() - 1) {
            return logEntries.get(index + 1);
        }
        return null;
    }

    public List<E> getPreceedingEntries(E entry) {
        Validate.notNull(entry);
        List<E> result = new ArrayList<>();
        Integer index = null;
        for (E traceEntry : logEntries) {
            if (traceEntry == entry) {
                index = logEntries.indexOf(traceEntry);
                break;
            }
        }
        if (index != null && index > 0) {
            for (int i = 0; i < index; i++) {
                result.add(logEntries.get(i));
            }
        }
        return result;
    }

    public E getDirectPredecessor(E entry) {
        Validate.notNull(entry);
        Integer index = null;
        for (E traceEntry : logEntries) {
            if (traceEntry == entry) {
                index = logEntries.indexOf(traceEntry);
                break;
            }
        }
        if (index != null && index > 0) {
            return logEntries.get(index - 1);
        }
        return null;
    }

    public boolean removeEntry(E entry) {
        return logEntries.remove(entry);
    }

    public boolean removeAllEntries(Collection<E> entries) {
        Validate.noNullElements(entries);
        boolean entriesChanged = false;
        for (E entry : entries) {
            if (removeEntry(entry)) {
                entriesChanged = true;
            }
        }
        return entriesChanged;
    }

    public void setCaseNumber(int caseNumber) {
        Validate.notNull(caseNumber);
        this.caseNumber = caseNumber;
    }

    public int getCaseNumber() {
        return caseNumber;
    }

    public int size() {
        return logEntries.size();
    }

    public Set<Integer> getSimilarInstances() {
        return Collections.unmodifiableSet(similarInstances);
    }

    public int getNumberOfSimilarInstances() {
        return similarInstances.size();
    }

    public void addSimilarInstance(Integer similarInstance) {
        Validate.notNull(similarInstance);
        this.similarInstances.add(similarInstance);
    }

    public void setSimilarInstances(Collection<Integer> similarInstances) {
        Validate.notNull(similarInstances);
        this.similarInstances.clear();
        this.similarInstances.addAll(similarInstances);
    }

    public boolean containsActivity(String activity) {
        return getDistinctActivities().contains(activity);
    }

    public int activitySupport(String activity) {
        int result = 0;
        for (E entry : logEntries) {
            if (entry.getActivity().equals(activity)) {
                result++;
            }
        }
        return result;
    }

    public Map<String, Integer> getActivitySupports() {
        Map<String, Integer> result = new HashMap<>();
        for (E entry : logEntries) {
            if (!result.containsKey(entry.getActivity())) {
                result.put(entry.getActivity(), 1);
            } else {
                result.put(entry.getActivity(), result.get(entry.getActivity()) + 1);
            }
        }
        return result;
    }

    public boolean containsDuplicateActivities() {
        Map<String, Integer> activitySupports = getActivitySupports();
        for (String activity : activitySupports.keySet()) {
            if (activitySupports.get(activity) > 1) {
                return true;
            }
        }
        return false;
    }

    public List<String> getActivities() {
        List<String> result = new ArrayList<>();
        for (E entry : logEntries) {
            result.add(entry.getActivity());
        }
        return result;
    }

    public void reduceToActivities() {
        for (E entry : logEntries) {
            entry.reduceToActivity();
        }
    }

    public Set<String> getDistinctActivities() {
        Set<String> result = new HashSet<>();
        for (E entry : logEntries) {
            result.add(entry.getActivity());
        }
        return result;
    }

    public Set<String> getDistinctOriginators() {
        Set<String> result = new HashSet<>();
        for (E entry : logEntries) {
            result.add(entry.getOriginator());
        }
        return result;
    }

    public Collection<? extends String> getDistinctRoles() {
        Set<String> result = new HashSet<>();
        for (E entry : logEntries) {
            result.add(entry.getRole());
        }
        return result;
    }

    public void sort() {
        Collections.sort(logEntries);
    }

    @Override
    public String toString() {
        return logEntries.toString();
    }
}
