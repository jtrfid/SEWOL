package de.uni.freiburg.iig.telematik.jawl.log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import de.invation.code.toval.time.TimeValue;
import de.invation.code.toval.validate.Validate;



public class LogEntry implements Comparable<LogEntry>, Cloneable{
	
	private static final String toStringFormat = "[%s|%s|%s]";
	/**
	 * The timestamp of the log entry (the time the related activity was conducted).<br>
	 */
	protected Date timestamp = null;
	/**
	 * The related activity that was conducted and triggered the log entry.
	 */
	protected String activity = null;
	/**
	 * The originator of the log entry (the person/process responsible for it).
	 */
	protected String originator = null;
	/**
	 * The event type of the log entry.<br>
	 * Activities can be related to a set of different events such as <i>start</i>,<i>complete</i>, etc.
	 */
	protected EventType eventType = null;
	/**
	 * A list of additional meta information for the log entry.
	 */
	protected Set<DataAttribute> metaData = new HashSet<DataAttribute>();
	/**
	 * Stores the locked fields of the log entry together with the locking reason.<br>
	 * Locked fields cannot be altered any more.
	 */
	private HashMap<EntryField, String> locking = new HashMap<EntryField, String>();
	/**
	 * Random generator.
	 */
	protected Random rand = new Random();
	/**
	 * Field that allows to group log traces together by assigning them the same group.<br>
	 * Can be useful when different event types for an activity are used (start, suspend, complete).
	 * Group assignment in this case allows to reflect togetherness.
	 */
	protected String group = null;
	
	protected SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	
	
	//------- Constructors -------------------------------------------------------------------------
	
	/**
	 * Generates a new log entry.
	 */
	public LogEntry(){}
	
	/**
	 * Generates a new log entry using the given activity.
	 * @param activity Activity of the log entry.
	 */
	public LogEntry(String activity) {
		this();
		try {
			setActivity(activity);
		} catch (LockingException e) {
			// Cannot happen since no field is locked by default.
		}
	}
	
	
	//------- Timestamp -------------------------------------------------------------------------
	
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
	 * @throws LockingException if the field TIME is locked and the given timestamp differs from the current value of {@link #timestamp}.
	 */
	public boolean setTimestamp(Date date) throws LockingException {
		Validate.notNull(date);
		
		if(isFieldLocked(EntryField.TIME)){
			if(!this.timestamp.equals(date))
				throw new LockingException(EntryField.TIME);
			return false;
		} else {
			this.timestamp = date;
			return true;
		}
	}
	
	public boolean addTime(Long milliseconds) throws LockingException {
		Validate.notNegative(milliseconds);
		return modifyTime(milliseconds, TimeModification.ADD);
	}
	
	public boolean subTime(Long milliseconds) throws LockingException {
		Validate.notNegative(milliseconds);
		return modifyTime(milliseconds, TimeModification.SUB);
	}
	
	private boolean modifyTime(Long milliseconds, TimeModification mod) throws LockingException {
		Validate.notNegative(milliseconds);
		Validate.notNull(mod);
		if(milliseconds == 0)
			return false;
		
		long diff = milliseconds;
		if(mod == TimeModification.SUB){
			diff = (long) Math.copySign(diff, -1);
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp.getTime() + diff);
		return setTimestamp(cal.getTime());
	}
	
	private enum TimeModification {
		ADD, SUB;
	}
	
	public boolean addTimeValue(TimeValue timeValue) throws LockingException {
		Validate.notNull(timeValue);
		return addTime(timeValue.getValueInMilliseconds());
	}
	
	public boolean subTimeValue(TimeValue timeValue) throws LockingException {
		Validate.notNull(timeValue);
		return subTime(timeValue.getValueInMilliseconds());
	}
	
	public void removeTimestamp(){
		this.timestamp = null;
	}
	
	
	//------- Activity --------------------------------------------------------------------------
	
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
	 * @throws LockingException if the field ACTIVITY is locked and the given activity differs from the current value of {@link #activity}.
	 * @return <code>true</code> if {@link #activity} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean setActivity(String activity) throws LockingException {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		if(isFieldLocked(EntryField.ACTIVITY)){
			if(!this.activity.equals(activity))
				throw new LockingException(EntryField.ACTIVITY);
			return false;
		} else {
			this.activity = activity;
			return true;
		}
	}
	
	public void removeActivity(){
		this.activity = null;
	}
	
	
	//------- Originator -------------------------------------------------------------------------
	
	/**
	 * Returns the current value of the originator field.
	 * @return The originator of the log entry.
	 */
	public String getOriginator(){
		return originator;
	}
	
	/**
	 * Sets the originator of the log entry ({@link #originator}).
	 * @param originator Originator to set.
	 * @throws ParameterException if the given value is <code>null</code>.
	 * @throws LockingException if the originator field is locked <br>and the given value differs from the actual value of {@link #originator}.
	 * @return <code>true</code> if {@link #originator} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean setOriginator(String originator) throws LockingException{
		Validate.notNull(originator);
		Validate.notEmpty(originator);
		
		if(isFieldLocked(EntryField.ORIGINATOR)){
			if(!this.originator.equals(originator))
				throw new LockingException(EntryField.ORIGINATOR);
			return false;
		} else {
			this.originator = originator;
			return true;
		}
	}
	
	public void removeOriginator(){
		this.originator = null;
	}
	
	
	//------- Event Type ------------------------------------------------------------------------
	
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
	 * @throws LockingException if the field EVENTTYPE is locked and the given event type differs from {@link #eventType}.
	 * @return <code>true</code> if {@link #eventType} was modified;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean setEventType(EventType eventType) throws LockingException{
		Validate.notNull(eventType);
		
		if(isFieldLocked(EntryField.EVENTTYPE)){
			if(!this.eventType.equals(eventType))
				throw new LockingException(EntryField.EVENTTYPE);
			return false;
		} else {
			this.eventType = eventType;
			return true;
		}
	}
	
	
	//------- Meta Attribute --------------------------------------------------------------------
	
	public boolean addMetaAttribute(DataAttribute metaAttribute){
		if(metaAttribute != null){
			return this.metaData.add(metaAttribute);
		}
		return false;
	}
	
	public Set<DataAttribute> getMetaAttributes(){
		return Collections.unmodifiableSet(metaData);
	}
	
	
	//------- Field Values ---------------------------------------------------------------------
	
	public Object getFieldValue(EntryField field){
		switch (field) {
		case TIME:
			return getTimestamp();
		case ACTIVITY:
			return getActivity();
		case ORIGINATOR:
			return originator;
		case EVENTTYPE:
			return eventType;
		case META:
			return Collections.unmodifiableSet(metaData);
		default:
			return null;
		}
	}
	
	
	//------- Group -----------------------------------------------------------------------------
	
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
	
	
	//------- Locking ---------------------------------------------------------------------------
	
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
	
	
	//------- Helper methods ---------------------------------------------------------------------
	
	/**
	 * Returns a copy of the log entry.
	 */
	@Override
	public LogEntry clone(){
		try {
			LogEntry result = newInstance();
			copyFieldValues(result);
			return result;
		} catch (Exception e) {}
		return null;
	}
	
	protected LogEntry newInstance(){
		return new LogEntry();
	}
	
	protected void copyFieldValues(LogEntry clone) throws LockingException{
		clone.setActivity(activity);
		clone.setTimestamp((Date) this.timestamp.clone());
		clone.setEventType(eventType);
		clone.setOriginator(originator);
		for(EntryField lockedField: locking.keySet())
			clone.lockField(lockedField, locking.get(lockedField));
	}
	
	@Override
	public int compareTo(LogEntry o) {
		return getTimestamp().compareTo(o.getTimestamp());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activity == null) ? 0 : activity.hashCode());
		result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result + ((metaData == null) ? 0 : metaData.hashCode());
		result = prime * result + ((originator == null) ? 0 : originator.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LogEntry other = (LogEntry) obj;
		if (activity == null) {
			if (other.activity != null)
				return false;
		} else if (!activity.equals(other.activity))
			return false;
		if (eventType != other.eventType)
			return false;
		if (metaData == null) {
			if (other.metaData != null)
				return false;
		} else if (!metaData.equals(other.metaData))
			return false;
		if (originator == null) {
			if (other.originator != null)
				return false;
		} else if (!originator.equals(other.originator))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}
	
	@Override
	public String toString(){
		String timestamp = (this.timestamp == null) ? "-" : sdf.format(this.timestamp);
		return String.format(toStringFormat, timestamp , getActivity(), getOriginator());
	}
	
}
