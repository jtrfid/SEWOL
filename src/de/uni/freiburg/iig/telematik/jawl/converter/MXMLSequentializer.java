package de.uni.freiburg.iig.telematik.jawl.converter;

import java.io.File;
import java.io.FileWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

/**
 * Takes an MXML file as input and outputs a non-XML file that contains a process trace per line,
 * that simply consists of the plain activity names.
 * 
 * @author Thomas Stocker
 *
 */
public class MXMLSequentializer {
	
	public static void convertMXML(String path) throws Exception{
		File mxmlfile = new File(path);
		try {
			File file = new File(mxmlfile.getAbsolutePath().substring(0,mxmlfile.getAbsolutePath().indexOf('.'))+"sequential.txt");
			FileWriter output = new FileWriter(file, true);
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			XMLStreamReader stax = inputFactory.createXMLStreamReader(new StreamSource(new File(path)));
			boolean element = false;
		
			while (stax.hasNext()) {
				switch (stax.next()) {
				case XMLStreamConstants.START_ELEMENT:
					element = stax.getLocalName().equals("WorkflowModelElement");
					break;
				case XMLStreamConstants.CDATA:
				case XMLStreamConstants.CHARACTERS:
					if (!stax.isWhiteSpace() && element) {
						output.write(stax.getText());
					}
					break;
				case XMLStreamConstants.END_ELEMENT:
					if (stax.getLocalName().equals("ProcessInstance")) {
						output.write('\n');
					} else if(stax.getLocalName().equals("WorkflowModelElement")){
						output.write('	');
					}
				}
			}
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		MXMLSequentializer.convertMXML("/Users/stocker/Documents/Kooperationen/Micronas/Prozessdaten/Testdaten 2/2013/Logistisch/Logistisch - keine AdHoc - ohne offene Rechnungen in Zahlungsfrist.mxml");
	}

}