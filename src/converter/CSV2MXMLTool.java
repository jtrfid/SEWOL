package converter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import de.invation.code.toval.graphic.diagrams.models.DotChartModel;
import de.invation.code.toval.graphic.diagrams.panels.AdjustableDiagramPanel;
import de.invation.code.toval.graphic.diagrams.panels.DotChartPanel;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;

import log.DataAttribute;
import log.LockingException;
import log.LogEntry;
import log.LogTrace;
import logformat.LogFormat;
import logformat.MXMLLogFormat;
import writer.LogWriter;
import writer.PerspectiveException;

//VORSICHT: Probleme beim Zeitstempel-Handling, evtl. auch in LogWriter


/**
 * Diese Klasse dient dem Import von CSV-Dateien in das MXML-Format.<br>
 * <br>
 * Es gelten folgende Annahmen bezüglich der Struktur der CSV-Datei:<br>
 * <ul>
 * <li>Werte sind Komma-, Semikolon- oder Tab-separiert.</li>
 * <li>Jede Zeile steht für eine Prozessaktivität.</li>
 * <li>Die Spalten stehen für die einzelnen Felder innerhalb einer Prozessaktivität (Name des Vorgangs, durchführender Akteur, ...)</li>
 * <li>Die erste Zeile enthält die Namen die einzelnen Felder von Prozessaktivitäten.</li>
 * <li>Alle Aktivitäten eines Prozessdurchlaufs sind gruppiert und in stehen in korrekter zeitlicher Abfolge untereinander.</li>
 * <li>Die Case-ID liegt entweder als Zahl vor oder enthält am Ende der Zeichenkette eine Zahl.</li>
 * <li>Die Zahlen für die Case-IDs sind für jeden Prozessdurchlauf verschieden.</li>
 * </ul>
 * 
 * Werte für einzelne Felder werden vor der Verarbeitung gesäubert,
 * d.h. etwaige Anführungszeichen werden entfernt. 
 * 
 * @author Thomas Stocker
 */
//TODO: Datumsformat aus erstem Eintrag verwenden
@SuppressWarnings("serial")
public class CSV2MXMLTool extends JFrame {
	
	protected final Dimension WINDOW_SIZE = new Dimension(600,580);
	protected static final Font defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	protected final int previewAreaCols = 80;
	protected File file = null;
	protected JTextArea previewArea = null;
	protected JComboBox inputCharsetChooser = null;
	protected JComboBox outputCharsetChooser = null;
	protected JComboBox separatorChooser = null;
	protected Charset charset = null;
	protected Character separator = null;
	protected Map<String, LogConcept> columnInterpretation;
	protected JPanel interpretationPanel = null;
	protected JPanel interpretations = new JPanel();
	protected ArrayList<String> columnNames;
	protected LogFormat logFormat = new MXMLLogFormat();

	public CSV2MXMLTool() {
		setContentPane(getContentPane());
	    setPreferredSize(WINDOW_SIZE);
	    setBackground(Color.white);
	    setJMenuBar(createMenu());
	    pack();
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setVisible(true);
	}
	
	@Override
	public JPanel getContentPane() {
		JPanel content = new JPanel();
	    content.setLayout(new BorderLayout(0,0));
	    content.add(getNavigationPanel(), BorderLayout.NORTH);
	    content.add(getMidPanel(), BorderLayout.CENTER);
	    return content;
	}
	
	protected JPanel getMidPanel() {
		JPanel midPanel = new JPanel();
		
		JPanel previewPanel = new JPanel(new BorderLayout());
		previewPanel.add(new JLabel("Preview (first 10 lines):"), BorderLayout.PAGE_START);
		previewPanel.add(getPreviewArea(), BorderLayout.CENTER);
		midPanel.add(previewPanel, BorderLayout.NORTH);
		
		midPanel.add(getInterpretationPanel(), BorderLayout.CENTER);
	    return midPanel;
	}
	
	protected JPanel getInterpretationPanel(){
		if(interpretationPanel == null){
			interpretationPanel = new JPanel(new BorderLayout());
			JPanel headingPanel = new JPanel(new GridLayout(1, 2,10,0));
			headingPanel.setPreferredSize(new Dimension(560,40));
			headingPanel.add(new JLabel("CSV Column", SwingConstants.CENTER));
			headingPanel.add(new JLabel("Interpretation"));
			headingPanel.setBackground(Color.lightGray);
			interpretationPanel.add(headingPanel, BorderLayout.NORTH);
			
			JScrollPane interpretationScrollPane = new JScrollPane(interpretations);
			interpretationScrollPane.setPreferredSize(new Dimension(560, 250));
			interpretationPanel.add(interpretationScrollPane, BorderLayout.CENTER);
		}
		return interpretationPanel;
	}
	
	private Integer extractCaseNumber(String caseID){
		int caseNumber;
		for(int i=0; i<caseID.length(); i++){
			try {
				caseNumber = Integer.parseInt(caseID.substring(i));
				return caseNumber;
			} catch(NumberFormatException ex){}
		}
		return null;
	}
	
	private String clean(String value){
		return value.replace("\"", "");
	}
	
	private JPanel getNavigationPanel(){
		JPanel navigationPanel = new JPanel(new GridLayout(1,2,20,0));
		navigationPanel.setBackground(Color.lightGray);
		navigationPanel.setPreferredSize(new Dimension(600,80));
		
		JPanel inputNavigation = new JPanel(new GridLayout(3,2));
		inputNavigation.setBackground(Color.lightGray);
		JLabel label0 = new JLabel("INPUT");
		label0.setHorizontalAlignment(SwingConstants.CENTER);
		inputNavigation.add(Box.createGlue());
		inputNavigation.add(label0);
		JLabel label1 = new JLabel("Charset: ");
		label1.setHorizontalAlignment(SwingConstants.RIGHT);
		inputNavigation.add(label1);
		inputNavigation.add(getInputCharsetChooser());
		JLabel label2 = new JLabel("Separator: ");
		label2.setHorizontalAlignment(SwingConstants.RIGHT);
		inputNavigation.add(label2);
		inputNavigation.add(getSeparatorChooser());
		navigationPanel.add(inputNavigation);
		
		JPanel outputNavigation = new JPanel(new GridLayout(3,2));
		outputNavigation.setBackground(Color.lightGray);
		JLabel label3 = new JLabel("OUTPUT");
		label3.setHorizontalAlignment(SwingConstants.CENTER);
		outputNavigation.add(Box.createGlue());
		outputNavigation.add(label3);

		JButton startButton = new JButton("RUN");
		startButton.addActionListener(new ImportAction());
		outputNavigation.add(Box.createGlue());
		outputNavigation.add(startButton);
		navigationPanel.add(outputNavigation);
		return navigationPanel;
	}
	
	private JComboBox getInputCharsetChooser(){
		if(inputCharsetChooser == null){
			inputCharsetChooser = new JComboBox();
			for(String charset: Charset.availableCharsets().keySet()){
				inputCharsetChooser.addItem(charset);
			}
			changeInputCharset(inputCharsetChooser.getSelectedItem().toString());
			inputCharsetChooser.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					changeInputCharset(inputCharsetChooser.getSelectedItem().toString());
					displayPreview();
				}
			});
		}
		return inputCharsetChooser;
	}
	

	
	private void changeInputCharset(String charset){
		this.charset = Charset.forName(charset);
	}
	
	
	private JComboBox getSeparatorChooser(){
		if(separatorChooser == null){
			separatorChooser = new JComboBox();
			separatorChooser.addItem("Semicolon");
			separatorChooser.addItem("Comma");
			separatorChooser.addItem("Tab");
			separator = ';';
			separatorChooser.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					switch(separatorChooser.getSelectedIndex()){
					case 0: separator = ';';
					break;
					case 1: separator = ',';
					break;
					case 2: separator = '\t';
					}
					displayPreview();
				}
			});
		}
		return separatorChooser;
	}
	
	private JTextArea getPreviewArea(){
		if(previewArea == null){
			previewArea = new JTextArea();
			previewArea.setColumns(previewAreaCols);
			previewArea.setRows(10);
			previewArea.setFont(defaultFont);
			previewArea.setEditable(false);
//			previewArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0), BorderFactory.createLineBorder(Color.black, 1)));
			previewArea.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		}
		return previewArea;
	}
	
	public JMenuBar createMenu(){
		JMenuBar menuBar = new JMenuBar();
		JMenu file = (JMenu) menuBar.add(new JMenu("File"));
		file.setMnemonic('F');
		JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(new OpenAction());
		file.add(openItem);
		openItem.setMnemonic('O');
		return menuBar ;
	}
	
	private void displayPreview() {
		try {
			previewArea.setText("");
			if(charset == null || file == null)
				return;
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
			String line = null;
			int lineCount = 0;
			while ((line = in.readLine()) != null && lineCount<11) {
				StringTokenizer st = new StringTokenizer(line, String.valueOf(separator));
				if(lineCount == 0){
					interpretations.removeAll();
					interpretations.setLayout(new GridLayout(st.countTokens(), st.countTokens(), 0 ,0 ));
					columnInterpretation = new LinkedHashMap<String, LogConcept>(st.countTokens());
					columnNames = new ArrayList<String>(st.countTokens());
					while(st.hasMoreTokens()){
						String nextToken = st.nextToken();
						columnInterpretation.put(nextToken, null);
						columnNames.add(nextToken);
						JLabel newLabel = new JLabel(nextToken);
						newLabel.setHorizontalAlignment(SwingConstants.CENTER);
						interpretations.add(newLabel);
						interpretations.add(getNewInterpretationBox(nextToken));
					}
				} else {
					previewArea.append(line.substring(0, Math.min(line.length(), previewAreaCols-1))+'\n');
				}
				lineCount++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected JComboBox getNewInterpretationBox(String columnName){
		final String column = columnName;
		final JComboBox comboBox = new JComboBox(LogConcept.values());
		comboBox.setSelectedIndex(comboBox.getItemCount()-1);
		columnInterpretation.put(column, (LogConcept) comboBox.getSelectedItem());
		comboBox.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				columnInterpretation.put(column, (LogConcept) comboBox.getSelectedItem());
			}
		});
		return comboBox;
	}
	
	
	protected class OpenAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			if (chooser.showOpenDialog(CSV2MXMLTool.this) == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
				if(!file.exists()) {
					JOptionPane.showMessageDialog(null,"File does not exist!","I/O Error on opening file", JOptionPane.ERROR_MESSAGE);
				}
				if(!file.canRead()){
					JOptionPane.showMessageDialog(null,"Unable to read file!","I/O Error on opening file", JOptionPane.ERROR_MESSAGE);
				}
				displayPreview();
			}

		}
		
	}
	
	protected class ImportAction extends AbstractAction {
		
		private Map<Integer,Integer> numberOfActivities = new HashMap<Integer, Integer>();
		
		private void addActivityNumber(int number){
			if(!numberOfActivities.containsKey(number)){
				numberOfActivities.put(number, 1);
				return;
			}
			numberOfActivities.put(number, numberOfActivities.get(number)+1);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if(charset == null || file == null)
					return;
				
				numberOfActivities.clear();
				
				//Prepare output file writer
				String inputName = file.getAbsolutePath();
				File outfile = new File(inputName.substring(0, inputName.lastIndexOf('.'))+"."+logFormat.getFileExtension());
				if(outfile.exists()) 
					outfile.delete();
				outfile.createNewFile();
				BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), logFormat.getCharset()));
				output.write(logFormat.getFileHeader());
				
				LogWriter logWriter = null;
				try {
					logWriter = new LogWriter(new MXMLLogFormat());
				} catch (PerspectiveException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (CompatibilityException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				} catch (ParameterException e4) {
					// TODO Auto-generated catch block
					e4.printStackTrace();
				}
				
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
				String line = null;
				int lineCount = 0;
				int tokenCount;
				Integer lastCaseID = null;
				Integer actualCaseID = null;
				LogTrace actualTrace = null;
				while ((line = in.readLine()) != null) {
					if(lineCount == 0){
						lineCount++;
						continue;
					} else {
						String[] tokens = line.split(";", -1);
//						StringTokenizer st = new StringTokenizer(line, String.valueOf(separator));
						tokenCount = 0;
						LogEntry newEntry = new LogEntry();
						boolean abort = false;
						for(int i=0; i<tokens.length; i++){
//						while(st.hasMoreTokens() && !abort){
							String nextToken = tokens[i];
							if(!nextToken.isEmpty()){
							switch(columnInterpretation.get(columnNames.get(tokenCount))){
							case CASEID: 
								actualCaseID = extractCaseNumber(clean(nextToken));
								if(actualCaseID == null){
									System.out.println("Skipped process activity due to missing case-ID");
									abort = true;
									continue;
								}
								break;
							case ACTIVITY:
								newEntry.setActivity(clean(nextToken.toString()));
								break;
							case ORIGINATOR:
								newEntry.setOriginatorCandidate(clean(nextToken.toString()));
								break;
							case TIMESTAMP:
								DateFormat formatter = new SimpleDateFormat();
								try {
									newEntry.setTimestamp((Date) formatter.parse(clean(nextToken.toString())));
								} catch (ParseException e1) {
									System.out.println("Unable to parse date: " + clean(nextToken.toString()));
								}
								break;
							case DATA:
								newEntry.addDataUsage(new DataAttribute(clean(columnNames.get(tokenCount)), clean(nextToken.toString())), null);
								break;
							case META:
								newEntry.addMetaAttribute(new DataAttribute(clean(columnNames.get(tokenCount)), clean(nextToken.toString())));
								break;
							}
							}
							tokenCount++;
						}
						if(abort){
							continue;
						}
						if(!actualCaseID.equals(lastCaseID)){
							if(actualTrace != null){
								//Write actual Trace into file
								output.write(logFormat.getTraceAsString(actualTrace));
								System.out.println("Writing new trace ["+lastCaseID+"]: "+actualTrace.size()+" entries");
								addActivityNumber(actualTrace.size());
								if(actualTrace.size()>19)
									try {
										logWriter.writeTrace(actualTrace);
									} catch (PerspectiveException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
							}
							actualTrace = new LogTrace(actualCaseID);
						}
						actualTrace.addEntry(newEntry);
						lastCaseID = actualCaseID;
						lineCount++;
					}
				}
				output.write(logFormat.getTraceAsString(actualTrace));
				addActivityNumber(actualTrace.size());
				if(actualTrace.size()>19)
					try {
						logWriter.writeTrace(actualTrace);
					} catch (PerspectiveException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				logWriter.closeFile();
				System.out.println("Writing new trace ["+actualCaseID+"]: "+actualTrace.size()+" entries");
				System.out.println();
				
				//Write out statistics
				System.out.println("Trace complexity (#activities -> number of traces)");
				List<Integer> activityNumbers = new ArrayList<Integer>();
				activityNumbers.addAll(numberOfActivities.keySet());
				Collections.sort(activityNumbers);
				for(Integer activityNumber: activityNumbers){
					System.out.println(activityNumber + " -> " + numberOfActivities.get(activityNumber));
				}
				
				List<Integer> numTraces = new ArrayList<Integer>();
				numTraces.addAll(numberOfActivities.values());
				DotChartModel<Integer> chartModel = new DotChartModel<Integer>(activityNumbers, numTraces);
				DotChartPanel panel = new DotChartPanel(chartModel, true, true);
				AdjustableDiagramPanel adjustablePanel = new AdjustableDiagramPanel(panel);
				adjustablePanel.asFrame();
				
				output.write(logFormat.getFileFooter());
				output.close();
				System.out.println("Done");
			} catch (IOException ioException) {
				ioException.printStackTrace();
			} catch (LockingException lockingException) {
				// Cannot occur since no field is locked by default.
			} catch (NullPointerException nullPointerException) {
				nullPointerException.printStackTrace();
			}
		}
		
	}
	
	public static void main(String[] args){
		new CSV2MXMLTool();
	}
	
	private enum LogConcept {CASEID, ACTIVITY, ORIGINATOR, TIMESTAMP, DATA, META};
	
}





