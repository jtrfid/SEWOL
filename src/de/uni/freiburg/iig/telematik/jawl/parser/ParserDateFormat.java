package de.uni.freiburg.iig.telematik.jawl.parser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

/**
 * The enumeration contains the different date formats that can occur in MXML and XES files.
 * 
 * @author Adrian Lange
 */
public enum ParserDateFormat {
	DEFAULT_MXML, MXML_WITHOUT_MILIS;

	public final static String DEFAULT_MXML_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public final static String MXML_WITHOUT_MILIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	/**
	 * Returns the date format string that belongs to the ParserDateFormat.
	 * 
	 * @see SimpleDateFormat
	 */
	public static String getFormatString(ParserDateFormat pdf) {
		Validate.notNull(pdf);
		switch (pdf) {
		case DEFAULT_MXML:
			return DEFAULT_MXML_FORMAT;
		case MXML_WITHOUT_MILIS:
			return MXML_WITHOUT_MILIS_FORMAT;
		default:
			throw new ParameterException("Date format \"" + pdf + "\" is not allowed.");
		}
	}

	/**
	 * Returns the {@link DateFormat} that belongs to the ParserDateFormat.
	 */
	public static DateFormat getDateFormat(ParserDateFormat pdf) {
		return new SimpleDateFormat(getFormatString(pdf));
	}
}
