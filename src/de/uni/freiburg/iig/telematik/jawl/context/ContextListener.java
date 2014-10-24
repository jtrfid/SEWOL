package de.uni.freiburg.iig.telematik.jawl.context;


public interface ContextListener {
	
	public void subjectAdded(String subject);
	
	public void subjectRemoved(String subject);
	
	public void objectAdded(String object);
	
	public void objectRemoved(String object);
	
	public void activityAdded(String activities);
	
	public void activityRemoved(String activities);

}
