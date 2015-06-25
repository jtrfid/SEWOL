package de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

public class ACLModelProperties extends ACModelProperties {
	
	private final String SUBJECT_TRANSACTION_PERMISSION_FORMAT = ACLModelProperty.SUBJECT_TRANSACTION_PERMISSION + "_%s";
	private final String SUBJECT_OBJECT_PERMISSION_FORMAT = ACLModelProperty.SUBJECT_OBJECT_PERMISSION + "_%s";
	private final String OBJECT_PERMISSION_FORMAT = ACLModelProperty.OBJECT_PERMISSION + "_%s";
	private final String OBJECT_PERMISSION_VALUE_FORMAT = "\"%s\" %s";
	
	public ACLModelProperties(){
		super();
		setType(ACModelType.ACL);
	}
	
	public ACLModelProperties(ACModelProperties properties) throws PropertyException{
		setName(properties.getName());
		setSubjectDescriptor(properties.getSubjectDescriptor());
		setContextName(properties.getContextName());
		setValidUsageModes(properties.getValidUsageModes());
		setType(ACModelType.ACL);
	}

	public void setActivityPermission(String subject, Collection<String> transactions) {
		validateStringValue(subject);
		if(transactions.isEmpty())
			return;
		validateStringCollection(transactions);
		
		props.setProperty(String.format(SUBJECT_TRANSACTION_PERMISSION_FORMAT, subject), ArrayUtils.toString(encapsulateValues(new HashSet<String>(transactions))));
	}
	
	public Set<String> getActivityPermission(String subject) throws PropertyException {
		validateStringValue(subject);
		Set<String> result = new HashSet<String>();
		String propertyValue = props.getProperty(String.format(SUBJECT_TRANSACTION_PERMISSION_FORMAT, subject));
		if(propertyValue == null)
			return result;
		StringTokenizer subjectTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(subjectTokens.hasMoreTokens()){
			String nextToken = subjectTokens.nextToken();
			if(nextToken.length() < 3)
				throw new PropertyException(ACLModelProperty.SUBJECT_TRANSACTION_PERMISSION, propertyValue);
			result.add(nextToken.substring(1, nextToken.length()-1));
		}
		return result;
	}
	
	public void setObjectPermission(String subject, Map<String, Set<DataUsage>> permissions) throws PropertyException{
		validateStringValue(subject);
		Validate.notNull(permissions);
		if(permissions.isEmpty())
			return;
		validateStringCollection(permissions.keySet());
		Validate.noNullElements(permissions.values());
		
		//1. Add permissions
		//   This also adds the permissions to the list of permissions
		List<String> propertyNamesForPermissions = new ArrayList<String>();
		for(String object: permissions.keySet()){
			propertyNamesForPermissions.add(addObjectPermission(object, permissions.get(object)));
		}
		
		//2. Add permissions for this activity
		props.setProperty(String.format(SUBJECT_OBJECT_PERMISSION_FORMAT, subject), ArrayUtils.toString(propertyNamesForPermissions.toArray()));
	}
	
	private String addObjectPermission(String object, Set<DataUsage> permissions) throws PropertyException{
		Validate.notNull(object);
		Validate.notEmpty(object);
		Validate.notNull(permissions);
		Validate.noNullElements(permissions);
		String permissionName = String.format(OBJECT_PERMISSION_FORMAT, getNextObjectPermissionIndex());
		props.setProperty(permissionName, String.format(OBJECT_PERMISSION_VALUE_FORMAT, object, ArrayUtils.toString(permissions.toArray())));
		addObjectPermissionNameToList(permissionName);
		return permissionName;
	}
	
	private void addObjectPermissionNameToList(String permissionName){
		validateStringValue(permissionName);
		Set<String> currentValues = getObjectPermissionNameList();
		currentValues.add(permissionName);
		props.setProperty(ACLModelProperty.ALL_OBJECT_PERMISSIONS.toString(), ArrayUtils.toString(currentValues.toArray()));
	}
	
	private Set<String> getObjectPermissionNameList(){
		Set<String> result = new HashSet<String>();
		String propertyValue = props.getProperty(ACLModelProperty.ALL_OBJECT_PERMISSIONS.toString());
		if(propertyValue == null)
			return result;
		StringTokenizer attributeTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(attributeTokens.hasMoreTokens()){
			String nextToken = attributeTokens.nextToken();
			result.add(nextToken);
		}
		return result;
	}
	
	private int getNextObjectPermissionIndex() throws PropertyException{
		Set<Integer> usedIndexes = getObjectPermissionNameIndexes();
		int nextIndex = 1;
		while(usedIndexes.contains(nextIndex)){
			nextIndex++;
		}
		return nextIndex;
	}
	
	private Set<Integer> getObjectPermissionNameIndexes() throws PropertyException{
		Set<Integer> result = new HashSet<Integer>();
		Set<String> permissionNames = getObjectPermissionNameList();
		if(permissionNames.isEmpty())
			return result;
		for(String permissionName: permissionNames){
			int separatorIndex = permissionName.lastIndexOf("_");
			if(separatorIndex == -1 || (permissionName.length() == separatorIndex + 1))
				throw new PropertyException(ACLModelProperty.OBJECT_PERMISSION, permissionName, "Corrupted property file (invalid permission name)");
			Integer index = null;
			try {
				index = Integer.parseInt(permissionName.substring(separatorIndex+1));
			} catch(Exception e){
				throw new PropertyException(ACLModelProperty.OBJECT_PERMISSION, permissionName, "Corrupted property file (invalid permission name)");
			}
			result.add(index);
		}
		return result;
	}
	
	public Map<String, Set<DataUsage>> getObjectPermission(String subject) throws PropertyException{
		Validate.notNull(subject);
		Validate.notEmpty(subject);
		
		Set<String> permissionNames = getObjectPermissionNames(subject);
		Map<String, Set<DataUsage>> result = new HashMap<String, Set<DataUsage>>();
		for(String permissionName: permissionNames){
			Map<String, Set<DataUsage>> dataUsage = getPermission(permissionName);
			String object = dataUsage.keySet().iterator().next();
			result.put(object, dataUsage.get(object));
		}
		return result;
	}
	
	private Set<String> getObjectPermissionNames(String activity){
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> result = new HashSet<String>();
		String propertyValue = props.getProperty(String.format(SUBJECT_OBJECT_PERMISSION_FORMAT, activity));
		if(propertyValue == null)
			return result;
		StringTokenizer subjectTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(subjectTokens.hasMoreTokens()){
			String nextToken = subjectTokens.nextToken();
			result.add(nextToken);
		}
		return result;
	}
	
	private Map<String, Set<DataUsage>> getPermission(String permissionName) throws PropertyException{
		String permissionString = props.getProperty(permissionName);
		if(permissionString == null)
			throw new PropertyException(ACLModelProperty.OBJECT_PERMISSION, permissionName, "No permission with name \""+permissionName+"\"");
		Map<String, Set<DataUsage>> result = new HashMap<String, Set<DataUsage>>();
		int delimiterIndex = permissionString.indexOf(" ");
		if(delimiterIndex == -1)
			throw new PropertyException(ACLModelProperty.OBJECT_PERMISSION, permissionName, "Invalid property value for permission with name \""+permissionName+"\"");
		String attributeString = null;
		String dataUsagesString = null;
		try {
			attributeString = permissionString.substring(0, delimiterIndex);
			dataUsagesString = permissionString.substring(delimiterIndex+1);
			
			attributeString = attributeString.substring(1, attributeString.length()-1);
		} catch(Exception e){
			throw new PropertyException(ACLModelProperty.OBJECT_PERMISSION, permissionName, "Invalid property value for permission with name \""+permissionName+"\"");
		}
		
		Set<DataUsage> usageModes = new HashSet<DataUsage>();
		StringTokenizer usageModeTokens = StringUtils.splitArrayString(dataUsagesString, " ");
		while(usageModeTokens.hasMoreTokens()){
			try {
				usageModes.add(DataUsage.parse(usageModeTokens.nextToken()));
			} catch (ParameterException e) {
				throw new PropertyException(ACLModelProperty.OBJECT_PERMISSION, permissionName, "Invalid property value for permission with name \""+permissionName+"\"");
			}
		}
		result.put(attributeString, usageModes);
		
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		
		SOABase c = new SOABase("Context");
		c.setSubjects(new HashSet<String>(Arrays.asList("subj1", "subj2", "subj3")));
		c.setObjects(new HashSet<String>(Arrays.asList("obj1", "obj2", "obj3")));
		c.setActivities(new HashSet<String>(Arrays.asList("t1", "t2", "t3")));
		c.getProperties().store("/Users/stocker/Desktop/Context");
		
		ACLModelProperties p = new ACLModelProperties();
		String name = "acModel1";
		
		p.setContextName(c.getName());
		p.setName(name);
		p.setActivityPermission("subj1", Arrays.asList("t1", "t2"));
		
		Map<String, Set<DataUsage>> object_permission = new HashMap<String, Set<DataUsage>>();
		Set<DataUsage> modes = new HashSet<DataUsage>(Arrays.asList(DataUsage.READ, DataUsage.WRITE));
		object_permission.put("obj1", modes);
		p.setObjectPermission("subj1", object_permission);
		p.store("/Users/stocker/Desktop/ACL");
	}

}
