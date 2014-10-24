package de.uni.freiburg.iig.telematik.jawl.context;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.properties.AbstractProperties;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.Validate;

public class ContextProperties extends AbstractProperties{
	
	//------- Property setting -------------------------------------------------------------
	
	private void setProperty(ContextProperty contextProperty, Object value){
		props.setProperty(contextProperty.toString(), value.toString());
	}
	
	private String getProperty(ContextProperty contextProperty){
		return props.getProperty(contextProperty.toString());
	}
	
	//-- Context name
	
	public void setName(String name){
		Validate.notNull(name);
		Validate.notEmpty(name);
		setProperty(ContextProperty.NAME, name);
	}
	
	public String getName() throws PropertyException {
		String propertyValue = getProperty(ContextProperty.NAME);
		if(propertyValue == null)
			throw new PropertyException(ContextProperty.NAME, propertyValue);
		return propertyValue;
	}
	
	//-- Subjects
	
	public void setSubjects(Set<String> subjects){
		Validate.notNull(subjects);
		if(subjects.isEmpty())
			return;
		Validate.noNullElements(subjects);
		setProperty(ContextProperty.SUBJECTS, ArrayUtils.toString(encapsulateValues(subjects)));
	}
	
	public Set<String> getSubjects(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(ContextProperty.SUBJECTS);
		if(propertyValue == null)
			return result;
		StringTokenizer subjectTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(subjectTokens.hasMoreTokens()){
			String nextToken = subjectTokens.nextToken();
			result.add(nextToken.substring(1, nextToken.length()-1));
		}
		return result;
	}
	
	//-- Objects
	
	public void setObjects(Set<String> objects) {
		Validate.notNull(objects);
		if(objects.isEmpty())
			return;
		Validate.noNullElements(objects);
		setProperty(ContextProperty.OBJECTS, ArrayUtils.toString(encapsulateValues(objects)));
	}
	
	public Set<String> getObjects(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(ContextProperty.OBJECTS);
		if(propertyValue == null)
			return result;
		StringTokenizer subjectTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(subjectTokens.hasMoreTokens()){
			String nextToken = subjectTokens.nextToken();
			result.add(nextToken.substring(1, nextToken.length()-1));
		}
		return result;
	}
	
	//-- Activities
	
	public void setActivities(Set<String> transactions){
		Validate.notNull(transactions);
		if(transactions.isEmpty())
			return;
		Validate.noNullElements(transactions);
		setProperty(ContextProperty.ACTIVITIES, ArrayUtils.toString(encapsulateValues(transactions)));
	}
	
	public Set<String> getActivities(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(ContextProperty.ACTIVITIES);
		if(propertyValue == null)
			return result;
		StringTokenizer subjectTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(subjectTokens.hasMoreTokens()){
			String nextToken = subjectTokens.nextToken();
			result.add(nextToken.substring(1, nextToken.length()-1));
		}
		return result;
	}

}
