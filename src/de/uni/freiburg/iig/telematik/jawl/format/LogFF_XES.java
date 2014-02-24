package de.uni.freiburg.iig.telematik.jawl.format;

import java.nio.charset.Charset;

import de.invation.code.toval.file.FileFormat;

public class LogFF_XES extends FileFormat{

	@Override
	public String getFileExtension() {
		return "xes";
	}

	@Override
	public String getName() {
		return "XES";
	}

	@Override
	public boolean supportsCharset(Charset charset) {
		return true;
	}

}
