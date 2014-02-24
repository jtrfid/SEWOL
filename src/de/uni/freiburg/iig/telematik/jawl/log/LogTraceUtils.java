package de.uni.freiburg.iig.telematik.jawl.log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

public class LogTraceUtils {

	public static LogTrace<LogEntry> createTraceFromActivities(int caseNumber, String... activities) throws ParameterException{
		return createTraceFromActivities(caseNumber, LogEntry.class, activities);
	}
		
	public static <E extends LogEntry> LogTrace<E> createTraceFromActivities(int caseNumber, Class<E> entryClass, String... activities) throws ParameterException{
		Validate.notNegative(caseNumber);
		Validate.notNull(entryClass);
		try {
			entryClass.newInstance();
		} catch(Exception e){
			throw new ParameterException("Cannot instantiate entry class: " + entryClass.getSimpleName());
		}
		Validate.notNull(activities);
		Validate.notEmpty(activities);
		Validate.noNullElements(activities);
		
		LogTrace<E> result = new LogTrace<E>(caseNumber);
		for(String activity: activities){
			E newEntry = null;
			try {
				newEntry = entryClass.newInstance();
				newEntry.setActivity(activity);
			} catch(IllegalAccessException e){
				// Cannot happen, since we validated this before.
			} catch (InstantiationException e) {
				// Cannot happen, since we validated this before.
			} catch (LockingException e) {
				// Cannot happen, since the field activity is not locked.
			}
			result.addEntry(newEntry);
		}
		
		return result;
	}
	
	public static <E extends LogEntry> List<LogTrace<E>> createTraceList(LogTrace<E>... traces){
		return Arrays.asList(traces);
	}
	
	public static <E extends LogEntry> List<List<String>> createStringRepresentation(List<LogTrace> traceList){
		List<List<String>> result = new ArrayList<List<String>>();
		for(LogTrace trace: traceList){
			result.add(trace.getActivities());
		}
		return result;
	}

}
