package de.uni.freiburg.iig.telematik.jawl.parser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import de.invation.code.toval.validate.ParameterException;

/**
 * TODO
 * 
 * @author Adrian Lange
 */
public enum ParserDateFormat {
	DEFAULT_MXML, MXML_WITHOUT_MILIS;

	/**
	 * Returns the date format string that belongs to the ParserDateFormat.
	 * 
	 * @see SimpleDateFormat
	 */
	public static String getFormatString(ParserDateFormat pdf) throws ParameterException {
		if (pdf == DEFAULT_MXML) {
			return "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
		} else if (pdf == MXML_WITHOUT_MILIS) {
			return "yyyy-MM-dd'T'HH:mm:ssZ";
		}
		throw new ParameterException("Date format \"" + pdf + "\" is not allowed.");
	}

	/**
	 * Returns the {@link DateFormat} that belongs to the ParserDateFormat.
	 */
	public static DateFormat getDateFormat(ParserDateFormat pdf) throws ParameterException {
		return new SimpleDateFormat(getFormatString(pdf));
	}
}
