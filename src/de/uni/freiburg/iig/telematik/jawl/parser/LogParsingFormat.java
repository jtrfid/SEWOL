package de.uni.freiburg.iig.telematik.jawl.parser;

import de.invation.code.toval.file.FileFormat;
import de.uni.freiburg.iig.telematik.jawl.format.LogFF_Plain;
import de.uni.freiburg.iig.telematik.jawl.format.LogFF_Petrify;
import de.uni.freiburg.iig.telematik.jawl.format.LogFF_XES;


public enum LogParsingFormat {
	
	XES(new LogFF_XES()),
	
	PETRIFY(new LogFF_Petrify()),
	
	PLAIN_TAB(new LogFF_Plain()),
	
	PLAIN_SPACE(new LogFF_Plain());
	
	private FileFormat fileFormat = null;
	
	private LogParsingFormat(FileFormat fileFormat){
		this.fileFormat = fileFormat;
	}
	
	public FileFormat getFileFormat(){
		return fileFormat;
	}

}
