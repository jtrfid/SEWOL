package de.uni.freiburg.iig.telematik.sewol.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.invation.code.toval.validate.Validate;


public class LogEntryUtils {
	
	public static <E extends LogEntry> void lockFieldForEntries(EntryField field, String reason, Collection<E> entries) throws ParameterException{
		Validate.notNull(field);
		Validate.notNull(entries);
		for(E entry: entries){
			if(entry != null){
				entry.lockField(field, reason);
			} else {
				throw new ParameterException(ErrorCode.NULLPOINTER);
			}
		}
	}
	
	public static <E extends LogEntry> List<E> getEntriesWithLockedField(List<E> entries, EntryField field) throws ParameterException{
		validateEntries(entries);
		Validate.notNull(field);
		List<E> entriesWithLockedField = new ArrayList<E>();
		for(E entry: entries){
			if(entry.isFieldLocked(field)){
				entriesWithLockedField.add(entry);
			}
		}
		return entriesWithLockedField;
	}
	
	public static <E extends LogEntry> List<E> getEntriesWithoutLockedField(List<E> entries, EntryField field) throws ParameterException{
		validateEntries(entries);
		Validate.notNull(field);
		List<E> entriesWithoutLockedField = new ArrayList<E>();
		for(E entry: entries){
			if(!entry.isFieldLocked(field)){
				entriesWithoutLockedField.add(entry);
			}
		}
		return entriesWithoutLockedField;
	}
	
	
	
	public static <E extends LogEntry> List<String> getFieldValues(List<E> entries, EntryField field) throws ParameterException{
		validateEntries(entries);
		Validate.notNull(field);
		List<String> values = new ArrayList<String>();
		for(E entry: entries){
			values.add(entry.getFieldValue(field).toString());
		}
		return values;
	}
	
	public static <E extends LogEntry> List<E> getEntriesWithActivity(List<E> entries, String activity) throws ParameterException{
		validateEntries(entries);
		Validate.notNull(activity);
		List<E> result = new ArrayList<E>();
		for(E entry: entries){
			if(entry.getActivity().equals(activity)){
				result.add(entry);
			}
		}
		return result;
	}
	
	public static <E extends LogEntry> Map<String, List<E>> clusterEntriesAccordingToActivities(List<E> entries) throws ParameterException{
		validateEntries(entries);
		Map<String, List<E>> result = new HashMap<String, List<E>>();
		for(E entry: entries){
			if(!result.keySet().contains(entry.getActivity())){
				result.put(entry.getActivity(), new ArrayList<E>());
			}
			result.get(entry.getActivity()).add(entry);
		}
		return result;
	}
	
	public static <E extends LogEntry> Map<String, Set<String>> clusterOriginatorsAccordingToActivity(List<E> entries) throws ParameterException{
		validateEntries(entries);
		Map<String, Set<String>> originatorSets = new HashMap<String, Set<String>>();
		for(E entry: entries){
			if(!originatorSets.containsKey(entry.getActivity())){
				originatorSets.put(entry.getActivity(), new HashSet<String>());
			}
			originatorSets.get(entry.getActivity()).add(entry.getOriginator());
		}
		return originatorSets;
	}
	
	public static <E extends LogEntry> Set<String> getDistinctValuesForField(List<E> entries, EntryField field) throws ParameterException{
		validateEntries(entries);
		Validate.notNull(field);
		Set<String> result = new HashSet<String>();
		for(E entry: entries){
			result.add(entry.getFieldValue(field).toString());
		}
		return result;
	}
	
	public static <E extends LogEntry> void setOriginatorForEntries(String originator, List<E> entries) throws ParameterException{
		validateEntries(entries);
		try {
			for(E entry: entries){
				entry.setOriginator(originator);
			}
		} catch (ParameterException e) {
			e.printStackTrace();
		} catch (LockingException e) {
			e.printStackTrace();
		}
	}
	
	public static <E extends LogEntry> void setRoleForEntries(String role, List<E> entries) throws ParameterException{
		validateEntries(entries);
		try {
			for(E entry: entries) {
				entry.setRole(role);
			}
		} catch (ParameterException e) {
			e.printStackTrace();
		} catch (LockingException e) {
			e.printStackTrace();
		}
	}
	
	protected static <T extends LogEntry> void validateEntries(Collection<T> entries) throws ParameterException{
		Validate.notNull(entries);
		Validate.notEmpty(entries);
		Validate.noNullElements(entries);
	}
	
}
