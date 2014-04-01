package de.uni.freiburg.iig.telematik.jawl.parser.xes;

import java.net.URI;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XGlobalAttributeNameMap;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;

/**
 * This extension adds the data attribute with usage specification to events.
 * 
 * @version 1.0
 * @author Adrian Lange
 */
public class DataUsageExtension extends XExtension {

	private static final long serialVersionUID = 8737134413446055085L;

	/** Unique URI of this extension. */
	public static final URI EXTENSION_URI = URI.create("http://xes.process-security.de/extensions/dataUsage.xesext");

	/** Key for the data attribute. */
	public static final String KEY_DATA = "dataUsage:usage";

	/** Data attribute prototype. */
	public static XAttributeLiteral ATTR_DATA;

	/** Singleton instance of this extension. */
	private static DataUsageExtension singleton = new DataUsageExtension();

	/**
	 * Provides access to the singleton instance of this extension.
	 * 
	 * @return The AttributeDataUsage extension singleton.
	 */
	public static DataUsageExtension instance() {
		return singleton;
	}

	private Object readResolve() {
		return singleton;
	}

	private DataUsageExtension() {
		super("AttributeDataUsage", "dataUsage", EXTENSION_URI);
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		ATTR_DATA = factory.createAttributeLiteral(KEY_DATA, "", this);
		this.eventAttributes.add((XAttribute) ATTR_DATA.clone());
		// register mapping aliases
		XGlobalAttributeNameMap.instance().registerMapping(XGlobalAttributeNameMap.MAPPING_ENGLISH, KEY_DATA, "Data usage (comma separated list of read, create, write, and delete)");
		XGlobalAttributeNameMap.instance().registerMapping(XGlobalAttributeNameMap.MAPPING_GERMAN, KEY_DATA, "Datenzugriff (durch Kommata getrennte Liste aus read, create, write und delete)");
	}

	/**
	 * Extracts the data attribute string from an event.
	 * 
	 * @param event
	 *            Event to be queried.
	 * @return Data string for the given event (may be <code>null</code> if not defined)
	 */
	public String extractData(XEvent event) {
		XAttribute attribute = event.getAttributes().get(KEY_DATA);
		if (attribute == null) {
			return null;
		} else {
			return ((XAttributeLiteral) attribute).getValue();
		}
	}

	/**
	 * Assigns the data attribute value for a given event.
	 * 
	 * @param event
	 *            Event to be modified.
	 * @param data
	 *            Data string to be assigned.
	 */
	public void assignData(XEvent event, String data) {
		if (data != null && data.trim().length() > 0) {
			XAttributeLiteral attr = (XAttributeLiteral) ATTR_DATA.clone();
			attr.setValue(data.trim());
			event.getAttributes().put(KEY_DATA, attr);
		}
	}
}
