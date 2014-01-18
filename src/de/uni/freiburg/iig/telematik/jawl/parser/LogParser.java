package de.uni.freiburg.iig.telematik.jawl.parser;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import de.invation.code.toval.parser.ParserException;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.DataAttribute;
import de.uni.freiburg.iig.telematik.jawl.log.EventType;
import de.uni.freiburg.iig.telematik.jawl.log.LockingException;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;

/**
 * <p>
 * A parser class for MXML and XES files for the JAWL log classes.
 * </p>
 * <p>
 * The {@link XUniversalParser} from OpenXES is used, as it already converts MXML and XES files into an uniform format. Because of the transformation of the files to an OpenXES log format and the subsequent transformation to the JAWL log format, the complexity in time and space ends up in O(2n). An own implementation without the OpenXES classes could result in O(n).
 * </p>
 * 
 * @author Adrian Lange
 */
public class LogParser {

	private XUniversalParser parser = new XUniversalParser();

	/**
	 * Checks whether the given file can be parsed by the file extension.
	 */
	public boolean canParse(File file) {
		return parser.canParse(file);
	}

	/**
	 * Parses the specified log file path and returns a collection of processes.
	 * 
	 * @param filePath
	 *            Path to file to parse
	 * @return Collection of processes, which consist of a collection of instances, which again consist of a collection of {@link LogTrace} objects.
	 * @throws ParameterException
	 *             Gets thrown if there's a discrepancy in how the file should be interpreted.
	 * @throws IOException
	 *             Gets thrown if the file under the given path can't be read, is a directory, or doesn't exist.
	 */
	public Collection<Collection<LogTrace>> parse(String filePath) throws ParameterException, ParserException {
		Validate.notNull(filePath);
		return parse(new File(filePath));
	}

	/**
	 * Parses the specified log file and returns a collection of processes.
	 * 
	 * @param file
	 *            File to parse
	 * @return Collection of processes, which consist of a collection of instances, which again consist of a collection of {@link LogTrace} objects.
	 * @throws ParameterException
	 *             Gets thrown if there's a discrepancy in how the file should be interpreted.
	 * @throws IOException
	 *             Gets thrown if the given file can't be read, is a directory, or doesn't exist.
	 */
	public Collection<Collection<LogTrace>> parse(File file) throws ParameterException, ParserException {
		Validate.noDirectory(file);
		if (!file.canRead())
			throw new ParameterException("Unable to read input file!");

		Collection<XLog> logs = null;
		try {
			logs = parser.parse(file);
		} catch (Exception e) {
			throw new ParserException("Error while parsing log with OpenXES-Parser: " + e.getMessage());
		}
		Collection<Collection<LogTrace>> logTracesCollection = new ArrayList<Collection<LogTrace>>(logs.size());
		for (XLog log : logs) {
			Collection<LogTrace> logTraces = new ArrayList<LogTrace>();
			for (XTrace trace : log) {
				Integer traceID = null;

				// Extract trace ID
				for (Map.Entry<String, XAttribute> attribute : trace.getAttributes().entrySet()) {
					String key = attribute.getKey();
					String value = attribute.getValue().toString();
					if (key.equals("concept:name")) {
						try {
							Validate.notNegativeInteger(value);
							traceID = Integer.parseInt(value);
						} catch (ParameterException e) {
							throw new ParserException("Cannot extract case-id.");
						}
					}
				}
				if (traceID == null)
					throw new ParserException("Cannot extract case-id");

				// Build new log trace
				LogTrace logTrace = new LogTrace(traceID);
				for (XEvent event : trace) {
					// Add events to log trace
					logTrace.addEntry(buildLogEntry(event));
				}
				logTraces.add(logTrace);
			}
			logTracesCollection.add(logTraces);
		}

		return logTracesCollection;
	}

	private LogEntry buildLogEntry(XEvent xesEvent) throws ParserException, ParameterException {
		LogEntry logEntry = new LogEntry();
		for (Map.Entry<String, XAttribute> attribute : xesEvent.getAttributes().entrySet()) {
			String key = attribute.getKey();
			if (key.equals("concept:name")) {
				addName(logEntry, attribute.getValue().toString());
			} else if (key.equals("org:resource")) {
				addOriginator(logEntry, attribute.getValue().toString());
			} else if (key.equals("lifecycle:transition")) {
				addEventType(logEntry, attribute.getValue().toString());
			} else if (key.equals("time:timestamp")) {
				addTimestamp(logEntry, attribute.getValue().toString());
			} else if (key.equals("dataUsage:data")) {
				addDataUsage(logEntry, attribute);
			} else {
				// If the key is unknown, a meta attribute with the key/value pair is added
				addMetaInformation(logEntry, attribute);
			}
		}
		return logEntry;
	}

	private void addName(LogEntry entry, String value) throws ParserException {
		if (value == null || value.isEmpty())
			throw new ParserException("No value for concept:name");
		try {
			entry.setActivity(value);
		} catch (Exception e) {
			throw new ParserException("Cannot set activity of log entry: " + e.getMessage());
		}
	}

	private void addOriginator(LogEntry entry, String value) throws ParserException {
		if (value == null || value.isEmpty())
			throw new ParserException("No value for org:resource");
		try {
			if (!entry.getOriginatorCandidates().contains(value))
				entry.addOriginatorCandidate(value);
			entry.setOriginator(value);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ParserException("Cannot set originator of log entry: " + e.getMessage());
		}
	}

	private void addEventType(LogEntry entry, String value) throws ParserException {
		if (value == null || value.isEmpty())
			throw new ParserException("No value for lifecycle:transition");
		EventType eventType = EventType.parse(value);
		if (eventType == null)
			throw new ParserException("Cannot parse event type: " + eventType);
		try {
			entry.setEventType(eventType);
		} catch (Exception e) {
			throw new ParserException("Cannot set event type of log entry: " + e.getMessage());
		}
	}

	private void addTimestamp(LogEntry entry, String value) throws ParserException {
		if (value == null || value.isEmpty())
			throw new ParserException("No value for time:timestamp");
		Date date = null;
		String sanitizedDateString = value.replaceAll(":(\\d\\d)$", "$1");
		for (ParserDateFormat pdf : ParserDateFormat.values()) {
			if (date == null) {
				try {
					date = ParserDateFormat.getDateFormat(pdf).parse(sanitizedDateString);
				} catch (ParseException e) {
					// is allowed to happen
				} catch (ParameterException e) {
					// cannot happen.
					e.printStackTrace();
				}
			}
		}
		if (date == null)
			throw new ParserException("Cannot read timestamp.");

		try {
			entry.setTimestamp(date);
		} catch (Exception e) {
			throw new ParserException("Cannot set log entry timestamp: " + e.getMessage());
		}
	}

	private void addDataUsage(LogEntry entry, Map.Entry<String, XAttribute> attribute) throws ParameterException {
		// Get sub-attributes
		for (Map.Entry<String, XAttribute> xAttribute : attribute.getValue().getAttributes().entrySet()) {
			String dataAttributeKey = xAttribute.getKey();
			Object dataAttributeValue = parseAttributeValue(xAttribute.getValue());
			DataAttribute dataAttribute = new DataAttribute(dataAttributeKey, dataAttributeValue);
			String dataAttributeDataUsageString = null;
			for (Map.Entry<String, XAttribute> dataUsage : xAttribute.getValue().getAttributes().entrySet()) {
				if (dataUsage.getKey().equals("dataUsage")) {
					dataAttributeDataUsageString = dataUsage.getValue().toString();
				}
			}
			List<DataUsage> dataUsageList = parseDataUsageString(dataAttributeDataUsageString);
			for (DataUsage d : dataUsageList) {
				try {
					entry.addDataUsage(dataAttribute, d);
				} catch (NullPointerException e) {
					// shouldn't happen
					e.printStackTrace();
				} catch (LockingException e) {
					// shouldn't happen
					e.printStackTrace();
				}
			}
		}
	}

	private void addMetaInformation(LogEntry entry, Map.Entry<String, XAttribute> attribute) throws ParserException {
		entry.addMetaAttribute(new DataAttribute(attribute.getKey(), attribute.getValue()));
	}

	/**
	 * Tries to parse the value of a {@link XAttribute} to a numeric, boolean, or string value.
	 */
	private Object parseAttributeValue(XAttribute xAttribute) {
		String attributeString = xAttribute.toString();

		// TODO better solution?

		// All numeric values as double
		try {
			double a = Double.parseDouble(attributeString);
			return a;
		} catch (NumberFormatException e) {
			// Ignore
		}
		// Boolean
		if (attributeString.trim().toLowerCase().equals("true"))
			return true;
		if (attributeString.trim().toLowerCase().equals("false"))
			return false;
		// String
		return attributeString;
	}

	/**
	 * Takes a String containing {@link DataUsage} identifier separated by commas, removes every leading and training whitespace, and parses them into a {@link List}. <br>
	 * TODO move to TOVAL into enum {@link DataUsage}?
	 */
	private static List<DataUsage> parseDataUsageString(String dataUsageString) throws ParameterException {
		List<String> dataUsageStrings = Arrays.asList(dataUsageString.split("\\s*,\\s*"));
		List<DataUsage> dataUsageList = new Vector<DataUsage>(dataUsageStrings.size());
		for (String d : dataUsageStrings) {
			DataUsage dataUsage = DataUsage.parse(d);
			if (!dataUsageList.contains(dataUsage))
				dataUsageList.add(dataUsage);
		}
		return dataUsageList;
	}

	public static void main(String[] args) throws ParameterException, ParserException {
		new LogParser().parse("/Users/stocker/Desktop/XESTest2.xes");
	}
}
