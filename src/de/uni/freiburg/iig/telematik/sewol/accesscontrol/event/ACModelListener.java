package de.uni.freiburg.iig.telematik.sewol.accesscontrol.event;

import java.util.Collection;
import java.util.Set;

import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.types.DataUsage;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;

public interface ACModelListener {
	
	@SuppressWarnings("rawtypes")
	public void validUsageModesChanged(AbstractACModel sender, Set<DataUsage> oldModes, Set<DataUsage> newModes);
	
	@SuppressWarnings("rawtypes")
	public void contextChanged(AbstractACModel sender, SOABase context);
	
	@SuppressWarnings("rawtypes")
	public void accessPermissionAdded(AbstractACModel sender, String subject, String object, Collection<DataUsage> dataUsageModes);
	
	@SuppressWarnings("rawtypes")
	public void accessPermissionRemoved(AbstractACModel sender, String subject, String object, Collection<DataUsage> dataUsageModes);
	
	@SuppressWarnings("rawtypes")
	public void executionPermissionAdded(AbstractACModel sender, String subject, String transaction);
	
	@SuppressWarnings("rawtypes")
	public void executionPermissionRemoved(AbstractACModel sender, String subject, String transaction);
}
