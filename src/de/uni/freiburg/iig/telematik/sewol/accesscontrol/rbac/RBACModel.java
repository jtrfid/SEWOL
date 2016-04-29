package de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.misc.CollectionUtils;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.types.HashList;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.acl.ACLModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACModelType;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.RBACModelProperties;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.RoleLattice;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.RoleRelation;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.event.RoleLatticeListener;



/**
 * Within an RBAC model, users are assigned to different roles of a system.<br>
 * Each user can be assigned to different roles.
 * 
 * @author Thomas Stocker
 *
 */
public class RBACModel extends AbstractACModel<RBACModelProperties> implements RoleLatticeListener {

	protected RoleLattice roleLattice;
	protected HashMap<String, HashList<String>> roleMembershipRU;
	protected HashMap<String, HashList<String>> roleMembershipUR;
	protected ACLModel rolePermissions;
	
	private boolean rightPropagationAlongLattice;
	
	public RBACModel(String name){
		super(ACModelType.RBAC, name);
	}
	
	public RBACModel(String name, RoleLattice roleLattice){
		super(ACModelType.RBAC, name);
		setRoleLattice(roleLattice);
	}
	
	public RBACModel(String name, SOABase context, RoleLattice roleLattice){
		super(ACModelType.RBAC, name, context);
		setRoleLattice(roleLattice);
	}
	
	public RBACModel(RBACModelProperties properties, SOABase context) throws PropertyException{
		super(properties, context);
	}
	
	@Override
	protected void initialize(RBACModelProperties properties) throws PropertyException {
		super.initialize(properties);
		RoleLattice lattice = new RoleLattice(properties.getRoles());
		for(RoleRelation relation: properties.getRoleRelations()){
			lattice.addRelation(relation);
		}
		setRoleLattice(lattice);
		setRightsPropagation(properties.getRightsPropagation());
		for(String subject: getContext().getSubjects()){
			addRoleMembership(subject, properties.getRoleMembership(subject));
		}
		for(String role: properties.getRoles()){
			setActivityPermission(role, properties.getActivityPermission(role));
			setObjectPermission(role, properties.getObjectPermission(role));
		}
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		roleMembershipRU = new HashMap<>();
		roleMembershipUR = new HashMap<>();
		rolePermissions = new ACLModel("rolePermissions");
		rolePermissions.addACModelListener(this);
		rightPropagationAlongLattice = false;
		setRoleLattice(new RoleLattice());
	}

	private void setRoleLattice(RoleLattice roleLattice){
		Validate.notNull(roleLattice);
		rolePermissions.getContext().setSubjects(roleLattice.getRoles());
		this.roleLattice = roleLattice;
		this.roleLattice.addRoleLatticeListener(this);
	}
	
	@Override
	public void checkContextChange(SOABase context) {
		rolePermissions.checkContextChange(context);
	}
	
	@Override
	public void contextChangeProcedure() {
		SOABase clonedContext = context.clone();
		clonedContext.setSubjectDescriptor("Role;Roles");
		rolePermissions.setContext(clonedContext);
		rolePermissions.getContext().setSubjects(roleLattice.getRoles());
	}

	public ACLModel getRolePermissions(){
		return rolePermissions;
	}
	
	public RoleLattice getRoleLattice(){
		return roleLattice;
	}
	
	public void setRightsPropagation(boolean propagate){
		this.rightPropagationAlongLattice = propagate;
	}
	
	public boolean propagatesRights(){
		return rightPropagationAlongLattice;
	}
	
	//------- Role methods ---------------------------------------------------------------
	
	public Set<String> getRoles(){
		return roleLattice.getRoles();
	}
	
	public Set<String> getRolesFor(String subject, boolean withPropagation) throws CompatibilityException{
		getContext().validateSubject(subject);
		Set<String> userRoles = new HashSet<>();
		if(roleMembershipUR.containsKey(subject)){
			userRoles.addAll(roleMembershipUR.get(subject));
			if(withPropagation && propagatesRights()){
				for(String primaryRole: roleMembershipUR.get(subject)){
					try {
						userRoles.addAll(roleLattice.getDominatedRolesFor(primaryRole));
					} catch (Exception e) {}
				}
			}
		}
		return userRoles;
	}
	
	public void setRoleMembership(String roleName, List<String> members) throws CompatibilityException {
		validateRole(roleName);
		getContext().validateSubjects(members);
		if(roleMembershipRU.containsKey(roleName)){
			//role membership was already set before -> cleanup
			for(String member: roleMembershipRU.get(roleName))
				roleMembershipUR.get(member).remove(roleName);
		}
		roleMembershipRU.put(roleName, new HashList<>(members));
		for(String member: members){
			if(!roleMembershipUR.containsKey(member)){
				roleMembershipUR.put(member, new HashList<>());
			}
			roleMembershipUR.get(member).add(roleName);
		}
	}
	
	public void setRoleMembership(String roleName, String... members) throws CompatibilityException {
		setRoleMembership(roleName, Arrays.asList(members));
	}
	
	public void addRoleMembership(String subject, String... roles) throws CompatibilityException {
		addRoleMembership(subject, Arrays.asList(roles));
	}
	
	public void addRoleMembership(String subject, Collection<String> roles) throws CompatibilityException {
		getContext().validateSubject(subject);
		validateRoles(roles);
		if(roles.isEmpty())
			return;
		
		if(!roleMembershipUR.containsKey(subject)){
			roleMembershipUR.put(subject, new HashList<>());
		}
		roleMembershipUR.get(subject).addAll(roles);
		
		for(String role: roles){
			if(!roleMembershipRU.containsKey(role)){
				roleMembershipRU.put(role, new HashList<>());
			}
			roleMembershipRU.get(role).add(subject);
		}
	}
	
	public void removeRoleMembership(String subject, String... roles) throws CompatibilityException {
		removeRoleMembership(subject, Arrays.asList(roles));
	}
	
	public void removeRoleMembership(String subject, Collection<String> roles) throws CompatibilityException {
		getContext().validateSubject(subject);
		validateRoles(roles);
		
		if(!roleMembershipUR.containsKey(subject))
			return;
		roleMembershipUR.get(subject).removeAll(roles);
		if(roleMembershipUR.get(subject).isEmpty())
			roleMembershipUR.remove(subject);
		
		for(String role: roles){
			if(!roleMembershipRU.containsKey(role))
				return;
			roleMembershipRU.get(role).remove(subject);
			if(roleMembershipRU.get(role).isEmpty())
				roleMembershipRU.remove(role);
		}
	}
	
	protected void validateRole(String roleName){
		Validate.notNull(roleName);
		if(!roleLattice.containsRole(roleName))
			throw new ParameterException(ErrorCode.INCOMPATIBILITY, "Unknown role: " + roleName);
	}
	
	protected void validateRoles(Collection<String> roleNames){
		Validate.notNull(roleNames);
		for(String roleName: roleNames)
			validateRole(roleName);
	}
	
	public void setActivityPermission(String roleName, Set<String> transactions) throws CompatibilityException {
		rolePermissions.setActivityPermission(roleName, transactions);
	}
	
	public void setActivityPermission(String roleName, String... transactions) throws CompatibilityException {
		setActivityPermission(roleName, new HashSet<>(Arrays.asList(transactions)));
	}
	
	public void setObjectPermission(String roleName, Set<String> objects) throws CompatibilityException {
		rolePermissions.setObjectPermission(roleName, objects);
	}
	
	public void setObjectPermission(String roleName, String object, DataUsage... dataUsageModes) throws CompatibilityException{
		rolePermissions.setObjectPermission(roleName, object, dataUsageModes);
	}
	
	public void setObjectPermission(String roleName, Collection<String> objects) throws CompatibilityException{
		rolePermissions.setObjectPermission(roleName, objects);
	}
	
	public void setObjectPermission(String roleName, String object) throws CompatibilityException{
		rolePermissions.setObjectPermission(roleName, object);
	}
	
	public void setObjectPermission(String roleName, String object, Collection<DataUsage> dataUsageModes) throws CompatibilityException{
		rolePermissions.setObjectPermission(roleName, object, dataUsageModes);
	}
	
	public void setObjectPermission(String roleName, Map<String, Set<DataUsage>> permissions) throws CompatibilityException{
		rolePermissions.setObjectPermission(roleName, permissions);
	}
	
	//------- inherited methods -----------------------------------------------------------

	@Override
	public boolean isAuthorizedForTransaction(String subject, String transaction) throws CompatibilityException {
		getContext().validateSubject(subject);
		getContext().validateActivity(transaction);
		if(!roleMembershipUR.containsKey(subject))
			return false;
		for(String role: getRolesFor(subject, true))
			if(rolePermissions.isAuthorizedForTransaction(role, transaction))
				return true;
		return false;
	}

	@Override
	public boolean isAuthorizedForObject(String subject, String object) throws CompatibilityException {
		getContext().validateSubject(subject);
		getContext().validateObject(object);
		if(!roleMembershipUR.containsKey(subject))
			return false;
		for(String role: getRolesFor(subject, true))
			if(rolePermissions.isAuthorizedForObject(role, object))
				return true;
		return false;
	}
	
        @Override
	public boolean isAuthorizedForObject(String subject, String object, DataUsage dataUsage) throws CompatibilityException {
		getContext().validateSubject(subject);
		getContext().validateObject(object);
		if(!roleMembershipUR.containsKey(subject))
			return false;
		for(String role: getRolesFor(subject, true))
			if(rolePermissions.isAuthorizedForObject(role, object, dataUsage))
				return true;
		return false;
	}

	@Override
	public List<String> getAuthorizedSubjectsForTransaction(String transaction) throws CompatibilityException {
		getContext().validateActivity(transaction);
		HashList<String> authorizedRoles = new HashList<>();
		try { authorizedRoles.addAll(rolePermissions.getAuthorizedSubjectsForTransaction(transaction));
		} catch (Exception e1) {}
		if(propagatesRights()){
			for(String primaryRole: authorizedRoles.clone()){
				try {
					authorizedRoles.addAll(roleLattice.getDominatingRolesFor(primaryRole));
				} catch (Exception e) {}
			}
		}
		return getUsersFor(authorizedRoles);
	}

	@Override
	public List<String> getAuthorizedSubjectsForObject(String object) throws CompatibilityException {
		return getUsersFor(getAuthorizedRolesforObject(object));
	}
	
	@Override
	public Map<String, Set<DataUsage>> getAuthorizedSubjectsAndPermissionsForObject(String object) throws CompatibilityException {
		getContext().validateObject(object);
		Map<String, Set<DataUsage>> userPermissions = new HashMap<>();
		Map<String, Set<DataUsage>> userRolePermissions = new HashMap<>();
		for(String role: getAuthorizedRolesforObject(object)){
			userRolePermissions.put(role, this.rolePermissions.getObjectPermissionsForSubject(role, object));
		}
		for(String subject: getContext().getSubjects()){
			userPermissions.put(subject, new HashSet<>());
			for(String role: getRolesFor(subject, true)){
				if(!userRolePermissions.containsKey(role)){
					continue;
				}
				if(!userPermissions.containsKey(subject)){
					userPermissions.put(subject, new HashSet<>());
				}
				userPermissions.get(subject).addAll(userRolePermissions.get(role));
			}
		}
		return userPermissions;
	}
	
	@Override
	public Set<DataUsage> getObjectPermissionsForSubject(String subject, String object) throws CompatibilityException {
		getContext().validateObject(subject);
		getContext().validateObject(object);
		Set<DataUsage> userPermissions = new HashSet<>();
		for(String role: getRolesFor(subject, true)){
			userPermissions.addAll(getObjectPermissionsForRole(role, object));
		}
		return userPermissions;
	}
	
	public Set<DataUsage> getObjectPermissionsForRole(String role, String object){
		validateRole(role);
		getContext().validateObject(object);
		Set<DataUsage> objectRolePermissions = this.rolePermissions.getObjectPermissionsForSubject(role, object);
		if(propagatesRights()){
			for(String dominatedRole: getDominatedRoles(role)){
				objectRolePermissions.addAll(this.rolePermissions.getObjectPermissionsForSubject(dominatedRole, object));
			}
		}
		return objectRolePermissions;
	}
	
	private Set<String> getAuthorizedRolesforObject(String object) throws CompatibilityException{
		getContext().validateObject(object);
		HashList<String> authorizedRoles = new HashList<>();
		try { 
			authorizedRoles.addAll(rolePermissions.getAuthorizedSubjectsForObject(object));
		} catch (Exception e1) {}
		if(propagatesRights()){
			for(String primaryRole: authorizedRoles.clone()){
				try {
					authorizedRoles.addAll(getDominatedRoles(primaryRole));
				} catch (Exception e) {}
			}
		}
		return authorizedRoles;
	}

	@Override
	public List<String> getAuthorizedTransactionsForSubject(String subject) throws CompatibilityException {
		List<String> authorizedTransactions = new HashList<>();
		for(String role: getRolesFor(subject, true)){
			authorizedTransactions.addAll(rolePermissions.getAuthorizedTransactionsForSubject(role));
		}
		return authorizedTransactions;
	}

	@Override
	public List<String> getAuthorizedObjectsForSubject(String subject) throws CompatibilityException {
		List<String> authorizedObjects = new HashList<>();
		for(String role: getRolesFor(subject, true)){
			authorizedObjects.addAll(rolePermissions.getAuthorizedObjectsForSubject(role));
		}
		return authorizedObjects;
	}
	
	@Override
	public boolean hasTransactionPermissions() {
		return rolePermissions.hasTransactionPermissions();
	}

	@Override
	public boolean hasObjectPermissions() {
		return rolePermissions.hasObjectPermissions();
	}
	
	//------- helper methods ---------------------------------------------------------------
	
	/**
	 * Returns all users with the given role.<br>
	 * It is assumed that the caller ensures parameter validity.
	 * @param roles
	 */
	private List<String> getUsersFor(Collection<String> roles){
		List<String> users = new HashList<>();
		for(String role: roles){
			if(roleMembershipRU.containsKey(role))
				users.addAll(roleMembershipRU.get(role));
		}
		return users;
	}
	
	/**
	 * Returns all users with the given role.<br>
	 * It is assumed that the caller ensures parameter validity.
	 * @param roles
	 */
	private List<String> getUsersFor(String... roles){
		List<String> users = new HashList<>();
		for(String role: roles){
			if(roleMembershipRU.containsKey(role))
				users.addAll(roleMembershipRU.get(role));
		}
		return users;
	}
	
	/**
	 * Creates a new RBAC model and randomly assigns uses and permissions to roles.<br>
	 * The role lattice contains no relations between different roles.<br>
	 * Only transaction permissions are added to the RBAC model.<br>
	 * Each user is assigned to exactly one role.
	 * @param users The set of users.
         * @param transactions
	 * @param roles The set of roles.
	 * @return A new RBAC model with random role assignments.
	 * @throws ParameterException 
	 */
	public static RBACModel createRandomModel(Collection<String> users, Collection<String> transactions, Collection<String> roles){
		Validate.notNull(transactions);
		Validate.notEmpty(transactions);
		Validate.noNullElements(transactions);
		SOABase context = new SOABase("c1");
		context.setSubjects(users);
		context.setActivities(transactions);
		
		RoleLattice roleLattice = new RoleLattice(roles);
		RBACModel rbac = new RBACModel("rbac1", context, roleLattice);

		//Role membership and permissions
		List<String> transactionList = new ArrayList<>();
		transactionList.addAll(transactions);
		Collections.shuffle(transactionList);
		List<List<String>> rolePartitions = CollectionUtils.exponentialPartition(users, roles.size());
		List<List<String>> activityPartitions = CollectionUtils.exponentialPartition(transactionList, roles.size());
		List<String> roleList = new ArrayList<>();
		roleList.addAll(rbac.getRoles());
		for(int i=0; i<rolePartitions.size(); i++){
			try {
				rbac.setRoleMembership(roleList.get(i), rolePartitions.get(i));
				rbac.setActivityPermission(roleList.get(i), new HashSet<>(activityPartitions.get(i)));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return rbac;
	}
	
	public Set<String> getAuthorizedTransactionsForRole(String roleName) {
		Set<String> roles = new HashSet<>();
		roles.add(roleName);
		if(rightPropagationAlongLattice)
			roles.addAll(getDominatedRoles(roleName));
		Set<String> authorizedTransactions = new HashSet<>();
		for(String role: roles){
			authorizedTransactions.addAll(rolePermissions.getAuthorizedTransactionsForSubject(role));
		}
		return authorizedTransactions;
	}
	
	public Set<String> getAuthorizedObjectsForRole(String roleName) {
		Set<String> roles = new HashSet<>();
		roles.add(roleName);
		if(rightPropagationAlongLattice)
			roles.addAll(getDominatedRoles(roleName));
		Set<String> authorizedObjects = new HashSet<>();
		for(String role: roles){
			authorizedObjects.addAll(rolePermissions.getAuthorizedObjectsForSubject(role));
		}
		return authorizedObjects;
	}
	
	public Set<String> getDominatingRoles(String roleName) {
		validateRole(roleName);
		return roleLattice.getDominatingRolesFor(roleName);
	}
	
	public Set<String> getDominatedRoles(String roleName) {
		validateRole(roleName);
		return roleLattice.getDominatedRolesFor(roleName);
	}
	
	
	@Override
	protected String getStructureString() {
		StringBuilder builder = new StringBuilder();
		builder.append(super.getStructureString());
		builder.append("       roles: ").append(getRoles());
		builder.append('\n');
		return builder.toString();
	}
	
	protected String getRoleTransactionPermissionsString(){
		StringBuilder builder = new StringBuilder();
		if (hasTransactionPermissions()) {
			builder.append('\n');
			builder.append("Role transaction permissions:");
			builder.append('\n');
			builder.append('\n');
			Set<String> transactionSet;
			for (String role : getRoles()) {
				transactionSet = getAuthorizedTransactionsForRole(role);
				if (!transactionSet.isEmpty()) {
					builder.append(role);
					builder.append(": ");
					builder.append(transactionSet);
					builder.append('\n');
				}
			}
		}
		return builder.toString();
	}
	
	protected String getRoleObjectPermissionsString(){
		StringBuilder builder = new StringBuilder();
		if (hasObjectPermissions()) {
			builder.append('\n');
			builder.append("Role object permissions:");
			builder.append('\n');
			builder.append('\n');
			Set<String> objectSet;
			for (String role : getRoles()) {
				objectSet = getAuthorizedObjectsForRole(role);
				if (!objectSet.isEmpty()) {
					builder.append(role);
					builder.append(": ");
					builder.append(objectSet);
					builder.append('\n');
				}
			}
		}
		return builder.toString();
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append(getStructureString());
		builder.append(getRoleTransactionPermissionsString());
		builder.append(getRoleObjectPermissionsString());
		builder.append('\n');
		builder.append("role assignments:");
		builder.append('\n');
		for(String role : roleLattice.getRoles()){
			builder.append(role);
			builder.append(": ");
			builder.append(getUsersFor(role));
			builder.append('\n');
		}
		return builder.toString();
	}

	@Override
	public RBACModelProperties getProperties() throws PropertyException {
		RBACModelProperties result = super.getProperties();

		result.setRightsPropagation(propagatesRights());
		result.setRoles(roleLattice.getRoles());
		for(String role: roleLattice.getRoles()){
			Set<String> transactionPermissions = rolePermissions.getTransactionPermissionsForSubject(role);
			if(transactionPermissions != null && !transactionPermissions.isEmpty()){
				result.setActivityPermission(role, transactionPermissions);
			}
			Map<String,Set<DataUsage>> objectPermissions = rolePermissions.getObjectPermissionsForSubject(role);
			if(objectPermissions != null && !objectPermissions.isEmpty()){
				result.setObjectPermission(role, objectPermissions);
			}
		}
		for(RoleRelation relation: getRoleLattice().getRoleRelations()){
			result.addRoleRelation(relation.getDominatingRole(), relation.getDominatedRole());
		}
		for(String subject: getContext().getSubjects()){
			result.setRoleMembership(subject, getRolesFor(subject, false));
		}
		return result;
	}

	@Override
	protected RBACModelProperties createNewProperties() {
		return new RBACModelProperties();
	}

	@Override
	public Map<String, Set<DataUsage>> getObjectPermissionsForSubject(String subject) throws CompatibilityException {
		getContext().validateSubject(subject);
		Map<String, Set<DataUsage>> userPermissions = new HashMap<>();
		for(String role: getRolesFor(subject, true)){
			Map<String, Set<DataUsage>> permissions = rolePermissions.getObjectPermissionsForSubject(role);
			for(String object: permissions.keySet()){
				if(!userPermissions.containsKey(object)){
					userPermissions.put(object, new HashSet<>());
				}
				userPermissions.get(object).addAll(permissions.get(object));
			}
		}
		return userPermissions;
	}

	@Override
	public Set<String> getTransactionPermissionsForSubject(String subject) throws CompatibilityException {
		getContext().validateSubject(subject);
		Set<String> userPermissions = new HashSet<>();
		for(String role: getRolesFor(subject, true)){
			userPermissions.addAll(rolePermissions.getTransactionPermissionsForSubject(role));
		}
		return userPermissions;
	}

	@Override
	public void roleAdded(String roleName){
		rolePermissions.getContext().addSubject(roleName);
	}

	@Override
	public void roleRemoved(String roleName){
		rolePermissions.getContext().removeSubject(roleName);
		Set<String> removedSubjects = roleMembershipRU.remove(roleName);
		if(removedSubjects != null){
			for(String subject: removedSubjects){
				removeRoleMembership(subject, roleName);
			}
		}
	}
	
	@Override
	public void activityAdded(String activity) {
		super.activityAdded(activity);
		rolePermissions.getContext().addActivity(activity);
	}

	@Override
	public void activityRemoved(String activity) {
		super.activityRemoved(activity);
		rolePermissions.getContext().removeActivity(activity);
	}

	@Override
	public void objectAdded(String object) {
		super.objectAdded(object);
		rolePermissions.getContext().addObject(object);
	}

	@Override
	public void objectRemoved(String object) {
		super.objectRemoved(object);
		rolePermissions.getContext().removeObject(object);
	}

	@Override
	public RBACModel clone(){
		try {
			return new RBACModel(getProperties(), getContext());
		} catch (PropertyException e) {
			return null;
		}
	}
	
	@Override
	public void takeoverValues(AbstractACModel<RBACModelProperties> other) throws Exception {
		this.roleLattice.removeRoleLatticeListener(this);
		resetRoleMembership();
		resetPermissions();
		initialize(other.getProperties());
	}
	
	public void resetRoleMembership(){
		roleMembershipRU.clear();
		roleMembershipUR.clear();
	}
	
	@Override
	public void resetPermissions() {
		rolePermissions.resetPermissions();
	}

	public static void main(String[] args) throws Exception{
		SOABase context = new SOABase("c1");
		context.setSubjects(Arrays.asList("U1","U2","U3","U4","U5","U6","U7","U8","U9","U10"));
		context.setActivities(Arrays.asList("T1","T2","T3","T4","T5"));
		
		RoleLattice l = new RoleLattice(Arrays.asList("role0", "role1", "role2", "role3"));
		l.addRelation("role0", "role1");
		l.addRelation("role0", "role2");
		l.addRelation("role1", "role3");
		l.addRelation("role2", "role3");
		RBACModel rbac = new RBACModel("rbac1", context, l);
		
		
		rbac.setRoleMembership("role0", Arrays.asList("U8"));
		rbac.setRoleMembership("role1", Arrays.asList("U1","U3"));
		rbac.setRoleMembership("role2", Arrays.asList("U5","U7"));
		rbac.setRoleMembership("role3", Arrays.asList("U3","U4"));
		System.out.println("roles for user U1: " + rbac.getRolesFor("U1", true) + "(with rights propagation)");
		
		rbac.setActivityPermission("role0", new HashSet<>(Arrays.asList("T4")));
		rbac.setActivityPermission("role1", new HashSet<>(Arrays.asList("T2")));
		rbac.setActivityPermission("role2", new HashSet<>(Arrays.asList("T3")));
		rbac.setActivityPermission("role3", new HashSet<>(Arrays.asList("T1","T5")));
		
//		rbac.setRightsPropagation(true);
		System.out.println(rbac);
		
		rbac.setSubjectDescriptor("Gerds");
		
		RBACModelProperties props = rbac.getProperties();
		props.store("rbac");
		
		System.out.println(new RBACModel(props, context));
	}

	@Override
	public void nameChanged(String oldName, String newName) {}

	@Override
	public void accessPermissionAdded(AbstractACModel<?> sender, String subject, String object, Collection<DataUsage> dataUsageModes) {
		if(sender == rolePermissions){
			acModelListenerSupport.notifyAccessPermissionAdded(subject, object, dataUsageModes);
		}
	}

	@Override
	public void accessPermissionRemoved(AbstractACModel<?> sender, String subject, String object, Collection<DataUsage> dataUsageModes) {
		if(sender == rolePermissions){
			acModelListenerSupport.notifyAccessPermissionRemoved(subject, object, dataUsageModes);
		}
	}

	@Override
	public void executionPermissionAdded(AbstractACModel sender, String subject, String transaction) {
		if(sender == rolePermissions){
			acModelListenerSupport.notifyExecutionPermissionAdded(subject, transaction);
		}
	}

	@Override
	public void executionPermissionRemoved(AbstractACModel sender, String subject, String transaction) {
		if(sender == rolePermissions){
			acModelListenerSupport.notifyExecutionPermissionRemoved(subject, transaction);
		}
	}
	
	

}
