package de.uni.freiburg.iig.telematik.sewol.accesscontrol.event;

import java.util.Collection;
import java.util.Set;

import de.invation.code.toval.event.AbstractListenerSupport;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.types.DataUsage;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;

public class ACModelListenerSupport extends AbstractListenerSupport<ACModelListener>{

	private static final long serialVersionUID = -5210920321260360927L;
	
	@SuppressWarnings("rawtypes")
	private AbstractACModel acModel = null;
	
	@SuppressWarnings("rawtypes")
	public ACModelListenerSupport(AbstractACModel acModel) {
		super();
		this.acModel = acModel;
	}

	public void notifyValidUsageModesChanged(Set<DataUsage> oldModes, Set<DataUsage> newModes){
		for(ACModelListener listener: listeners){
			listener.validUsageModesChanged(acModel, oldModes, newModes);
		}
	}
	
	public void notifyContextChanged(SOABase context){
		for(ACModelListener listener: listeners){
			listener.contextChanged(acModel, context);
		}
	}
	
	public void notifyAccessPermissionAdded(String subject, String object, Collection<DataUsage> dataUsageModes){
		for(ACModelListener listener: listeners){
			listener.accessPermissionAdded(acModel, subject, object, dataUsageModes);
		}
	}
	
	public void notifyAccessPermissionRemoved(String subject, String object, Collection<DataUsage> dataUsageModes){
		for(ACModelListener listener: listeners){
			listener.accessPermissionRemoved(acModel, subject, object, dataUsageModes);
		}
	}
	
	public void notifyExecutionPermissionAdded(String subject, String transaction){
		for(ACModelListener listener: listeners){
			listener.executionPermissionAdded(acModel, subject, transaction);
		}
	}
	
	public void notifyExecutionPermissionRemoved(String subject, String transaction){
		for(ACModelListener listener: listeners){
			listener.executionPermissionRemoved(acModel, subject, transaction);
		}
	}

}
