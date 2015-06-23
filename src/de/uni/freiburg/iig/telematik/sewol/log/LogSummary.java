package de.uni.freiburg.iig.telematik.sewol.log;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.statistic.Observation;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

public class LogSummary<E extends LogEntry> {
	
	private final Set<String> activities = new HashSet<>();
	private final Set<String> originators = new HashSet<>();
	private final Set<String> roles = new HashSet<>();
	private final Observation traceLength = new Observation();
	
	public LogSummary() {}

	public LogSummary(List<LogTrace<E>> traces) throws ParameterException {
		addTraces(traces);
	}
	
	public void addTraces(List<LogTrace<E>> traces) throws ParameterException {
		Validate.notNull(traces);
		for(LogTrace<E> trace: traces){
			addTrace(trace);
		}
	}
	
	public void addTrace(LogTrace<E> trace) throws ParameterException{
		Validate.notNull(trace);
		activities.addAll(trace.getDistinctActivities());
		originators.addAll(trace.getDistinctOriginators());
		roles.addAll(trace.getDistinctRoles());
		traceLength.addValue(trace.size());
	}

	public Set<String> getActivities() {
		return Collections.unmodifiableSet(activities);
	}

	public Set<String> getOriginators() {
		return Collections.unmodifiableSet(originators);
	}

	public Set<String> getRoles() {
		return Collections.unmodifiableSet(roles);
	}
	
	public double getAverageTraceLength(){
		return traceLength.getAverage();
	}
	
}
