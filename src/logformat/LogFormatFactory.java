package logformat;

import writer.PerspectiveException;

public class LogFormatFactory {
	
	public static LogFormat MXML(){
		return new MXMLLogFormat();
	}
	
	public static LogFormat PLAIN(){
		LogFormat result = null;
		try {
			result = new PlainTraceLogFormat(LogPerspective.TRACE_PERSPECTIVE);
		} catch (PerspectiveException e) {
			// Cannot happen, since PlainTraceLogFormat accepts trace perspective.
			e.printStackTrace();
		}
		return result;
	}
	
	public static LogFormat getFormat(LogFormatType formatType){
		switch (formatType) {
			case PLAIN: return PLAIN();
			case MXML: return MXML();
			default: return null;
		}
		
	}
	
	public static LogFormatType getType(LogFormat logFormat){
		if(logFormat instanceof MXMLLogFormat)
			return LogFormatType.MXML;
		if(logFormat instanceof PlainTraceLogFormat)
			return LogFormatType.PLAIN;
		return null;
	}

}
