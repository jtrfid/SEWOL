package de.uni.freiburg.iig.telematik.sewol.format;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import de.uni.freiburg.iig.telematik.sewol.log.DULogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.DataAttribute;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import de.uni.freiburg.iig.telematik.sewol.writer.PerspectiveException;


/**
 * Weil die Zeilenumbr�che hardcoded in den String-Konstanten sind muss darauf geachtet werden,
 * dass immer dasselbe Zeilenumbruchs-Zeichen verwendet wird.
 * Die einzuhaltende Konvention ist '\n'.
 * LogWriter ist dementsprechend implementiert und erwartet ausschlie�lich solche Zeilenumbr�che.
 * 
 * @author ts552
 *
 */
public class MXMLLogFormat extends AbstractLogFormat {
    
        public static final String MXML_EXTENSION = "mxml";

	private static final String DEFAULT_LINE_SEPARATOR = "\n";
	private static final String FILE_HEADER_FORMAT = "<?xml version=\"1.0\" encoding=\"%%s\"?>%s<WorkflowLog>%s<Process id=\"%%s\">%s";
	private static final String FILE_HEADER = String.format(FILE_HEADER_FORMAT, DEFAULT_LINE_SEPARATOR, DEFAULT_LINE_SEPARATOR, DEFAULT_LINE_SEPARATOR);
	private static final String FILE_FOOTER_FORMAT = "</Process>%s</WorkflowLog>%s";
	private static final String FILE_FOOTER = String.format(FILE_FOOTER_FORMAT, DEFAULT_LINE_SEPARATOR, DEFAULT_LINE_SEPARATOR);
	private static final String INSTANCE_START_FORMAT = "<ProcessInstance id=\"%d\">%s";
	private static final String INSTANCE_END_FORMAT = "</ProcessInstance>%s";
	private static final String INSTANCE_END = String.format(INSTANCE_END_FORMAT, DEFAULT_LINE_SEPARATOR);
	
	private static final String ENTRY_START_FORMAT = "<AuditTrailEntry>%s" +
														 "<WorkflowModelElement>%s</WorkflowModelElement>%s" +
														 "<EventType>%s</EventType>%s" +
														 "<Timestamp>%s</Timestamp>%s";
	private static final String ENTRY_END_FORMAT = "</AuditTrailEntry>%s";
	
	private static final String DATA_SUB_FORMAT = "<Data>%%s%s</Data>%%s";
	
	//private static final String ATTRIBUTE_FORMAT = "<Attribute type=\"%s\" name=\"%s\">%s</Attribute>%s";
	private static final String ATTRIBUTE_FORMAT = "<Attribute name=\"%s\">%s</Attribute>%s";
	
	private static final String ORIGINATOR_FORMAT = "<Originator>%s</Originator>%s";
	
	private final String MXML_DATEPATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	protected String lineSeparator = DEFAULT_LINE_SEPARATOR;
	
//	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	public MXMLLogFormat(String processName) {
		super();
		try {
			setLogPerspective(LogPerspective.TRACE_PERSPECTIVE);
		} catch (PerspectiveException e) {
			e.printStackTrace();
		}
		setProcessName(processName);
	}
	
	public MXMLLogFormat(Charset charset, String processName) {
		this(processName);
		setCharset(charset);
	}
	
	@Override
	public String getDatePattern(){
		return MXML_DATEPATTERN;
	}
	
	@Override
	public String getName() {
		return "MXML-Format";
	}
	
	@Override
	public String getFileExtension() {
		return MXML_EXTENSION;
	}
	
	@Override
	public boolean supportsLogPerspective(LogPerspective logPerspective) {
		if(logPerspective == LogPerspective.TRACE_PERSPECTIVE)
			return true;
		return false;
	}

	@Override
	public boolean supportsCharset(Charset charset) {
		if(charset.name().equals("UTF-8"))
			return true;
		return false;
	}
	
	@Override
	public String getFileHeader() {
		return String.format(FILE_HEADER, charset.name(), processName);
	}
	
	@Override
	public String getFileFooter() {
		return FILE_FOOTER;
	}
	
	@Override
	public <E extends LogEntry> String getTraceAsString(LogTrace<E> trace) {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format(INSTANCE_START_FORMAT, trace.getCaseNumber(), DEFAULT_LINE_SEPARATOR));
		for(LogEntry e: trace.getEntries()) {
			builder.append(getEntryAsString(e, trace.getCaseNumber()));
		}
		builder.append(INSTANCE_END);
		return builder.toString();
	}

	@Override
	public <E extends LogEntry> String getEntryAsString(E entry, int caseNumber) {
		StringBuilder builder = new StringBuilder();
		builder.append(ENTRY_START_FORMAT);
		ArrayList<Object> formatArgs = new ArrayList<Object>();
		formatArgs.add(DEFAULT_LINE_SEPARATOR);
		formatArgs.add(entry.getActivity());
		formatArgs.add(DEFAULT_LINE_SEPARATOR);
		formatArgs.add(entry.getEventType()==null ? "" : entry.getEventType().toString());
		formatArgs.add(DEFAULT_LINE_SEPARATOR);
		Date timestamp = entry.getTimestamp();
		if(timestamp != null){
//			System.out.println(entry.getTimestamp());
//			System.out.println(dateFormat.format(entry.getTimestamp()));
//			System.out.println();
			formatArgs.add(dateFormat.format(entry.getTimestamp()));
		} else {
			formatArgs.add("-");
		}
		formatArgs.add(DEFAULT_LINE_SEPARATOR);
		if(entry.getOriginator()!=null){
			builder.append(ORIGINATOR_FORMAT);
			formatArgs.add(entry.getOriginator());
			formatArgs.add(DEFAULT_LINE_SEPARATOR);
		}
		
		if (entry instanceof DULogEntry) {
			Set<DataAttribute> data = ((DULogEntry) entry).getDataAttributes();
			if (data != null) {
				StringBuilder dataFormat = new StringBuilder();
				formatArgs.add(DEFAULT_LINE_SEPARATOR);
				if (data != null)
					for (DataAttribute input : data) {
						dataFormat.append(ATTRIBUTE_FORMAT);
						formatArgs.add(input.name);
						formatArgs.add(input.value);
						formatArgs.add(DEFAULT_LINE_SEPARATOR);
					}
				builder.append(String.format(DATA_SUB_FORMAT, dataFormat.toString()));
				formatArgs.add(DEFAULT_LINE_SEPARATOR);
			}
		}
		
		Set<DataAttribute> metaInformation = entry.getMetaAttributes();
		if(metaInformation != null && !metaInformation.isEmpty()){
			StringBuilder metaFormat = new StringBuilder();
			formatArgs.add(DEFAULT_LINE_SEPARATOR);
			for (DataAttribute meta: metaInformation) {
				metaFormat.append(ATTRIBUTE_FORMAT);
				formatArgs.add(meta.name);
				formatArgs.add(meta.value);
				formatArgs.add(DEFAULT_LINE_SEPARATOR);
			}
			builder.append(String.format(DATA_SUB_FORMAT, metaFormat.toString()));
			formatArgs.add(DEFAULT_LINE_SEPARATOR);
		}
		builder.append(ENTRY_END_FORMAT);
		formatArgs.add(DEFAULT_LINE_SEPARATOR);
		return String.format(builder.toString(), formatArgs.toArray());
	}

	@Override
	public LogFormatType getLogFormatType() {
		return LogFormatType.MXML;
	}

}
