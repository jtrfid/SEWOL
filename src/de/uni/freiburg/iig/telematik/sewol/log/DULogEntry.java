package de.uni.freiburg.iig.telematik.sewol.log;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.invation.code.toval.parser.ParserException;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

public class DULogEntry extends LogEntry {

        /**
         * The list of attributes that are affected on executing the logged
         * activity.
         */
        protected Map<DataAttribute, Set<DataUsage>> dataUsage = new HashMap<>();

        private static final Pattern STRING_PATTERN = Pattern.compile("\\w+(\\((\\w+:[rwcd]{1,4})(,(\\w+:[rwcd]{1,4}))*\\))?");

	// ------- Constructors ----------------------------------------------------------------------
        public DULogEntry() {
                super();
        }

        public DULogEntry(String activity) throws ParameterException {
                super(activity);
        }

	// ------- Data Usage ------------------------------------------------------------------------
        public boolean containsDataUsage() {
                return !dataUsage.isEmpty();
        }

        /**
         * Returns the list of attributes.<br>
         * Note that data attributes themselves are not copied or cloned.
         *
         * @return A list containing the attributes.
         */
        public Set<DataAttribute> getDataAttributes() {
                return Collections.unmodifiableSet(dataUsage.keySet());
        }

        /**
         * Returns the data usage, i.e. all data attributes together with the
         * usage (read, ...).
         *
         * @return A map containing data usage information for each attribute.
         */
        public Map<DataAttribute, Set<DataUsage>> getDataUsage() {
                return Collections.unmodifiableMap(dataUsage);
        }

        /**
         * Removes the given attribute from the set of managed attributes.
         *
         * @param attribute Attribute to remove.
         * @return if the set of managed attributes was modified;<br>
         * <code>false</code> otherwise.
         * @throws LockingException if the corresponding field is locked <br>
         * and the given attribute is not already contained in the set of
         * managed attributes.
         */
        public boolean removeDataAttribute(DataAttribute attribute) throws LockingException {
                if (isFieldLocked(EntryField.DATA)) {
                        if (dataUsage.containsKey(attribute)) {
                                throw new LockingException(EntryField.DATA);
                        }
                        return false;
                } else {
                        return dataUsage.remove(attribute) != null;
                }
        }

        /**
         * Sets the given data usage as data usage for this entry.
         *
         * @param dataUsage The data usage to adopt.
         * @return <code>true</code> if the current data usage was modified;<br>
         * <code>false</code> otherwise.
         * @throws ParameterException if the given data usage is invalid
         * (<code>null</code> or empty).
         * @throws LockingException if the corresponding field is locked <br>
         * and the given data usage is not the same than the current one.
         */
        public boolean setDataUsage(Map<DataAttribute, Set<DataUsage>> dataUsage) throws ParameterException, LockingException {
                Validate.notNull(dataUsage);
                Validate.notEmpty(dataUsage.keySet());
                Validate.noNullElements(dataUsage.keySet());
                Validate.noNullElements(dataUsage.values());

                if (isFieldLocked(EntryField.DATA)) {
                        if (!this.dataUsage.equals(dataUsage)) {
                                throw new LockingException(EntryField.DATA);
                        }
                        return false;
                } else {
                        this.dataUsage = dataUsage;
                        return true;
                }
        }

        /**
         * Sets the data usage for a given attribute.
         *
         * @param attribute The attribute (data element) for which the usage is
         * specified.
         * @param dataUsage The usage of the data element specified by the given
         * attribute.
         * @return <code>true</code> if the data usage for the given attribute
         * was modified;<br>
         * <code>false</code> otherwise.
         * @throws ParameterException if the given attribute is
         * <code>null</code> or data usage is invalid (<code>null</code> or
         * empty).
         * @throws LockingException if the corresponding field is locked <br>
         * and the given data usage is not identical to the current one.
         */
        public boolean setDataUsageFor(DataAttribute attribute, Set<DataUsage> dataUsage) throws ParameterException, LockingException {
                Validate.notNull(attribute);
                Validate.notNull(dataUsage);
                Validate.notEmpty(dataUsage);

                if (isFieldLocked(EntryField.DATA)) {
                        if (!(this.dataUsage.containsKey(attribute) && this.dataUsage.get(attribute).equals(dataUsage))) {
                                throw new LockingException(EntryField.DATA);
                        }
                        return false;
                } else {
                        this.dataUsage.put(attribute, dataUsage);
                        return true;
                }
        }

        /**
         * Adds the given attribute to the list of attributes
         *
         * @param attribute The attribute to add.
         * @param usage The data usage
         * @throws ParameterException if the given attribute or usage is
         * <code>null</code>.
         * @throws LockingException if the field INPUT_DATA is locked <br>
         * and the attribute is not already contained in {@link #dataUsage}.
         * @return <code>true</code> if {@link #dataUsage} was modified;<br>
         * <code>false</code> otherwise.
         */
        public boolean addDataUsage(DataAttribute attribute, DataUsage usage) throws ParameterException, LockingException {
                Validate.notNull(attribute);

                if (isFieldLocked(EntryField.DATA)) {
                        if (!dataUsage.containsKey(attribute)) {
                                throw new LockingException(EntryField.DATA);
                        }
                        return false;
                } else {
                        if (dataUsage.get(attribute) == null) {
                                dataUsage.put(attribute, new HashSet<>());
                        }
                        if (usage != null) {
                                dataUsage.get(attribute).add(usage);
                        }
                        return true;
                }
        }

	//------- Overridden methods ---------------------------------------------------------------------
        @Override
        protected LogEntry newInstance() {
                return new DULogEntry();
        }

        @Override
        public DULogEntry clone() {
                return (DULogEntry) super.clone();
        }

        @Override
        protected void copyFieldValues(LogEntry clone) throws LockingException, ParameterException {
                super.copyFieldValues(clone);
                for (DataAttribute att : dataUsage.keySet()) {
                        ((DULogEntry) clone).setDataUsageFor(att, new HashSet<>(dataUsage.get(att)));
                }
        }

        @Override
        public Object getFieldValue(EntryField field) {
                Object superValue = super.getFieldValue(field);
                if (superValue != null) {
                        return superValue;
                }
                if (field == EntryField.DATA) {
                        if (dataUsage != null) {
                                return Collections.unmodifiableMap(dataUsage);
                        }
                }
                return null;
        }

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = super.hashCode();
                result = prime * result + ((dataUsage == null) ? 0 : dataUsage.hashCode());
                return result;
        }

        @Override
        public boolean equals(Object obj) {
                if (this == obj) {
                        return true;
                }
                if (!super.equals(obj)) {
                        return false;
                }
                if (getClass() != obj.getClass()) {
                        return false;
                }
                DULogEntry other = (DULogEntry) obj;
                if (dataUsage == null) {
                        if (other.dataUsage != null) {
                                return false;
                        }
                } else if (!dataUsage.equals(other.dataUsage)) {
                        return false;
                }
                return true;
        }

        public static DULogEntry parse(String entryString) throws ParserException {
                Matcher matcher = STRING_PATTERN.matcher(entryString);
                if (!entryString.matches(STRING_PATTERN.pattern())) {
                        throw new ParserException("Cannot parse log entry \"" + entryString + "\". Required format: " + STRING_PATTERN);
                }
                int bracketIndex = entryString.indexOf('(');
                boolean containsDataUsage = bracketIndex != -1;
                String activityName = null;
                if (!containsDataUsage) {
                        activityName = entryString;
                } else {
                        activityName = entryString.substring(0, bracketIndex);
                }
                DULogEntry entry = new DULogEntry(activityName);
                if (containsDataUsage && matcher.find()) {
                        String dataUsageString = matcher.group(1).substring(1, matcher.group(1).length() - 1);
                        StringTokenizer tokenizer = new StringTokenizer(dataUsageString, ",");
                        while (tokenizer.hasMoreTokens()) {
                                String nextDataUsage = tokenizer.nextToken();
                                String attributeName = nextDataUsage.substring(0, nextDataUsage.indexOf(':'));
                                String dataUsageCodes = nextDataUsage.substring(nextDataUsage.indexOf(':') + 1);
                                Set<DataUsage> dataUsageModes = new HashSet<>();
                                for (int i = 0; i < dataUsageCodes.length(); i++) {
                                        dataUsageModes.add(DataUsage.fromAbbreviation(String.valueOf(dataUsageCodes.charAt(i)).toUpperCase()));
                                }
                                if (dataUsageModes.contains(DataUsage.CREATE) && dataUsageModes.contains(DataUsage.DELETE)) {
                                        throw new ParserException("Invalid usage modes for attribute \"" + attributeName + "\": CREATE and DELETE");
                                }
                                for (DataUsage dataUsage : dataUsageModes) {
                                        try {
                                                entry.addDataUsage(new DataAttribute(attributeName), dataUsage);
                                        } catch (LockingException e) {
                                                // Cannot happen, since the data usage field is not locked in this method.
                                                throw new RuntimeException(e);
                                        }
                                }
                        }
                }
                return entry;
        }
//	
//	public static void main(String[] args) throws ParserException {
//		DULogEntry entry = DULogEntry.parse("A(att1:wrc,g:r,h:w)");
//		System.out.println(entry.getDataUsage());
//	}
}
