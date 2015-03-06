package de.uni.freiburg.iig.telematik.sewol.context;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.constraint.AbstractConstraint;
import de.invation.code.toval.constraint.NumberConstraint;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.acl.ACLModel;

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
public class ProcessConstraintContext extends ProcessContext {

	/**
	 * Constraints on attributes, which are used for routing purposes.<br>
	 * Example: Activity "Double Check" is executed when the credit amount exceeds $50.000.<br>
	 */
	protected Map<String, Set<AbstractConstraint<?>>> routingConstraints = new HashMap<String, Set<AbstractConstraint<?>>>();
	
	//------- Constructors ------------------------------------------------------------------
	
	/**
	 * Creates a new context using the given activity names.
	 * @param activities Names of process activities.
	 * @throws ParameterException 
	 * @throws Exception If activity list is <code>null</code> or empty.
	 */
	public ProcessConstraintContext(String name){
		super(name);
	}
	
	public ProcessConstraintContext(ProcessConstraintContextProperties properties) throws PropertyException{
		super(properties);
		// Set routing constraints
		routingConstraints.clear();
		for (String activity : properties.getActivitiesWithRoutingConstraints()) {
			Set<AbstractConstraint<?>> otherRoutingConstraints = properties.getRoutingConstraints(activity);
			for (AbstractConstraint<?> routingConstraint : otherRoutingConstraints) {
				addRoutingConstraint(activity, routingConstraint.clone());
			}
		}
	}
	
	public ProcessConstraintContext(SOABase context) throws Exception{
		super(context);
	}
	
	
	//------- Attributes ------------------------------------------------------------
	
	/**
	 * Removes the given attribute from the context.
	 */
	@Override
	public boolean removeAttribute(String attribute, boolean removeFromACModel, boolean notifyListeners){
		if(!super.removeAttribute(attribute, removeFromACModel, false))
			return false;
		
		// Remove all routing constraints that relate to removed attributes
		Set<AbstractConstraint<?>> constraintsToRemove = new HashSet<AbstractConstraint<?>>();
		for(String activity: activities){
			if(routingConstraints.containsKey(activity)){
				constraintsToRemove.clear();
				for(AbstractConstraint<?> c: routingConstraints.get(activity)){
					if(attribute.equals(c.getElement())){
						constraintsToRemove.add(c);
					}
				}
				routingConstraints.get(activity).removeAll(constraintsToRemove);
			}
		}
		if(notifyListeners){
			contextListenerSupport.notifyObjectRemoved(attribute);
		}
		return true;
	}

	
	//------- Constraints -----------------------------------------------------------------------------------------------
	
	
	public <C extends AbstractConstraint<?>> boolean addRoutingConstraint(String activity, C constraint) throws CompatibilityException{
		validateActivity(activity);
		Validate.notNull(constraint);
		validateAttribute(constraint.getElement());
		if(!getAttributesFor(activity).contains(constraint.getElement()))
			throw new CompatibilityException("Cannot add constraint on attribute " + constraint.getElement() + " for activity " + activity + ". Activity does not use attribute.");
		
		
		if(!routingConstraints.containsKey(activity))
			routingConstraints.put(activity, new HashSet<AbstractConstraint<?>>());
		return routingConstraints.get(activity).add(constraint);
	}
	
	public boolean hasRoutingConstraints(String activity) throws CompatibilityException{
		validateActivity(activity);
		return routingConstraints.containsKey(activity);
	}
	
	public Set<AbstractConstraint<?>> getRoutingConstraints(String activity) throws CompatibilityException{
		validateActivity(activity);
		return routingConstraints.get(activity);
	}
	
	public Set<String> getActivitiesWithRoutingConstraints(){
		return routingConstraints.keySet();
	}
	
	public <C extends AbstractConstraint<?>> void removeRoutingConstraint(String activity, C constraint) throws CompatibilityException{
		validateActivity(activity);
		Validate.notNull(constraint);
		validateAttribute(constraint.getElement());
		
		if(!routingConstraints.containsKey(activity))
			return;
		routingConstraints.get(activity).remove(constraint);
	}
	
	public boolean hasRoutingConstraints(){
		return !routingConstraints.isEmpty();
	}
	
	
	@Override
	protected void addStringContent(StringBuilder builder) {
		super.addStringContent(builder);
		
		if(hasRoutingConstraints()){
			builder.append('\n');
			builder.append("routing constraints:");
			builder.append('\n');
			for(String activity: routingConstraints.keySet()){
				builder.append(activity);
				builder.append(": ");
				builder.append(routingConstraints.get(activity));
				builder.append('\n');
			}
		}
	}

	public ProcessConstraintContextProperties getProperties() throws PropertyException{
		if(!isValid())
			throw new ParameterException(ErrorCode.INCONSISTENCY, "Cannot extract properties in invalid state!");
		
		ProcessConstraintContextProperties result = new ProcessConstraintContextProperties();
		
		result.setName(getName());
		result.setActivities(getActivities());
		result.setSubjects(getSubjects());
		result.setObjects(getAttributes());
		if(getACModel() != null)
		result.setACModelName(getACModel().getName());
		
		for(String activity: getActivities()){
			Set<AbstractConstraint<?>> routingConstraints = getRoutingConstraints(activity);
			if(routingConstraints != null && !routingConstraints.isEmpty()){
				for(AbstractConstraint<?> routingConstraint: routingConstraints){
					result.addRoutingConstraint(activity, routingConstraint);
				}
			}
			Map<String, Set<DataUsage>> dataUsage = getDataUsageFor(activity);
			if(dataUsage != null && !dataUsage.isEmpty()){
				result.setDataUsage(activity, dataUsage);
			}
		}
		return result;
	}
	
	public void takeoverValues(ProcessConstraintContext context) throws Exception{
		super.takeoverValues(context, false);
		
		//Set routing constraints
		routingConstraints.clear();
		for(String activity: context.getActivitiesWithRoutingConstraints()){
			Set<AbstractConstraint<?>> otherRoutingConstraints = context.getRoutingConstraints(activity);
			for(AbstractConstraint<?> routingConstraint: otherRoutingConstraints){
				addRoutingConstraint(activity, routingConstraint.clone());
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((routingConstraints == null) ? 0 : routingConstraints.hashCode());
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
		ProcessConstraintContext other = (ProcessConstraintContext) obj;
		if (routingConstraints == null) {
			if (other.routingConstraints != null)
				return false;
		} else if (!routingConstraints.equals(other.routingConstraints))
			return false;
		return true;
	}

	public static void main(String[] args) throws PropertyException, IOException {
		Map<String, Set<DataUsage>> usage1 = new HashMap<String, Set<DataUsage>>();
		Set<DataUsage> modes1 = new HashSet<DataUsage>(Arrays.asList(DataUsage.READ, DataUsage.WRITE));
		usage1.put("attribute1", modes1);
		
		Map<String, Set<DataUsage>> usage2 = new HashMap<String, Set<DataUsage>>();
		Set<DataUsage> modes2 = new HashSet<DataUsage>(Arrays.asList(DataUsage.READ, DataUsage.WRITE));
		usage2.put("attribute2", modes2);
		
		Set<String> activities = new HashSet<String>(Arrays.asList("act1", "act2"));
		Set<String> attributes = new HashSet<String>(Arrays.asList("attribute1", "attribute2"));
		Set<String> subjects = new HashSet<String>(Arrays.asList("s1", "s2"));
		ProcessConstraintContext c = new ProcessConstraintContext("c1");
		c.setActivities(activities);
		c.addAttributes(attributes);
		c.addSubjects(subjects);
		c.setDataUsageFor("act1", usage1);
		c.setDataUsageFor("act2", usage2);
		c.addRoutingConstraint("act1", NumberConstraint.parse("attribute1 < 200"));
		
		ACLModel acModel = new ACLModel("acl1", c);
		acModel.setName("acmodel1");
		acModel.setActivityPermission("s1", activities);
		c.setACModel(acModel);
		
		System.out.println(c);
		
//		c.getProperties().store("/Users/stocker/Desktop/processContext");
	}

}
