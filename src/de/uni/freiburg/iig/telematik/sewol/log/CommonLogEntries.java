package de.uni.freiburg.iig.telematik.sewol.log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

public class CommonLogEntries<E extends LogEntry> {
	
	Map<String, Map<LogTrace<E>, List<E>>> commonEntries = new HashMap<>();;
	
	public CommonLogEntries(LogTrace<E>...traces) throws ParameterException{
		Validate.noNullElements(traces);
		
		for(LogTrace<E> trace: traces){
			Map<String, List<E>> traceEntries = getEntryActivityMap(trace);
			if(commonEntries.isEmpty()){
				//First run
				for(String activity: traceEntries.keySet()){
					Map<LogTrace<E>, List<E>> newMap = new HashMap<>();
					newMap.put(trace, traceEntries.get(activity));
					commonEntries.put(activity, newMap);
				}
			} else {
				Set<String> keysToRemove = new HashSet<>(); 
				for(String activity: commonEntries.keySet()){
					if(traceEntries.keySet().contains(activity)){
						commonEntries.get(activity).put(trace, trace.getEntriesForActivity(activity));
					} else {
						keysToRemove.add(activity);
					}
				}
				for(String key: keysToRemove){
					commonEntries.remove(key);
				}
			}
		}
	}
	
	public Map<String, List<E>> getEntryActivityMap(LogTrace<E> trace) throws ParameterException{
		Map<String, List<E>> result = new HashMap<>();
		for(String activity: trace.getDistinctActivities()){
			result.put(activity, trace.getEntriesForActivity(activity));
		}
		return result;
	}
	
	public boolean isEmpty(){
		return commonEntries.isEmpty();
	}
	
	public boolean isCommonEntry(String activity){
		return commonEntries.containsKey(activity);
	}
	
	public Set<String> getCommonEntries(){
		return commonEntries.keySet();
	}
	
	public Map<LogTrace<E>, List<E>> getTraceMap(String activity){
		if(activity == null)
			throw new NullPointerException();
		if(!commonEntries.containsKey(activity))
			return null;
		return commonEntries.get(activity);
	}
	
	public List<E> getEntriesFor(String activity, LogTrace<E> trace){
		if(activity == null)
			throw new NullPointerException();
		if(!commonEntries.containsKey(activity))
			return null;
		if(!commonEntries.get(activity).containsKey(trace))
			return null;
		return commonEntries.get(activity).get(trace);
	}
	

}
