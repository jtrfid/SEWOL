package de.uni.freiburg.iig.telematik.sewol.context.process;

import java.util.Collection;

import de.invation.code.toval.event.AbstractListenerSupport;
import de.invation.code.toval.types.DataUsage;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;


public class ProcessContextListenerSupport extends AbstractListenerSupport<ProcessContextListener>{

	private static final long serialVersionUID = 7745740154715410397L;

	public void notifyACModelSet(AbstractACModel<?> acModel){
		for(ProcessContextListener listener: listeners){
			listener.acModelSet(acModel);
		}
	}
	
	public void notifyACModelRemoved(){
		for(ProcessContextListener listener: listeners){
			listener.acModelRemoved();
		}
	}
	
	public void notifyValidUsageModesChange(Collection<DataUsage> validUsageModes){
		for(ProcessContextListener listener: listeners){
			listener.validUsageModesChanged(validUsageModes);
		}
	}

}
