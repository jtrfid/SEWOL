package de.uni.freiburg.iig.telematik.jawl.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.invation.code.toval.parser.ParserException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogSummary;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;

public interface LogParserInterface {

	public List<List<LogTrace<LogEntry>>> parse(File file, ParsingMode parsingMode) throws IOException, ParserException, ParameterException;
	
	public List<LogTrace<LogEntry>> getParsedLog(int index) throws ParameterException;
	
	public List<LogTrace<LogEntry>> getFirstParsedLog() throws ParameterException;
	
	public LogSummary<LogEntry> getSummary(int index) throws ParameterException;
	
	public LogSummary<LogEntry> getSummaryForFirstParsedLog() throws ParameterException;
}
