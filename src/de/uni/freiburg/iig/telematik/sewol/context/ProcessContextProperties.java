package de.uni.freiburg.iig.telematik.sewol.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.misc.soabase.SOABaseProperties;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACModelProperty;

public class ProcessContextProperties extends SOABaseProperties{
	
	private final String DATA_USAGE_FORMAT = ProcessContextProperty.DATA_USAGE + "_%s";
	private final String DATA_USAGE_VALUE_FORMAT = "\"%s\" %s";
	private final String ACTIVITY_DATA_USAGES_FORMAT = ProcessContextProperty.ACTIVITY_DATA_USAGES + "_%s";
	
	//------- Property setting -------------------------------------------------------------
	
	private void setProperty(ProcessContextProperty contextProperty, Object value){
		props.setProperty(contextProperty.toString(), value.toString());
	}
	
	private String getProperty(ProcessContextProperty contextProperty){
		return props.getProperty(contextProperty.toString());
	}
	
	//-- AC Model
	
	public void setACModelName(String acModelName) {
		validateStringValue(acModelName);
		setProperty(ProcessContextProperty.AC_MODEL_NAME, acModelName);
	}
	
	public String getACModelName() throws PropertyException {
		String propertyValue = getProperty(ProcessContextProperty.AC_MODEL_NAME);
		if(propertyValue == null)
			throw new PropertyException(ProcessContextProperty.AC_MODEL_NAME, propertyValue);
		
		validateStringValue(propertyValue);
		
		return propertyValue;
	}
	
	// Data Usage
	
	public void setDataUsage(String activity, Map<String, Set<DataUsage>> dataUsage) throws PropertyException{
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Validate.notNull(dataUsage);
		Validate.noNullElements(dataUsage.keySet());
		Validate.noNullElements(dataUsage.values());
		
		//1. Add data usages
		//   This also adds the data usages to the list of data usages
		List<String> propertyNamesForDataUsages = new ArrayList<String>();
		for(String attribute: dataUsage.keySet()){
			propertyNamesForDataUsages.add(addDataUsage(attribute, dataUsage.get(attribute)));
		}
		
		//2. Add data usage names to the list of data usages for this activity
		addActivityWithDataUsage(activity);
		props.setProperty(String.format(ACTIVITY_DATA_USAGES_FORMAT, activity), ArrayUtils.toString(propertyNamesForDataUsages.toArray()));
	}
	
	public Map<String, Set<DataUsage>> getDataUsageFor(String activity) throws PropertyException{
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		
		Set<String> dataUsageNames = getDataUsageNames(activity);
		Map<String, Set<DataUsage>> result = new HashMap<String, Set<DataUsage>>();
		for(String dataUsageName: dataUsageNames){
			Map<String, Set<DataUsage>> dataUsage = getDataUsage(dataUsageName);
			String attribute = dataUsage.keySet().iterator().next();
			result.put(attribute, dataUsage.get(attribute));
		}
		return result;
	}
	
	/**
	 * Adds the given data usage to the context properties.<br>
	 * This requires to generate a new name for the data usage (e.g. DATA_USAGE_1) <br>
	 * and storing a string-representation of this data usage under this name.<br>
	 * Additionally, the new data usage name is stored in the property field which is summing up all data usage names (ACTIVITY_DATA_USAGES).
	 * @return The newly generated name for the data usage under which it is accessible.
	 * @throws ParameterException if the given data usage parameters are invalid. 
	 * @throws PropertyException if the data usage property name cannot be generated or the data usage cannot be stored.
	 * @see #getNextDataUsageIndex()
	 * @see #addDataUsageNameToList(String)
	 */
	private String addDataUsage(String attribute, Set<DataUsage> usages) throws PropertyException{
		Validate.notNull(attribute);
		Validate.notEmpty(attribute);
		Validate.notNull(usages);
		Validate.noNullElements(usages);
		String dataUsageName = String.format(DATA_USAGE_FORMAT, getNextDataUsageIndex());
		props.setProperty(dataUsageName, String.format(DATA_USAGE_VALUE_FORMAT, attribute, ArrayUtils.toString(usages.toArray())));
		addDataUsageNameToList(dataUsageName);
		return dataUsageName;
	}
	
	/**
	 * Extracts the data usage with the given name in form of a map containing the attribute as key and the data usages as value.<br>
	 * @param dataUsageName The property-name of the data usage (DATA_USAGE_X)
	 * @return The map-representation of the data usage-property
	 * @throws PropertyException if there is no constraint with the given name or the value cannot be converted into a number- or string-constraint.
	 */
	private Map<String, Set<DataUsage>> getDataUsage(String dataUsageName) throws PropertyException{
		String dataUsageString = props.getProperty(dataUsageName);
		if(dataUsageString == null)
			throw new PropertyException(ProcessContextProperty.DATA_USAGE, dataUsageName, "No data usage with name \""+dataUsageName+"\"");
		Map<String, Set<DataUsage>> result = new HashMap<String, Set<DataUsage>>();
		int delimiterIndex = dataUsageString.indexOf(" ");
		if(delimiterIndex == -1)
			throw new PropertyException(ProcessContextProperty.DATA_USAGE, dataUsageName, "Invalid property value for data usage with name \""+dataUsageName+"\"");
		String attributeString = null;
		String dataUsagesString = null;
		try {
			attributeString = dataUsageString.substring(0, delimiterIndex);
			dataUsagesString = dataUsageString.substring(delimiterIndex+1);
			
			attributeString = attributeString.substring(1, attributeString.length()-1);
		} catch(Exception e){
			throw new PropertyException(ProcessContextProperty.DATA_USAGE, dataUsageName, "Invalid property value for data usage with name \""+dataUsageName+"\"");
		}
		
		Set<DataUsage> usageModes = new HashSet<DataUsage>();
		StringTokenizer usageModeTokens = StringUtils.splitArrayString(dataUsagesString, " ");
		while(usageModeTokens.hasMoreTokens()){
			try {
				usageModes.add(DataUsage.parse(usageModeTokens.nextToken()));
			} catch (ParameterException e) {
				throw new PropertyException(ProcessContextProperty.DATA_USAGE, dataUsageName, "Invalid property value for data usage with name \""+dataUsageName+"\"");
			}
		}
		result.put(attributeString, usageModes);
		
		return result;
	}
	
	/**
	 * Adds an activity to the list of activities with data usage.
	 * @param activity The name of the activity to add.
	 * @throws ParameterException if the activity name is invalid.
	 */
	private void addActivityWithDataUsage(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> currentActivities = getActivitiesWithDataUsage();
		currentActivities.add(activity);
		setProperty(ProcessContextProperty.ACTIVITIES_WITH_DATA_USAGE, ArrayUtils.toString(currentActivities.toArray()));
	}
	
	/**
	 * Returns the names of all activities with data usage.
	 * @return A set of all activities with data usage.
	 */
	public Set<String> getActivitiesWithDataUsage(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(ProcessContextProperty.ACTIVITIES_WITH_DATA_USAGE);
		if(propertyValue == null)
			return result;
		StringTokenizer activityTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(activityTokens.hasMoreTokens()){
			result.add(activityTokens.nextToken());
		}
		return result;
	}
	
	/**
	 * Adds a new data usage property name to the list of data usage properties (DATA_USAGES-field).
	 * @param dataUsageName The name of the data usage-property to add (e.g. DATA_USAGE_5).
	 * @throws ParameterException if the given property name is invalid.
	 */
	private void addDataUsageNameToList(String dataUsageName) {
		validateStringValue(dataUsageName);
		Set<String> currentValues = getDataUsageNameList();
		currentValues.add(dataUsageName);
		setProperty(ProcessContextProperty.ALL_DATA_USAGES, ArrayUtils.toString(currentValues.toArray()));
	}
	
	/**
	 * Returns the lowest unused index for data usage names.<br>
	 * Data usage names are enumerated (DATA_USAGE_1, DATA_USAGE_2, ...).<br>
	 * When new data usages are added, the lowest unused index is used as property name.
	 * @return The lowest free index to be used for data usage naming.
	 * @throws PropertyException if the extraction of used indexes fails.
	 * @see #getDataUsageNameIndexes()
	 */
	private int getNextDataUsageIndex() throws PropertyException{
		Set<Integer> usedIndexes = getDataUsageNameIndexes();
		int nextIndex = 1;
		while(usedIndexes.contains(nextIndex)){
			nextIndex++;
		}
		return nextIndex;
	}
	
	/**
	 * Returns all used indexes for data usage property names.<br>
	 * Constraint names are enumerated (DATA_USAGE_1, DATA_USAGE_2, ...).<br>
	 * When new data usages are added, the lowest unused index is used within the new property name.
	 * @return The set of indexes in use.
	 * @throws PropertyException if existing constraint names are invalid (e.g. due to external file manipulation).
	 */
	private Set<Integer> getDataUsageNameIndexes() throws PropertyException{
		Set<Integer> result = new HashSet<Integer>();
		Set<String> dataUsageNames = getDataUsageNameList();
		if(dataUsageNames.isEmpty())
			return result;
		for(String dataUsageName: dataUsageNames){
			int separatorIndex = dataUsageName.lastIndexOf("_");
			if(separatorIndex == -1 || (dataUsageName.length() == separatorIndex + 1))
				throw new PropertyException(ProcessContextProperty.DATA_USAGE, dataUsageName, "Corrupted property file (invalid data usage name)");
			Integer index = null;
			try {
				index = Integer.parseInt(dataUsageName.substring(separatorIndex+1));
			} catch(Exception e){
				throw new PropertyException(ProcessContextProperty.DATA_USAGE, dataUsageName, "Corrupted property file (invalid data usage name)");
			}
			result.add(index);
		}
		return result;
	}
	
	/**
	 * Returns the property-names of all data usages related to the given activity.<br>
	 * These names are required to extract data usage property-values.
	 * @param activity The name of the activity
	 * @return A set of data usage property-names related to the given activity.
	 * @throws ParameterException if the given activity name is <code>null</code or invalid.
	 */
	private Set<String> getDataUsageNames(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> result = new HashSet<String>();
		String propertyValue = props.getProperty(String.format(ACTIVITY_DATA_USAGES_FORMAT, activity));
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
	 * Returns all property names for data usages, <br>
	 * i.e. the value of the context property ACTIVITY_DATA_USAGES.
	 * @return A set of all used property names for data usages.
	 */
	private Set<String> getDataUsageNameList(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(ProcessContextProperty.ALL_DATA_USAGES);
		if(propertyValue == null)
			return result;
		StringTokenizer attributeTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(attributeTokens.hasMoreTokens()){
			String nextToken = attributeTokens.nextToken();
			result.add(nextToken);
		}
		return result;
	}
	
	//-- Valid usage modes
	
	public void setValidUsageModes(Collection<DataUsage> validUsageModes) throws PropertyException{
		Validate.notNull(validUsageModes);
		if(validUsageModes.isEmpty())
			return;
		Validate.noNullElements(validUsageModes);
		Set<DataUsage> usageSet = new HashSet<DataUsage>(validUsageModes);
		setProperty(ProcessContextProperty.VALID_USAGE_MODES, ArrayUtils.toString(encapsulateValues(usageSet)));
	}
	
	public Set<DataUsage> getValidUsageModes() throws PropertyException{
		String propertyValue = getProperty(ProcessContextProperty.VALID_USAGE_MODES);
		if(propertyValue == null)
			throw new PropertyException(ACModelProperty.VALID_USAGE_MODES, propertyValue);
		StringTokenizer tokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		Set<DataUsage> result = new HashSet<DataUsage>();
		while(tokens.hasMoreTokens()){
			String nextToken = tokens.nextToken();
			if(nextToken.length() < 3)
				throw new PropertyException(ACModelProperty.VALID_USAGE_MODES, propertyValue);
			nextToken = nextToken.substring(1, nextToken.length()-1);
			DataUsage nextUsage = null;
			try{
				nextUsage = DataUsage.parse(nextToken);
			} catch(Exception e){
				throw new PropertyException(ACModelProperty.VALID_USAGE_MODES, nextToken);
			}
			result.add(nextUsage);
		}
		return result;
	}

}
