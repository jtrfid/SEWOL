package de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.invation.code.toval.graphic.dialog.DialogObject;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jagal.graph.Edge;
import de.uni.freiburg.iig.telematik.jagal.graph.Graph;
import de.uni.freiburg.iig.telematik.jagal.graph.Vertex;
import de.uni.freiburg.iig.telematik.jagal.graph.exception.GraphException;
import de.uni.freiburg.iig.telematik.jagal.graph.exception.VertexNotFoundException;
import de.uni.freiburg.iig.telematik.jagal.traverse.TraversalUtils;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.event.RoleLatticeListener;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.event.RoleLatticeListenerSupport;


public class RoleLattice implements Cloneable, DialogObject<RoleLattice>{
	
	protected Set<String> roles = new HashSet<String>();
	protected Graph<String> lattice = new Graph<String>();
	protected RoleLatticeListenerSupport listenerSupport = new RoleLatticeListenerSupport();
	
	public RoleLattice() {}
	
	public RoleLattice(Collection<String> roles) {
		setRoles(roles);
	}
	
	public void addRoleLatticeListener(RoleLatticeListener listener){
		Validate.notNull(listener);
		listenerSupport.addRoleLatticeListener(listener);
	}

	public void removeRoleLatticeListener(RoleLatticeListener listener) {
		listenerSupport.removeRoleLatticeListener(listener);
	}
	
	public Set<String> getRoles(){
		return Collections.unmodifiableSet(roles);
	}
	
	public boolean containsRole(String role){
		Validate.notNull(role);
		return roles.contains(role);
	}
	
	public boolean containsRoles(Collection<String> roles){
		Validate.notNull(roles);
		return this.roles.containsAll(roles);
	}
	
	public void setRoles(Collection<String> roles){
		Validate.notNull(roles);
		Validate.notEmpty(roles);
		Validate.noNullElements(roles);
		Set<String> newRoles = new HashSet<String>(roles);
		newRoles.removeAll(getRoles());
		Set<String> obsoleteRoles = new HashSet<String>(getRoles());
		obsoleteRoles.removeAll(roles);
		addRoles(newRoles);
		removeRoles(obsoleteRoles);
		lattice = new Graph<String>();
		lattice.addVertices(roles);
	}
	
	public void addRoles(Collection<String> roles){
		Validate.notNull(roles);
		if(roles.isEmpty())
			return;
		for(String role: roles){
			addRole(role);
		}
	}
	
	public void addRole(String role){
		Validate.notNull(role);
		this.roles.add(role);
		lattice.addVertex(role);
		try {
			listenerSupport.notifyRoleAdded(role);
		} catch (Exception e) {
			throw new ParameterException(e.getMessage());
		}
	}
	
	public Set<RoleRelation> getRoleRelations(){
		Set<RoleRelation> result = new HashSet<RoleRelation>();
		for(Edge<Vertex<String>> edge: lattice.getEdges()){
			result.add(new RoleRelation(edge.getSource().getName(), edge.getTarget().getName()));
		}
		return result;
	}
	
	/**
	 * Returns all roles within the lattice that dominate the given role.
	 * @param role A role of the lattice
	 * @return All dominating roles.
	 * @throws CompatibilityException If the role is not known.
	 */
	public Set<String> getDominatingRolesFor(String role) throws CompatibilityException{
		return getDominatingRolesFor(role, true);
	}
	
	public Set<String> getDominatingRolesFor(String role, boolean transitive) throws CompatibilityException {
		validateRole(role);
		Set<String> dominatingRoles = new HashSet<String>();
		try {
			if(!transitive){
				for(Vertex<String> parentVertex : lattice.getParents(role))
					dominatingRoles.add(parentVertex.getName());
			} else {
				for(Vertex<String> predecessorVertex: TraversalUtils.getPredecessorsFor(lattice, lattice.getVertex(role))){
					dominatingRoles.add(predecessorVertex.getName());
				}
			}
		} catch (GraphException e) {
			// Cannot happen, since the role is contained in the lattice (enforced by setRoles).
			e.printStackTrace();
		}
		return dominatingRoles;
	}
	
	
	/**
	 * Returns all roles within the lattice that are dominated by the given role.
	 * @param role A role of the lattice
	 * @return All dominated roles.
	 * @throws CompatibilityException If the role is not known.
	 */
	public Set<String> getDominatedRolesFor(String role) throws CompatibilityException{
		return getDominatedRolesFor(role, true);
	}
	
	public Set<String> getDominatedRolesFor(String role, boolean transitive) throws CompatibilityException {
		validateRole(role);
		Set<String> dominatedRoles = new HashSet<String>();
		try {
			if(!transitive){
				for(Vertex<String> childVertex : lattice.getChildren(role))
					dominatedRoles.add(childVertex.getName());
			} else {
				for(Vertex<String> successorVertex: TraversalUtils.getSuccessorsFor(lattice, lattice.getVertex(role))){
					dominatedRoles.add(successorVertex.getName());
				}
			}
		} catch (GraphException e) {
			// Cannot happen, since the role is contained in the lattice (enforced by setRoles).
			e.printStackTrace();
		}
		return dominatedRoles;
	}
	
	public boolean addRelation(RoleRelation relation){
		Validate.notNull(relation);
		return addRelation(relation.getDominatingRole(), relation.getDominatedRole());
	}
	
	public boolean addRelation(String dominatingRole, String dominatedRole){
		validateRole(dominatedRole);
		validateRole(dominatingRole);
		try {
			return (lattice.addEdge(dominatingRole, dominatedRole) != null);
		} catch(VertexNotFoundException e){
			// Cannot happen, since both roles are in the lattice (enforced by setRoles).
			return false;
		}
	}
	
	public boolean removeRelation(String dominatingRole, String dominatedRole){
		validateRole(dominatedRole);
		validateRole(dominatingRole);
		try {
			return lattice.removeEdge(dominatingRole, dominatedRole);
		} catch(GraphException e){
			return false;
		}
	}
	
	public void removeRoles(Collection<String> roles){
		Validate.notNull(roles);
		if(roles.isEmpty())
			return;
		for(String role: roles){
			removeRole(role);
		}
	}
	
	public boolean removeRole(String role) throws CompatibilityException{
		validateRole(role);
		try {
			if(roles.remove(role)){
				boolean success = lattice.removeVertex(role);
				try {
					listenerSupport.notifyRoleRemoved(role);
				} catch (Exception e) {
					throw new ParameterException(e.getMessage());
				}
				return success;
			}
		} catch(GraphException e){
			return false;
		}
		return false;
	}
	
	protected void validateRole(String role) throws CompatibilityException{
		Validate.notNull(role);
		if(!this.roles.contains(role))
			throw new CompatibilityException("Unknown role");
	}
	
	@Override
	public RoleLattice clone(){
		if(getRoles().isEmpty())
			return new RoleLattice();
		RoleLattice result = new RoleLattice();
		result.takeoverValues(this);
		return result;
	}
	
	public void takeoverValues(RoleLattice lattice){
		setRoles(lattice.getRoles());
		for(RoleRelation relation: lattice.getRoleRelations()){
			addRelation(relation);
		}
	}
	
	@Override
	public String toString(){
		return lattice.toString();
	}

}
