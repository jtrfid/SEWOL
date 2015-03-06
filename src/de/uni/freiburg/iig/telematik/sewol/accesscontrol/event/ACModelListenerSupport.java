package de.uni.freiburg.iig.telematik.sewol.accesscontrol.event;

import java.util.Set;

import de.invation.code.toval.event.AbstractListenerSupport;
import de.invation.code.toval.types.DataUsage;

public class ACModelListenerSupport extends AbstractListenerSupport<ACModelListener>{

	private static final long serialVersionUID = -5210920321260360927L;
	
	public void notifyValidUsageModesChanged(Set<DataUsage> oldModes, Set<DataUsage> newModes){
		for(ACModelListener listener: listeners){
			listener.validUsageModesChanged(oldModes, newModes);
		}
	}

}
