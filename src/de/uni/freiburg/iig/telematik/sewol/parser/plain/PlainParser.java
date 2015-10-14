package de.uni.freiburg.iig.telematik.sewol.parser.plain;

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
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogSummary;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import de.uni.freiburg.iig.telematik.sewol.parser.AbstractLogParser;
import de.uni.freiburg.iig.telematik.sewol.parser.ParsingMode;

public class PlainParser extends AbstractLogParser {

	private final String delimiter;

	public PlainParser(String delimiter) {
		this.delimiter = delimiter;
	}

	public List<List<LogTrace<LogEntry>>> parse(InputStream inputStream, ParsingMode parsingMode) throws IOException, ParameterException, ParserException {
		try {
			inputStream.available();
		} catch (IOException e) {
			throw new ParameterException("Unable to read input file: " + e.getMessage());
		}
		
		parsedLogFiles = new ArrayList<>();
		List<LogTrace<LogEntry>> traceList = new ArrayList<>();
		parsedLogFiles.add(traceList);

		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		String nextLine = null;
		int traceCount = 0;

		Set<List<String>> activitySequences = new HashSet<>();
		while ((nextLine = bufferedReader.readLine()) != null) {
			LogTrace<LogEntry> newTrace = new LogTrace<>(++traceCount);
			for (String nextToken : nextLine.split(delimiter)) {
				if (nextToken != null && !nextToken.isEmpty()) {
					newTrace.addEntry(new LogEntry(nextToken));
				}
			}
			switch(parsingMode){
			case COMPLETE:
				traceList.add(newTrace);
				break;
//			case DISTINCT_TRACES:
			case DISTINCT_ACTIVITY_SEQUENCES:
				if(activitySequences.add(newTrace.getActivities())){
					newTrace.reduceToActivities();
					traceList.add(newTrace);
				}
				break;
			}
		}
		
		summaries.add(new LogSummary<>(traceList));
		return parsedLogFiles;
	}

	@Override
	public List<List<LogTrace<LogEntry>>> parse(File file, ParsingMode parsingMode) throws IOException, ParserException, ParameterException {
		InputStream inputStream = new FileInputStream(file);
		return parse(inputStream, parsingMode);
	}
}
