package de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.event;

import java.util.HashSet;
import java.util.Set;

import de.invation.code.toval.validate.Validate;

public class RoleLatticeListenerSupport {
	
	private Set<RoleLatticeListener> listeners = new HashSet<RoleLatticeListener>();
	
	public void addRoleLatticeListener(RoleLatticeListener listener){
		Validate.notNull(listener);
		listeners.add(listener);
	}

	public void removeRoleLatticeListener(RoleLatticeListener listener) {
		listeners.remove(listener);
	}
	
	public void notifyRoleAdded(String roleName) throws Exception{
		for(RoleLatticeListener listener: listeners){
			listener.roleAdded(roleName);
		}
	}
	
	public void notifyRoleRemoved(String roleName) throws Exception{
		for(RoleLatticeListener listener: listeners){
			listener.roleRemoved(roleName);
		}
	}
}
