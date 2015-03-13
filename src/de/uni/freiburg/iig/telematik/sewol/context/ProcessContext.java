package de.uni.freiburg.iig.telematik.sewol.context;

import java.awt.Window;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.misc.SetUtils;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.misc.soabase.SOABaseChangeReply;
import de.invation.code.toval.misc.soabase.SOABaseListener;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.InconsistencyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.acl.ACLModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;

/**
 * This class provides context information for process execution.<br>
 * More specifically:<br>
 * <ul>
 * <li></li>
 * </ul>
 * 
 * To decide which activities can be executed by which subjects, it uses an access control model.
 * 
 * Note: A context must be compatible with the process it is used for,<br>
 * i.e. contain all process activities.
 * 
 * @author Thomas Stocker
 */
public class ProcessContext extends SOABase implements SOABaseListener {
	/**
	 * Data usage (read, write, ...) for attributes which are used in process activities.
	 */
	protected Map<String, Map<String, Set<DataUsage>>> activityDataUsage;
	/**
	 * Access control model used to decide which subjects can execute which activities.
	 */
	protected AbstractACModel acModel;
	
	protected List<DataUsage> validUsageModes;
	
	protected ProcessContextListenerSupport duContextListenerSupport;
	
	//------- Constructors ------------------------------------------------------------------
	
	protected ProcessContext(){
		super();
	}
	
	/**
	 * Creates a new context using the given activity names.
	 * @param activities Names of process activities.
	 * @throws ParameterException 
	 * @throws Exception If activity list is <code>null</code> or empty.
	 */
	public ProcessContext(String name){
		super(name);
	}
	
	public ProcessContext(ProcessContextProperties properties) throws PropertyException{
		super(properties);
		// Set valid usage modes
		setValidUsageModes(properties.getValidUsageModes());

		// Set data usage
		activityDataUsage.clear();
		for (String activity : properties.getActivitiesWithDataUsage()) {
			Map<String, Set<DataUsage>> dataUsage = properties.getDataUsageFor(activity);
			for (String attribute : dataUsage.keySet()) {
				setDataUsageFor(activity, attribute, new HashSet<DataUsage>(dataUsage.get(attribute)));
			}
		}
	}
	
	public ProcessContext(SOABase context) throws Exception{
		super();
		takeoverValues(context, false);
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		duContextListenerSupport = new ProcessContextListenerSupport();
		activityDataUsage = new HashMap<String, Map<String, Set<DataUsage>>>();
		validUsageModes = new ArrayList<DataUsage>(Arrays.asList(DataUsage.values()));
	}
	
	public boolean addContextListener(ProcessContextListener listener){
		return contextListenerSupport.addListener(listener) && duContextListenerSupport.addListener(listener);
	}
	
	public boolean removeContextListener(ProcessContextListener listener){
		return contextListenerSupport.removeListener(listener) && duContextListenerSupport.removeListener(listener);
	}
	
	//------- Activities ------------------------------------------------------------

	@Override
	public boolean addActivity(String activity) {
		return addActivity(activity, false);
	}
	
	public boolean addActivity(String activity, boolean addToACModel){
		return addActivity(activity, addToACModel, true);
	}
	
	public boolean addActivity(String activity, boolean addToACModel, boolean notifyListeners) throws InconsistencyException{
		if(!super.addActivity(activity, false))
			return false;
		
		if(acModel != null && acModel.getContext() != this){
			if(!addToACModel){
				// Check if new activities cause an inconsistency
				if(!getACModel().getContext().containsActivity(activity)){
					super.removeObject(activity, false);
					throw new InconsistencyException("Incompatible access control model: Missing " + getObjectDescriptorSingular() + ": " + activity);
				}
			} else {
				acModel.getContext().addActivity(activity);
			}
		}
		if(notifyListeners){
			contextListenerSupport.notifyActivityAdded(activity);
		}
		return true;
	}
	
	public void removeActivities(Collection<String> activities, boolean removeFromACModel){
		removeActivities(activities, removeFromACModel, true);
	}
	
	public void removeActivities(Collection<String> activities, boolean removeFromACModel, boolean notifyListeners){
		for(String activity: activities)
			removeActivity(activity, removeFromACModel, notifyListeners);
	}
	
	@Override
	public boolean removeActivity(String activity) {
		return removeActivity(activity, false);
	}
	
	public boolean removeActivity(String activity, boolean removeFromACModel){
		return removeActivity(activity, removeFromACModel, true);
	}

	public boolean removeActivity(String activity, boolean removeFromACModel, boolean notifyListeners){
		if(!super.removeActivity(activity, false))
			return false;
		
		if(acModel != null && removeFromACModel && acModel.getContext() != this){
			acModel.getContext().removeActivity(activity);
		}
		this.activityDataUsage.keySet().remove(activity);
		if(notifyListeners){
			contextListenerSupport.notifyActivityRemoved(activity);
		}
		return true;
	}
	
	
	//------- Subjects --------------------------------------------------------------
	
	@Override
	public boolean addSubject(String subject) {
		return addSubject(subject, false);
	}
	
	public boolean addSubject(String subject, boolean addToACModel){
		return addSubject(subject, addToACModel, true);
	}
	
	public boolean addSubject(String subject, boolean addToACModel, boolean notifyListeners) throws InconsistencyException{
		if(!super.addSubject(subject, false))
			return false;
		
		if(acModel != null && acModel.getContext() != this){
			if(!addToACModel){
				// Check if new activities cause an inconsistency
				if(!getACModel().getContext().containsObject(subject)){
					super.removeObject(subject, false);
					throw new InconsistencyException("Incompatible access control model: Missing " + getSubjectDescriptorSingular() + ": " + subject);
				}
			} else {
				acModel.getContext().addSubject(subject);
			}
		}
		if(notifyListeners){
			contextListenerSupport.notifySubjectAdded(subject);
		}
		return true;
	}
	
	public void removeSubjects(Collection<String> subjects, boolean removeFromACModel){
		removeSubjects(subjects, removeFromACModel, true);
	}
	
	public void removeSubjects(Collection<String> subjects, boolean removeFromACModel, boolean notifyListeners){
		for(String subject: subjects)
			removeSubject(subject, removeFromACModel, notifyListeners);
	}
	
	@Override
	public boolean removeSubject(String subject){
		return removeSubject(subject, false);
	}
	
	public boolean removeSubject(String subject, boolean removeFromACModel){
		return removeSubject(subject, removeFromACModel, true);
	}
	
	public boolean removeSubject(String subject, boolean removeFromACModel, boolean notifyListeners){
		if(!super.removeSubject(subject, false))
			return false;
		
		if(acModel != null && removeFromACModel && acModel.getContext() != this){
			acModel.getContext().removeSubject(subject);
		}
		if(notifyListeners){
			contextListenerSupport.notifySubjectRemoved(subject);
		}
		return true;
	}
	
	
	//------- Attributes ------------------------------------------------------------
	
//	@Override
//	public void setObjects(Collection<String> objects) {
//		throw new UnsupportedOperationException();
//	}
//	@Override
//	public void removeObjects() {
//		throw new UnsupportedOperationException();
//	}
//	@Override
//	public void removeObjects(Collection<String> objects) {
//		throw new UnsupportedOperationException();
//	}
//	@Override
//	public boolean removeObject(String object) {
//		throw new UnsupportedOperationException();
//	}
//	@Override
//	public Set<String> getObjects() {
//		throw new UnsupportedOperationException();
//	}
//	@Override
//	public void addObjects(Collection<String> objects) {
//		throw new UnsupportedOperationException();
//	}
//	@Override
//	public boolean addObject(String object) {
//		throw new UnsupportedOperationException();
//	}
//	@Override
//	protected boolean addObject(String object, boolean notifyListeners) {
//		throw new UnsupportedOperationException();
//	}

	/**
	 * Checks if this context contains attributes.
	 */
	public boolean containsAttributes(){
		return containsObjects();
	}
	
	/**
	 * Sets the context attributes.<br>
	 * When context attributes are set, data usage is also reset.
	 * @param attributes A list of attribute names.
	 * @see #clearAttributes()
	 */
	public void setAttributes(Set<String> attributes){
		setObjects(attributes);
	}
	
	/**
	 * Resets the context attributes.<br>
	 * This method clears the attribute list and also the maps for data usage.
	 */
	public void removeAttributes(){
		removeObjects();
	}
	
	public void removeAttributes(Collection<String> attributes){
		removeAttributes(attributes, false);
	}
	
	public void removeAttributes(Collection<String> attributes, boolean removeFromACModel){
		removeAttributes(attributes, removeFromACModel, true);
	}
	
	public void removeAttributes(Collection<String> attributes, boolean removeFromACModel, boolean notifyListeners){
		Validate.notNull(attributes);
		for(String attribute: attributes)
			removeAttribute(attribute, removeFromACModel, notifyListeners);
	}
	
	public boolean removeAttribute(String attribute){
		return removeAttribute(attribute, false);
	}
	
	/**
	 * Removes the given attribute from the context.
	 */
	public boolean removeAttribute(String attribute, boolean removeFromACModel){
		return removeAttribute(attribute, removeFromACModel, true);
	}
	
	/**
	 * Removes the given attribute from the context.
	 */
	public boolean removeAttribute(String attribute, boolean removeFromACModel, boolean notifyListeners){
		if(!super.removeObject(attribute, false))
			return false;
		
		if(acModel != null && removeFromACModel && acModel.getContext() != this){
			acModel.getContext().removeObject(attribute);
		}
		
		// Remove data usage of removed attributes
		for(String activity: activities){
			removeDataUsageFor(activity, attribute);
		}
		if(notifyListeners){
			contextListenerSupport.notifyObjectRemoved(attribute);
		}
		return true;
	}

	/**
	 * Returns the names of context attributes.
	 * @return An unmodifiable list of attribute names.
	 */
	public Set<String> getAttributes(){
		return super.getObjects();
	}
	
	public void addAttributes(Collection<String> attributes) {
		addAttributes(attributes, false);
	}
	
	public void addAttributes(Collection<String> attributes, boolean addToACModel){
		addAttributes(attributes, addToACModel, true);
	}
	
	public void addAttributes(Collection<String> attributes, boolean addToACModel, boolean notifyListeners) throws InconsistencyException{
		for(String attribute: attributes)
			addAttribute(attribute, addToACModel, notifyListeners);
	}
	
	public boolean addAttribute(String attribute) throws InconsistencyException{
		return addAttribute(attribute, false);
	}
	
	public boolean addAttribute(String attribute, boolean addToACModel){
		return addAttribute(attribute, addToACModel, true);
	}
	
	public boolean addAttribute(String attribute, boolean addToACModel, boolean notifyListeners) throws InconsistencyException{
		if(!super.addObject(attribute, false))
		
		if(acModel != null && acModel.getContext() != this){
			if(!addToACModel){
				// Check if new activities cause an inconsistency
				if(!getACModel().getContext().containsObject(attribute)){
					super.removeObject(attribute, false);
					throw new InconsistencyException("Incompatible access control model: Missing " + getObjectDescriptorSingular() + ": " + attribute);
				}
			} else {
				acModel.getContext().addObject(attribute);
			}
		}
		if(notifyListeners){
			contextListenerSupport.notifyObjectAdded(attribute);
		}
		return true;
	}
	

	//-------- AC-Model -----------------------------------------------------------------------
	
	/**
	 * Returns the access control model which is used to determine
	 * authorized subjects for activity execution.
	 * @return The access control model of the context.
	 */
	public AbstractACModel getACModel(){
		return acModel;
	}
	
	/**
	 * Sets the access control model which is used to determine
	 * authorized subjects for activity execution.<br>
	 * This method checks, if the access control model is compatible with the log context,
	 * i.e. is contains all subjects and activities of the log context.
	 * 
	 * @param acModel An access control model.
	 */
	public void setACModel(AbstractACModel acModel) {
		setACModel(acModel, true);
	}
	
	/**
	 * Sets the access control model which is used to determine
	 * authorized subjects for activity execution.<br>
	 * This method checks, if the access control model is compatible with the log context,
	 * i.e. is contains all subjects and activities of the log context.
	 * 
	 * @param acModel An access control model.
	 */
	public void setACModel(AbstractACModel acModel, boolean notifyListeners) {
		Validate.notNull(acModel);
		if(this.acModel == acModel)
			return;
		validateACModel(acModel);
		this.acModel = acModel;
		acModel.getContext().addContextListener(this);
		if(notifyListeners){
			duContextListenerSupport.notifyACModelSet(acModel);
		}
	}
	
	public void removeACModel(){
		removeACModel(true);
	}
	
	public void removeACModel(boolean notifyListeners){
		this.acModel.getContext().removeContextListener(this);
		this.acModel = null;
		if(notifyListeners){
			duContextListenerSupport.notifyACModelRemoved();
		}
	}
	
	public boolean isCompatible(AbstractACModel acModel){
		try {
			validateACModel(acModel);
			return true;
		} catch (Exception e) {
			return false;
		} 
	}
	
	
	//------- Valid Usage Modes -----------------------------------------------------------------------------------------
	
	public List<DataUsage> getValidUsageModes(){
		return Collections.unmodifiableList(validUsageModes);
	}
	
	public void setValidUsageModes(Collection<DataUsage> validUsageModes){
		setValidUsageModes(validUsageModes, true);
	}
	
	@SuppressWarnings("unchecked")
	public void setValidUsageModes(Collection<DataUsage> validUsageModes, boolean notifyListeners){
		Validate.notNull(validUsageModes);
		Validate.notEmpty(validUsageModes);
		Validate.noNullElements(validUsageModes);
		if(getValidUsageModes().equals(validUsageModes))
			return;
		
		if(acModel != null){
			if(!SetUtils.containSameElements(new HashSet<DataUsage>(validUsageModes), new HashSet<DataUsage>(acModel.getValidUsageModes())))
				throw new ParameterException(ErrorCode.INCONSISTENCY, "Existing object permissions are in conflict with new set of valid usage modes.");
		}
		if(!activityDataUsage.isEmpty()){
			for(String activity: activities){
				if(activityDataUsage.containsKey(activity)){
					for(String attribute: getAttributes()){
						if(activityDataUsage.get(activity).containsKey(attribute)){
							if(activityDataUsage.get(activity).get(attribute) != null){
								if(!validUsageModes.containsAll(activityDataUsage.get(activity).get(attribute)))
									throw new ParameterException(ErrorCode.INCONSISTENCY, "Existing activity data usages are in conflict with new set of valid usage modes.");
							}	
						}
					}
				}
			}
		}
		this.validUsageModes.clear();
		this.validUsageModes.addAll(validUsageModes);
		if(notifyListeners){
			duContextListenerSupport.notifyValidUsageModesChange(validUsageModes);
		}
	}
	
	//------- Data Usge -------------------------------------------------------------------------------------------------
	
	/**
	 * Sets the data attributes used by the given activity.<br>
	 * The given activity/attributes have to be known by the context, i.e. be contained in the activity/attribute list.
	 * 
	 * @param activity Activity for which the attribute usage is set.
	 * @param attributes Attributes used as input by the given activity.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws IllegalArgumentException If the given activity/attributes are not known.
	 * @throws NullPointerException If the attribute set is <code>null</code>.
	 * @see {@link #getActivities()}
	 * @see #getAttributes()
	 * @see #setAttributes(List)
	 */
	public void setDataFor(String activity, Set<String> attributes) throws CompatibilityException{
		validateActivity(activity);
		validateAttributes(attributes);
		if(!attributes.isEmpty()){
			Map<String, Set<DataUsage>> dataUsage = new HashMap<String, Set<DataUsage>>();
			for(String attribute: attributes){
				dataUsage.put(attribute, new HashSet<DataUsage>(validUsageModes));
			}
			activityDataUsage.put(activity, dataUsage);
		}
	}
	
	
	/**
	 * Sets the data attributes used by the given activity together with their usage.<br>
	 * The given activity/attributes have to be known by the context, i.e. be contained in the activity/attribute list.
	 * 
	 * @param activity Activity for which the attribute usage is set.
	 * @param dataUsage Data usage of input attributes.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws IllegalArgumentException If the given activity/attributes are not known.
	 * @throws NullPointerException If the data usage set is <code>null</code> 
	 * @see {@link DataUsage}
	 * @see #getActivities()
	 * @see #getAttributes()
	 * @see #setAttributes(List)
	 */
	public void setDataUsageFor(String activity, Map<String, Set<DataUsage>> dataUsage) throws CompatibilityException{
		validateActivity(activity);
		validateDataUsage(dataUsage);
		activityDataUsage.put(activity, dataUsage);
	}
	
	public void setDataUsageFor(String activity, String attribute, Set<DataUsage> usageModes) throws CompatibilityException{
		validateActivity(activity);
		validateAttribute(attribute);
		validateUsageModes(usageModes);
		if(!activityDataUsage.containsKey(activity)){
			activityDataUsage.put(activity, new HashMap<String, Set<DataUsage>>());
		}
		activityDataUsage.get(activity).put(attribute, usageModes);
	}
	
	/**
	 * Adds a data attribute for an activity.<br>
	 * The given activity/attributes have to be known by the context, i.e. be contained in the activity/attribute list.
	 * 
	 * @param activity Activity for which the attribute usage is set.
	 * @param attribute Attribute used by the given activity.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws IllegalArgumentException If the given activity/attributes are not known.
	 * @see {@link #getActivities()}
	 * @see #getAttributes()
	 * @see #setAttributes(List)
	 */
	public void addDataUsageFor(String activity, String attribute) throws CompatibilityException{
		setDataUsageFor(activity, attribute, new HashSet<DataUsage>(validUsageModes));
	}
	
	/**
	 * Adds a data attribute for an activity together with its usage.<br>
	 * The given activity/attributes have to be known by the context, i.e. be contained in the activity/attribute list.<br>
	 * When dataUsage is null, then no usage is added.
	 * 
	 * @param activity Activity for which the attribute usage is set.
	 * @param attribute Attribute used by the given activity.
	 * @param dataUsage Usage of the data attribute by the given activity.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws IllegalArgumentException IllegalArgumentException If the given activity/attributes are not known.
	 * @see {@link #getActivities()}
	 * @see #getAttributes()
	 * @see #setAttributes(List)
	 */
	public void addDataUsageFor(String activity, String attribute, DataUsage dataUsage) throws CompatibilityException{
		validateActivity(activity);
		validateAttribute(attribute);
		Validate.notNull(dataUsage);
		if(activityDataUsage.get(activity) == null){
			activityDataUsage.put(activity, new HashMap<String, Set<DataUsage>>());
		}
		if(activityDataUsage.get(activity).get(attribute)==null){
			activityDataUsage.get(activity).put(attribute, new HashSet<DataUsage>());
		}
		activityDataUsage.get(activity).get(attribute).add(dataUsage);
	}
	
	public void removeDataUsageFor(String activity, String attribute, DataUsage dataUsage) throws CompatibilityException{
		validateActivity(activity);
		validateAttribute(attribute);
		Validate.notNull(dataUsage);
		if(!activityDataUsage.containsKey(activity))
			return;
		if(!activityDataUsage.get(activity).containsKey(attribute))
			return;
		activityDataUsage.get(activity).get(attribute).remove(dataUsage);
	}
	
	public void removeDataUsageFor(String activity, String attribute) throws CompatibilityException{
		validateActivity(activity);
		validateAttribute(attribute);
		if(!activityDataUsage.containsKey(activity))
			return;
		activityDataUsage.get(activity).remove(attribute);
	}
	
	
	/**
	 * Adds a data attribute for all given activities together with its usage.<br>
	 * The given activities/attributes have to be known by the context, i.e. be contained in the activity/attribute list.
	 * 
	 * @param activities Activities for which the attribute usage is set.
	 * @param attribute Attribute used by the given activities.
	 * @param dataUsage Usage of the data attribute by the given activities.
	 * @throws ParameterException 
	 * @throws IllegalArgumentException IllegalArgumentException If the given activities/attributes are not known.
	 * @see {@link #getActivities()}
	 * @see #getAttributes()
	 * @see #setAttributes(List)
	 */
	public void addDataUsageForAll(Collection<String> activities, String attribute, DataUsage dataUsage){
		Validate.notNull(activities);
		Validate.notEmpty(activities);
		for(String activity: activities){
			addDataUsageFor(activity, attribute, dataUsage);
		}
	}
	
	/**
	 * Returns the usage of the given data attribute by the given activity.<br>
	 * If the given attribute is not used as input by the given activity, the returned
	 * set contains no elements.
	 * The given activity/attribute have to be known by the context, i.e. be contained in the activity/attribute list.
	 * 
	 * @param activity Activity for which the usage is requested.
	 * @param attribute Attribute used by the given activity.
	 * @return The usage of the given data attribute by the given activity.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws IllegalArgumentException IllegalArgumentException If the given activity/attribute is not known.
	 * @see {@link #getActivities()}
	 * @see #getAttributes()
	 */
	public Set<DataUsage> getDataUsageFor(String activity, String attribute) throws CompatibilityException{
		validateActivity(activity);
		validateAttribute(attribute);
		if(activityDataUsage.get(activity) == null)
			return new HashSet<DataUsage>(); //No input data elements for this activity
		if(activityDataUsage.get(activity).get(attribute) == null)
			return new HashSet<DataUsage>(); //Attribute not used by the given activity
		return Collections.unmodifiableSet(activityDataUsage.get(activity).get(attribute));
	}
	
	public Set<String> getActivitiesWithDataUsage(){
		return activityDataUsage.keySet();
	}
	
	/**
	 * Returns all attributes of the given activity together with their usage.<br>
	 * If the given attribute has no attributes, the returned set contains no elements.<br>
	 * The given activity has to be known by the context, i.e. be contained in the activity list.
	 * 
	 * @param activity Activity for which the attribute usage is requested.
	 * @return All attributes of the given activity together with their usage.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws IllegalArgumentException IllegalArgumentException If the given activity is not known.
	 * @see {@link #getActivities()}
	 */
	public Map<String, Set<DataUsage>> getDataUsageFor(String activity) throws CompatibilityException{
		validateActivity(activity);
		if(activityDataUsage.get(activity) == null)
			return new HashMap<String, Set<DataUsage>>(); //No input data elements for this activity
		return Collections.unmodifiableMap(activityDataUsage.get(activity));
	}
	
	public boolean hasDataUsage(){
		return !activityDataUsage.isEmpty();
	}
	
	public boolean hasDataUsage(String activity) throws CompatibilityException{
		validateActivity(activity);
		return activityDataUsage.containsKey(activity);
	}
	
	/**
	 * Returns all attributes of the given activity.<br>
	 * If the given attribute has no attributes, the returned set contains no elements.
	 * The given activity has to be known by the context, i.e. be contained in the activity list.
	 * 
	 * @param activity Activity for which the attributes are requested.
	 * @return All attributes of the given activity.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws IllegalArgumentException IllegalArgumentException If the given activity is not known.
	 * @see {@link #getActivities()}
	 */
	public Set<String> getAttributesFor(String activity) throws CompatibilityException{
		validateActivity(activity);
		if(activityDataUsage.get(activity) == null)
			return new HashSet<String>(); //No input data elements for this activity
		return Collections.unmodifiableSet(activityDataUsage.get(activity).keySet());
	}
	
	//------- Authorization -----------------------------------------------------------------------------------------
	
	/**
	 * Checks if the given subject is authorized to execute the given activity.<br>
	 * A subject is authorized for execution, if it is authorized to execute the activity itself,<br>
	 * plus has permission to access all attributes the activity uses in the same modes.<br>
	 * This method delegates the call to the access control model.
	 * 
	 * @param subject The subject in question.
	 * @param activity The name of a process activity.
	 * @return <code>true</code> if the given subject is authorized to execute the given activity;<br>
	 * <code>false</code> otherwise.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws Exception If the activity or subject are not known,<br>
	 * or if the access control model throws an exception.
	 */
	public boolean isAuthorized(String subject, String activity) throws CompatibilityException{
		if(!subjects.contains(subject))
			throw new CompatibilityException("Unknown subject: " + subject);
		validateActivity(activity);
		boolean authorizedForActivity = acModel.isAuthorizedForTransaction(subject, activity);
		if(!authorizedForActivity)
			return false;
		if(hasDataUsage(activity)){
			for(String attribute: activityDataUsage.get(activity).keySet()){
				for(DataUsage usageMode: activityDataUsage.get(activity).get(attribute)){
					if(!acModel.isAuthorizedForObject(subject, attribute, usageMode))
						return false;
				}
			}
		}
		return true;
	}
	
	public Set<String> getAuthorizedSubjects(String activity) throws CompatibilityException{
		validateActivity(activity);
		Set<String> authorizedSubjects = new HashSet<String>();
		for(String subject: getSubjects()){
			if(isAuthorized(subject, activity)){
				authorizedSubjects.add(subject);
			}
		}
		return authorizedSubjects;
	}
	
	/**
	 * Checks if the given activity is executable, i.e. there is at least one subject
	 * which is authorized to execute it.<br>
	 * This method delegates the call to the access control model.
	 * 
	 * @param activity The activity in question.
	 * @return <code>true</code> if the given activity is executable;<br>
	 * <code>false</code> otherwise.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws Exception If the activity is not known,<br>
	 * or if the access control model throws an exception.
	 */
	public boolean isExecutable(String activity) throws CompatibilityException{
		validateActivity(activity);
		return acModel != null && acModel.isExecutable(activity);
	}
	
	//------- Helper methods ----------------------------------------------------------------
	
	
	/**
	 * Checks if the given attribute is known, i.e. is contained in the attribute list.
	 * @param attribute Attribute to be checked.
	 * @throws IllegalArgumentException If the given attribute is not known.
	 */
	public void validateAttribute(String attribute) throws CompatibilityException{
		try{
			super.validateObject(attribute);
		} catch(CompatibilityException e){
			throw new CompatibilityException("Unknown attribute: " + attribute);
		}
	}
	
	/**
	 * Checks if the given attributes are known, i.e. they are all contained in the attribute list.
	 * @param attributes Attributes to be checked.
	 * @throws IllegalArgumentException If not all given attribute are known.
	 */
	public void validateAttributes(Collection<String> attributes) throws CompatibilityException{
		Validate.notNull(attributes);
		for(String attribute: attributes)
			validateAttribute(attribute);
	}
	
	public void validateDataUsage(Map<String, Set<DataUsage>> dataUsage){
		Validate.notNull(dataUsage);
		for(Set<DataUsage> usageModes: dataUsage.values())
			validateUsageModes(usageModes);
	}
	
	protected void validateUsageModes(Collection<DataUsage> usageModes){
		Validate.notNull(usageModes);
		Validate.notEmpty(usageModes);
		Validate.noNullElements(usageModes);
		if(!validUsageModes.containsAll(usageModes))
			throw new ParameterException(ErrorCode.INCOMPATIBILITY, "Invalid usage mode. Permitted values: " + validUsageModes);
	}
	
	public void validateACModel(AbstractACModel acModel) throws InconsistencyException{
		Validate.notNull(acModel);
		if(!SetUtils.containSameElements(new HashSet<DataUsage>(getValidUsageModes()), new HashSet<DataUsage>(acModel.getValidUsageModes())))
			throw new InconsistencyException("Incompatible access control model: Different set of valid data usage modes.");
		if(acModel.getContext() != this){
			for(String thisSubject: getSubjects()){
				if(!acModel.getContext().containsSubject(thisSubject))
					throw new InconsistencyException("Incompatible access control model: Missing " + getSubjectDescriptorSingular() + ": " + thisSubject);
			}
			for(String thisObject: getObjects()){
				if(!acModel.getContext().containsObject(thisObject))
					throw new InconsistencyException("Incompatible access control model: Missing " + getObjectDescriptorSingular() + ": " + thisObject);
			}
			for(String thisActivity: getActivities()){
				if(!acModel.getContext().containsActivity(thisActivity))
					throw new InconsistencyException("Incompatible access control model: Missing " + getActivityDescriptorSingular() + ": " + thisActivity);
			}
		}
	}
	
	
	//------- Static methods ----------------------------------------------------------------
	
	/**
	 * Creates a new context using an RBAC access control model.<br>
	 * Users and permissions to execute transactions are randomly assigned to the given roles.<br>
	 * Each person is assigned to exactly one role.
	 * @param activities The process activities.
	 * @param originatorCount The number of desired originators.
	 * @param roles The roles to use.
	 * @return A new randomly generated Context.
	 * @throws Exception 
	 */
	public static ProcessContext createRandomContext(Set<String> activities, int originatorCount, List<String> roles){
		Validate.notNull(activities);
		Validate.noNullElements(activities);
		Validate.notNegative(originatorCount);
		Validate.notNull(roles);
		Validate.noNullElements(roles);
		
		ProcessContext newContext = new ProcessContext("Random Context");
		newContext.setActivities(activities);
		List<String> cOriginators = createSubjectList(originatorCount);
		newContext.setSubjects(new HashSet<String>(cOriginators));
		//Create a new access control model.
		newContext.setACModel(RBACModel.createRandomModel(cOriginators, activities, roles));
		return newContext;
	}
	
	/**
	 * Creates a list of subject names with the given size.
	 * @param number Number of subjects to create.
	 * @return A list of subject names.
	 * @throws ParameterException 
	 */
	public static List<String> createSubjectList(int number){
		return createSubjectList(number, "%s");
	}
	
	/**
	 * Creates a list of subject names in the given format with the given size.
	 * @param number Number of subjects to create.
	 * @param stringFormat The format for subject names.
	 * @return A list of subject names.
	 * @throws ParameterException 
	 */
	public static List<String> createSubjectList(int number, String stringFormat){
		Validate.notNegative(number);
		Validate.notNull(stringFormat);
		List<String> result = new ArrayList<String>(number);
		for(int i=1; i<=number; i++){
			result.add(String.format(stringFormat, i));
		}
		return result;
	}
	

	
	@Override
	protected void addStringContent(StringBuilder builder) {
		builder.append("      name: ");
		builder.append(getName());
		builder.append('\n');
		
		if(containsActivities()){
			builder.append(getActivityDescriptorPlural().toLowerCase() + ": ");
			builder.append(getActivities());
			builder.append('\n');
		}
		if(containsSubjects()){
			builder.append("  " + getSubjectDescriptorPlural().toLowerCase() + ": ");
			builder.append(getSubjects());
			builder.append('\n');
		}
		if(containsAttributes()){
			builder.append("   " + getObjectDescriptorPlural().toLowerCase() + ": ");
			builder.append(getAttributes());
			builder.append('\n');
		}
		
		if(hasDataUsage()){
			builder.append('\n');
			builder.append(getActivityDescriptorSingular().toLowerCase());
			builder.append(" data usage:");
			builder.append('\n');
			for(String activity: activityDataUsage.keySet()){
				builder.append(activity);
				builder.append(": ");
				builder.append(activityDataUsage.get(activity));
				builder.append('\n');
			}
		}
		
		if(getACModel() != null){
			builder.append('\n');
			builder.append(getActivityDescriptorSingular().toLowerCase());
			builder.append(" permissions:");
			builder.append('\n');
			for(String activity: getActivities()){
				builder.append(activity);
				builder.append(": ");
				try {
					builder.append(getACModel().getAuthorizedSubjectsForTransaction(activity));
				} catch (CompatibilityException e) {
					e.printStackTrace();
				}
				builder.append('\n');
			}
			

			builder.append('\n');
			builder.append(getObjectDescriptorSingular().toLowerCase());
			builder.append(" permissions:");
			builder.append('\n');
			for(String attribute: getAttributes()){
				builder.append(attribute);
				builder.append(": ");
				try {
					Map<String, Set<DataUsage>> subjectsAndPermissions = getACModel().getAuthorizedSubjectsAndPermissionsForObject(attribute);
					if(!subjectsAndPermissions.isEmpty()){
						builder.append('[');
						for(String subject: subjectsAndPermissions.keySet()){
							builder.append(subject);
							builder.append(subjectsAndPermissions.get(subject));
							builder.append(' ');
						}
						builder.append(']');
					}
				} catch (CompatibilityException e) {
					e.printStackTrace();
				}
				builder.append('\n');
			}
			
			builder.append('\n');
			builder.append("execution authorization:");
			builder.append('\n');
			builder.append('\n');
			for(String activity: getActivities()){
				builder.append(activity);
				builder.append(": ");
				try {
					builder.append(getAuthorizedSubjects(activity));
				} catch (CompatibilityException e) {
					e.printStackTrace();
				}
				builder.append('\n');
			}
		}
	}

	public ProcessContextProperties getProperties() throws PropertyException{
		if(!isValid())
			throw new ParameterException(ErrorCode.INCONSISTENCY, "Cannot extract properties in invalid state!");
		
		ProcessContextProperties result = new ProcessContextProperties();
		
		result.setName(getName());
		result.setActivities(getActivities());
		result.setSubjects(getSubjects());
		result.setObjects(getAttributes());
		if(getACModel() != null)
		result.setACModelName(getACModel().getName());
		
		for(String activity: getActivities()){
			Map<String, Set<DataUsage>> dataUsage = getDataUsageFor(activity);
			if(dataUsage != null && !dataUsage.isEmpty()){
				result.setDataUsage(activity, dataUsage);
			}
		}
		return result;
	}
	
	public void takeOverValues(ProcessContext context, boolean notifyListeners) throws Exception{
		super.takeoverValues(context, notifyListeners);
		
		//Set AC Model
		acModel = null;
		AbstractACModel otherACModel = context.getACModel();
		if(otherACModel != null){
			setACModel(otherACModel, notifyListeners);
		}
		
		//Set valid usage modes
		setValidUsageModes(context.getValidUsageModes(), notifyListeners);
		
		//Set data usage
		activityDataUsage.clear();
		for(String activity: context.getActivitiesWithDataUsage()){
			Map<String, Set<DataUsage>> dataUsage = context.getDataUsageFor(activity);
			for(String attribute: dataUsage.keySet()){
				setDataUsageFor(activity, attribute, new HashSet<DataUsage>(dataUsage.get(attribute)));
			}
		}
	}
	
	@Override
	public ProcessContext clone() {
		ProcessContext result = null;
		try{
			result = new ProcessContext(getName());
			result.takeOverValues(this, false);
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
		return result;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((acModel == null) ? 0 : acModel.hashCode());
		result = prime * result + ((activityDataUsage == null) ? 0 : activityDataUsage.hashCode());
		result = prime * result + ((validUsageModes == null) ? 0 : validUsageModes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessContext other = (ProcessContext) obj;
		if (acModel == null) {
			if (other.acModel != null)
				return false;
		} else if (!acModel.equals(other.acModel))
			return false;
		if (activityDataUsage == null) {
			if (other.activityDataUsage != null)
				return false;
		} else if (!activityDataUsage.equals(other.activityDataUsage))
			return false;
		if (validUsageModes == null) {
			if (other.validUsageModes != null)
				return false;
		} else if (!validUsageModes.equals(other.validUsageModes))
			return false;
		return true;
	}

	public static void main(String[] args) throws PropertyException, IOException {
		Map<String, Set<DataUsage>> usage1 = new HashMap<String, Set<DataUsage>>();
		Set<DataUsage> modes1 = new HashSet<DataUsage>(Arrays.asList(DataUsage.READ, DataUsage.WRITE));
		usage1.put("attribute1", modes1);
		
		Map<String, Set<DataUsage>> usage2 = new HashMap<String, Set<DataUsage>>();
		Set<DataUsage> modes2 = new HashSet<DataUsage>(Arrays.asList(DataUsage.READ, DataUsage.CREATE));
		usage2.put("attribute2", modes2);
		
		Set<String> activities = new HashSet<String>(Arrays.asList("act1", "act2"));
		Set<String> attributes = new HashSet<String>(Arrays.asList("attribute1", "attribute2"));
		Set<String> subjects = new HashSet<String>(Arrays.asList("s1", "s2"));
		ProcessContext c = new ProcessContext("c1");
		c.setActivities(activities);
		c.addAttributes(attributes);
		c.addSubjects(subjects);
		c.setDataUsageFor("act1", usage1);
		c.setDataUsageFor("act2", usage2);
		
		ACLModel acModel = new ACLModel("acl1", c);
		acModel.setName("acmodel1");
		acModel.setActivityPermission("s1", activities);
		c.setACModel(acModel);
		
		System.out.println(c);
//		c.getProperties().store("/Users/stocker/Desktop/processContext");
	}

	@Override
	public void nameChanged(String oldName, String newName) {}

	@Override
	public void subjectAdded(String subject) {}

	@Override
	public void subjectRemoved(String subject) {}

	@Override
	public void objectAdded(String object) {}

	@Override
	public void objectRemoved(String object) {}

	@Override
	public void activityAdded(String activities) {}

	@Override
	public void activityRemoved(String activities) {}

	@Override
	public SOABaseChangeReply allowSubjectRemoval(String subject) {
		return new SOABaseChangeReply(this, !containsSubject(subject), subject);
	}

	@Override
	public SOABaseChangeReply allowObjectRemoval(String object) {
		return new SOABaseChangeReply(this, !containsObject(object), object);
	}

	@Override
	public SOABaseChangeReply allowActivityRemoval(String activity) {
		return new SOABaseChangeReply(this, !containsActivity(activity), activity);
	}

	@Override
	public String getListenerDescription() {
		return "context " + getName();
	}

	@Override
	public void showDialog(Window parent) throws Exception {
		ProcessContextDialog.showDialog(parent, this);
	}

	
	
	
	

}
