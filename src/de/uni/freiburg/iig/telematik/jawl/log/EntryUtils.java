package de.uni.freiburg.iig.telematik.jawl.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.types.HashList;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.invation.code.toval.validate.ParameterException.ErrorCode;


public class EntryUtils {
	
	public static void lockFieldForEntries(EntryField field, String reason, Collection<LogEntry> entries) throws ParameterException{
		Validate.notNull(field);
		Validate.notNull(entries);
		for(LogEntry entry: entries){
			if(entry != null){
				entry.lockField(field, reason);
			} else {
				throw new ParameterException(ErrorCode.NULLPOINTER);
			}
		}
	}
	
	public static List<LogEntry> getEntriesWithLockedField(List<LogEntry> entries, EntryField field) throws ParameterException{
		validateEntries(entries);
		Validate.notNull(field);
		List<LogEntry> entriesWithLockedField = new ArrayList<LogEntry>();
		for(LogEntry entry: entries){
			if(entry.isFieldLocked(field)){
				entriesWithLockedField.add(entry);
			}
		}
		return entriesWithLockedField;
	}
	
	public static List<LogEntry> getEntriesWithoutLockedField(List<LogEntry> entries, EntryField field) throws ParameterException{
		validateEntries(entries);
		Validate.notNull(field);
		List<LogEntry> entriesWithoutLockedField = new ArrayList<LogEntry>();
		for(LogEntry entry: entries){
			if(!entry.isFieldLocked(field)){
				entriesWithoutLockedField.add(entry);
			}
		}
		return entriesWithoutLockedField;
	}
	
	public static List<LogEntry> getEntriesWithAlternativeOriginator(List<LogEntry> entries) throws ParameterException{
		validateEntries(entries);
		List<LogEntry> entriesWithAlternativeOriginator = new ArrayList<LogEntry>();
		for(LogEntry entry: entries){
			if(!entry.isFieldLocked(EntryField.ORIGINATOR) && entry.getOriginatorCandidates().size() > 1){
				entriesWithAlternativeOriginator.add(entry);
			}
		}
		return entriesWithAlternativeOriginator;
	}
	
	public static List<LogEntry> getEntriesWithNoAlternativeOriginator(List<LogEntry> entries) throws ParameterException{
		validateEntries(entries);
		List<LogEntry> entriesWithNoAlternativeOriginator = new ArrayList<LogEntry>();
		for(LogEntry entry: entries){
			if(entry.isFieldLocked(EntryField.ORIGINATOR) || entry.getOriginatorCandidates().size() == 1){
				entriesWithNoAlternativeOriginator.add(entry);
			}
		}
		return entriesWithNoAlternativeOriginator;
	}
	
	public static List<String> getFieldValues(List<LogEntry> entries, EntryField field) throws ParameterException{
		validateEntries(entries);
		Validate.notNull(field);
		List<String> values = new ArrayList<String>();
		for(LogEntry entry: entries){
			values.add(entry.getFieldValue(field).toString());
		}
		return values;
	}
	
	public static List<LogEntry> getEntriesWithActivity(List<LogEntry> entries, String activity) throws ParameterException{
		validateEntries(entries);
		Validate.notNull(activity);
		List<LogEntry> result = new ArrayList<LogEntry>();
		for(LogEntry entry: entries){
			if(entry.getActivity().equals(activity)){
				result.add(entry);
			}
		}
		return result;
	}
	
	public static Map<String, List<LogEntry>> clusterEntriesAccordingToActivities(List<LogEntry> entries) throws ParameterException{
		validateEntries(entries);
		Map<String, List<LogEntry>> result = new HashMap<String, List<LogEntry>>();
		for(LogEntry entry: entries){
			if(!result.keySet().contains(entry.getActivity())){
				result.put(entry.getActivity(), new ArrayList<LogEntry>());
			}
			result.get(entry.getActivity()).add(entry);
		}
		return result;
	}
	
	public static Map<String, Set<String>> clusterOriginatorsAccordingToActivity(List<LogEntry> entries) throws ParameterException{
		validateEntries(entries);
		Map<String, Set<String>> originatorSets = new HashMap<String, Set<String>>();
		for(LogEntry entry: entries){
			if(!originatorSets.containsKey(entry.getActivity())){
				originatorSets.put(entry.getActivity(), new HashSet<String>());
			}
			originatorSets.get(entry.getActivity()).add(entry.getOriginator());
		}
		return originatorSets;
	}
	
	public static Set<String> getDistinctValuesForField(List<LogEntry> entries, EntryField field) throws ParameterException{
		validateEntries(entries);
		Validate.notNull(field);
		Set<String> result = new HashSet<String>();
		for(LogEntry entry: entries){
			result.add(entry.getFieldValue(field).toString());
		}
		return result;
	}
	
	public static void setOriginatorForEntries(String originator, List<LogEntry> entries) throws ParameterException{
		validateEntries(entries);
		try {
			for(LogEntry entry: entries){
				entry.setOriginator(originator);
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (LockingException e) {
			e.printStackTrace();
		} catch (ModificationException e) {
			e.printStackTrace();
		}
	}
	
	private static void validateEntries(Collection<LogEntry> entries) throws ParameterException{
		Validate.notNull(entries);
		Validate.notEmpty(entries);
		Validate.noNullElements(entries);
	}
	
	/**
	 * Returns the intersection of all sets of originator candidates for the given log entries.<br>
	 * That set contains all originators that are candidates for all entries.
	 * @param entries A set of log entries
	 * @return Shared originator candidates (shuffled)
	 * @throws ParameterException 
	 */
	public static HashList<String> getSharedOriginatorCandidates(List<LogEntry> entries) throws ParameterException{
		validateEntries(entries);
		HashList<String> sharedCandidates = new HashList<String>();
		sharedCandidates.addAll(entries.get(0).getOriginatorCandidates());
		for(int i=1; i<entries.size(); i++)
			sharedCandidates.retainAll(entries.get(i).getOriginatorCandidates());
		Collections.shuffle(sharedCandidates);
		return sharedCandidates;
	}

}
