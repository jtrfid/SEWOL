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

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.LockingException;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.jawl.log.ModificationException;

/**
 * TODO
 * 
 * @author Adrian Lange
 */
public class Parser {

	private static XUniversalParser parser = new XUniversalParser();

	/**
	 * Checks whether the given file can be parsed.
	 */
	public static boolean canParse(File file) {
		return parser.canParse(file);
	}

	/**
	 * TODO
	 * 
	 * @param filePath
	 * @return
	 * @throws ParameterException
	 * @throws IOException
	 */
	public static Collection<Collection<LogTrace>> parse(String filePath) throws ParameterException, IOException {
		Validate.notNull(filePath);

		return parse(new File(filePath));
	}

	/**
	 * TODO
	 * 
	 * @param file
	 * @return
	 * @throws ParameterException
	 * @throws IOException
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
								if (logEntry.getOriginatorCandidates().contains(value) == false)
									try {
										logEntry.addOriginatorCandidate(value);
									} catch (NullPointerException e) {
										// shouldn't happen
										e.printStackTrace();
									} catch (LockingException e) {
										// shouldn't happen
										e.printStackTrace();
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
						}
					}
					logTrace.addEntry(logEntry);
				}
				// TODO extensions
				logTraces.add(logTrace);
				logTraceIndex++;
			}
			logTracesCollection.add(logTraces);
		}

		return logTracesCollection;
	}
}
