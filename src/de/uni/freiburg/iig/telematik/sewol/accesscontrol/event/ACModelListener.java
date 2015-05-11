package de.uni.freiburg.iig.telematik.sewol.accesscontrol.event;

import java.util.Collection;
import java.util.Set;

import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.types.DataUsage;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;

public interface ACModelListener {

	public void validUsageModesChanged(AbstractACModel<?> sender, Set<DataUsage> oldModes, Set<DataUsage> newModes);

	public void contextChanged(AbstractACModel<?> sender, SOABase context);

	public void accessPermissionAdded(AbstractACModel<?> sender, String subject, String object, Collection<DataUsage> dataUsageModes);

	public void accessPermissionRemoved(AbstractACModel<?> sender, String subject, String object, Collection<DataUsage> dataUsageModes);

	public void executionPermissionAdded(AbstractACModel<?> sender, String subject, String transaction);

	public void executionPermissionRemoved(AbstractACModel<?> sender, String subject, String transaction);
}
