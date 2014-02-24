package de.uni.freiburg.iig.telematik.jawl.log;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

public class LogSummary<E extends LogEntry> {
	
	private Set<String> activities = new HashSet<String>();
	private Set<String> originators = new HashSet<String>();
	
	public LogSummary() {}

	public LogSummary(List<LogTrace<E>> traces) throws ParameterException{
		addTraces(traces);
	}
	
	public void addTraces(List<LogTrace<E>> traces) throws ParameterException{
		Validate.notNull(traces);
		for(LogTrace<E> trace: traces){
			addTrace(trace);
		}
	}
	
	public void addTrace(LogTrace<E> trace) throws ParameterException{
		Validate.notNull(trace);
		activities.addAll(trace.getDistinctActivities());
		originators.addAll(trace.getDistinctOriginators());
	}

	public Set<String> getActivities() {
		return Collections.unmodifiableSet(activities);
	}

	public Set<String> getOriginators() {
		return Collections.unmodifiableSet(originators);
	}
	
}
