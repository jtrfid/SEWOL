package de.uni.freiburg.iig.telematik.sewol.log;

import java.util.HashMap;
import java.util.Map;

/**
 * The EventType enumeration represents the different types an event can be.
 *
 * @author Thomas Stocker, Adrian Lange
 */
public enum EventType {

        schedule(new String[]{"schedule"}), autoskip(new String[]{"autoskip"}),
        assign(new String[]{"assign"}), reassign(new String[]{"reassign"}),
        start(new String[]{"start"}), withdraw(new String[]{"withdraw"}),
        manual_skip(new String[]{"manual_skip", "manualskip"}),
        suspend(new String[]{"suspend"}), resume(new String[]{"resume"}),
        complete(new String[]{"complete"}),
        ate_abort(new String[]{"ate_abort", "ateabort"}),
        pi_abort(new String[]{"pi_abort", "piabort"});

        public final static Map<String, EventType> typeByName;
        public final String[] names;

        static {
                typeByName = new HashMap<>(15);
                for (EventType t : EventType.values()) {
                        for (String name : t.names) {
                                typeByName.put(name, t);
                        }
                }
        }

        private EventType(String[] names) {
                this.names = names;
        }

        /**
         * Returns the {@link EventType} that has the same name as the given
         * string. All underlines get dropped and the name is not case
         * sensitive. If no fitting EventType value was found, the method
         * returns <code>null</code>.
         *
         * @param type Name of the type
         * @param sanitize
         * @return The corresponding EventType
         */
        public static EventType parse(String type, boolean sanitize) {
                if (type == null || type.isEmpty()) {
                        return null;
                }
                if (sanitize) {
                        // The underlines are removed because of the different representation in XES. E.g. "manual_skip" is named "manualskip" there.
                        return typeByName.get(type.toLowerCase());
                } else {
                        return typeByName.get(type);
                }
        }

        /**
         * Returns the {@link EventType} that has the same name as the given
         * string. All underlines get dropped and the name is not case
         * sensitive. If no fitting EventType value was found, the method
         * returns <code>null</code>.
         *
         * @param type Name of the type
         * @return The corresponding EventType
         */
        public static EventType parse(String type) {
                return parse(type, true);
        }
}
