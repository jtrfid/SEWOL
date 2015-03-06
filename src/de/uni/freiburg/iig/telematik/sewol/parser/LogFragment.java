package de.uni.freiburg.iig.telematik.sewol.parser;

import java.io.IOException;
import java.io.InputStream;

public class LogFragment extends InputStream {
	
	private byte[] content = null;
	private StringBuffer buffer = null;
	private int counter = 0;
	
	public LogFragment(){
		reset();
	}
	
	public void addLine(String line){
		buffer.append(line);
		buffer.append(System.getProperty("line.separator"));
	}
	
	@Override
	public void reset(){
		content = null;
		buffer = new StringBuffer();
		counter = 0;
	}
	
	@Override
	public void close(){
		content = buffer.toString().getBytes();
	}

	@Override
	public int read() throws IOException {
		if(counter == content.length)
			return -1;
		return content[counter++];
	}
	
	@Override
	public String toString(){
		return new String(content);
	}

}
