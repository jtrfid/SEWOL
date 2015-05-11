package de.uni.freiburg.iig.telematik.sewol.context;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.constraint.AbstractConstraint;
import de.invation.code.toval.constraint.NumberConstraint;
import de.invation.code.toval.constraint.StringConstraint;
import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

public class ProcessConstraintContextProperties extends ProcessContextProperties{
	
	private final String CONSTRAINT_FORMAT = ProcessConstraintContextProperty.CONSTRAINT + "_%s";
	private final String ACTIVITY_CONSTRAINTS_FORMAT = ProcessConstraintContextProperty.ACTIVITY_CONSTRAINTS + "_%s";
	
	//------- Property setting -------------------------------------------------------------
	
	private void setProperty(ProcessConstraintContextProperty contextProperty, Object value){
		props.setProperty(contextProperty.toString(), value.toString());
	}
	
	private String getProperty(ProcessConstraintContextProperty contextProperty){
		return props.getProperty(contextProperty.toString());
	}
	
	//-- Routing Constraints
	
	/**
	 * Adds a routing constraint for an activity.
	 * @param activity The name of the activity for which the constraint is added.
	 * @param constraint The routing constraint to add.
	 * @throws ParameterException if the given parameters are invalid.
	 * @throws PropertyException if the given constraint cannot be added as a property.
	 * @see #addConstraint(AbstractConstraint)
	 * @see #addActivityWithConstraints(String)
	 */
	public void addRoutingConstraint(String activity, AbstractConstraint<?> constraint) throws PropertyException{
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Validate.notNull(constraint);
		
		//1. Add constraint itself
		//   This also adds the constraint to the list of constraints
		String propertyNameForNewConstraint = addConstraint(constraint);
		
		//2. Add constraint name to the list of constraints for this activity
		Set<String> currentConstraintNames = getConstraintNames(activity);
		currentConstraintNames.add(propertyNameForNewConstraint);
		if(currentConstraintNames.size() == 1){
			//Add the activity to the list of activities with routing constraints
			addActivityWithConstraints(activity);
		}
		props.setProperty(String.format(ACTIVITY_CONSTRAINTS_FORMAT, activity), ArrayUtils.toString(currentConstraintNames.toArray()));
	}
	
	/**
	 * Adds an activity to the list of activities with routing constraints.
	 * @param activity The name of the activity to add.
	 * @throws ParameterException if the activity name is invalid.
	 */
	private void addActivityWithConstraints(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> currentActivities = getActivitiesWithRoutingConstraints();
		currentActivities.add(activity);
		setProperty(ProcessConstraintContextProperty.ACTIVITIES_WITH_CONSTRAINTS, ArrayUtils.toString(currentActivities.toArray()));
	}
	
	/**
	 * Removes an activity from the list of activities with routing constraints.
	 * @param activity The name of the activity to remove.
	 * @throws ParameterException if the activity name is invalid.
	 */
	private void removeActivityWithConstraints(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> currentActivities = getActivitiesWithRoutingConstraints();
		currentActivities.remove(activity);
		setProperty(ProcessConstraintContextProperty.ACTIVITIES_WITH_CONSTRAINTS, ArrayUtils.toString(currentActivities.toArray()));
	}
	
	/**
	 * Returns the names of all activities with routing constraints.
	 * @return A set of all activities with routing constraints.
	 */
	public Set<String> getActivitiesWithRoutingConstraints(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(ProcessConstraintContextProperty.ACTIVITIES_WITH_CONSTRAINTS);
		if(propertyValue == null)
			return result;
		StringTokenizer activityTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(activityTokens.hasMoreTokens()){
			result.add(activityTokens.nextToken());
		}
		return result;
	}
	
	public boolean hasConstraints(String activity) {
		Validate.notNull(activity);
		String propertyValue = props.getProperty(String.format(ACTIVITY_CONSTRAINTS_FORMAT, activity));
		return propertyValue != null;
	}
	
	/**
	 * Adds the given routing constraint to the context properties.<br>
	 * This requires to generate a new name for the constraint (e.g. CONSTRAINT_1) <br>
	 * and storing a string-representation of this constraint under this name.<br>
	 * Additionally, the new constraint name is stored in the property field which is summing up all constraint names (CONSTRAINTS).
	 * @param constraint The routing constraint to add.
	 * @return The newly generated name for the constraint under which it is accessible.
	 * @throws ParameterException if the given routing constraint is <code>null</code>. 
	 * @throws PropertyException if the constraint property name cannot be generated or the constraint cannot be stored.
	 * @see #getNextConstraintIndex()
	 */
	private String addConstraint(AbstractConstraint<?> constraint) throws PropertyException{
		Validate.notNull(constraint);
		String constraintName = String.format(CONSTRAINT_FORMAT, getNextConstraintIndex());
		props.setProperty(constraintName, constraint.toString());
		addConstraintNameToList(constraintName);
		return constraintName;
	}
	
	
	/**
	 * Returns the lowest unused index for constraint names.<br>
	 * Constraint names are enumerated (CONSTRAINT_1, CONSTRAINT_2, ...).<br>
	 * When new constraints are added, the lowest unused index is used as property name.
	 * @return The lowest free index to be used for constraint naming.
	 * @throws PropertyException if the extraction of used indexes fails.
	 */
	private int getNextConstraintIndex() throws PropertyException{
		Set<Integer> usedIndexes = getConstraintNameIndexes();
		int nextIndex = 1;
		while(usedIndexes.contains(nextIndex)){
			nextIndex++;
		}
		return nextIndex;
	}
	
	/**
	 * Returns all used indexes for constraint property names.<br>
	 * Constraint names are enumerated (CONSTRAINT_1, CONSTRAINT_2, ...).<br>
	 * When new constraints are added, the lowest unused index is used within the new property name.
	 * @return The set of indexes in use.
	 * @throws PropertyException if existing constraint names are invalid (e.g. due to external file manipulation).
	 */
	private Set<Integer> getConstraintNameIndexes() throws PropertyException{
		Set<Integer> result = new HashSet<Integer>();
		Set<String> constraintNames = getConstraintNameList();
		if(constraintNames.isEmpty())
			return result;
		for(String constraintName: constraintNames){
			int separatorIndex = constraintName.lastIndexOf("_");
			if(separatorIndex == -1 || (constraintName.length() == separatorIndex + 1))
				throw new PropertyException(ProcessConstraintContextProperty.CONSTRAINT, constraintName, "Corrupted property file (invalid constraint name)");
			Integer index = null;
			try {
				index = Integer.parseInt(constraintName.substring(separatorIndex+1));
			} catch(Exception e){
				throw new PropertyException(ProcessConstraintContextProperty.CONSTRAINT, constraintName, "Corrupted property file (invalid constraint name)");
			}
			result.add(index);
		}
		return result;
	}
	
	/**
	 * Adds a new constraint property name to the list of constraint properties (CONSTRAINTS-field).
	 * @param constraintName The name of the constraint-property to add (e.g. CONSTRAINT_5).
	 * @throws ParameterException if the given property name is invalid.
	 */
	private void addConstraintNameToList(String constraintName) {
		validateStringValue(constraintName);
		Set<String> currentValues = getConstraintNameList();
		currentValues.add(constraintName);
		setProperty(ProcessConstraintContextProperty.ALL_CONSTRAINTS, ArrayUtils.toString(currentValues.toArray()));
	}
	
	/**
	 * Removes the constraint property with the given name from the list of constraint properties (CONSTRAINTS-field).
	 * @param constraintName The name of the constraint-property to remove (e.g. CONSTRAINT_5).
	 * @throws ParameterException if the given property name is invalid.
	 */
	private void removeConstraintNameFromList(String constraintName) {
		validateStringValue(constraintName);
		Set<String> currentValues = getConstraintNameList();
		currentValues.remove(constraintName);
		setProperty(ProcessConstraintContextProperty.ALL_CONSTRAINTS, ArrayUtils.toString(currentValues.toArray()));
	}
	
	/**
	 * Returns all property names for routing constraints, <br>
	 * i.e. the value of the context property CONSTRAINTS.
	 * @return A set of all used property names for routing constraints.
	 */
	private Set<String> getConstraintNameList(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(ProcessConstraintContextProperty.ALL_CONSTRAINTS);
		if(propertyValue == null)
			return result;
		StringTokenizer attributeTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(attributeTokens.hasMoreTokens()){
			String nextToken = attributeTokens.nextToken();
			result.add(nextToken);
		}
		return result;
	}
	
	/**
	 * Returns all routing constraints of the given activity in form of constraint-objects.
	 * 
	 * @param activity The name of the activity whose routing constraints are requested.
	 * @return A possibly empty set of routing constraints.
	 * @throws ParameterException if the activity name is <code>null</code> or empty.
	 * @throws PropertyException if corresponding constraint-properties cannot be extracted.
	 * @see #getConstraintNames(String)
	 * @see #getConstraint(String)
	 */
	public Set<AbstractConstraint<?>> getRoutingConstraints(String activity) throws PropertyException{
		Set<String> constraintNames = getConstraintNames(activity);
		Set<AbstractConstraint<?>> result = new HashSet<AbstractConstraint<?>>();
		for(String constraintName: constraintNames){
			result.add(getConstraint(constraintName));
		}
		return result;
	}
	
	/**
	 * Returns the property-names of all routing constraints related to the given activity.<br>
	 * These names are required to extract constraint property-values.
	 * @param activity The name of the activity
	 * @return A set of constraint property-names related to the given activity.
	 * @throws ParameterException if the given activity name is <code>null</code or invalid.
	 */
	private Set<String> getConstraintNames(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> result = new HashSet<String>();
		String propertyValue = props.getProperty(String.format(ACTIVITY_CONSTRAINTS_FORMAT, activity));
		if(propertyValue == null)
			return result;
		StringTokenizer subjectTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(subjectTokens.hasMoreTokens()){
			String nextToken = subjectTokens.nextToken();
			result.add(nextToken);
		}
		return result;
	}
	
	/**
	 * Extracts the constraint with the given name in form of a constraint-object.<br>
	 * @param constraintName The property-name of the constraint (CONSTRAINT_X)
	 * @return The constraint-representation of the constraint-property
	 * @throws PropertyException if there is no constraint with the given name or the value cannot be converted into a number- or string-constraint.
	 */
	private AbstractConstraint<?> getConstraint(String constraintName) throws PropertyException{
		String constraintString = props.getProperty(constraintName);
		if(constraintString == null)
			throw new PropertyException(ProcessConstraintContextProperty.CONSTRAINT, constraintName, "Unparseable constraint");
		AbstractConstraint<?> result = null;
		try{
			result = NumberConstraint.parse(constraintString);
		}catch(Exception e){
			try{
				result = StringConstraint.parse(constraintString);
			}catch(Exception e1){
				throw new PropertyException(ProcessConstraintContextProperty.CONSTRAINT, constraintName, "Unparseable constraint");
			}
		}
		return result;
	}
	
	public void removeRoutingConstraint(String activity, AbstractConstraint<?> constraint) throws PropertyException{
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Validate.notNull(constraint);
		//Find name of the given constraint
		Set<String> propertyNamesForActivityConstraints = getConstraintNames(activity);
		if(propertyNamesForActivityConstraints.isEmpty()){
			//This activity has no constraints.
			return;
		}
		String propertyNameForConstraintToRemove = null;
		for(String propertyNameForActivityConstraint: propertyNamesForActivityConstraints){
			AbstractConstraint<?> activityConstraint = getConstraint(propertyNameForActivityConstraint);
			if(activityConstraint.equals(constraint)){
				propertyNameForConstraintToRemove = propertyNameForActivityConstraint;
				break;
			}
		}
		if(propertyNameForConstraintToRemove == null){
			//There is no stored reference to the given constraint to remove
			//-> Removal not necessary
			return;
		}
		
		//1. Remove the constraint itself
		props.remove(propertyNameForConstraintToRemove);
		
		//2. Remove the constraint from the list of constraints
		removeConstraintNameFromList(propertyNameForConstraintToRemove);
		
		//3. Remove the constraint from the activity constraint list.
		Set<String> currentConstraintNames = getConstraintNames(activity);
		currentConstraintNames.remove(propertyNameForConstraintToRemove);
		if(currentConstraintNames.isEmpty()){
			removeActivityWithConstraints(activity);
			props.remove(String.format(ACTIVITY_CONSTRAINTS_FORMAT, activity));
		} else {
			props.setProperty(String.format(ACTIVITY_CONSTRAINTS_FORMAT, activity), ArrayUtils.toString(currentConstraintNames.toArray()));
		}
	}

}
