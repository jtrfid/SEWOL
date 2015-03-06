package de.uni.freiburg.iig.telematik.sewol.accesscontrol.event;

import java.util.Set;

import de.invation.code.toval.types.DataUsage;

public interface ACModelListener {
	
	public void validUsageModesChanged(Set<DataUsage> oldModes, Set<DataUsage> newModes);
}
