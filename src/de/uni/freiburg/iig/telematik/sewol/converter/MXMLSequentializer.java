package de.uni.freiburg.iig.telematik.sewol.converter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

/**
 * Takes an MXML file as input and outputs a non-XML file that contains a
 * process trace per line, that simply consists of the plain activity names.
 *
 * @author Thomas Stocker
 *
 */
public class MXMLSequentializer {

        public static void convertMXML(String path) throws Exception {
                File mxmlfile = new File(path);
                try {
                        File file = new File(mxmlfile.getAbsolutePath().substring(0, mxmlfile.getAbsolutePath().indexOf('.')) + "sequential.txt");
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
                                                switch (stax.getLocalName()) {
                                                        case "ProcessInstance":
                                                                output.write('\n');
                                                                break;
                                                        case "WorkflowModelElement":
                                                                output.write('	');
                                                                break;
                                                }
                                }
                        }
                        output.close();
                } catch (IOException | XMLStreamException e) {
                        throw new RuntimeException(e);
                }
        }

        public static void main(String[] args) throws Exception {
                MXMLSequentializer.convertMXML("/Users/stocker/Documents/Kooperationen/Micronas/Prozessdaten/Testdaten 2/2013/Logistisch/Logistisch - keine AdHoc - ohne offene Rechnungen in Zahlungsfrist.mxml");
        }

}
