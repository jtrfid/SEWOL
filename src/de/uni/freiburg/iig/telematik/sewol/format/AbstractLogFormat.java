package de.uni.freiburg.iig.telematik.sewol.format;

import java.nio.charset.Charset;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import de.invation.code.toval.file.FileFormat;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import de.uni.freiburg.iig.telematik.sewol.writer.PerspectiveException;
import de.uni.freiburg.iig.telematik.sewol.writer.PerspectiveException.PerspectiveError;

public abstract class AbstractLogFormat extends FileFormat {

        protected final TimeZone DEFAULT_TIMEZONE = Calendar.getInstance().getTimeZone();
        protected final Locale DEFAULT_LOCALE = new Locale(System.getProperty("user.language"));
        protected final LogPerspective DEFAULT_PERSPECTIVE = LogPerspective.TRACE_PERSPECTIVE;
        protected final String DEFAULT_DATEPATTERN = "yyyy-MM-dd HH:mm:ss.SSSZ";

        protected LogPerspective logPerspective;
        private TimeZone timeZone;
        private Locale locale;
        protected SimpleDateFormat dateFormat;

        protected String logName;
        protected String processName;

        public AbstractLogFormat() {
                initialize(DEFAULT_PERSPECTIVE);
        }

        public AbstractLogFormat(LogPerspective logPerspective) throws PerspectiveException {
                super();
                initialize(logPerspective);
        }

        public AbstractLogFormat(LogPerspective logPerspective, Charset charset) throws PerspectiveException {
                super(charset);
                initialize(logPerspective);
        }

        protected final void initialize(LogPerspective logPerspective) {
                try {
                        dateFormat = new SimpleDateFormat(getDatePattern());
                        setLogPerspective(logPerspective);
                        setTimeZone(DEFAULT_TIMEZONE.getID());
                        setLocale(DEFAULT_LOCALE);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }

        protected void setLogName(String logName) {
                Validate.notNull(logName);
                this.logName = logName;
        }

        protected void setProcessName(String processName) {
                Validate.notNull(processName);
                this.processName = processName;
        }

        public String getDatePattern() {
                // Replace all RFC 822 timezones (+0200, -1300) by the timezone with colon as separator.
                return DEFAULT_DATEPATTERN.replaceAll("([\\+-]\\d{2})(\\d\\d)$", "$1:$2");
        }

        public LogPerspective getLogPerspective() {
                return logPerspective;
        }

        public void setLogPerspective(LogPerspective logPerspective) throws PerspectiveException {
                if (!supportsLogPerspective(logPerspective)) {
                        throw new PerspectiveException(PerspectiveError.INCOMPATIBLE_LOGFORMAT);
                }
                this.logPerspective = logPerspective;
        }

        public TimeZone getTimeZone() {
                return timeZone;
        }

        public void setTimeZone(String ID) throws Exception {
                for (String validID : TimeZone.getAvailableIDs()) {
                        if (ID.equals(validID)) {
                                this.timeZone = TimeZone.getTimeZone(ID);
                                dateFormat.setTimeZone(timeZone);
                                return;
                        }
                }
                throw new Exception("Invalid time zone ID.");
        }

        public Locale getLocale() {
                return locale;
        }

        public void setLocale(Locale locale) {
                this.locale = locale;
                dateFormat.setDateFormatSymbols(DateFormatSymbols.getInstance(this.locale));
        }

        public abstract boolean supportsLogPerspective(LogPerspective logPerspective);

        public abstract <E extends LogEntry> String getTraceAsString(LogTrace<E> trace);

        public abstract <E extends LogEntry> String getEntryAsString(E entry, long caseNumber);

        public abstract LogFormatType getLogFormatType();

}
