package de.uni.freiburg.iig.telematik.sewol.format;

import de.uni.freiburg.iig.telematik.sewol.writer.PerspectiveException;

public class LogFormatFactory {

	public static AbstractLogFormat MXML(){
		return new MXMLLogFormat();
	}
	
	public static AbstractLogFormat XES(){
		return new XESLogFormat("");
	}
	
	public static AbstractLogFormat PLAIN(){
		AbstractLogFormat result = null;
		try {
			result = new PlainTraceLogFormat(LogPerspective.TRACE_PERSPECTIVE);
		} catch (PerspectiveException e) {
			// Cannot happen, since PlainTraceLogFormat accepts trace perspective.
			e.printStackTrace();
		}
		return result;
	}
	
	public static AbstractLogFormat getFormat(LogFormatType formatType){
		switch (formatType) {
			case MXML: return MXML();
			case XES: return XES();
			case PLAIN: return PLAIN();
			default: return null;
		}
		
	}
	
	public static LogFormatType getType(AbstractLogFormat logFormat){
		if(logFormat instanceof MXMLLogFormat)
			return LogFormatType.MXML;
		if(logFormat instanceof XESLogFormat)
			return LogFormatType.XES;
		if(logFormat instanceof PlainTraceLogFormat)
			return LogFormatType.PLAIN;
		return null;
	}

}
