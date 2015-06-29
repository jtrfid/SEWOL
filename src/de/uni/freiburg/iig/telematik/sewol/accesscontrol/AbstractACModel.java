package de.uni.freiburg.iig.telematik.sewol.accesscontrol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.graphic.dialog.DialogObject;
import de.invation.code.toval.misc.NamedComponent;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.misc.soabase.SOABaseChangeReply;
import de.invation.code.toval.misc.soabase.SOABaseListener;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.event.ACModelListener;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.event.ACModelListenerSupport;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACMValidationException;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACModelProperties;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACModelType;



public abstract class AbstractACModel<T extends ACModelProperties> implements NamedComponent, SOABaseListener, ACModelListener, Cloneable, DialogObject<AbstractACModel<T>> {
	
	public static final String DEFAULT_AC_MODEL_NAME = "ACModel";
	
	protected String name;
	protected Set<DataUsage> validUsageModes;
	protected String subjectDescriptor = "Subjects";
	protected SOABase context;
	protected ACModelType type = null;
	
	protected ACModelListenerSupport acModelListenerSupport;
	
//	public ACModel(){
//		super();
//	}
//	
	protected AbstractACModel(ACModelType type, String name) {
		this(type, name, new SOABase(DEFAULT_AC_MODEL_NAME.concat("Context")));
	}
	
	protected AbstractACModel(ACModelType type, String name, SOABase context) {
		setName(name);
		this.type = type;
		initialize();
		setContext(context);
	}
	
	protected AbstractACModel(T properties, SOABase context) throws PropertyException {
		Validate.notNull(properties);
		Validate.notNull(context);
		if(!properties.getContextName().equals(context.getName()))
			throw new ParameterException(ErrorCode.INCOMPATIBILITY, "Name of given context does not match stored context name.");
                initialize();
		setContext(context);
		initialize(properties);
	}
	
	protected void initialize(T properties) throws PropertyException {
		setName(properties.getName());
		setSubjectDescriptor(properties.getSubjectDescriptor());
		this.type = properties.getType();
	}
	
	protected void initialize() {
		validUsageModes = new HashSet<DataUsage>(Arrays.asList(DataUsage.values()));
		acModelListenerSupport = new ACModelListenerSupport(this);
		acModelListenerSupport.addListener(this);
	}
	
	public boolean isEmpty(){
		return context.isEmpty();
	}
	
	public ACModelType getType(){
		return type;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name) {
		Validate.notNull(name);
		this.name = name;
	}

	public SOABase getContext() {
		return context;
	}

	public void setContext(SOABase context) {
		Validate.notNull(context);
		checkContextChange(context);
		if(this.context != null){
			this.context.removeContextListener(this);
		}
		this.context = context;
		this.context.addContextListener(this);
		contextChangeProcedure();
	}

	public abstract void checkContextChange(SOABase context);
	
	protected abstract void contextChangeProcedure();

	public boolean addACModelListener(ACModelListener listener){
		return acModelListenerSupport.addListener(listener);
	}
	
	public boolean removeACModelListener(ACModelListener listener){
		return acModelListenerSupport.removeListener(listener);
	}
	
	public void setSubjectDescriptor(String descriptor) {
		Validate.notNull(name);
		this.subjectDescriptor = descriptor;
	}
	
	public String getSubjectDescriptor(){
		return subjectDescriptor;
	}
	
	public Set<DataUsage> getValidUsageModes(){
		return Collections.unmodifiableSet(validUsageModes);
	}
	
	public void setValidUsageModes(Collection<DataUsage> validUsageModes) {
		validateNewUsageModes(validUsageModes);
		Set<DataUsage> oldModes = new HashSet<DataUsage>(getValidUsageModes());
		if(this.validUsageModes.equals(validUsageModes))
			return;
		this.validUsageModes.clear();
		this.validUsageModes.addAll(validUsageModes);
		acModelListenerSupport.notifyValidUsageModesChanged(oldModes, new HashSet<DataUsage>(validUsageModes));
	}

	public abstract boolean isAuthorizedForTransaction(String subject, String transaction) throws CompatibilityException;
	
	public abstract boolean isAuthorizedForObject(String subject, String object) throws CompatibilityException;
	
	public abstract boolean isAuthorizedForObject(String subject, String object, DataUsage dataUsageMode) throws CompatibilityException;
	
	public abstract List<String> getAuthorizedSubjectsForTransaction(String transaction) throws CompatibilityException;
	
	public abstract List<String> getAuthorizedSubjectsForObject(String object) throws CompatibilityException;
	
	public abstract Map<String, Set<DataUsage>> getAuthorizedSubjectsAndPermissionsForObject(String object) throws CompatibilityException;
	
	public abstract List<String> getAuthorizedTransactionsForSubject(String subject) throws CompatibilityException;
	
	public abstract List<String> getAuthorizedObjectsForSubject(String subject) throws CompatibilityException;
	
	public abstract Set<DataUsage> getObjectPermissionsForSubject(String subject, String object) throws CompatibilityException;
	
	public abstract Map<String, Set<DataUsage>> getObjectPermissionsForSubject(String subject) throws CompatibilityException;
	
	public abstract Set<String> getTransactionPermissionsForSubject(String subject) throws CompatibilityException;
	
	public abstract boolean hasTransactionPermissions();
	
	public abstract boolean hasObjectPermissions();
	
	public T getProperties() throws PropertyException {
		T result = createNewProperties();
		result.setName(getName());
		result.setContextName(context.getName());
		result.setSubjectDescriptor(getSubjectDescriptor());
		result.setValidUsageModes(getValidUsageModes());
		return result;
	}
	
	protected abstract T createNewProperties();
	
	
	
	public boolean isExecutable(String activity) throws CompatibilityException {
		getContext().validateActivity(activity);
		return !getAuthorizedSubjectsForTransaction(activity).isEmpty();
	}
	
	/**
	 * An Access Control Model is considered valid, if all transactions are executable.
	 * @return <code>true</code> if all transactions are executable;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean isValid(){
		try {
			checkValidity();
		} catch (ACMValidationException e) {
			return false;
		}
		return true; 
	}
	
	/**
	 * An Access Control Model is considered valid, if all transactions are executable.
	 */
	public void checkValidity() throws ACMValidationException{
		if(context == null)
			throw new ACMValidationException("Invalid state of AC model: No context assigned");
		if(!getContext().containsActivities())
			return;
		for(String activity: getContext().getActivities()){
			try {
				if(!isExecutable(activity)){
					throw new ACMValidationException("Model contains non-executable transactions: " + activity);
				}
			} catch (CompatibilityException e) {
				throw new ACMValidationException("Error during validation check: " + e.getMessage());
			}
		}
	}
	
	protected void validateNewUsageModes(Collection<DataUsage> validUsageModes) {
		Validate.notNull(validUsageModes);
		Validate.notEmpty(validUsageModes);
		Validate.noNullElements(validUsageModes);
	}
	
	protected void validateUsageModes(Collection<DataUsage> usageModes) {
		Validate.notNull(usageModes);
		Validate.noNullElements(usageModes);
		if(!validUsageModes.containsAll(usageModes))
			throw new ParameterException(ErrorCode.INCOMPATIBILITY, "Invalid usage mode. Permitted values: " + validUsageModes);
	}

	
	protected void validateSubjectPermissions(Map<String, Set<DataUsage>> permissions) {
		Validate.notNull(permissions);
		getContext().validateSubjects(permissions.keySet());
		for(Set<DataUsage> subjectPermissions: permissions.values()){
			validateUsageModes(subjectPermissions);
		}
	}
	
	protected void validateObjectPermissions(Map<String, Set<DataUsage>> permissions) {
		Validate.notNull(permissions);
		if(permissions.isEmpty())
			return;
		getContext().validateObjects(permissions.keySet());
		for(Set<DataUsage> objectPermissions: permissions.values()){
			validateUsageModes(objectPermissions);
		}
	}
	
	protected String getStructureString(){
		StringBuilder builder = new StringBuilder();
		builder.append("    ");
		builder.append(getSubjectDescriptor().toLowerCase());
		builder.append(": ");
		builder.append(getContext().getSubjects());
		builder.append('\n');
		builder.append("transactions: " + getContext().getActivities());
		builder.append('\n');
		builder.append("     objects: " + getContext().getObjects());
		builder.append('\n');
		return builder.toString();
	}
	
	protected String getTransactionPermissionsString(){
		StringBuilder builder = new StringBuilder();

		if (hasTransactionPermissions()) {
			builder.append('\n');
			builder.append("transaction permissions:");
			builder.append('\n');
			List<String> transactionList;
			for (String subject : getContext().getSubjects()) {
				transactionList = getAuthorizedTransactionsForSubject(subject);
				if (!transactionList.isEmpty()) {
					builder.append(subject);
					builder.append(": ");
					builder.append(transactionList);
					builder.append('\n');
				}
			}
		}
		return builder.toString();
	}
	
	protected String getObjectPermissionsString(){
		StringBuilder builder = new StringBuilder();
		if (hasObjectPermissions()) {
			builder.append('\n');
			builder.append("Object permissions:");
			builder.append('\n');
			builder.append('\n');
			List<String> objectList;
			for (String subject : getContext().getSubjects()) {
				objectList = getAuthorizedObjectsForSubject(subject);
				if (!objectList.isEmpty()) {
					builder.append(subject);
					builder.append(": ");
					builder.append(objectList);
					builder.append('\n');
				}
			}
		}
		return builder.toString();
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("ACModel{");
		builder.append('\n');
		builder.append(" name: ");
		builder.append(getName());
		builder.append('\n');
		builder.append(getStructureString());
		builder.append(getTransactionPermissionsString());
		builder.append(getObjectPermissionsString());
		builder.append('}');
		builder.append('\n');
		return builder.toString();
	}

	@Override
	public void subjectAdded(String subject) {}

	@Override
	public void subjectRemoved(String subject) {}

	@Override
	public void objectAdded(String object) {}

	@Override
	public void objectRemoved(String object) {}

	@Override
	public void activityAdded(String transaction) {}

	@Override
	public void activityRemoved(String transaction) {}
	
	@Override
	public SOABaseChangeReply allowSubjectRemoval(String subject) {
		return new SOABaseChangeReply(this, true, subject);
	}

	@Override
	public SOABaseChangeReply allowObjectRemoval(String object) {
		return new SOABaseChangeReply(this, true, object);
	}

	@Override
	public SOABaseChangeReply allowActivityRemoval(String activity) {
		return new SOABaseChangeReply(this, true, activity);
	}
	
	@Override
	public String getListenerDescription() {
		return "AC model " + getName();
	}

	@Override
	public void validUsageModesChanged(AbstractACModel<?> sender, Set<DataUsage> oldModes, Set<DataUsage> newModes) {}

	@Override
	public void contextChanged(AbstractACModel<?> sender, SOABase context) {}

	@Override
	public void accessPermissionAdded(AbstractACModel<?> sender, String subject, String object, Collection<DataUsage> dataUsageModes) {}

	@Override
	public void accessPermissionRemoved(AbstractACModel<?> sender, String subject, String object, Collection<DataUsage> dataUsageModes) {}

	@Override
	public void executionPermissionAdded(AbstractACModel<?> sender, String subject, String transaction) {}

	@Override
	public void executionPermissionRemoved(AbstractACModel<?> sender, String subject, String transaction) {}
	
	@Override
	public abstract AbstractACModel<T> clone();
	
	public abstract void takeoverValues(AbstractACModel<T> other) throws Exception;
	
	public abstract void resetPermissions();
}
