package de.uni.freiburg.iig.telematik.jawl.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.Validate;

public class Context {
	
	public static final String DEFAULT_CONTEXT_NAME = "Context";

	protected String name;
	
	protected Set<String> subjects;
	protected Set<String> objects;
	protected Set<String> activities;
	
	protected ContextListenerSupport contextListenerSupport;
	
	public Context(){
		initialize();
	}
	
	public Context(String name) {
		initialize();
		setName(name);
	}
	
	protected void initialize(){
		setName(getDefaultName());
		subjects = new HashSet<String>();
		objects = new HashSet<String>();
		activities = new HashSet<String>();
		contextListenerSupport = new ContextListenerSupport();
	}
	
	public Context(ContextProperties properties) throws PropertyException{
		Validate.notNull(properties);
		initialize();
		setName(properties.getName());
		Set<String> subjects = properties.getSubjects();
		if(subjects != null)
			setSubjects(subjects);
		Set<String> objects = properties.getObjects();
		if(objects != null)
			setObjects(objects);
		Set<String> activities = properties.getActivities();
		if(activities != null)
			setActivities(activities);
	}
	
	protected String getDefaultName(){
		return DEFAULT_CONTEXT_NAME;
	}
	
	public boolean addContextListener(ContextListener listener){
		return contextListenerSupport.addListener(listener);
	}
	
	public boolean removeContextListener(ContextListener listener){
		return contextListenerSupport.removeListener(listener);
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name) {
		Validate.notNull(name);
		this.name = name;
	}
	
	public Set<String> getSubjects(){
		return Collections.unmodifiableSet(subjects);
	}
	
	public boolean hasSubjects(){
		return !subjects.isEmpty();
	}
	
	public boolean containsSubject(String subject){
		return subjects.contains(subject);
	}
	
	public void addSubjects(Collection<String> subjects) {
		Validate.notNull(subjects);
		if(subjects.isEmpty())
			return;
		for(String subject: subjects){
			addSubject(subject);
		}
	}
	
	public boolean addSubject(String subject) {
		return addSubject(subject, true);
	}
	
	protected boolean addSubject(String subject, boolean notifyListeners) {
		Validate.notNull(subject);
		if(subjects.add(subject)){
			if(notifyListeners)
				contextListenerSupport.notifySubjectAdded(subject);
			return true;
		}
		return false;
	}
	
	public void setSubjects(Collection<String> subjects) {
		Validate.notNull(subjects);
		if(subjects.isEmpty())
			return;
		Set<String> newSubjects = new HashSet<String>(subjects);
		newSubjects.removeAll(getSubjects());
		Set<String> obsoleteSubjects = new HashSet<String>(getSubjects());
		obsoleteSubjects.removeAll(subjects);
		addSubjects(newSubjects);
		removeSubjects(obsoleteSubjects);
	}
	
	public void removeSubjects() {
		List<String> subjects = new ArrayList<String>(getSubjects());
		this.subjects.clear();
		for(String subject: subjects)
			contextListenerSupport.notifySubjectRemoved(subject);
	}
	
	public void removeSubjects(Collection<String> subjects) {
		Validate.notNull(subjects);
		if(subjects.isEmpty())
			return;
		for(String subject: subjects)
			removeSubject(subject);
	}
	
	public boolean removeSubject(String subject) {
		return removeSubject(subject, true);
	}
	
	protected boolean removeSubject(String subject, boolean notifyListeners) {
		if(subjects.remove(subject)){
			if(notifyListeners)
				contextListenerSupport.notifySubjectRemoved(subject);
			return true;
		}
		return false;
	}
	
	public Set<String> getActivities(){
		return Collections.unmodifiableSet(activities);
	}
	
	public boolean hasActivities(){
		return !activities.isEmpty();
	}
	
	public boolean containsActivity(String activity){
		return activities.contains(activity);
	}
	
	public void setActivities(Collection<String> activities) {
		Validate.notNull(activities);
		if(activities.isEmpty())
			return;
		Set<String> newActivities = new HashSet<String>(activities);
		newActivities.removeAll(getActivities());
		Set<String> obsoleteActivities = new HashSet<String>(getActivities());
		obsoleteActivities.removeAll(activities);
		addActivities(newActivities);
		removeActivities(obsoleteActivities);
	}
	
	public void addActivities(Collection<String> activities) {
		Validate.notNull(activities);
		if(activities.isEmpty())
			return;
		for(String activity: activities){
			addActivity(activity);
		}
	}
	
	public boolean addActivity(String activity) {
		return addActivity(activity, true);
	}
	
	public boolean addActivity(String activity, boolean notifyListeners) {
		Validate.notNull(activity);
		if(activities.add(activity)){
			if(notifyListeners)
				contextListenerSupport.notifyActivityAdded(activity);
			return true;
		}
		return false;
	}
	
	public void removeActivities() {
		List<String> activities = new ArrayList<String>(getActivities());
		this.activities.clear();
		for(String activity: activities)
			contextListenerSupport.notifyActivityRemoved(activity);
	}
	
	public void removeActivities(Collection<String> activities) {
		Validate.notNull(activities);
		if(activities.isEmpty())
			return;
		for(String activity: activities)
			removeActivity(activity);
	}
	
	public boolean removeActivity(String activity) {
		return removeActivity(activity, true);
	}
	
	protected boolean removeActivity(String activity, boolean notifyListeners) {
		if(activities.remove(activity)){
			if(notifyListeners)
				contextListenerSupport.notifyActivityRemoved(activity);
			return true;
		}
		return false;
	}
	
	public Set<String> getObjects(){
		return Collections.unmodifiableSet(objects);
	}
	
	public boolean hasObjects(){
		return !objects.isEmpty();
	}
	
	public boolean containsObject(String object){
		return objects.contains(object);
	}
	
	public void setObjects(Collection<String> objects) {
		Validate.notNull(objects);
		if(objects.isEmpty())
			return;
		Set<String> newObjects = new HashSet<String>(objects);
		newObjects.removeAll(getObjects());
		Set<String> obsoleteObjects = new HashSet<String>(getObjects());
		obsoleteObjects.removeAll(objects);
		addObjects(newObjects);
		removeObjects(obsoleteObjects);
	}
	
	public void addObjects(Collection<String> objects) {
		Validate.notNull(objects);
		if(objects.isEmpty())
			return;
		for(String object: objects){
			addObject(object);
		}
	}
	
	public boolean addObject(String object) {
		return addObject(object, true);
	}
	
	protected boolean addObject(String object, boolean notifyListeners) {
		Validate.notNull(object);
		if(objects.add(object)){
			if(notifyListeners)
				contextListenerSupport.notifyObjectAdded(object);
			return true;
		}
		return false;
	}
	
	public void removeObjects() {
		List<String> objects = new ArrayList<String>(getObjects());
		this.objects.clear();
		for(String object: objects)
			contextListenerSupport.notifyObjectRemoved(object);
	}
	
	public void removeObjects(Collection<String> objects) {
		Validate.notNull(objects);
		if(objects.isEmpty())
			return;
		for(String object: objects)
			removeObject(object);
	}
	
	public boolean removeObject(String object) {
		return removeObject(object, true);
	}
	
	protected boolean removeObject(String object, boolean notifyListeners) {
		if(objects.remove(object)){
			if(notifyListeners)
				contextListenerSupport.notifyObjectRemoved(object);
			return true;
		}
		return false;
	}
	
	public void validateSubject(String subject) throws CompatibilityException {
		Validate.notNull(subject);
		if(!subjects.contains(subject))
			throw new CompatibilityException("Unknown subject: " + subject);
	}
	
	public void validateSubjects(Collection<String> subjects) throws CompatibilityException {
		Validate.notNull(subjects);
		Validate.noNullElements(subjects);
		Validate.notEmpty(subjects);
		if(!this.subjects.containsAll(subjects))
			throw new CompatibilityException("Unknown subjects.");
	}
	
	public void validateActivity(String activity) throws CompatibilityException {
		Validate.notNull(activity);
		if(!activities.contains(activity))
			throw new CompatibilityException("Unknown activity: " + activity);
	}
	
	public void validateActivities(Collection<String> activities) throws CompatibilityException {
		Validate.notNull(activities);
		Validate.noNullElements(activities);
		if(!this.activities.containsAll(activities))
			throw new CompatibilityException("Unknown activities.");
	}
	
	public void validateObject(String object) throws CompatibilityException {
		Validate.notNull(object);
		if(!objects.contains(object))
			throw new CompatibilityException("Unknown object: " + object);
	}
	
	public void validateObjects(Collection<String> objects) throws CompatibilityException {
		Validate.notNull(objects);
		Validate.noNullElements(objects);
		if(!this.objects.containsAll(objects))
			throw new CompatibilityException("Unknown objects.");
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("  Subjects: " + getSubjects());
		builder.append('\n');
		builder.append("Activities: " + getActivities());
		builder.append('\n');
		builder.append("   Objects: " + getObjects());
		builder.append('\n');
		return builder.toString();
	}
	
	public ContextProperties getProperties() throws PropertyException {
		ContextProperties result = new ContextProperties();
		result.setName(getName());
		result.setSubjects(getSubjects());
		result.setObjects(getObjects());
		result.setActivities(getActivities());
		return result;
	}

}
