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
import de.uni.freiburg.iig.telematik.sewol.log.DataAttribute;
import de.uni.freiburg.iig.telematik.sewol.log.EventType;
import de.uni.freiburg.iig.telematik.sewol.log.LockingException;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogSummary;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import de.uni.freiburg.iig.telematik.sewol.parser.AbstractLogParser;
import de.uni.freiburg.iig.telematik.sewol.parser.ParserDateFormat;
import de.uni.freiburg.iig.telematik.sewol.parser.ParsingMode;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>
 * A parser class for MXML files for the SEWOL log classes. To parse large MXML
 * log files, this parser is a SAX parser instead of a DOM parser.
 * </p>
 *
 * @version 1.0.2
 * @since 1.0.2
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
        @Override
        public List<List<LogTrace<LogEntry>>> parse(String filePath, ParsingMode parsingMode) throws ParameterException, ParserException {
                Validate.notNull(filePath);
                return parse(new File(filePath), parsingMode);
        }

        /**
         * Parses the specified log file and returns a collection of processes.
         *
         * @param inputStream {@link InputStream} to parse
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
        public List<List<LogTrace<LogEntry>>> parse(InputStream inputStream, ParsingMode parsingMode) throws ParameterException, ParserException {
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
                                return parse(is, parsingMode);
                        } catch (FileNotFoundException | ParameterException | ParserException e) {
                                throw new ParserException(e);
                        }
                } catch (Exception e) {
                        throw new ParserException(e);
                }
        }

        private static class MXMLSAXHandler extends DefaultHandler {

                private final List<List<LogTrace<LogEntry>>> logs = new ArrayList<>();
                private final List<LogSummary<LogEntry>> summaries = new ArrayList<>();

                private List<LogTrace<LogEntry>> currentLog = null;
                private LogTrace<LogEntry> currentTrace = null;
                private LogEntry currentEntry = null;
                private LogSummary<LogEntry> currentSummary = null;
                private DataAttribute currentAttribute = null;
                private final StringBuilder lastCharacters = new StringBuilder();
                private boolean recordCharacters = false;
                private Date date = null;
                private static ParserDateFormat PARSER_DATE_FORMAT = null;
                private static boolean PARSER_DATE_FORMAT_INITIALIZED = false;
                private static boolean PARSER_DATE_FORMAT_SANITIZE = false;

                private static final String TIMESTAMP_ENDING_PATTERN = ":(\\d\\d)$";

                private static final String INT_PATTERN = "^0*(\\d+)$";
                private static final String DOUBLE_PATTERN = "^((?:\\d+\\.\\d+)|(?:\\d+\\.)|(?:\\.\\d+))$";
                private static final String NON_INT_PATTERN = "(\\D+)";

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
                                        lastCharacters.setLength(0);
                                        recordCharacters = true;
                                        break;
                                case MXMLLogFormat.ELEMENT_ATTRIBUTE:
                                        if (currentEntry != null) {
                                                lastCharacters.setLength(0);
                                                recordCharacters = true;
                                                if (attributes.getIndex(MXMLLogFormat.ATTRIBUTE_NAME) >= 0) {
                                                        String nameString = attributes.getValue(attributes.getIndex(MXMLLogFormat.ATTRIBUTE_NAME)).intern();
                                                        currentAttribute = new DataAttribute(nameString);
                                                }
                                        }
                                        break;
                        }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
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
                                                currentEntry = null;
                                                break;
                                        case MXMLLogFormat.ELEMENT_ACTIVITY:
                                                currentEntry.setActivity(lastCharacters.toString().intern());
                                                recordCharacters = false;
                                                break;
                                        case MXMLLogFormat.ELEMENT_TYPE:
                                                EventType type = EventType.parse(lastCharacters.toString().intern(), false);
                                                if (type != null) {
                                                        currentEntry.setEventType(type);
                                                }
                                                recordCharacters = false;
                                                break;
                                        case MXMLLogFormat.ELEMENT_TIME:
                                                String dateStr = lastCharacters.toString().intern();
                                                date = parseTimestamp(dateStr, true);
                                                if (date != null) {
                                                        currentEntry.setTimestamp(date);
                                                }
                                                recordCharacters = false;
                                                break;
                                        case MXMLLogFormat.ELEMENT_ORIGINATOR:
                                                currentEntry.setOriginator(lastCharacters.toString().intern());
                                                recordCharacters = false;
                                                break;
                                        case MXMLLogFormat.ELEMENT_ATTRIBUTE:
                                                if (currentAttribute != null) {
                                                        String value = lastCharacters.toString().intern();

                                                        if (value.matches(INT_PATTERN)) {
                                                                currentAttribute.value = Long.valueOf(value);
                                                        } else if (value.matches(DOUBLE_PATTERN)) {
                                                                currentAttribute.value = Double.valueOf(value);
                                                        } else {
                                                                currentAttribute.value = value;
                                                        }
                                                        currentEntry.addMetaAttribute(currentAttribute);
                                                        currentAttribute = null;
                                                }
                                                recordCharacters = false;
                                                break;
                                }
                        } catch (LockingException ex) {
                                throw new RuntimeException(ex);
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

                private static Date parseTimestamp(String value, boolean sanitize) {
                        if (value == null || value.isEmpty()) {
                                return null;
                        }

                        if (!PARSER_DATE_FORMAT_INITIALIZED) {
                                String sanitizedValue = value.replaceAll(TIMESTAMP_ENDING_PATTERN, "$1");
                                if (!sanitizedValue.equals(value)) {
                                        PARSER_DATE_FORMAT_SANITIZE = true;
                                        value = sanitize ? value = sanitizedValue : value;
                                }
                                PARSER_DATE_FORMAT_INITIALIZED = true;

                                for (ParserDateFormat pdf : ParserDateFormat.values()) {
                                        try {
                                                ParserDateFormat.getDateFormat(pdf).parse(value);
                                                PARSER_DATE_FORMAT = pdf;
                                        } catch (ParseException e) {
                                                // is allowed to happen
                                        } catch (ParameterException e) {
                                                // cannot happen.
                                                throw new RuntimeException(e);
                                        }
                                }
                        }

                        if (sanitize && PARSER_DATE_FORMAT_SANITIZE) {
                                value = value.replaceAll(TIMESTAMP_ENDING_PATTERN, "$1");
                        }

                        try {
                                return ParserDateFormat.getDateFormat(PARSER_DATE_FORMAT).parse(value);
                        } catch (ParseException ex) {
                                // is allowed to happen
                        }
                        return null;
                }

                private static long idStrToInt(String idString) {
                        if (idString.matches(INT_PATTERN)) {
                                try {
                                        return Long.parseLong(idString);
                                } catch (NumberFormatException e) {
                                        // if number is too big for long
                                        return e.hashCode();
                                }
                        } else if (idString.replaceAll(NON_INT_PATTERN, "").matches(INT_PATTERN)) {
                                return Long.parseLong(idString.replaceAll(NON_INT_PATTERN, ""));
                        } else {
                                return idString.hashCode();
                        }
                }
        }
//        public static void main(String[] args) throws ParameterException, ParserException {
//                MXMLLogParser p = new MXMLLogParser();
//                File file = new File("/home/alange/B1large.mxml");
//                File file = new File("/home/alange/WriterTest.mxml");
//                File file = new File("/home/alange/validLogExample.mxml");
//                long max = file.length();
//
//                try (
//                        MonitoredInputStream mis = new MonitoredInputStream(new FileInputStream(file), max, 1024 * 1024 * 5)) {
//
//                        mis.addChangeListener(new ChangeListener() {
//                                @Override
//                                public void stateChanged(ChangeEvent e) {
//                                        SwingUtilities.invokeLater(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                        System.out.println(mis.getProgress());
//                                                }
//                                        });
//                                }
//                        });
//
//                        p.parse(mis, ParsingMode.COMPLETE);
//                } catch (IOException e) {
//                        throw new RuntimeException(e);
//                }
//
//                System.out.println(p.summaries.get(0).getAverageTraceLength());
//        }
}
