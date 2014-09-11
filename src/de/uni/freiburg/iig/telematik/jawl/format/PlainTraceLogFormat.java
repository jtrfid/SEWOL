package de.uni.freiburg.iig.telematik.jawl.format;

import java.nio.charset.Charset;

import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.jawl.writer.PerspectiveException;

public class PlainTraceLogFormat extends AbstractLogFormat {
	
	private static final char DEFAULT_ACTIVITY_DELIMITER = '\t';
	
	protected char activityDelimiter = DEFAULT_ACTIVITY_DELIMITER;
	
	public PlainTraceLogFormat(LogPerspective logPerspective) throws PerspectiveException {
		super(logPerspective);
	}
	
	public PlainTraceLogFormat(LogPerspective logPerspective, char delimiter) throws PerspectiveException {
		super(logPerspective);
		this.activityDelimiter = delimiter;
	}

	@Override
	public String getFileExtension() {
		return "txt";
	}

	@Override
	public String getFileHeader() {
		return "";
	}

	@Override
	public String getFileFooter() {
		return "";
	}

	@Override
	public boolean supportsLogPerspective(LogPerspective logPerspective) {
		return logPerspective == LogPerspective.TRACE_PERSPECTIVE;
	}

	@Override
	public boolean supportsCharset(Charset charset) {
		return true;
	}

	@Override
	public String getName() {
		return "PLAIN";
	}

	@Override
	public LogFormatType getLogFormatType() {
		return LogFormatType.PLAIN;
	}
	
	@Override
	public <E extends LogEntry> String getTraceAsString(LogTrace<E> trace) {
		StringBuilder builder = new StringBuilder();
		for(LogEntry e: trace.getEntries()) {
			builder.append(getEntryAsString(e, trace.getCaseNumber()));
			builder.append(activityDelimiter);
		}
		builder.append('\n');
		return builder.toString();
	}

	@Override
	public <E extends LogEntry> String getEntryAsString(E entry, int caseNumber) {
		return entry.getActivity();
	}

}
