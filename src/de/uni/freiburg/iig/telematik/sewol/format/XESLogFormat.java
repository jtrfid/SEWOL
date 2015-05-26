package de.uni.freiburg.iig.telematik.sewol.format;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.file.FileWriter;
import de.invation.code.toval.types.DataUsage;
import de.uni.freiburg.iig.telematik.sewol.log.DULogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.DataAttribute;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import de.uni.freiburg.iig.telematik.sewol.writer.PerspectiveException;

/**
 * {@link FileWriter} for the XES log file format.
 * 
 * @author Adrian Lange
 *
 */
public class XESLogFormat extends AbstractLogFormat {

	public static final String XES_EXTENSION = "xes";
	public static final String XES_LOG_FORMAT_NAME = "XES format";

	private static final String DEFAULT_LINE_SEPARATOR = "\n";
	private static final String DEFAULT_INDENT = "  ";
	private static final String INDENT1 = DEFAULT_INDENT;
	private static final String INDENT2 = INDENT1 + DEFAULT_INDENT;
	private static final String INDENT3 = INDENT2 + DEFAULT_INDENT;
	private static final String INDENT4 = INDENT3 + DEFAULT_INDENT;
	private static final String INDENT5 = INDENT4 + DEFAULT_INDENT;
	private static final String FILE_HEADER_FORMAT = "<?xml version=\"1.0\" encoding=\"%%s\"?>%s<log xes.version=\"1.0\" xes.features=\"nested-attributes\" openxes.version=\"2.0\" xmlns=\"http://www.xes-standard.org/\">%s";
	private static final String FILE_HEADER = String.format(FILE_HEADER_FORMAT, DEFAULT_LINE_SEPARATOR, DEFAULT_LINE_SEPARATOR);
	private static final String FILE_FOOTER_FORMAT = "</log>%s";
	private static final String FILE_FOOTER = String.format(FILE_FOOTER_FORMAT, DEFAULT_LINE_SEPARATOR);
	private static final String TRACE_START_FORMAT = "%s<trace>%s";
	private static final String TRACE_START = String.format(TRACE_START_FORMAT, INDENT1, DEFAULT_LINE_SEPARATOR);
	private static final String TRACE_END_FORMAT = "%s</trace>%s";
	private static final String TRACE_END = String.format(TRACE_END_FORMAT, INDENT1, DEFAULT_LINE_SEPARATOR);

	private static final String EXTENSION_FORMAT = "%s<extension name=\"%s\" prefix=\"%s\" uri=\"%s\" />%s";

	private static final String EVENT_START_FORMAT = "%s<event>%s";
	private static final String EVENT_START = String.format(EVENT_START_FORMAT, INDENT2, DEFAULT_LINE_SEPARATOR);
	private static final String EVENT_END_FORMAT = "%s</event>%s";
	private static final String EVENT_END = String.format(EVENT_END_FORMAT, INDENT2, DEFAULT_LINE_SEPARATOR);

	private static final String ATTRIBUTE_STRING_FORMAT = "%s<string key=\"%s\" value=\"%s\">%s</string>%s";
	private static final String ATTRIBUTE_STRING_FORMAT_C = "%s<string key=\"%s\" value=\"%s\" />%s";
	private static final String ATTRIBUTE_DATE_FORMAT = "%s<date key=\"%s\" value=\"%s\">%s</date>%s";
	private static final String ATTRIBUTE_DATE_FORMAT_C = "%s<date key=\"%s\" value=\"%s\" />%s";
	private static final String ATTRIBUTE_INT_FORMAT = "%s<int key=\"%s\" value=\"%d\">%s</int>%s";
	private static final String ATTRIBUTE_INT_FORMAT_C = "%s<int key=\"%s\" value=\"%d\" />%s";
	private static final String ATTRIBUTE_FLOAT_FORMAT = "%s<float key=\"%s\" value=\"%f\">%s</float>%s";
	private static final String ATTRIBUTE_FLOAT_FORMAT_C = "%s<float key=\"%s\" value=\"%f\" />%s";
	private static final String ATTRIBUTE_BOOLEAN_FORMAT = "%s<boolean key=\"%s\" value=\"%b\">%s</boolean>%s";
	private static final String ATTRIBUTE_BOOLEAN_FORMAT_C = "%s<boolean key=\"%s\" value=\"%b\" />%s";

	private static final String XES_DATEPATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	

	public XESLogFormat(String logName) {
		super();
		try {
			setLogPerspective(LogPerspective.TRACE_PERSPECTIVE);
		} catch (PerspectiveException e) {
			e.printStackTrace();
		}
		setLogName(logName);
	}

	@Override
	public String getDatePattern() {
		return XES_DATEPATTERN;
	}

	/**
	 * @return the logName
	 */
	public String getLogName() {
		return logName;
	}

	/**
	 * @param logName
	 *            the logName to set
	 */
	public void setLogName(String logName) {
		this.logName = logName;
	}

	public XESLogFormat(String logName, Charset charset) {
		this(logName);
		setCharset(charset);
	}

	@Override
	public String getName() {
		return XES_LOG_FORMAT_NAME;
	}

	@Override
	public String getFileExtension() {
		return XES_EXTENSION;
	}

	@Override
	public boolean supportsLogPerspective(LogPerspective logPerspective) {
		if (logPerspective == LogPerspective.TRACE_PERSPECTIVE)
			return true;
		return false;
	}

	@Override
	public boolean supportsCharset(Charset charset) {
		if (charset.name().equals("UTF-8"))
			return true;
		return false;
	}

	@Override
	public String getFileHeader() {
		StringBuilder builder = new StringBuilder();

		// Header
		builder.append(String.format(FILE_HEADER, charset.name()));

		// Extensions =>
		// "%s<extension name=\"%s\" prefix=\"%s\" uri=\"%s\" />%s"
		for (XESExtensions e : XESExtensions.values()) {
			builder.append(String.format(EXTENSION_FORMAT, INDENT1, e.name, e.prefix, e.uri, DEFAULT_LINE_SEPARATOR));
		}

		// log name =>
		// "%s<string key=\"%s\" value=\"%s\">%s</string>%s"
		builder.append(String.format(ATTRIBUTE_STRING_FORMAT_C, INDENT1, XESExtensions.CONCEPT.prefix + ":name", logName, DEFAULT_LINE_SEPARATOR));
		// lifecycle model
		builder.append(String.format(ATTRIBUTE_STRING_FORMAT_C, INDENT1, XESExtensions.LIFECYCLE.prefix + ":model", "standard", DEFAULT_LINE_SEPARATOR));

		return builder.toString();
	}

	@Override
	public String getFileFooter() {
		return FILE_FOOTER;
	}

	@Override
	public <E extends LogEntry> String getTraceAsString(LogTrace<E> trace) {
		StringBuilder builder = new StringBuilder();
		builder.append(TRACE_START);

		// concept name
		builder.append(String.format(ATTRIBUTE_STRING_FORMAT_C, INDENT2, XESExtensions.CONCEPT.prefix + ":name", trace.getCaseNumber(), DEFAULT_LINE_SEPARATOR));

		for (LogEntry e : trace.getEntries()) {
			builder.append(getEntryAsString(e, trace.getCaseNumber()));
		}
		builder.append(TRACE_END);
		return builder.toString();
	}

	@Override
	public <E extends LogEntry> String getEntryAsString(E entry, int caseNumber) {
		StringBuilder builder = new StringBuilder();

		builder.append(EVENT_START);

		// originator => "%s<string key=\"%s\" value=\"%s\">%s</string>%s"
		builder.append(String.format(ATTRIBUTE_STRING_FORMAT_C, INDENT3, XESExtensions.ORGANIZATIONAL.prefix + ":resource", entry.getOriginator(), DEFAULT_LINE_SEPARATOR));
		// concept name
		builder.append(String.format(ATTRIBUTE_STRING_FORMAT_C, INDENT3, XESExtensions.CONCEPT.prefix + ":name", entry.getActivity(), DEFAULT_LINE_SEPARATOR));
		// time
		builder.append(String.format(ATTRIBUTE_DATE_FORMAT_C, INDENT3, XESExtensions.TIME.prefix + ":timestamp", entry.getTimestamp() != null ? dateFormat.format(entry.getTimestamp()) : "-", DEFAULT_LINE_SEPARATOR));
		// data
		if (entry instanceof DULogEntry) {
			DULogEntry duEntry = (DULogEntry) entry;
			Set<DataAttribute> data = duEntry.getDataAttributes();
			if (data != null && !data.isEmpty()) {
				StringBuilder dataBlock = new StringBuilder();
				dataBlock.append(DEFAULT_LINE_SEPARATOR);
				Map<DataAttribute, Set<DataUsage>> dataUsage = duEntry.getDataUsage();
				for (DataAttribute dataAttribute : data) {
					StringBuilder du = new StringBuilder();
					du.append(DEFAULT_LINE_SEPARATOR);
					StringBuilder v = new StringBuilder();
					for (DataUsage dataUsageValue : dataUsage.get(dataAttribute)) {
						v.append(dataUsageValue.toString().toLowerCase());
						v.append(",");
					}
					String dataUsageString = v.length() > 0 ? v.substring(0, v.length() - 1) : "";
					du.append(String.format(ATTRIBUTE_STRING_FORMAT_C, INDENT5, "dataUsage", dataUsageString, DEFAULT_LINE_SEPARATOR));
					du.append(INDENT4);

					// retrieve data type
					String dataAttributeFormat = ATTRIBUTE_STRING_FORMAT;
					if (dataAttribute.value instanceof Date)
						dataAttributeFormat = ATTRIBUTE_DATE_FORMAT;
					else if (dataAttribute.value instanceof Integer)
						dataAttributeFormat = ATTRIBUTE_INT_FORMAT;
					else if (dataAttribute.value instanceof Float)
						dataAttributeFormat = ATTRIBUTE_FLOAT_FORMAT;
					else if (dataAttribute.value instanceof Double)
						dataAttributeFormat = ATTRIBUTE_FLOAT_FORMAT;
					else if (dataAttribute.value instanceof Boolean)
						dataAttributeFormat = ATTRIBUTE_BOOLEAN_FORMAT;

					dataBlock.append(String.format(dataAttributeFormat, INDENT4, dataAttribute.name, dataAttribute.value, du.toString(), DEFAULT_LINE_SEPARATOR));
				}
				dataBlock.append(INDENT3);
				builder.append(String.format(ATTRIBUTE_STRING_FORMAT, INDENT3, XESExtensions.ATTRIBUTE_DATA_USAGE.prefix + ":data", "", dataBlock.toString(), DEFAULT_LINE_SEPARATOR));
			}
		}

		// meta
		if (entry.getMetaAttributes() != null && !entry.getMetaAttributes().isEmpty()) {
			Set<DataAttribute> metaAttributes = entry.getMetaAttributes();
			if (metaAttributes != null && !metaAttributes.isEmpty()) {
				for (DataAttribute metaAttribute : metaAttributes) {
					// retrieve data type
					String metaAttributeFormat = ATTRIBUTE_STRING_FORMAT_C;
					if (metaAttribute.value instanceof Date)
						metaAttributeFormat = ATTRIBUTE_DATE_FORMAT_C;
					else if (metaAttribute.value instanceof Integer)
						metaAttributeFormat = ATTRIBUTE_INT_FORMAT_C;
					else if (metaAttribute.value instanceof Float)
						metaAttributeFormat = ATTRIBUTE_FLOAT_FORMAT_C;
					else if (metaAttribute.value instanceof Double)
						metaAttributeFormat = ATTRIBUTE_FLOAT_FORMAT_C;
					else if (metaAttribute.value instanceof Boolean)
						metaAttributeFormat = ATTRIBUTE_BOOLEAN_FORMAT_C;

					builder.append(String.format(metaAttributeFormat, INDENT3, metaAttribute.name, metaAttribute.value, DEFAULT_LINE_SEPARATOR));
				}
			}
		}

		builder.append(EVENT_END);

		return builder.toString();
	}

	@Override
	public LogFormatType getLogFormatType() {
		return LogFormatType.XES;
	}

	/**
	 * Represents all possible XES extensions.
	 * 
	 * @author Adrian Lange
	 */
	protected enum XESExtensions {
		ATTRIBUTE_DATA_USAGE("AttributeDataUsage", "dataUsage", "http://xes.process-security.de/extensions/dataUsage.xesext"), CONCEPT("Concept", "concept", "http://www.xes-standard.org/concept.xesext"), LIFECYCLE("Lifecycle", "lifecycle",
				"http://www.xes-standard.org/lifecycle.xesext"), ORGANIZATIONAL("Organizational", "org", "http://www.xes-standard.org/org.xesext"), SEMANTIC("Semantic", "semantic", "http://www.xes-standard.org/semantic.xesext"), TIME(
				"Time", "time", "http://www.xes-standard.org/time.xesext");

		String name;
		String prefix;
		String uri;

		XESExtensions(String name, String prefix, String uri) {
			this.name = name;
			this.prefix = prefix;
			this.uri = uri;
		}
	}
}
