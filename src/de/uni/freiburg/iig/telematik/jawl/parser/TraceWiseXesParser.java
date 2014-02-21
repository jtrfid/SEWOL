package de.uni.freiburg.iig.telematik.jawl.parser;

import java.io.IOException;
import java.util.Iterator;

import de.invation.code.toval.file.FileReader;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

public class TraceWiseXesParser implements Iterator<LogFragment> {

	private final String LOG_END = "</log>";
	public static final int DEFAULT_FRAGMENT_SIZE = 1;

	private FileReader fileReader = null;
	private String header = null;
	private Boolean hasNextTrace = null;
	private String lastTraceStart = null;
	private int fragmentSize = 1;

	public TraceWiseXesParser(String logFile) throws ParameterException, IOException {
		this(logFile, DEFAULT_FRAGMENT_SIZE);
	}

	public TraceWiseXesParser(String logFile, int fragmentSize) throws ParameterException, IOException {
		Validate.exists(logFile);
		Validate.positive(fragmentSize);
		fileReader = new FileReader(logFile);
		header = nextStringFragment();
		this.fragmentSize = fragmentSize;
	}

	private String nextStringFragment() throws IOException {
		if (hasNextTrace != null && !hasNextTrace)
			return null;

		StringBuffer buffer = new StringBuffer();
		if (lastTraceStart != null) {
			buffer.append(lastTraceStart);
			buffer.append(System.getProperty("line.separator"));
		}
		String nextLine = null;
		while ((nextLine = fileReader.readLine()) != null && !headerEnd(nextLine)) {
			buffer.append(nextLine);
			buffer.append(System.getProperty("line.separator"));
		}
		if (!nextLine.trim().startsWith("</log")) {
			lastTraceStart = nextLine;
			hasNextTrace = true;
		} else {
			hasNextTrace = false;
		}
		return buffer.toString();
	}

	private boolean headerEnd(String line) {
		String cleanedLine = line.trim();
		return cleanedLine.startsWith("<trace") || cleanedLine.startsWith("</log");
	}

	@Override
	public boolean hasNext() {
		return hasNextTrace;
	}

	// @Override
	// public LogTrace next() {
	// if(hasNext()){
	// try {
	// System.out.println(nextStringFragment());
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// return null;
	// }

	@Override
	public LogFragment next() {
		if (hasNext()) {
			try {
				LogFragment newFragment = new LogFragment();
				newFragment.addLine(header);
				int traceCount = 0;
				while (hasNext() && traceCount++ < fragmentSize) {
					newFragment.addLine(nextStringFragment());
				}
				newFragment.addLine(LOG_END);
				newFragment.close();
				// System.out.println(newFragment);
				return newFragment;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
