package de.uni.freiburg.iig.telematik.jawl.format;

import de.uni.freiburg.iig.telematik.jawl.writer.PerspectiveException;

public class LogFormatFactory {
	
	public static AbstractLogFormat MXML(){
		return new MXMLLogFormat();
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
			case PLAIN: return PLAIN();
			case MXML: return MXML();
			default: return null;
		}
		
	}
	
	public static LogFormatType getType(AbstractLogFormat logFormat){
		if(logFormat instanceof MXMLLogFormat)
			return LogFormatType.MXML;
		if(logFormat instanceof PlainTraceLogFormat)
			return LogFormatType.PLAIN;
		return null;
	}

}
