package de.uni.freiburg.iig.telematik.sewol.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogSummary;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;

public abstract class AbstractLogParser implements LogParserInterface {

	protected List<List<LogTrace<LogEntry>>> parsedLogFiles = null;
	protected Map<Integer, LogSummary<LogEntry>> summaries = new HashMap<Integer, LogSummary<LogEntry>>();

	protected boolean parsed() {
		return parsedLogFiles != null;
	}

	protected int parsedLogFiles() {
		if (!parsed())
			return 0;
		return parsedLogFiles.size();
	}
	
	public List<LogTrace<LogEntry>> getParsedLog(int index) throws ParameterException {
		if (!parsed())
			throw new ParameterException("Log not parsed yet!");
		Validate.notNegative(index);
		if (index > parsedLogFiles() - 1)
			throw new ParameterException(ErrorCode.RANGEVIOLATION, "No log for index " + index);
		return parsedLogFiles.get(index);
	}
	
	@Override
	public List<LogTrace<LogEntry>> getFirstParsedLog() throws ParameterException {
		return getParsedLog(0);
	}
	
	public LogSummary<LogEntry> getSummary(int index) throws ParameterException {
		if (!parsed())
			throw new ParameterException("Log not parsed yet!");
		Validate.notNegative(index);
		if (index > parsedLogFiles() - 1)
			throw new ParameterException(ErrorCode.RANGEVIOLATION, "No log for index " + index);
		if (!summaries.containsKey(index))
			summaries.put(index, new LogSummary<LogEntry>(getParsedLog(index)));
		return summaries.get(index);
	}
	
	@Override
	public LogSummary<LogEntry> getSummaryForFirstParsedLog() throws ParameterException {
		return getSummary(0);
	}
	
}
