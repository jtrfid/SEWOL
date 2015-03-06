package de.uni.freiburg.iig.telematik.sewol.parser;

import java.io.File;

import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XParserRegistry;
import org.deckfour.xes.in.XesXmlParser;

import de.invation.code.toval.validate.ParameterException;

/**
 * Possible file formats the parser can deal with.
 * 
 * @version 1.0
 * @author Adrian Lange
 */
public enum ParserFileFormat {
	MXML, XES;

	/**
	 * Returns the needed parser for the ParserFileFormat.
	 */
	public XParser getParser() throws ParameterException {
		switch (this) {
		case MXML:
			return new XMxmlParser();
		case XES:
			return new XesXmlParser();
		default:
			throw new ParameterException("Couldn't interpret given ParserFileFormat.");
		}
	}

	/**
	 * Returns the ParserFileFormat for a given file.
	 */
	public static ParserFileFormat getFileFormat(File file) {
		for (XParser parser : XParserRegistry.instance().getAvailable()) {
			if (parser.canParse(file)) {
				if (parser instanceof XesXmlParser)
					return ParserFileFormat.XES;
				else if (parser instanceof XMxmlParser)
					return ParserFileFormat.MXML;
				throw new UnsupportedOperationException("Couldn't find a file format for that parser.");
			}
		}
		throw new UnsupportedOperationException("Couldn't find a parser for that file format.");
	}
}
