package de.uni.freiburg.iig.telematik.sewol.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.invation.code.toval.parser.ParserException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogSummary;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import java.io.InputStream;

public interface LogParserInterface {

        public List<List<LogTrace<LogEntry>>> parse(File file, ParsingMode parsingMode) throws IOException, ParserException;

        public List<List<LogTrace<LogEntry>>> parse(String filePath, ParsingMode parsingMode) throws IOException, ParserException;

        public List<List<LogTrace<LogEntry>>> parse(InputStream inputStream, ParsingMode parsingMode) throws ParameterException, ParserException;

        public List<LogTrace<LogEntry>> getParsedLog(int index);

        public List<LogTrace<LogEntry>> getFirstParsedLog();

        public LogSummary<LogEntry> getSummary(int index);

        public LogSummary<LogEntry> getSummaryForFirstParsedLog();
}
