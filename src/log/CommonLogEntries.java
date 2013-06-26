package log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommonLogEntries {
	
	Map<String, Map<LogTrace, List<LogEntry>>> commonEntries = new HashMap<String, Map<LogTrace, List<LogEntry>>>();;
	
	public CommonLogEntries(LogTrace...traces){
		for(LogTrace trace: traces){
			Map<String, List<LogEntry>> traceEntries = getEntryActivityMap(trace);
			if(commonEntries.isEmpty()){
				//First run
				for(String activity: traceEntries.keySet()){
					Map<LogTrace, List<LogEntry>> newMap = new HashMap<LogTrace, List<LogEntry>>();
					newMap.put(trace, traceEntries.get(activity));
					commonEntries.put(activity, newMap);
				}
			} else {
				Set<String> keysToRemove = new HashSet<String>(); 
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
	
	public Map<String, List<LogEntry>> getEntryActivityMap(LogTrace trace){
		Map<String, List<LogEntry>> result = new HashMap<String, List<LogEntry>>();
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
	
	public Map<LogTrace, List<LogEntry>> getTraceMap(String activity){
		if(activity == null)
			throw new NullPointerException();
		if(!commonEntries.containsKey(activity))
			return null;
		return commonEntries.get(activity);
	}
	
	public List<LogEntry> getEntriesFor(String activity, LogTrace trace){
		if(activity == null)
			throw new NullPointerException();
		if(!commonEntries.containsKey(activity))
			return null;
		if(!commonEntries.get(activity).containsKey(trace))
			return null;
		return commonEntries.get(activity).get(trace);
	}
	

}
