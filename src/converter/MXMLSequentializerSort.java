package converter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

public class MXMLSequentializerSort {
	//TODO: Funktioniert das?
	public static void convertMXML(String path) throws Exception{
		HashMap<Integer, String> lines = new HashMap<Integer, String>();
		ArrayList<Integer> lineNumbers = new ArrayList<Integer>();
		String actualLine = "";
		int lineNumber = 0;
		File mxmlfile = new File(path);
		try {
			File file = new File(mxmlfile.getAbsolutePath().substring(0,mxmlfile.getAbsolutePath().indexOf('.'))+"sequentialSort.txt");
			FileWriter output = new FileWriter(file, true);
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			XMLStreamReader stax = inputFactory.createXMLStreamReader(new StreamSource(new File(path)));
			boolean element = false;
		
			while (stax.hasNext()) {
				switch (stax.next()) {
				case XMLStreamConstants.START_ELEMENT:
					element = stax.getLocalName().equals("WorkflowModelElement");
					if (stax.getLocalName().equals("ProcessInstance")) {
						String id = getID(stax);
						lineNumber = Integer.parseInt(id.substring(id.indexOf("Case")+4));
						lineNumbers.add(lineNumber);
					}
					break;
				case XMLStreamConstants.CDATA:
				case XMLStreamConstants.CHARACTERS:
					if (!stax.isWhiteSpace() && element) {
						actualLine += stax.getText();
					}
					break;
				case XMLStreamConstants.END_ELEMENT:
					if (stax.getLocalName().equals("ProcessInstance")) {
						actualLine += '\n';
						lines.put(lineNumber, actualLine);
						actualLine = "";
					} else if(stax.getLocalName().equals("WorkflowModelElement")){
						actualLine += '	';
					}
				}
			}
			System.out.println(lineNumbers);
			Collections.sort(lineNumbers);
			System.out.println(lineNumbers);
			for(int line: lineNumbers){
//				System.out.println("write line: "+line);
				output.write(lines.get(line));
			}
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String getID(XMLStreamReader reader){
		if(reader.getAttributeCount() > 0){
			for(int i=0; i<reader.getAttributeCount(); i++)
				if(reader.getAttributeLocalName(i).equals("id")){
					return reader.getAttributeValue(i); 
				}
		}
		return null;
	}

}