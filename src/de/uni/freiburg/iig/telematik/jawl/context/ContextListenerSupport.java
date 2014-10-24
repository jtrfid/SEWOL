package de.uni.freiburg.iig.telematik.jawl.context;

import de.invation.code.toval.event.AbstractListenerSupport;

public class ContextListenerSupport extends AbstractListenerSupport<ContextListener>{

	private static final long serialVersionUID = 5444506073390015628L;

	public void notifySubjectAdded(String subject){
		for(ContextListener listener: listeners){
			listener.subjectAdded(subject);
		}
	}
	
	public void notifySubjectRemoved(String subject){
		for(ContextListener listener: listeners){
			listener.subjectRemoved(subject);
		}
	}
	
	public void notifyObjectAdded(String object){
		for(ContextListener listener: listeners){
			listener.objectAdded(object);
		}
	}
	
	public void notifyObjectRemoved(String object){
		for(ContextListener listener: listeners){
			listener.objectRemoved(object);
		}
	}
	
	public void notifyActivityAdded(String activity){
		for(ContextListener listener: listeners){
			listener.activityAdded(activity);
		}
	}
	
	public void notifyActivityRemoved(String activity){
		for(ContextListener listener: listeners){
			listener.activityRemoved(activity);
		}
	}
	

}
