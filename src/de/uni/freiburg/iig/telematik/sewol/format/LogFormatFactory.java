package de.uni.freiburg.iig.telematik.sewol.format;

import de.uni.freiburg.iig.telematik.sewol.writer.PerspectiveException;

public class LogFormatFactory {

        private static final String DEFAULT_LOG_NAME = "";

        public static AbstractLogFormat MXML() {
                return MXML(DEFAULT_LOG_NAME);
        }

        public static AbstractLogFormat MXML(String logName) {
                return new MXMLLogFormat(logName);
        }

        public static AbstractLogFormat XES() {
                return XES(DEFAULT_LOG_NAME);
        }

        public static AbstractLogFormat XES(String logName) {
                return new XESLogFormat(logName);
        }

        public static AbstractLogFormat PLAIN() {
                AbstractLogFormat result = null;
                try {
                        result = new PlainTraceLogFormat(LogPerspective.TRACE_PERSPECTIVE);
                } catch (PerspectiveException e) {
                        // Cannot happen, since PlainTraceLogFormat accepts trace perspective.
                        throw new RuntimeException(e);
                }
                return result;
        }

        public static AbstractLogFormat getFormat(LogFormatType formatType) {
                switch (formatType) {
                        case MXML:
                                return MXML();
                        case XES:
                                return XES();
                        case PLAIN:
                                return PLAIN();
                        default:
                                return null;
                }
        }

        public static AbstractLogFormat getFormat(LogFormatType formatType, String logName) {
                switch (formatType) {
                        case MXML:
                                return MXML(logName);
                        case XES:
                                return XES(logName);
                        case PLAIN:
                                return PLAIN();
                        default:
                                return null;
                }
        }

        public static LogFormatType getType(AbstractLogFormat logFormat) {
                if (logFormat instanceof MXMLLogFormat) {
                        return LogFormatType.MXML;
                }
                if (logFormat instanceof XESLogFormat) {
                        return LogFormatType.XES;
                }
                if (logFormat instanceof PlainTraceLogFormat) {
                        return LogFormatType.PLAIN;
                }
                return null;
        }
}
