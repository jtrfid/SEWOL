package de.uni.freiburg.iig.telematik.sewol.log;

/**
 * The EventType enumeration represents the different types an event can be.
 * 
 * @author Thomas Stocker, Adrian Lange
 */
public enum EventType {

	schedule, autoskip, assign, reassign, start, withdraw, manual_skip, suspend, resume, complete, ate_abort, pi_abort;

	/**
	 * Returns the {@link EventType} that has the same name as the given string. All underlines get dropped and the name is not case sensitive. If no fitting EventType value was found, the method returns <code>null</code>.
	 */
	public static EventType parse(String type) {
		if(type == null || type.isEmpty())
			return null;
		// The underlines are removed because of the different representation in XES. E.g. "manual_skip" is named "manualskip" there.
		String sanitizedType = type.toUpperCase().replaceAll("_", "");
		for (EventType eventType : EventType.values()) {
			if (eventType.toString().toUpperCase().replaceAll("_", "").equals(sanitizedType)) {
				return eventType;
			}
		}
		return null;
	}
}
