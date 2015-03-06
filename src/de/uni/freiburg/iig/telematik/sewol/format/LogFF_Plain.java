package de.uni.freiburg.iig.telematik.sewol.format;

import java.nio.charset.Charset;

import de.invation.code.toval.file.FileFormat;

public class LogFF_Plain extends FileFormat{

	@Override
	public String getFileExtension() {
		return "txt";
	}

	@Override
	public String getName() {
		return "PLAIN";
	}

	@Override
	public boolean supportsCharset(Charset charset) {
		return true;
	}

}
