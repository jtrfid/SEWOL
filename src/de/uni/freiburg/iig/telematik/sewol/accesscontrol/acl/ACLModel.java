package de.uni.freiburg.iig.telematik.sewol.accesscontrol.acl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.types.HashList;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACLModelProperties;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACModelType;

public class ACLModel extends AbstractACModel<ACLModelProperties> {
	
	protected Map<String, Set<String>> activityPermissionsUT;
	protected Map<String, Set<String>> activityPermissionsTU;
	protected Map<String, Map<String, Set<DataUsage>>> objectPermissionsUO;
	protected Map<String, Map<String, Set<DataUsage>>> objectPermissionsOU;

	public ACLModel(String name) {
		super(ACModelType.ACL, name);
	}
	
	public ACLModel(String name, SOABase context) {
		super(ACModelType.ACL, name, context);
	}

	public ACLModel(ACLModelProperties properties, SOABase context) throws PropertyException{
		super(properties, context);
	}

	@Override
	protected void initialize(ACLModelProperties properties) throws PropertyException {
		super.initialize(properties);
		for(String subject: getContext().getSubjects()){
			setActivityPermission(subject, properties.getActivityPermission(subject));
			setObjectPermission(subject, properties.getObjectPermission(subject));
		}
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		activityPermissionsUT = new HashMap<String, Set<String>>();
		activityPermissionsTU = new HashMap<String, Set<String>>();
		objectPermissionsUO = new HashMap<String, Map<String, Set<DataUsage>>>();
		objectPermissionsOU = new HashMap<String, Map<String, Set<DataUsage>>>();
	}
	
	@Override
	public void setValidUsageModes(Collection<DataUsage> validUsageModes){
		validateUsageModes(validUsageModes);
		Set<DataUsage> oldModes = new HashSet<DataUsage>(getValidUsageModes());
		if(hasObjectPermissions()){
			for(String subject: context.getSubjects()){
				for(String object: context.getObjects()){
					if(!validUsageModes.containsAll(objectPermissionsUO.get(subject).get(object)))
						throw new ParameterException(ErrorCode.INCONSISTENCY, "Existing object permissions are in conflict with new set of valid usage modes.");
				}
			}
		}
		this.validUsageModes.clear();
		this.validUsageModes.addAll(validUsageModes);
		acModelListenerSupport.notifyValidUsageModesChanged(oldModes, new HashSet<DataUsage>(validUsageModes));
	}

	public boolean addActivityPermission(String subject, String activity) throws CompatibilityException {
		context.validateSubject(subject);
		context.validateActivity(activity);
		if(!activityPermissionsUT.containsKey(subject)){
			activityPermissionsUT.put(subject, new HashList<String>());
		}
		if(activityPermissionsUT.get(subject).add(activity)){
			addActivityPermissionTU(activity, subject);
			acModelListenerSupport.notifyExecutionPermissionAdded(subject, activity);
			return true;
		}
		return false;
	}
	
	private void addActivityPermissionTU(String activity, String subject){
		if(!activityPermissionsTU.containsKey(activity)){
			activityPermissionsTU.put(activity, new HashList<String>());
		}
		activityPermissionsTU.get(activity).add(subject);
	}
	
	public void setActivityPermission(String subject, Collection<String> activities) throws CompatibilityException{
		context.validateSubject(subject);
		context.validateActivities(activities);
		if(activities.isEmpty())
			return;
		
		if(activityPermissionsUT.containsKey(subject)){
			Set<String> activitiesToRemove = new HashSet<String>();
			for(String storedActivity: activityPermissionsUT.get(subject)){
				if(!activities.contains(storedActivity))
					activitiesToRemove.add(storedActivity);
			}
			for(String activity: activitiesToRemove){
				removeActivityPermission(subject, activity);
			}
		}
		
		for(String activity: activities){
			addActivityPermission(subject, activity);
		}
	}
	
	public void removeActivityPermissions(String subject) {
		if(!activityPermissionsUT.containsKey(subject))
			return;
		for(String activity: activityPermissionsUT.get(subject)){
			if(activityPermissionsTU.containsKey(activity)){
				activityPermissionsTU.get(activity).remove(subject);
				if(activityPermissionsTU.get(activity).isEmpty()){
					activityPermissionsTU.remove(activity);
				}
			}
			acModelListenerSupport.notifyExecutionPermissionRemoved(subject, activity);
		}
		activityPermissionsUT.remove(subject);
	}
	
	public void removeActivityPermission(String subject, String activity){
		if(!activityPermissionsUT.containsKey(subject))
			return;
		if(activityPermissionsUT.get(subject).remove(activity)){
			if (activityPermissionsUT.get(subject).isEmpty()) {
				activityPermissionsUT.remove(subject);
			}
			if (activityPermissionsTU.containsKey(activity)) {
				activityPermissionsTU.get(activity).remove(subject);
				if (activityPermissionsTU.get(activity).isEmpty())
					activityPermissionsTU.remove(activity);
			}
			acModelListenerSupport.notifyExecutionPermissionRemoved(subject, activity);
		}
	}
	
	public boolean addObjectPermission(String subject, String object, DataUsage... dataUsageModes) throws CompatibilityException{
		return addObjectPermission(subject, object, Arrays.asList(dataUsageModes));
	}
	
	public boolean addObjectPermission(String subject, String object, Collection<DataUsage> dataUsageModes) throws CompatibilityException {
		context.validateSubject(subject);
		context.validateObject(object);
		if(!objectPermissionsUO.containsKey(subject)){
			objectPermissionsUO.put(subject, new HashMap<String, Set<DataUsage>>());
		}
		if(!objectPermissionsUO.get(subject).containsKey(object)){
			objectPermissionsUO.get(subject).put(object, new HashSet<DataUsage>());
		}
		Set<DataUsage> addedUsageModes = new HashSet<DataUsage>();
		for(DataUsage dataUsage: dataUsageModes){
			if(objectPermissionsUO.get(subject).get(object).add(dataUsage)){
				addedUsageModes.add(dataUsage);
			}
		}
		if(addedUsageModes.size() > 0){
			addObjectPermissionsOU(object, subject, addedUsageModes);
			acModelListenerSupport.notifyAccessPermissionAdded(subject, object, addedUsageModes);
			return true;
		} else {
			return false;
		}
	}
	
	private void addObjectPermissionsOU(String object, String subject, Collection<DataUsage> dataUsageModes){
		if(!objectPermissionsOU.containsKey(object)){
			objectPermissionsOU.put(object, new HashMap<String, Set<DataUsage>>());
		}
		if(!objectPermissionsOU.get(object).containsKey(subject)){
			objectPermissionsOU.get(object).put(subject, new HashSet<DataUsage>());
		}
		objectPermissionsOU.get(object).get(subject).addAll(dataUsageModes);
	}
	
//	private void setObjectPermissionsOU(String object, String subject, Collection<DataUsage> dataUsageModes){
//		if(!objectPermissionsOU.containsKey(object)){
//			objectPermissionsOU.put(object, new HashMap<String, Set<DataUsage>>());
//		}
//		objectPermissionsOU.get(object).put(subject, new HashSet<DataUsage>(dataUsageModes));
//	}
	
	
	private void removeObjectPermissionsOU(String object, String subject, Collection<DataUsage> dataUsageModes){
		if(!objectPermissionsOU.containsKey(object))
			return;
		if(!objectPermissionsOU.get(object).containsKey(subject))
			return;
		Map<String, Set<DataUsage>> objectPermissions = objectPermissionsOU.get(object);
		if(objectPermissions == null)
			return;
		if(!objectPermissions.containsKey(subject))
			return;
		Set<DataUsage> subjectPermissions = objectPermissions.get(subject);
		if(subjectPermissions == null)
			return;
		subjectPermissions.removeAll(dataUsageModes);
		if(subjectPermissions.isEmpty())
			objectPermissions.remove(subject);
		if(objectPermissions.isEmpty())
			objectPermissionsOU.remove(object);
	}
	
	private void removeObjectPermissionsOU(String object, String subject){
		if(!objectPermissionsOU.containsKey(object))
			return;
		if(!objectPermissionsOU.get(object).containsKey(subject))
			return;
		Map<String, Set<DataUsage>> objectPermissions = objectPermissionsOU.get(object);
		if(objectPermissions == null)
			return;
		objectPermissions.remove(subject);
		if(objectPermissions.isEmpty())
			objectPermissionsOU.remove(object);
	}
	
	private void removeObjectPermissionsOU(String subject){
		Set<String> objectsToRemove = new HashSet<String>();
		for(String object: objectPermissionsOU.keySet()){
			objectPermissionsOU.get(object).remove(subject);
			if(objectPermissionsOU.get(object).isEmpty())
				objectsToRemove.add(object);
		}
		for(String object: objectsToRemove)
			objectPermissionsOU.remove(object);
	}
	
	public void setObjectPermission(String subject, String object, DataUsage... dataUsageModes) throws CompatibilityException{
		setObjectPermission(subject, object, Arrays.asList(dataUsageModes));
	}
	
	public void setObjectPermission(String subject, Collection<String> objects) throws CompatibilityException{
		for(String object: objects){
			setObjectPermission(subject, object);
		}
	}
	
	public void setObjectPermission(String subject, String object) throws CompatibilityException{
		setObjectPermission(subject, object, getValidUsageModes());
	}
	
	public void setObjectPermission(String subject, String object, Collection<DataUsage> dataUsageModes) throws CompatibilityException{
		context.validateSubject(subject);
		context.validateObject(object);
		validateUsageModes(dataUsageModes);
		if(!objectPermissionsUO.containsKey(subject)){
			objectPermissionsUO.put(subject, new HashMap<String, Set<DataUsage>>());
		}
		
		if(!objectPermissionsUO.get(subject).containsKey(object)){
			Set<DataUsage> modesToRemove = new HashSet<DataUsage>();
			for(DataUsage storedUsage: objectPermissionsUO.get(subject).get(object)){
				if(!dataUsageModes.contains(storedUsage))
					modesToRemove.add(storedUsage);
			}
			removeObjectPermissions(subject, object, modesToRemove);
		}
		
		Set<DataUsage> modesToAdd = new HashSet<DataUsage>(dataUsageModes);
		modesToAdd.removeAll(objectPermissionsUO.get(subject).get(object));
		
		addObjectPermission(subject, object, modesToAdd);
//		setObjectPermissionsOU(object, subject, dataUsageModes);
	}
	
	public void setObjectPermission(String subject, Map<String, Set<DataUsage>> permissions) throws CompatibilityException{
		context.validateSubject(subject);
		validateObjectPermissions(permissions);
		if(permissions.isEmpty())
			return;
		
		for(String object: permissions.keySet()){
			setObjectPermission(subject, object, permissions.get(object));
		}
//		Set<String> old_authorized_objects = new HashSet<String>(getAuthorizedObjectsForSubject(subject));
//		old_authorized_objects.removeAll(permissions.keySet());
//		for(String obj: old_authorized_objects){
//			removeObjectPermissionsOU(obj, subject);
//		}
//		objectPermissionsUO.put(subject, permissions);
//		for(String object: permissions.keySet()){
//			setObjectPermissionsOU(object, subject, permissions.get(object));
//		}
	}
	
	public void removeObjectPermissions(String subject) {
		if(!objectPermissionsUO.containsKey(subject))
			return;
		
		for(String object: objectPermissionsUO.get(subject).keySet()){
			acModelListenerSupport.notifyAccessPermissionRemoved(subject, object, objectPermissionsUO.get(subject).get(object));
		}
		objectPermissionsUO.remove(subject);
		removeObjectPermissionsOU(subject);
	}
	
	public void removeObjectPermissions(String subject, String object){
		if(!objectPermissionsUO.containsKey(subject))
			return;
		if(objectPermissionsUO.get(subject) == null)
			return;
		if(!objectPermissionsUO.get(subject).containsKey(object))
			return;
		Set<DataUsage> permissions = objectPermissionsUO.get(subject).get(object);
		if(permissions == null)
			return;
		if(permissions.isEmpty()){
			objectPermissionsUO.get(subject).remove(object);
			return;
		}
		if(objectPermissionsUO.get(subject).remove(object) != null){
			acModelListenerSupport.notifyAccessPermissionRemoved(subject, object, permissions);
		}
		if(objectPermissionsUO.get(subject).isEmpty()){
			objectPermissionsUO.remove(subject);
		}	
		removeObjectPermissionsOU(object, subject);
	}
	
	public void removeObjectPermissions(String subject, String object, DataUsage... dataUsages){
		removeObjectPermissions(subject, object, Arrays.asList(dataUsages));
	}
	
	public void removeObjectPermissions(String subject, String object, Collection<DataUsage> dataUsageModes){
		if(!objectPermissionsUO.containsKey(subject))
			return;
		if(!objectPermissionsUO.get(subject).containsKey(object))
			return;
		Set<DataUsage> permissions = objectPermissionsUO.get(subject).get(object);
		permissions.removeAll(dataUsageModes);
		Set<DataUsage> removedPermissions = new HashSet<DataUsage>(objectPermissionsUO.get(subject).get(object));
		removedPermissions.retainAll(dataUsageModes);
		acModelListenerSupport.notifyAccessPermissionRemoved(subject, object, removedPermissions);
		if(permissions.isEmpty())
			objectPermissionsUO.get(subject).remove(object);
		if(objectPermissionsUO.get(subject).isEmpty())
			objectPermissionsUO.remove(subject);
		removeObjectPermissionsOU(object, subject, dataUsageModes);
	}
	
	//------- Inherited methods ----------------------------------------------------------

	@Override
	public boolean isAuthorizedForTransaction(String subject, String activity) throws CompatibilityException {
		context.validateSubject(subject);
		context.validateActivity(activity);
		if(!activityPermissionsUT.containsKey(subject))
			return false;
		return activityPermissionsUT.get(subject).contains(activity);
	}

	@Override
	public boolean isAuthorizedForObject(String subject, String object) throws CompatibilityException {
		context.validateSubject(subject);
		context.validateObject(object);
		if(!objectPermissionsUO.containsKey(subject))
			return false;
		return objectPermissionsUO.get(subject).containsKey(object);
	}
	
	@Override
	public boolean isAuthorizedForObject(String subject, String object, DataUsage dataUsageMode) throws CompatibilityException {
		if(!isAuthorizedForObject(subject, object))
			return false;
		return objectPermissionsUO.get(subject).get(object).contains(dataUsageMode);
	}

	@Override
	public List<String> getAuthorizedSubjectsForTransaction(String activity) throws CompatibilityException {
		context.validateActivity(activity);
		List<String> authorizedSubjects = new HashList<String>();
		if(activityPermissionsTU.containsKey(activity)){
			authorizedSubjects.addAll(activityPermissionsTU.get(activity));
		}
		return authorizedSubjects;
	}

	@Override
	public List<String> getAuthorizedSubjectsForObject(String object) throws CompatibilityException {
		context.validateObject(object);
		List<String> authorizedSubjects = new HashList<String>();
		if(objectPermissionsOU.containsKey(object)){
			authorizedSubjects.addAll(objectPermissionsOU.get(object).keySet());
		}
		return authorizedSubjects;
	}
	
	@Override
	public Map<String, Set<DataUsage>> getAuthorizedSubjectsAndPermissionsForObject(String object) throws CompatibilityException {
		context.validateObject(object);
		if(objectPermissionsOU.containsKey(object)){
			return objectPermissionsOU.get(object);
		}
		return new HashMap<String, Set<DataUsage>>();
	}
	
	@Override
	public Set<DataUsage> getObjectPermissionsForSubject(String subject, String object) throws CompatibilityException{
		Set<DataUsage> result = new HashSet<DataUsage>();
		if(!isAuthorizedForObject(subject, object))
			return result;
		return objectPermissionsUO.get(subject).get(object);
	}
	
	@Override
	public Map<String, Set<DataUsage>> getObjectPermissionsForSubject(String subject) throws CompatibilityException{
		Map<String, Set<DataUsage>> result = objectPermissionsUO.get(subject);
		if(result != null)
			return result;
		
		return new HashMap<String, Set<DataUsage>>();
	}
	
	@Override
	public Set<String> getTransactionPermissionsForSubject(String subject) throws CompatibilityException{
		Set<String> result = activityPermissionsUT.get(subject);
		if(result != null)
			return result;
		
		return new HashSet<String>();
	}

	@Override
	public List<String> getAuthorizedTransactionsForSubject(String subject) throws CompatibilityException {
		context.validateSubject(subject);
		List<String> authorizedTransactions = new HashList<String>();
		if(activityPermissionsUT.containsKey(subject)){
			authorizedTransactions.addAll(activityPermissionsUT.get(subject));
		}
		return authorizedTransactions;
	}

	@Override
	public List<String> getAuthorizedObjectsForSubject(String subject) throws CompatibilityException {
		context.validateSubject(subject);
		List<String> authorizedObjects = new HashList<String>();
		if(objectPermissionsUO.containsKey(subject)){
			authorizedObjects.addAll(objectPermissionsUO.get(subject).keySet());
		}
		return authorizedObjects;
	}
	
	@Override
	public boolean hasTransactionPermissions() {
		return !activityPermissionsTU.isEmpty();
	}

	@Override
	public boolean hasObjectPermissions() {
		return !objectPermissionsOU.isEmpty();
	}
	
	@Override
	public ACLModelProperties getProperties() throws PropertyException {
		ACLModelProperties result = super.getProperties();
		for(String subject: context.getSubjects()){
			result.setObjectPermission(subject, getObjectPermissionsForSubject(subject));
			result.setActivityPermission(subject, getTransactionPermissionsForSubject(subject));
		}
		return result;
	}

	@Override
	protected ACLModelProperties createNewProperties() {
		return new ACLModelProperties();
	}

	@Override
	public void subjectRemoved(String subject) {
		removeObjectPermissions(subject);
		removeActivityPermissions(subject);
	}

	@Override
	public void objectRemoved(String object) {
		Map<String, Set<DataUsage>> removedSubjectPermissions = objectPermissionsOU.remove(object);
		if(removedSubjectPermissions != null){
			for(String subject: removedSubjectPermissions.keySet()){
				removeObjectPermissions(subject, object);
			}
		}
	}

	@Override
	public void activityRemoved(String transaction) {
		Set<String> removedSubjects = activityPermissionsTU.remove(transaction);
		if(removedSubjects != null){
			for(String subject: removedSubjects){
				removeActivityPermission(subject, transaction);
			}
		}
	}

	@Override
	public void validUsageModesChanged(AbstractACModel<?> sender, Set<DataUsage> oldModes, Set<DataUsage> newModes) {
		Set<DataUsage> removedModes = new HashSet<DataUsage>(oldModes);
		removedModes.removeAll(newModes);
		for(String subject: context.getSubjects()){
			for(String object: context.getObjects()){
				removeObjectPermissions(subject, object, removedModes);
			}
		}
	}
	
	@Override
	public void checkContextChange(SOABase context) {
		for(String subject: activityPermissionsUT.keySet()){
			if(!context.getSubjects().contains(subject)){
				throw new ParameterException("ACModel contains " + context.getActivityDescriptorSingular() + " permissions for " + context.getSubjectDescriptorSingular() + " " + subject);
			}
		}
		for(String subject: objectPermissionsUO.keySet()){
			if(!context.getSubjects().contains(subject)){
				throw new ParameterException("ACModel contains " + context.getObjectDescriptorSingular() + " permissions for " + context.getSubjectDescriptorSingular() + " " + subject);
			}
		}
		for(String object: objectPermissionsOU.keySet()){
			if(!context.containsObject(object)){
				throw new ParameterException("ACModel contains permissions for " + context.getObjectDescriptorSingular() + " " + object);
			}
		}
		for(String activity: activityPermissionsTU.keySet()){
			if(!context.containsActivity(activity)){
				throw new ParameterException("ACModel contains permissions for " + context.getActivityDescriptorSingular() + " " + activity);
			}
		}
	}
	
	@Override
	protected void contextChangeProcedure() {}
	
	@Override
	public void nameChanged(String oldName, String newName) {}
	
	@Override
	public ACLModel clone(){
		try {
			return new ACLModel(getProperties(), getContext());
		} catch (PropertyException e) {
			return null;
		}
	}
	

	@Override
	public void takeoverValues(AbstractACModel<ACLModelProperties> other) throws Exception{
		resetPermissions();
		initialize(other.getProperties());
	}
	
	@Override
	public void resetPermissions() {
		activityPermissionsTU.clear();
		activityPermissionsUT.clear();
		objectPermissionsOU.clear();
		objectPermissionsUO.clear();
	}

	public static void main(String[] args) throws Exception {
		SOABase c = new SOABase("Context");
		c.setSubjects(Arrays.asList("U1","U2","U3","U4"));
		c.setObjects(Arrays.asList("O1","O2","O3","O4"));
		c.setActivities(Arrays.asList("T1","T2","T3","T4"));
		
		ACLModel acl = new ACLModel("acl1", c);
		acl.setActivityPermission("U1", Arrays.asList("T1","T2"));
		acl.setActivityPermission("U2", Arrays.asList("T3","T2"));
		acl.addObjectPermission("U1", "O2", DataUsage.CREATE, DataUsage.READ);
		acl.addObjectPermission("U2", "O3", DataUsage.WRITE, DataUsage.READ);
		acl.addObjectPermission("U2", "O4", DataUsage.CREATE, DataUsage.DELETE);
		System.out.println(acl);
		
		ACLModelProperties props = acl.getProperties();
		props.store("/Users/stocker/Desktop/acl_test");
		
		ACLModelProperties props2 = new ACLModelProperties();
		props2.load("/Users/stocker/Desktop/acl_test");
//		System.out.println(new ACLModel(props));
//		acl.removeObject("O2");
//		System.out.println(acl);
		
	}

}
