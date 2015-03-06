package de.uni.freiburg.iig.telematik.sewol.converter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XMLModification {

	public static void removeComments(String xmlSource, String xslSheet, String output) throws Exception{
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(new StreamSource(xslSheet));
		transformer.transform(new StreamSource(xmlSource), new StreamResult(output));
	}

}