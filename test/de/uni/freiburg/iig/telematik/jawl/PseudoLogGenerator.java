package de.uni.freiburg.iig.telematik.jawl;

import java.io.IOException;

import de.invation.code.toval.file.FileWriter;

public class PseudoLogGenerator {
	
	private static final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final String log_begin = "<log xes.version=\"1.0\" openxes.version=\"test\" xes.features=\"arbitrary-depth\" xmlns=\"http://code.fluxicon.com/xes\">\n" +
											"<extension name=\"Concept\" prefix=\"concept\" uri=\"http://code.fluxicon.com/xes/concept.xesext\"/>";
	private static final String log_end = "</log>";
	
	private static final String log_entry = 
											"<trace>\n" +
											"<event><string key=\"concept:name\" value=\"A\"/></event>\n" +
											"<event><string key=\"concept:name\" value=\"B\"/></event>\n" +
											"<event><string key=\"concept:name\" value=\"C\"/></event>\n" + 
											"<event><string key=\"concept:name\" value=\"B\"/><string key=\"concept:name\" value=\"F\"/></event>\n" +
											"<event><string key=\"concept:name\" value=\"D\"/></event>\n" +
											"<event><string key=\"concept:name\" value=\"C\"/></event>\n" +
											"<event><string key=\"concept:name\" value=\"D\"/></event>\n" +
											"<event><string key=\"concept:name\" value=\"B\"/></event>\n" +
											"</trace>\n\n";

	public static void generateLog(String path, String fileName, int numTraces) throws IOException {
		FileWriter writer = new FileWriter(path, fileName);
		writer.setFileExtension("xes");
		writer.writeLine(header);
		writer.writeLine(log_begin);
		for(int i=0; i<numTraces; i++){
			writer.write(log_entry);
		}
		writer.writeLine(log_end);
	}

	public static void main(String[] args) throws Exception {
		generateLog("/Users/stocker/Desktop/", "pseudoLog", 10000000);
	}
	
}
