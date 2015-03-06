package de.uni.freiburg.iig.telematik.sewol.log;


public class LockingException extends ModificationException {
	
	private static final long serialVersionUID = 1L;
	private static final String F_LOCKING_MSG = "Exception due to locked field \"%s\".";
	
	public LockingException(EntryField affectedField){
		super(affectedField, String.format(F_LOCKING_MSG, affectedField));
	}
	
	public LockingException(EntryField lockedField, String message){
		super(lockedField, message);
	}

}
