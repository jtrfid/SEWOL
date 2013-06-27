package de.uni.freiburg.iig.telematik.jawl.logformat;

import java.nio.charset.Charset;

import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.jawl.writer.PerspectiveException;




public class PlainTraceLogFormat extends LogFormat {
	
	private static final char ACTIVITY_DELIMITER = '\t';
	
	public PlainTraceLogFormat(LogPerspective logPerspective) throws PerspectiveException {
		super(logPerspective);
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
	public String getTraceAsString(LogTrace trace) {
		StringBuilder builder = new StringBuilder();
		for(LogEntry e: trace.getEntries()) {
			builder.append(getEntryAsString(e, trace.getCaseNumber()));
			builder.append(ACTIVITY_DELIMITER);
		}
		builder.append('\n');
		return builder.toString();
	}

	@Override
	public String getEntryAsString(LogEntry entry, int caseNumber) {
		return entry.getActivity();
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


}
