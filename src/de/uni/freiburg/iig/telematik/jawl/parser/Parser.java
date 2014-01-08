package de.uni.freiburg.iig.telematik.jawl.parser;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.DataAttribute;
import de.uni.freiburg.iig.telematik.jawl.log.EventType;
import de.uni.freiburg.iig.telematik.jawl.log.LockingException;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.jawl.log.ModificationException;

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
public class Parser {

	private static XUniversalParser parser = new XUniversalParser();

	/**
	 * Checks whether the given file can be parsed by the file extension.
	 */
	public static boolean canParse(File file) {
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
	public static Collection<Collection<LogTrace>> parse(String filePath) throws ParameterException, IOException {
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
	public static Collection<Collection<LogTrace>> parse(File file) throws ParameterException, IOException {
		Validate.notNull(file);
		if (!file.exists())
			throw new IOException("I/O Error on opening file: File does not exist!");
		if (file.isDirectory())
			throw new IOException("I/O Error on opening file: File is a directory!");
		if (!file.canRead())
			throw new IOException("I/O Error on opening file: Unable to read file!");

		Collection<XLog> logs = null;
		try {
			logs = parser.parse(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Collection<Collection<LogTrace>> logTracesCollection = new ArrayList<Collection<LogTrace>>(logs.size());
		for (XLog log : logs) {
			Collection<LogTrace> logTraces = new ArrayList<LogTrace>();
			int logTraceIndex = 0;
			for (XTrace trace : log) {
				LogTrace logTrace = new LogTrace(logTraceIndex);
				for (XEvent event : trace) {
					System.out.println(event.getExtensions());
					LogEntry logEntry = new LogEntry();
					for (Map.Entry<String, XAttribute> attribute : event.getAttributes().entrySet()) {
						String key = attribute.getKey();
						String value = attribute.getValue().toString();
						if (key.equals("concept:name")) {
							try {
								logEntry.setActivity(value);
							} catch (LockingException e) {
								// shouldn't happen
								e.printStackTrace();
							}
						} else if (key.equals("org:resource")) {
							if (value != null && value.length() > 0) {
								if (logEntry.getOriginatorCandidates().contains(value) == false) {
									try {
										logEntry.addOriginatorCandidate(value);
									} catch (NullPointerException e) {
										// shouldn't happen
										e.printStackTrace();
									} catch (LockingException e) {
										// shouldn't happen
										e.printStackTrace();
									}
								}
								try {
									logEntry.setOriginator(value);
								} catch (NullPointerException e) {
									// shouldn't happen
									e.printStackTrace();
								} catch (LockingException e) {
									// shouldn't happen
									e.printStackTrace();
								} catch (ModificationException e) {
									// shouldn't happen
									e.printStackTrace();
								}
							}
						} else if (key.equals("lifecycle:transition")) {
							if (value != null && value.length() > 0) {
								EventType eventType = EventType.parse(value);
								if (eventType != null) {
									try {
										logEntry.setEventType(eventType);
									} catch (LockingException e) {
										// shouldn't happen
										e.printStackTrace();
									}
								}
							}
						} else if (key.equals("time:timestamp")) {
							Date date = null;
							String sanitizedDateString = value.replaceAll(":(\\d\\d)$", "$1");
							for (ParserDateFormat pdf : ParserDateFormat.values()) {
								if (date == null) {
									try {
										date = ParserDateFormat.getDateFormat(pdf).parse(sanitizedDateString);
									} catch (ParseException e) {
										// is allowed to happen
									}
								}
							}
							try {
								if (date != null)
									logEntry.setTimestamp(date);
							} catch (NullPointerException e) {
								// shouldn't happen
								e.printStackTrace();
							} catch (LockingException e) {
								// shouldn't happen
								e.printStackTrace();
							}
						} else if (key.equals("dataUsage:dataUsage")) {
							System.out.println(attribute.getValue().getAttributes());
						} else {
							// If the key is unknown, a data attribute with the key and value pair is added
							try {
								// FIXME which data usage by default?
								logEntry.addDataUsage(new DataAttribute(key, value), DataUsage.CREATE);
								System.out.println(key + ": " + value);
							} catch (NullPointerException e) {
								// shouldn't happen
								e.printStackTrace();
							} catch (LockingException e) {
								// shouldn't happen
								e.printStackTrace();
							}
						}
					}
					logTrace.addEntry(logEntry);
				}
				// TODO extensions?
				logTraces.add(logTrace);
				logTraceIndex++;
			}
			logTracesCollection.add(logTraces);
		}

		return logTracesCollection;
	}
	
	public static void main(String[] args) throws ParameterException, IOException {
		Parser.parse("/Users/stocker/Desktop/XESTest2.xes");
	}
}
