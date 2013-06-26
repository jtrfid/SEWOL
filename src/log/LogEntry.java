package log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import types.DataUsage;
import types.HashList;


public class LogEntry implements Comparable<LogEntry>{
	
	/**
	 * The timestamp of the log entry (the time the related activity was conducted).<br>
	 */
	private Date timestamp = null;
	/**
	 * The related activity that was conducted and triggered the log entry.
	 */
	private String activity = null;
	/**
	 * The originator of the log entry (the person/process responsible for it).
	 */
	private String originator = null;
	/**
	 * The list of originator candidates.<br>
	 * The originator of the log entry can only be chosen out of this candidate list.
	 */
	private HashList<String> originatorCandidates = new HashList<String>();
	/**
	 * The event type of the log entry.<br>
	 * Activities can be related to a set of different events such as <i>start</i>,<i>complete</i>, etc.
	 */
	private EventType eventType = null;
	/**
	 * The list of attributes that are affected on executing the logged activity.
	 */
	private Map<DataAttribute, Set<DataUsage>> dataUsage = new HashMap<DataAttribute, Set<DataUsage>>();
	/**
	 * A list of additional meta information for the log entry.
	 */
	private Set<DataAttribute> metaData = new HashSet<DataAttribute>();
	/**
	 * Random generator.
	 */
	private Random rand = new Random();
	/**
	 * Stores the locked fields of the log entry together with the locking reason.<br>
	 * Locked fields cannot be altered any more.
	 */
	private HashMap<EntryField, String> locking = new HashMap<EntryField, String>();
	/**
	 * Field that allows to group log traces together by assigning them the same group.<br>
	 * Can be useful when different event types for an activity are used (start, suspend, complete).
	 * Group assignment in this case allows to reflect togetherness.
	 */
	private String group = null;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	
	/**
	 * Generates a new log entry.
	 */
	public LogEntry(){}
	
	/**
	 * Generates a new log entry using the given activity.
	 * @param activity Activity of the log entry.
	 * @throws NullPointerException if the given activity is <code>null</code>.
	 */
	public LogEntry(String activity) throws NullPointerException {
		this();
		if(activity == null)
			throw new NullPointerException();
		try {
			setActivity(activity);
		} catch (LockingException e) {
			// Cannot happen since no field is locked by default.
		}
	}
	
	/**
	 * Generates a new log entry using the given activity and the set of originator candidates.
	 * @param activity Activity of the log entry.
	 * @param originatorCandidates List of originator candidates.
	 * @throws Exception
	 */
	public LogEntry(String activity, List<String> originatorCandidates) throws NullPointerException{
		this(activity);
		if(originatorCandidates==null)
			throw new NullPointerException();
		try {
			this.setOriginatorCandidates(originatorCandidates);
		} catch (LockingException e) {
			// Cannot happen since no field is locked by default.
		}
	}
	
	/**
	 * Returns the timestamp of the log entry {@link #timestamp}).
	 * @return the timestamp of the log entry.
	 */
	public Date getTimestamp(){
		if(timestamp != null)
			return (Date) timestamp.clone();
		return null;
	}
	
	/**
	 * Sets the timestamp of the log entry ({@link #timestamp}).
	 * @param date the time to set.
	 * @return <code>true</code> if {@link #timestamp} was modified;<br>
	 * <code>false</code> otherwise.
	 * @throws NullPointerException if the given timestamp is <code>null</code>
	 * @throws LockingException if the field TIME is locked and the given timestamp differs from the current value of {@link #timestamp}.
	 */
	public boolean setTimestamp(Date date) throws NullPointerException,LockingException {
		if(date== null)
			throw new NullPointerException();
		if(isFieldLocked(EntryField.TIME)){
			if(!this.timestamp.equals(date))
				throw new LockingException(EntryField.TIME);
			return false;
		} else {
			this.timestamp = date;
			return true;
		}
	}
	
	/**
	 * Returns the activity of the log entry ({@link #activity}).
	 * @return the activity of the log entry.
	 */
	public String getActivity(){
		return activity;
	}
	
	/**
	 * Sets the activity of the log entry ({@link #activity}).
	 * @param activity Activity to set.
	 * @throws NullPointerException if the given activity is <code>null</code>.
	 * @throws LockingException if the field ACTIVITY is locked and the given activity differs from the current value of {@link #activity}.
	 * @return <code>true</code> if {@link #activity} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean setActivity(String activity) throws LockingException {
		if(isFieldLocked(EntryField.ACTIVITY)){
			if(!this.activity.equals(activity))
				throw new LockingException(EntryField.ACTIVITY);
			return false;
		} else {
			this.activity = activity;
			return true;
		}
	}
	
	/**
	 * Returns the current value of the originator field.
	 * @return The originator of the log entry.
	 */
	public String getOriginator(){
		return originator;
	}
	
	/**
	 * Sets the originator of the log entry ({@link #originator}).<br>
	 * Generally only originators out of {@link #originatorCandidates} may be set as originators.<br>
	 * Depending on the actual state of the log entry, this operation may be prohibited due to locking.
	 * @param originator Originator to set.
	 * @throws NullPointerException if the given value is <code>null</code>.
	 * @throws LockingException if the originator field is locked <br>and the given value differs from the actual value of {@link #originator}.
	 * @throws ModificationException if the given originator is not contained in {@link #originatorCandidates}.
	 * @return <code>true</code> if {@link #originator} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean setOriginator(String originator) throws NullPointerException,LockingException,ModificationException {
		if(originator == null)
			throw new NullPointerException();
		if(isFieldLocked(EntryField.ORIGINATOR)){
			if(!this.originator.equals(originator))
				throw new LockingException(EntryField.ORIGINATOR);
			return false;
		} else {
			if(!originatorCandidates.contains(originator))
				throw new ModificationException(EntryField.ORIGINATOR, "Originator not contained in candidate list.");
			this.originator = originator;
			return true;
		}
	}
	

	/**
	 * Chooses the originator with the given index from {@link #originatorCandidates} as new value for {@link #originator}..
	 * @param index Index of the originator candidate.
	 * @throws IndexOutOfBoundsException if the given index outruns the valid index range of {@link #originatorCandidates}.
	 * @throws NullPointerException if the extracted originator candidate is <code>null</code>.
	 * @throws LockingException if the originator field is locked <br>and the given value differs from the actual value of {@link #originator}.
	 * @throws ModificationException if the given originator is not contained in {@link #originatorCandidates}.
	 * @return <code>true</code> if {@link #originator} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean setOriginator(int index) throws IndexOutOfBoundsException,NullPointerException,LockingException,ModificationException{
		return setOriginator(originatorCandidates.get(index));
	}
	
	/**
	 * Returns the list of originator candidates ({@link #originatorCandidates}).
	 * @return all originator candidates
	 */
	public List<String> getOriginatorCandidates(){
		return new ArrayList<String>(originatorCandidates);
	}
	
	/**
	 * Sets the given originator as the only originator candidate and thus as the value of {@link #originator}.
	 * @param originator Originator candidate
	 * @throws NullPointerException if the given originator is <code>null</code>.
	 * @throws LockingException if the field ORIGINATOR_CANDIDATES is locked <br>and the given candidate is not already the only candidate.
	 * @return <code>true</code> if {@link #originatorCandidates} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean setOriginatorCandidate(String originator) throws NullPointerException,LockingException {
		if(originator==null)
			throw new NullPointerException();
		return setOriginatorCandidates(Collections.singletonList(originator));
	}
	
	/**
	 * Sets the elements of the given list as originator candidates and chooses a random entry as new value of {@link #originator}.
	 * @param originators List of originator candidates
	 * @throws NullPointerException if the list is <code>null</code>.
	 * @throws LockingException if the field ORIGINATOR_CANDIDATES is locked <br>and the given candidates are not the same than the current ones.
	 * @return <code>true</code> if {@link #originatorCandidates} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean setOriginatorCandidates(List<String> originators) throws NullPointerException,LockingException {
		if(originators==null)
			throw new NullPointerException();
		if(isFieldLocked(EntryField.ORIGINATOR_CANDIDATES)){
			if(!(originatorCandidates.containsAll(originators) && originators.containsAll(originatorCandidates)))
				throw new LockingException(EntryField.ORIGINATOR_CANDIDATES);
			return false;
		} else {
			this.originatorCandidates.clear();
			this.originatorCandidates.addAll(originators);
			chooseOriginator();
			return true;
		}
	}
	
	/**
	 * Adds the given originator to the list of originator candidates {@link #originatorCandidates}.
	 * @param originator Originator candidate to add
	 * @throws NullPointerException if the given originator candidate is <code>null</code>.
	 * @throws LockingException if The field ORIGINATOR_CANDIDATES is locked <br>and {@link #originatorCandidates} does not already contain the given candidate.
	 * @return <code>true</code> if {@link #originatorCandidates} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean addOriginatorCandidate(String originator) throws NullPointerException,LockingException {
		if(originator == null)
			throw new NullPointerException();
		return addOriginatorCandidates(Arrays.asList(originator));
	}
	
	/**
	 * Adds the given originators to the list of originator candidates ({@link #originatorCandidates}).
	 * @param originators Originators to add
	 * @throws NullPointerException if the given originator candidate list is <code>null</code>.
	 * @throws LockingException if the originator candidate field is locked <br>and {@link #originatorCandidates} does not already contain all given candidates.
	 * @return <code>true</code> if {@link #originatorCandidates} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean addOriginatorCandidates(List<String> originators) throws NullPointerException,LockingException {
		if(originators==null)
			throw new NullPointerException();
		if(isFieldLocked(EntryField.ORIGINATOR_CANDIDATES)){
			if(!originatorCandidates.containsAll(originators))
				throw new LockingException(EntryField.ORIGINATOR_CANDIDATES);
			return false;
		}
		this.originatorCandidates.addAll(originators);
		chooseOriginator();
		return true;
	}
	
	/**
	 * Removes the given originator from the list of originator candidates ({@link #originatorCandidates}).
	 * @param originator Originator to remove.
	 * @return <code>true</code> if {@link #originatorCandidates} was modified;<br>
	 * <code>false</code> otherwise.
	 * @throws NullPointerException if the given originator is <code>null</code>.
	 * @throws LockingException if the field ORIGINATOR_CANDIDATES is locked <br>and the given originator is not already contained in {@link #originatorCandidates}.
	 */
	public boolean removeOriginatorCandidate(String originator) throws NullPointerException,LockingException{
		if(originator==null)
			throw new NullPointerException();
		return removeOriginatorCandidates(Collections.singletonList(originator));
	}
	
	/**
	 * Removes the given originators from the list of originator candidates ({@link #originatorCandidates}).
	 * @param originators Originators to remove
	 * @return <code>true</code> if {@link #originatorCandidates} was modified;<br>
	 * <code>false</code> otherwise.
	 * @throws NullPointerException if the given originator list is <code>null</code>.
	 * @throws LockingException if the field ORIGINATOR_CANDIDATES is locked <br>and the given originators are not already contained in {@link #originatorCandidates}.
	 */
	public boolean removeOriginatorCandidates(Collection<String> originators) throws NullPointerException,LockingException{
		if(originators==null)
			throw new NullPointerException();
		if(originators.isEmpty())
			return false;
		if(isFieldLocked(EntryField.ORIGINATOR_CANDIDATES)){
			HashList<String> check = originatorCandidates.clone();
			check.removeAll(originators);
			if(check.size()<originatorCandidates.size())
				throw new LockingException(EntryField.ORIGINATOR_CANDIDATES);
			return false;
		} else {
			boolean change = this.originatorCandidates.removeAll(originators);
			chooseOriginator();
			return change;
		}
	}
	
	/**
	 * Removes all originator candidates.<br>
	 * Note that this operation sets {@link #originator} to <code>null</code>.
	 * @return <code>true</code> if {@link #originatorCandidates} was modified;<br>
	 * <code>false</code> otherwise.
	 * @throws LockingException if the field ORIGINATOR_CANDIDATE is locked and {@link #originatorCandidates} is not empty.
	 */
	public boolean removeAllOriginatorCandidates() throws LockingException{
		if(originatorCandidates.isEmpty())
			return false;
		if(isFieldLocked(EntryField.ORIGINATOR))
			throw new LockingException(EntryField.ORIGINATOR_CANDIDATES);
		originatorCandidates.clear();
		chooseOriginator();
		return true;
	}
	
	/**
	 * Sets the group field of this log entry.<br>
	 * Setting group IDs for entries allows to reflect relations 
	 * and togetherness amongst entries within a trace.
	 * @param group Group ID
	 * @see #getGroup()
	 * @see LogTrace#getEntriesForGroup(String)
	 */
	public void setGroup(String group){
		this.group = group;
	}
	
	/**
	 * Returns the group assigned to this entry or <code>null</code>.
	 * Setting group IDs for entries allows to reflect relations 
	 * and togetherness amongst entries within a trace.
	 * @return The group ID.
	 * @see #setGroup(String)
	 * @see LogTrace#getEntriesForGroup(String)
	 */
	public String getGroup(){
		return group;
	}
	
	/**
	 * Randomly chooses an originator candidate for {@link #originator}.<br>
	 * If there are no candidates, the value of {@link #originator} is set to <code>null</code>.
	 */
	protected void chooseOriginator(){
		if(!originatorCandidates.isEmpty()){
			originator = originatorCandidates.get(rand.nextInt(originatorCandidates.size()));
		} else {
			originator = null;
		}
	}
	
	/**
	 * Locks a field of the log entry.<br>
	 * Locked fields cannot be altered anymore. If the originator field is locked,
	 * then the originator candidates field is locked as well,
	 * since changing the candidates also changes the originator (consistency).
	 * @param entryField
	 */
	public void lockField(EntryField entryField, String reason){
		locking.put(entryField, reason);
		if(entryField == EntryField.ORIGINATOR)
			locking.put(EntryField.ORIGINATOR_CANDIDATES, reason);
	}
	
	/**
	 * Checks if the given entry field is locked.
	 * @param entryField field to check
	 * @return <code>true</code> if the field is locked;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean isFieldLocked(EntryField entryField){
		return locking.containsKey(entryField);
	}
	
	/**
	 * Returns the event type of the log entry.
	 * @return the event type of the log entry.
	 */
	public EventType getEventType(){
		return eventType;
	}
	
	/**
	 * Sets the event type of the log entry ({@link #eventType}).
	 * @param eventType Event type to set.
	 * @throws NullPointerException if the given event type is <code>null</code>.
	 * @throws LockingException if the field EVENTTYPE is locked and the given event type differs from {@link #eventType}.
	 * @return <code>true</code> if {@link #eventType} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean setEventType(EventType eventType) throws LockingException{
		if(isFieldLocked(EntryField.EVENTTYPE)){
			if(!this.eventType.equals(eventType))
				throw new LockingException(EntryField.EVENTTYPE);
			return false;
		} else {
			this.eventType = eventType;
			return true;
		}
	}
	
	public boolean containsDataUsage(){
		return !dataUsage.isEmpty();
	}
	
	/**
	 * Returns the list of attributes ({@link #inputDataUsage}).<br>
	 * Note that data attributes themselves are not copied or cloned.
	 * @return A list containing the attributes.
	 */
	public Set<DataAttribute> getDataAttributes(){
		return Collections.unmodifiableSet(dataUsage.keySet());
	}
	
	/**
	 * Returns the data usage, i.e. all data attributes 
	 * together with the usage (read, ...).
	 * @return A map containing data usage information for each attribute.
	 */
	public Map<DataAttribute, Set<DataUsage>> getDataUsage(){
		return Collections.unmodifiableMap(dataUsage);
	}
	
	/**
	 * Removes the given attribute from the set of managed attributes.
	 * @param attribute Attribute to remove.
	 * @return if the set of managed attributes was modified;<br>
	 * <code>false</code> otherwise.
	 * @throws LockingException if the corresponding field is locked <br>and the given attribute is not already contained in the set of managed attributes.
	 */
	public boolean removeDataAttribute(DataAttribute attribute) throws LockingException{
		if(isFieldLocked(EntryField.DATA)){
			if(dataUsage.containsKey(attribute))
				throw new LockingException(EntryField.DATA);
			return false;
		} else {
			return dataUsage.remove(attribute) != null;
		}	
	}
	
	/**
	 * Sets the given data usage as data usage for this entry.
	 * @param dataUsage The data usage to adopt.
	 * @return <code>true</code> if the current data usage was modified;<br>
	 * <code>false</code> otherwise.
	 * @throws NullPointerException if the given usage is <code>null</code>.
	 * @throws LockingException if the corresponding field is locked <br>and the given data usage is not the same than the current one.
	 */
	public boolean setDataUsage(Map<DataAttribute, Set<DataUsage>> dataUsage) throws NullPointerException,LockingException{
		if(dataUsage == null)
			throw new NullPointerException();
		if(isFieldLocked(EntryField.DATA)){
			if(!this.dataUsage.equals(dataUsage))
				throw new LockingException(EntryField.DATA);
			return false;
		} else {
			this.dataUsage = dataUsage;
			return true;
		}
	}
	
	/**
	 * Sets the data usage for a given attribute.
	 * @param attribute The attribute (data element) for which the usage is specified.
	 * @param dataUsage The usage of the data element specified by the given attribute.
	 * @return <code>true</code> if the data usage for the given attribute was modified;<br>
	 * <code>false</code> otherwise.
	 * @throws NullPointerException if the given data usage is <code>null</code>.
	 * @throws LockingException if the corresponding field is locked <br>>and the given data usage is not identical to the current one.
	 */
	public boolean setDataUsageFor(DataAttribute attribute, Set<DataUsage> dataUsage) throws NullPointerException,LockingException{
		if(attribute==null)
			throw new NullPointerException();
		if(isFieldLocked(EntryField.DATA)){
			if(!(this.dataUsage.containsKey(attribute) && this.dataUsage.get(attribute).equals(dataUsage)))
				throw new LockingException(EntryField.DATA);
			return false;
		} else {
			this.dataUsage.put(attribute, dataUsage);
			return true;
		}
	}
	
	/**
	 * Adds the given attribute to the list of attributes
	 * @param attribute The attribute to add.
	 * @param usage The data usage
	 * @throws NullPointerException if the given attribute is <code>null</code>.
	 * @throws LockingException if the field INPUT_DATA is locked <br>and the attribute is not already contained in {@link #dataUsage}.
	 * @return <code>true</code> if {@link #dataUsage} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean addDataUsage(DataAttribute attribute, DataUsage usage) throws NullPointerException, LockingException{
		if(attribute==null)
			throw new NullPointerException();
		if(isFieldLocked(EntryField.DATA)){
			if(!dataUsage.containsKey(attribute))
				throw new LockingException(EntryField.DATA);
			return false;
		} else {
			if(dataUsage.get(attribute)==null){
				dataUsage.put(attribute, new HashSet<DataUsage>());
			}
			if(usage != null){
				dataUsage.get(attribute).add(usage);
			}
			return true;
		}
	}
	
	public boolean addMetaAttribute(DataAttribute metaAttribute){
		if(metaAttribute != null){
			return this.metaData.add(metaAttribute);
		}
		return false;
	}
	
	public Set<DataAttribute> getMetaAttributes(){
		return Collections.unmodifiableSet(metaData);
	}
	
	//---- Helper methods
	
	/**
	 * Returns a copy of the log entry.
	 */
	public LogEntry clone(){
		try {
			LogEntry result = new LogEntry(this.activity, new ArrayList<String>(this.originatorCandidates));
			result.setTimestamp((Date) this.timestamp.clone());
			result.setEventType(this.eventType);
			for(DataAttribute att: dataUsage.keySet()){
				result.setDataUsageFor(att, new HashSet<DataUsage>(dataUsage.get(att)));
			}
			for(EntryField lockedField: locking.keySet())
				result.lockField(lockedField, locking.get(lockedField));
			return result;
		} catch (Exception e) {}
		return null;
	}
	
	public Object getFieldValue(EntryField field){
		switch (field) {
		case TIME:
			return getTimestamp();
		case ACTIVITY:
			return getActivity();
		case ORIGINATOR:
			return originator;
		case ORIGINATOR_CANDIDATES:
			return Collections.unmodifiableSet(originatorCandidates);
		case EVENTTYPE:
			return eventType;
		case DATA:
			if(dataUsage!=null)
				return Collections.unmodifiableMap(dataUsage);
		case META:
			return Collections.unmodifiableSet(metaData);
		}
		return null;
	}
	
	@Override
	public int compareTo(LogEntry o) {
		return getTimestamp().compareTo(o.getTimestamp());
	}

	@Override
	public String toString(){
		return '['+sdf.format(this.timestamp)+'|'+this.getActivity()+'|'+this.getOriginator()+']';
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((activity == null) ? 0 : activity.hashCode());
		result = prime * result
				+ ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result
				+ ((dataUsage == null) ? 0 : dataUsage.hashCode());
		result = prime * result
				+ ((metaData == null) ? 0 : metaData.hashCode());
		result = prime * result
				+ ((originator == null) ? 0 : originator.hashCode());
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}
	
}
