package de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.event;

public interface RoleLatticeListener {
	
	public void roleAdded(String roleName) throws Exception;
	
	public void roleRemoved(String roleName) throws Exception;
}
