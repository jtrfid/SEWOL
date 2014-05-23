package de.uni.freiburg.iig.telematik.jawl.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.invation.code.toval.parser.ParserException;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogSummary;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;

public interface LogParserInterface {

	public List<List<LogTrace<LogEntry>>> parse(File file, ParsingMode parsingMode) throws IOException, ParserException;
	
	public List<LogTrace<LogEntry>> getParsedLog(int index);
	
	public List<LogTrace<LogEntry>> getFirstParsedLog();
	
	public LogSummary<LogEntry> getSummary(int index);
	
	public LogSummary<LogEntry> getSummaryForFirstParsedLog();
}
