package de.uni.freiburg.iig.telematik.jawl.parser.plain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.parser.ParserException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogSummary;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.jawl.parser.AbstractLogParser;

public class PlainParser extends AbstractLogParser {

	private String delimiter = null;

	public PlainParser(String delimiter) {
		this.delimiter = delimiter;
	}

	public List<List<LogTrace<LogEntry>>> parse(InputStream inputStream, boolean onlyDistinctTraces) throws IOException, ParameterException, ParserException {
		try {
			inputStream.available();
		} catch (IOException e) {
			throw new ParameterException("Unable to read input file: " + e.getMessage());
		}
		
		parsedLogFiles = new ArrayList<List<LogTrace<LogEntry>>>();
		List<LogTrace<LogEntry>> traceList = new ArrayList<LogTrace<LogEntry>>();
		Set<LogTrace<LogEntry>> traceSet = new HashSet<LogTrace<LogEntry>>();
		parsedLogFiles.add(traceList);

		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		String nextLine = null;
		int traceCount = 0;

		while ((nextLine = bufferedReader.readLine()) != null) {
			LogTrace<LogEntry> newTrace = new LogTrace<LogEntry>(++traceCount);
			for (String nextToken : nextLine.split(delimiter)) {
				if (nextToken != null && !nextToken.isEmpty()) {
					newTrace.addEntry(new LogEntry(nextToken));
				}
			}
			if (!onlyDistinctTraces) {
				traceList.add(newTrace);
			} else {
				if (traceSet.add(newTrace)) {
					traceList.add(newTrace);
				}
			}
		}
		
		summaries.put(0, new LogSummary<LogEntry>(traceList));
		return parsedLogFiles;
	}

	@Override
	public List<List<LogTrace<LogEntry>>> parse(File file, boolean onlyDistinctTraces) throws IOException, ParserException, ParameterException {
		InputStream inputStream = new FileInputStream(file);
		return parse(inputStream, onlyDistinctTraces);
	}
}
