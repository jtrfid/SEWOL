package de.uni.freiburg.iig.telematik.jawl.parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.deckfour.xes.in.XesXmlParser;

import de.invation.code.toval.file.FileReader;
import de.invation.code.toval.graphic.component.DisplayFrame;
import de.invation.code.toval.graphic.diagrams.models.OneDimChartModel;
import de.invation.code.toval.graphic.diagrams.models.ScatterChartModel;
import de.invation.code.toval.graphic.diagrams.panels.AdjustableDiagramPanel;
import de.invation.code.toval.graphic.diagrams.panels.OneDimChartPanel;
import de.invation.code.toval.graphic.diagrams.panels.ScatterChartPanel;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

public class TraceWiseXesParser implements Iterator<LogFragment>{
	
	private final String LOG_END = "</log>";
	public static final int DEFAULT_FRAGMENT_SIZE = 1;
	
	private FileReader fileReader = null;
	private String header = null;
	private Boolean hasNextTrace = null;
	private String lastTraceStart = null;
	private int fragmentSize = 1;
	
	public TraceWiseXesParser(String logFile) throws ParameterException, IOException{
		this(logFile, DEFAULT_FRAGMENT_SIZE);
	}
	
	public TraceWiseXesParser(String logFile, int fragmentSize) throws ParameterException, IOException{
		Validate.exists(logFile);
		Validate.positive(fragmentSize);
		fileReader = new FileReader(logFile);
		header = nextStringFragment();
		this.fragmentSize = fragmentSize;
	}
	
	private String nextStringFragment() throws IOException{
		if(hasNextTrace != null && !hasNextTrace)
			return null;
		
		StringBuffer buffer = new StringBuffer();
		if(lastTraceStart != null){
			buffer.append(lastTraceStart);
			buffer.append(System.getProperty("line.separator"));
		}
		String nextLine = null;
		while((nextLine=fileReader.readLine()) != null && !headerEnd(nextLine)){
			buffer.append(nextLine);
			buffer.append(System.getProperty("line.separator"));
		}
		if(!nextLine.trim().startsWith("</log")){
			lastTraceStart = nextLine;
			hasNextTrace = true;
		} else {
			hasNextTrace = false;
		}
		return buffer.toString();
	}
	
	private boolean headerEnd(String line){
		String cleanedLine = line.trim();
		return cleanedLine.startsWith("<trace") || cleanedLine.startsWith("</log");
	}

	@Override
	public boolean hasNext() {
		return hasNextTrace;
	}

//	@Override
//	public LogTrace next() {
//		if(hasNext()){
//			try {
//				System.out.println(nextStringFragment());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		return null;
//	}
	
	@Override
	public LogFragment next(){
		if(hasNext()){
			try {
			LogFragment newFragment = new LogFragment();
			newFragment.addLine(header);
			int traceCount = 0;
			while(hasNext() && traceCount++ < fragmentSize){
				newFragment.addLine(nextStringFragment());
			}
			newFragment.addLine(LOG_END);
			newFragment.close();
			System.out.println(newFragment);
			return newFragment;
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	public static void main(String[] args) throws Exception {
//		LogFragment fragment = new LogFragment();
//		fragment.addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//		fragment.addLine("<log xes.version=\"1.0\" openxes.version=\"test\" xes.features=\"arbitrary-depth nested-attributes\" xmlns=\"http://code.fluxicon.com/xes\">");
//		fragment.addLine("<extension name=\"Concept\" prefix=\"concept\" uri=\"http://code.fluxicon.com/xes/concept.xesext\"/>");
//		fragment.addLine("<extension name=\"AttributeDataUsage\" prefix=\"dataUsage\" uri=\"http://files.telematik.uni-freiburg.de/xes/dataUsage.xesext\"/>");
//		fragment.addLine("<trace>");
//		fragment.addLine("<event><string key=\"concept:name\" value=\"A\"/></event>");
//		fragment.addLine("<event><string key=\"concept:name\" value=\"B\"/></event>");
//		fragment.addLine("</trace>");
//		fragment.addLine("</log>");
//		fragment.close();
//		
//		XesXmlParser parser = new XesXmlParser();
//		parser.parse(fragment);
		
		TraceWiseXesParser traceParser = new TraceWiseXesParser("/Users/stocker/Desktop/XESTest2.xes", 2);
		
		XesXmlParser parser = new XesXmlParser();
		while (traceParser.hasNext()) {
			parser.parse(traceParser.next());
		}


		ScatterChartModel<Integer, Integer> scatterModel = new ScatterChartModel<Integer, Integer>(Arrays.asList(1,2,3,4), Arrays.asList(1,50,22,44), true);
		OneDimChartModel<Integer> chartModel = new OneDimChartModel<Integer>(Arrays.asList(1,50,22,44));
		ScatterChartPanel scatter = new ScatterChartPanel(scatterModel, true, true);
		OneDimChartPanel chart = new OneDimChartPanel(chartModel, false, true);
		scatter.setPaintLines(true);
		AdjustableDiagramPanel adjustable = new AdjustableDiagramPanel(scatter);
		new DisplayFrame(adjustable, true);
	}

}
