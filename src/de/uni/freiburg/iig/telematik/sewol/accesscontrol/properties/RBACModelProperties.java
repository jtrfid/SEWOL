package de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.RoleRelation;


public class RBACModelProperties extends ACLModelProperties{
	
	private final String ROLE_MEMBERSHIP_FORMAT = RBACModelProperty.ROLE_MEMBERSHIP + "_%s";
	private final String ROLE_RELATION_FORMAT = RBACModelProperty.ROLE_RELATION + "_%s";
	private final String ROLE_RELATION_VALUE_FORMAT = "%s->%s";
	
	public RBACModelProperties() {
		super();
		setType(ACModelType.RBAC);
	}
	
	public RBACModelProperties(ACModelProperties properties) throws PropertyException{
		setName(properties.getName());
		setSubjectDescriptor(properties.getSubjectDescriptor());
		setContextName(properties.getContextName());
		setValidUsageModes(properties.getValidUsageModes());
		setType(ACModelType.RBAC);
	}
	
	//-- Rights propagation
	
	public void setRightsPropagation(Boolean propagation){
		Validate.notNull(propagation);
		props.setProperty(RBACModelProperty.RIGHTS_PROPAGATION.toString(), propagation.toString());
	}
	
	public Boolean getRightsPropagation() throws PropertyException {
		String propertyValue = props.getProperty(RBACModelProperty.RIGHTS_PROPAGATION.toString());
		if(propertyValue == null)
			throw new PropertyException(RBACModelProperty.RIGHTS_PROPAGATION, propertyValue);
		
		Boolean result = null;
		try {
			result = Boolean.parseBoolean(propertyValue);
		} catch(Exception e){
			throw new PropertyException(RBACModelProperty.RIGHTS_PROPAGATION, propertyValue, "Invalid value for rights propagation.");
		}
		
		return result;
	}
	
	//-- Roles
	
	public void setRoles(Set<String> roles){
		validateStringCollection(roles);
		props.setProperty(RBACModelProperty.ROLES.toString(), ArrayUtils.toString(encapsulateValues(roles)));
	}
	
	public Set<String> getRoles(){
		Set<String> result = new HashSet<String>();
		String propertyValue = props.getProperty(RBACModelProperty.ROLES.toString());
		if(propertyValue == null)
			return result;
		StringTokenizer subjectTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(subjectTokens.hasMoreTokens()){
			String nextToken = subjectTokens.nextToken();
			result.add(nextToken.substring(1, nextToken.length()-1));
		}
		return result;
	}
	
	//-- Role relations
	
	public void addRoleRelation(String roleFrom, String roleTo) throws PropertyException{
		validateStringValue(roleFrom);
		validateStringValue(roleTo);
		
		String relationName = String.format(ROLE_RELATION_FORMAT, getNextRelationIndex());
		props.setProperty(relationName, String.format(ROLE_RELATION_VALUE_FORMAT, roleFrom, roleTo));
		addRelationNameToList(relationName);
	}
	
	private void addRelationNameToList(String relationName){
		validateStringValue(relationName);
		Set<String> currentValues = getRelationNameList();
		currentValues.add(relationName);
		props.setProperty(RBACModelProperty.ALL_ROLE_RELATIONS.toString(), ArrayUtils.toString(currentValues.toArray()));
	}
	
	private int getNextRelationIndex() throws PropertyException{
		Set<Integer> usedIndexes = getRelationNameIndexes();
		int nextIndex = 1;
		while(usedIndexes.contains(nextIndex)){
			nextIndex++;
		}
		return nextIndex;
	}
	
	private Set<Integer> getRelationNameIndexes() throws PropertyException{
		Set<Integer> result = new HashSet<Integer>();
		Set<String> relationNames = getRelationNameList();
		if(relationNames.isEmpty())
			return result;
		for(String relationName: relationNames){
			int separatorIndex = relationName.lastIndexOf("_");
			if(separatorIndex == -1 || (relationName.length() == separatorIndex + 1))
				throw new PropertyException(RBACModelProperty.ROLE_RELATION, relationName, "Corrupted property file (invalid role relation name)");
			Integer index = null;
			try {
				index = Integer.parseInt(relationName.substring(separatorIndex+1));
			} catch(Exception e){
				throw new PropertyException(RBACModelProperty.ROLE_RELATION, relationName, "Corrupted property file (invalid role relation name)");
			}
			result.add(index);
		}
		return result;
	}
	
	private Set<String> getRelationNameList(){
		Set<String> result = new HashSet<String>();
		String propertyValue = props.getProperty(RBACModelProperty.ALL_ROLE_RELATIONS.toString());
		if(propertyValue == null)
			return result;
		StringTokenizer attributeTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(attributeTokens.hasMoreTokens()){
			String nextToken = attributeTokens.nextToken();
			result.add(nextToken);
		}
		return result;
	}
	
	public Set<RoleRelation> getRoleRelations() throws PropertyException{
		Set<String> relationNames = getRelationNameList();
		Set<RoleRelation> result = new HashSet<RoleRelation>();
		for(String relationName: relationNames){
			String propertyValue = props.getProperty(relationName);
			if(propertyValue == null)
				throw new PropertyException(RBACModelProperty.ROLE_RELATION, propertyValue, "Cannot extract role relation property \""+propertyValue+"\"");
			
			int separatorIndex = propertyValue.lastIndexOf("->");
			if(separatorIndex == -1 || (relationName.length() == separatorIndex + 1))
				throw new PropertyException(RBACModelProperty.ROLE_RELATION, relationName, "Corrupted property file (invalid role relation value)");
			
			String dominatingRole = null;
			String dominatedRole = null;
			try {
				dominatingRole = propertyValue.substring(0, separatorIndex);
				dominatedRole = propertyValue.substring(separatorIndex+2);
			}catch(Exception e){
				throw new PropertyException(RBACModelProperty.ROLE_RELATION, relationName, "Corrupted property file (invalid role relation value)");
			}
			result.add(new RoleRelation(dominatingRole, dominatedRole));
		}
		return result;
	}
	
	//-- Role membership
	
	public void setRoleMembership(String subject, Set<String> roles) {
		validateStringValue(subject);
		Validate.notNull(roles);
		if(roles.isEmpty())
			return;
		Validate.noNullElements(roles);
		
		props.setProperty(String.format(ROLE_MEMBERSHIP_FORMAT, subject), ArrayUtils.toString(encapsulateValues(roles)));
	}
	
	public Set<String> getRoleMembership(String subject) {
		validateStringValue(subject);
		
		Set<String> result = new HashSet<String>();
		String propertyValue = props.getProperty(String.format(ROLE_MEMBERSHIP_FORMAT, subject));
		if(propertyValue == null)
			return result;
		StringTokenizer subjectTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(subjectTokens.hasMoreTokens()){
			String nextToken = subjectTokens.nextToken();
			result.add(nextToken.substring(1, nextToken.length()-1));
		}
		return result;
	}

	@Override
	public void setActivityPermission(String roleName, Collection<String> transactions) {
		super.setActivityPermission(roleName, transactions);
	}

	@Override
	public Set<String> getActivityPermission(String roleName) throws PropertyException {
		return super.getActivityPermission(roleName);
	}

	@Override
	public void setObjectPermission(String roleName, Map<String, Set<DataUsage>> permissions) throws PropertyException {
		super.setObjectPermission(roleName, permissions);
	}

	@Override
	public Map<String, Set<DataUsage>> getObjectPermission(String roleName) throws PropertyException {
		return super.getObjectPermission(roleName);
	}

	public static void main(String[] args) throws Exception{
		
		Set<String> subjects = new HashSet<String>(Arrays.asList("subj1", "subj2", "subj3"));
		Set<String> objects = new HashSet<String>(Arrays.asList("obj1", "obj2", "obj3"));
		Set<String> transactions = new HashSet<String>(Arrays.asList("t1", "t2", "t3"));
		
		SOABase c = new SOABase("Context");
		c.setSubjects(subjects);
		c.setObjects(objects);
		c.setActivities(transactions);

		String name = "acModel2";
	
		RBACModelProperties p = new RBACModelProperties();
		p.setName(name);
		
		p.setActivityPermission("subj1", transactions);
		
		Map<String, Set<DataUsage>> object_permission = new HashMap<String, Set<DataUsage>>();
		Set<DataUsage> modes = new HashSet<DataUsage>(Arrays.asList(DataUsage.READ, DataUsage.WRITE));
		object_permission.put("obj1", modes);
		p.setObjectPermission("subj1", object_permission);
		
		Set<String> roles = new HashSet<String>(Arrays.asList("r1", "r2", "r3"));
		p.setRoles(roles);
		p.setRightsPropagation(true);
		p.addRoleRelation("r1", "r2");
		p.addRoleRelation("r2", "r3");
		
		p.store("/Users/holderer/Documents/SwatWorkingDirectory/acModels/smallrbac");
		p.setRoleMembership("subj1", roles);
		
	}
	

}
