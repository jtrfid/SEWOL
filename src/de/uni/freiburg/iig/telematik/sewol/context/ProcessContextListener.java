package de.uni.freiburg.iig.telematik.sewol.context;

import java.util.Collection;

import de.invation.code.toval.misc.soabase.SOABaseListener;
import de.invation.code.toval.types.DataUsage;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;

public interface ProcessContextListener extends SOABaseListener {
	
	public void acModelSet(AbstractACModel<?> acModel);
	
	public void acModelRemoved();
	
	public void validUsageModesChanged(Collection<DataUsage> usageModes);

}
