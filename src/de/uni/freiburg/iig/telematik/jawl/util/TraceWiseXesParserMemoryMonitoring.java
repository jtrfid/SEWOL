package de.uni.freiburg.iig.telematik.jawl.util;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;

import de.uni.freiburg.iig.telematik.jawl.parser.TraceWiseXesParser;

public class TraceWiseXesParserMemoryMonitoring {

	public static final String PATH_1 = "/Users/stocker/Desktop/XESTest2.xes";
	public static final String PATH_2 = "E:\\Documents\\IIG\\xes\\examples\\hjg.xes";
	public static final String PATH_3 = "E:\\Documents\\IIG\\xes\\examples\\BPI_Challenge_2012.xes";

	/** Interval of reading the used memory in milliseconds */
	public static final int TIMER_INTERVAL = 100;
	/** Specifies how many traces should be read per iteration */
	public static final int TRACES_PER_ITERATION = 1000;
	/** Multiplier for converting bytes to megabytes */
	public static final int MB = 1024 * 1024;

	private static Timer timer;
	private static boolean stop = false;
	private static List<Integer> memoryUsages = new Vector<Integer>();

	public static void main(String[] args) throws Exception {

		// Define timer task
		TimerTask addMemoryUsageTask = new TimerTask() {
			Runtime runtime = Runtime.getRuntime();

			@Override
			public void run() {
				memoryUsages.add((int) ((runtime.totalMemory() - runtime.freeMemory()) / MB));
			}
		};

		// Define timer thread
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						if (stop) {
							timer.cancel();
							break;
						}
						Thread.sleep(TIMER_INTERVAL);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		// Create and configure timer
		timer = new Timer("GetUsedMemoryTimer");
		timer.scheduleAtFixedRate(addMemoryUsageTask, 0, TIMER_INTERVAL);

		t.start();

		// Parse XES document
		TraceWiseXesParser traceParser = new TraceWiseXesParser(PATH_3, TRACES_PER_ITERATION);

		List<List<XLog>> parsedDocument = new Vector<List<XLog>>();
		XesXmlParser parser = new XesXmlParser();
		while (traceParser.hasNext()) {
			parsedDocument.add(parser.parse(traceParser.next()));
		}

		stop = true;

		printLogStatistics(parsedDocument);

		System.out.println(memoryUsages);

		// Vector<Integer> xAxis = new Vector<Integer>();
		// for (int i = 1; i <= memoryUsages.size(); i++) {
		// xAxis.add(i);
		// }

		// ScatterChartModel<Integer, Integer> scatterModel = new ScatterChartModel<Integer, Integer>(xAxis, memoryUsages, true);
		// OneDimChartModel<Integer> chartModel = new OneDimChartModel<Integer>(memoryUsages);
		// ScatterChartPanel scatter = new ScatterChartPanel(scatterModel, true, true);
		// OneDimChartPanel chart = new OneDimChartPanel(chartModel, false, true);
		// scatter.setPaintLines(true);
		// AdjustableDiagramPanel adjustable = new AdjustableDiagramPanel(scatter);
		// new DisplayFrame(adjustable, true);
	}

	private static void printLogStatistics(List<List<XLog>> logs) {
		System.out.println("splitted in " + logs.size() + " log parts");
		System.out.println("in an interval of " + TIMER_INTERVAL + " traces");
		double averageLogSize = 0;
		double averageTraceSize = 0;
		for (List<XLog> log : logs) {
			double temp = 0;
			averageLogSize += log.size();
			for (XLog xLog : log) {
				temp += xLog.size();
			}
			averageTraceSize += (temp / (double) log.size());
		}
		averageLogSize /= (double) logs.size();
		averageTraceSize /= (double) logs.size();
		System.out.println("Average log size: " + averageLogSize);
		System.out.println("Average trace size: " + averageTraceSize);
	}
}
