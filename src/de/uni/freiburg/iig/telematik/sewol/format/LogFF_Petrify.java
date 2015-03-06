package de.uni.freiburg.iig.telematik.sewol.format;

import java.nio.charset.Charset;

import de.invation.code.toval.file.FileFormat;

public class LogFF_Petrify extends FileFormat{

	@Override
	public String getFileExtension() {
		return "tr";
	}

	@Override
	public String getName() {
		return "Petrify";
	}

	@Override
	public boolean supportsCharset(Charset charset) {
		return true;
	}

}
