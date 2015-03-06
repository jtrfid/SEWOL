package de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice;

public class RoleRelation {
	
	private static final String toStringFormat = "%s->%s";
	
	private String dominatingRole = null;
	private String dominatedRole = null;
	
	public RoleRelation(String dominatingRole, String dominatedRole) {
		super();
		this.dominatingRole = dominatingRole;
		this.dominatedRole = dominatedRole;
	}
	
	public String getDominatingRole() {
		return dominatingRole;
	}
	public void setDominatingRole(String dominatingRole) {
		this.dominatingRole = dominatingRole;
	}
	public String getDominatedRole() {
		return dominatedRole;
	}
	public void setDominatedRole(String dominatedRole) {
		this.dominatedRole = dominatedRole;
	}
	
	@Override
	public String toString(){
		return String.format(toStringFormat, getDominatingRole(), getDominatedRole());
	}

}
