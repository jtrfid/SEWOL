package de.uni.freiburg.iig.telematik.jawl.log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogTrace {
	
	private List<LogEntry> logEntries = new ArrayList<LogEntry>();
	private int caseNumber;
	
	public LogTrace(int caseNumber){
		this.caseNumber = caseNumber;
	}
	
	public boolean addEntry(LogEntry entry){
		if(entry!=null){
			return logEntries.add(entry);
		}
		return false;
	}
	
	public List<LogEntry> getEntries(){
		return Collections.unmodifiableList(logEntries);
	}
	
	public List<LogEntry> getEntriesForActivity(String activity){
		List<LogEntry> result = new ArrayList<LogEntry>();
		for(LogEntry entry: logEntries){
			if(entry.getActivity().equals(activity)){
				result.add(entry);
			}
		}
		return result;
	}
	
	/**
	 * Returns all log entries of this trace whose activities are in the given activity set.
	 * @param activities
	 * @return
	 */
	public List<LogEntry> getEntriesForActivities(Set<String> activities){
		List<LogEntry> result = new ArrayList<LogEntry>();
		for(LogEntry entry: logEntries)
			if(activities.contains(entry.getActivity()))
				result.add(entry);
		return result;
	}
	
	public List<LogEntry> getEntriesForGroup(String groupID){
		List<LogEntry> result = new ArrayList<LogEntry>();
		for(LogEntry entry: logEntries)
			if(entry.getGroup().equals(groupID))
				result.add(entry);
		return result;
	}
	
	public List<LogEntry> getFirstKEntries(int k){
		if(k<0)
			throw new IllegalArgumentException();
		if(k>size())
			throw new IllegalArgumentException("Trace does only contain "+size()+" entries!");
		List<LogEntry> result = new ArrayList<LogEntry>();
		if(k==0)
			return result;
		for(int i=0; i<k; i++)
			result.add(logEntries.get(i));
		return result;
	}
	
	public List<LogEntry> getSucceedingEntries(LogEntry entry){
		List<LogEntry> result = new ArrayList<LogEntry>();
		Integer index = null;
		for(LogEntry traceEntry: logEntries){
			if(traceEntry == entry){
				index = logEntries.indexOf(traceEntry);
				break;
			}
		}
		if(index != null && index < logEntries.size()-1){
			for(int i=index+1; i<logEntries.size(); i++){
				result.add(logEntries.get(i));
			}
		}
		return result;
	}
	
	public LogEntry getDirectSuccessor(LogEntry entry){
		Integer index = null;
		for(LogEntry traceEntry: logEntries){
			if(traceEntry == entry){
				index = logEntries.indexOf(traceEntry);
				break;
			}
		}
		if(index != null && index < logEntries.size()-1){
			return logEntries.get(index + 1);
		}
		return null;
	}
	
	public List<LogEntry> getPreceedingEntries(LogEntry entry){
		List<LogEntry> result = new ArrayList<LogEntry>();
		Integer index = null;
		for(LogEntry traceEntry: logEntries){
			if(traceEntry == entry){
				index = logEntries.indexOf(traceEntry);
				break;
			}
		}
		if(index != null && index > 0){
			for(int i=0; i<index; i++){
				result.add(logEntries.get(i));
			}
		}
		return result;
	}
	
	public LogEntry getDirectPredecessor(LogEntry entry){
		Integer index = null;
		for(LogEntry traceEntry: logEntries){
			if(traceEntry == entry){
				index = logEntries.indexOf(traceEntry);
				break;
			}
		}
		if(index != null && index > 0){
			return logEntries.get(index - 1);
		}
		return null;
	}
	
	public boolean removeEntry(LogEntry entry){
		return logEntries.remove(entry);
	}
	
	public boolean removeAllEntries(Collection<LogEntry> entries){
		boolean entriesChanged = false;
		for(LogEntry entry: entries){
			if(removeEntry(entry))
				entriesChanged = true;
		}
		return entriesChanged;
	}
	
	public int getCaseNumber(){
		return caseNumber;
	}
	
	public int size(){
		return logEntries.size();
	}
	
	public boolean containsActivity(String activity){
		return getDistinctActivities().contains(activity);
	}
	
	public List<String> getActivities(){
		List<String> result = new ArrayList<String>();
		for(LogEntry entry: logEntries){
			result.add(entry.getActivity());
		}
		return result;
	}
	
	public Set<String> getDistinctActivities(){
		Set<String> result = new HashSet<String>();
		for(LogEntry entry: logEntries)
			result.add(entry.getActivity());
		return result;
	}
	
	public void sort(){
		Collections.sort(logEntries);
	}
	
	@Override
	public String toString(){
		//TODO: Format
		return logEntries.toString();
	}
	
	public static void main(String[] args) throws NullPointerException, LockingException, ParseException {
		LogEntry e1 = new LogEntry("a1");
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		e1.setTimestamp(sdf.parse("01.01.2013 06:00"));
		LogEntry e2 = new LogEntry("a2");
		e2.setTimestamp(sdf.parse("01.01.2013 12:00"));
		LogEntry e3 = new LogEntry("a3");
		e3.setTimestamp(sdf.parse("01.01.2013 18:00"));
		LogTrace t = new LogTrace(1);
		t.addEntry(e1);
		t.addEntry(e2);
		t.addEntry(e3);
		System.out.println(t);
	}
}
