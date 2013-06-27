package de.uni.freiburg.iig.telematik.jawl.log;


public class ModificationException extends Exception {
	
	private static final long serialVersionUID = 1L;
	private EntryField affectedField = null;
	
	public ModificationException(EntryField lockedField, String message){
		super(message);
		this.affectedField = lockedField;
	}
	
	public EntryField getAffectedField(){
		return affectedField;
	}

}
