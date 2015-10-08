package de.uni.freiburg.iig.telematik.sewol.parser.mxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.invation.code.toval.parser.ParserException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.format.MXMLLogFormat;
import de.uni.freiburg.iig.telematik.sewol.log.EventType;
import de.uni.freiburg.iig.telematik.sewol.log.LockingException;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogSummary;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import de.uni.freiburg.iig.telematik.sewol.parser.AbstractLogParser;
import de.uni.freiburg.iig.telematik.sewol.parser.ParserDateFormat;
import de.uni.freiburg.iig.telematik.sewol.parser.ParserFileFormat;
import de.uni.freiburg.iig.telematik.sewol.parser.ParsingMode;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>
 * A parser class for MXML files for the SEWOL log classes.
 * </p>
 *
 * @author Adrian Lange
 */
public class MXMLLogParser extends AbstractLogParser {

        /**
         * Parses the specified log file path and returns a collection of
         * processes.
         *
         * @param filePath Path to file to parse
         * @param parsingMode
         * @return Collection of processes, which consist of a collection of
         * instances, which again consist of a collection of {@link LogTrace}
         * objects.
         * @throws ParameterException Gets thrown if there's a discrepancy in
         * how the file should be interpreted.
         * @throws ParserException Gets thrown if the file under the given path
         * can't be read, is a directory, or doesn't exist.
         */
        public List<List<LogTrace<LogEntry>>> parse(String filePath, ParsingMode parsingMode) throws ParameterException, ParserException {
                Validate.notNull(filePath);
                return parse(new File(filePath), parsingMode);
        }

        /**
         * Parses the specified log file and returns a collection of processes.
         *
         * @param inputStream {@link InputStream} to parse
         * @param parsingMode
         * @param fileFormat Format of the {@link InputStream} as it can't be
         * determined automatically
         * @return Collection of processes, which consist of a collection of
         * instances, which again consist of a collection of {@link LogTrace}
         * objects.
         * @throws ParameterException Gets thrown if there's a discrepancy in
         * how the file should be interpreted.
         * @throws ParserException Gets thrown if the given file can't be read,
         * is a directory, or doesn't exist.
         */
        public List<List<LogTrace<LogEntry>>> parse(InputStream inputStream, ParsingMode parsingMode, ParserFileFormat fileFormat) throws ParameterException, ParserException {
                try {
                        inputStream.available();
                } catch (IOException e) {
                        throw new ParameterException("Unable to read input file: " + e.getMessage());
                }

                try {
                        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                        MXMLSAXHandler handler = new MXMLSAXHandler();
                        parser.parse(inputStream, handler);

                        summaries.addAll(handler.summaries);
                        parsedLogFiles = handler.logs;
                        return parsedLogFiles;
                } catch (ParserConfigurationException | SAXException | IOException ex) {
                        throw new ParserException(ex);
                }
        }

        /**
         * Parses the specified log file and returns a collection of processes.
         *
         * @param file File to parse
         * @param parsingMode
         * @return Collection of processes, which consist of a collection of
         * instances, which again consist of a collection of {@link LogTrace}
         * objects.
         * @throws ParameterException Gets thrown if there's a discrepancy in
         * how the file should be interpreted.
         * @throws ParserException Gets thrown if the given file can't be read,
         * is a directory, or doesn't exist.
         */
        @Override
        public List<List<LogTrace<LogEntry>>> parse(File file, ParsingMode parsingMode) throws ParameterException, ParserException {
                Validate.noDirectory(file);
                if (!file.canRead()) {
                        throw new ParameterException("Unable to read input file!");
                }

                try {
                        try {
                                InputStream is = new FileInputStream(file);
                                return parse(is, parsingMode, ParserFileFormat.getFileFormat(file));
                        } catch (FileNotFoundException | ParameterException | ParserException e) {
                                throw new ParserException("Exception while parsing with OpenXES: " + e.getMessage());
                        }
                } catch (Exception e) {
                        throw new ParserException("Error while parsing log with OpenXES-Parser: " + e.getMessage());
                }
        }

        // TODO remove
        public static void main(String[] args) throws ParameterException, ParserException {
                new MXMLLogParser().parse("/home/alange/validLogExample.mxml", ParsingMode.COMPLETE);
        }

        private static class MXMLSAXHandler extends DefaultHandler {

                private final List<List<LogTrace<LogEntry>>> logs = new ArrayList<>();
                private final List<LogSummary<LogEntry>> summaries = new ArrayList<>();

                private List<LogTrace<LogEntry>> currentLog = null;
                private LogTrace<LogEntry> currentTrace = null;
                private LogEntry currentEntry = null;
                private LogSummary<LogEntry> currentSummary = null;
                private final StringBuilder lastCharacters = new StringBuilder();
                private boolean recordCharacters = false;

                private static final Pattern INT_PATTERN = Pattern.compile("^(\\d+)$");
                private static final Pattern NON_INT_PATTERN = Pattern.compile("(\\D+)");

                @Override
                public void startDocument() throws SAXException {
                }

                @Override
                public void endDocument() throws SAXException {
                }

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                        switch (qName) {
                                case MXMLLogFormat.ELEMENT_LOG:
                                        currentLog = new ArrayList<>();
                                        currentSummary = new LogSummary<>();
                                        break;
                                case MXMLLogFormat.ELEMENT_TRACE:
                                        if (attributes.getIndex(MXMLLogFormat.ATTRIBUTE_ID) >= 0) {
                                                String idString = attributes.getValue(attributes.getIndex(MXMLLogFormat.ATTRIBUTE_ID));
                                                currentTrace = new LogTrace<>(idStrToInt(idString));
                                        } else {
                                                currentTrace = new LogTrace<>();
                                        }
                                        break;
                                case MXMLLogFormat.ELEMENT_ENTRY:
                                        currentEntry = new LogEntry();
                                        break;
                                case MXMLLogFormat.ELEMENT_ACTIVITY:
                                case MXMLLogFormat.ELEMENT_TYPE:
                                case MXMLLogFormat.ELEMENT_TIME:
                                case MXMLLogFormat.ELEMENT_ORIGINATOR:
                                        // TODO case MXMLLogFormat.ELEMENT_DATA:
                                        lastCharacters.setLength(0);
                                        recordCharacters = true;
                                        break;
                        }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                        {
                                try {
                                        switch (qName) {
                                                case MXMLLogFormat.ELEMENT_LOG:
                                                        summaries.add(currentSummary);
                                                        logs.add(currentLog);
                                                        break;
                                                case MXMLLogFormat.ELEMENT_TRACE:
                                                        currentLog.add(currentTrace);
                                                        currentSummary.addTrace(currentTrace);
                                                        break;
                                                case MXMLLogFormat.ELEMENT_ENTRY:
                                                        currentTrace.addEntry(currentEntry);
                                                        break;
                                                case MXMLLogFormat.ELEMENT_ACTIVITY:
                                                        currentEntry.setActivity(lastCharacters.toString());
                                                        recordCharacters = false;
                                                        break;
                                                case MXMLLogFormat.ELEMENT_TYPE:
                                                        currentEntry.setEventType(EventType.parse(lastCharacters.toString()));
                                                        recordCharacters = false;
                                                        break;
                                                case MXMLLogFormat.ELEMENT_TIME:
                                                        String dateStr = lastCharacters.toString();
                                                        Date date = parseTimestamp(dateStr);
                                                        currentEntry.setTimestamp(date);
                                                        recordCharacters = false;
                                                        break;
                                                case MXMLLogFormat.ELEMENT_ORIGINATOR:
                                                        currentEntry.setOriginator(lastCharacters.toString());
                                                        recordCharacters = false;
                                                        break;
                                                //case MXMLLogFormat.ELEMENT_DATA:
                                                //        // TODO
                                                //        recordCharacters = false;
                                                //        break;
                                        }
                                } catch (LockingException ex) {
                                        throw new RuntimeException(ex);
                                }
                        }
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                        if (recordCharacters) {
                                StringBuilder str = new StringBuilder();
                                for (int i = 0; i < length; i++) {
                                        str.append(ch[start + i]);
                                }
                                lastCharacters.append(str);
                        }
                }

                @Override
                public void error(SAXParseException e) throws SAXException {
                        throw e;
                }

                @Override
                public void warning(SAXParseException e) throws SAXException {
                        throw e;
                }

                private Date parseTimestamp(String value) {
                        if (value == null || value.isEmpty()) {
                                return null;
                        }
                        String sanitizedDateString = value.replaceAll(":(\\d\\d)$", "$1");
                        Date date = null;
//                        String sanitizedDateString = value.replaceAll(":(\\d\\d)$", "$1");
                        for (ParserDateFormat pdf : ParserDateFormat.values()) {
                                if (date == null) {
                                        try {
                                                date = ParserDateFormat.getDateFormat(pdf).parse(sanitizedDateString);
                                        } catch (ParseException e) {
                                                // is allowed to happen
                                        } catch (ParameterException e) {
                                                // cannot happen.
                                                throw new RuntimeException(e);
                                        }
                                }
                        }

                        return date;
                }

                private static int idStrToInt(String idString) {
                        if (idString.matches(INT_PATTERN.pattern())) {
                                return Integer.parseInt(idString);
                        } else if (idString.replaceAll(NON_INT_PATTERN.pattern(), "").matches(INT_PATTERN.pattern())) {
                                return Integer.parseInt(idString.replaceAll(NON_INT_PATTERN.pattern(), ""));
                        } else {
                                return idString.hashCode();
                        }
                }
        }
}
