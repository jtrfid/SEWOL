package de.uni.freiburg.iig.telematik.jawl.logformat;

import java.nio.charset.Charset;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import de.invation.code.toval.file.FileFormat;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.jawl.writer.PerspectiveException;
import de.uni.freiburg.iig.telematik.jawl.writer.PerspectiveException.PerspectiveError;



public abstract class AbstractLogFormat extends FileFormat{
	
	protected final TimeZone DEFAULT_TIMEZONE = Calendar.getInstance().getTimeZone();
	protected final Locale DEFAULT_LOCALE = new Locale(System.getProperty("user.language"));
	protected final LogPerspective DEFAULT_PERSPECTIVE = LogPerspective.TRACE_PERSPECTIVE;
	protected final String DEFAULT_DATEPATTERN = "yyyy-MM-dd HH:mm:ss.SSSZ";
	
	protected LogPerspective logPerspective;
	private TimeZone timeZone;
	private Locale locale;
	protected SimpleDateFormat dateFormat;
	
	public AbstractLogFormat(){
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
	
	protected void initialize(LogPerspective logPerspective) {
		try {
			dateFormat = new SimpleDateFormat(getDatePattern());
			setLogPerspective(logPerspective);
			setTimeZone(DEFAULT_TIMEZONE.getID());
			setLocale(DEFAULT_LOCALE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getDatePattern(){
		return DEFAULT_DATEPATTERN;
	}

	public LogPerspective getLogPerspective(){
		return logPerspective;
	}
	
	public void setLogPerspective(LogPerspective logPerspective) throws PerspectiveException{
		if(!supportsLogPerspective(logPerspective))
			throw new PerspectiveException(PerspectiveError.INCOMPATIBLE_LOGFORMAT);
		this.logPerspective = logPerspective;
	}
	
	public TimeZone getTimeZone(){
		return timeZone;
	}
	
	public void setTimeZone(String ID) throws Exception{
		for(String validID: TimeZone.getAvailableIDs())
			if(ID.equals(validID)){
				this.timeZone = TimeZone.getTimeZone(ID);
				dateFormat.setTimeZone(timeZone);
				return;
			}
		throw new Exception("Invalid time zone ID.");
	}
	
	public Locale getLocale(){
		return locale;
	}
	
	public void setLocale(Locale locale){
		this.locale = locale;
		dateFormat.setDateFormatSymbols(DateFormatSymbols.getInstance(this.locale));
	}
	
	public abstract boolean supportsLogPerspective(LogPerspective logPerspective);
	
	public abstract <E extends LogEntry> String getTraceAsString(LogTrace<E> trace);
	
	public abstract <E extends LogEntry> String getEntryAsString(E entry, int caseNumber);
	
	public abstract LogFormatType getLogFormatType();

}
