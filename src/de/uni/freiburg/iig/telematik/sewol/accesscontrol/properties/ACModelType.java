package de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties;


import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.invation.code.toval.validate.Validate;

public enum ACModelType {
	
	ACL, RBAC;
	
	public static ACModelType parse(String acModelTypeString) {
		Validate.notNull(acModelTypeString);
		Validate.notEmpty(acModelTypeString);
		
		for(ACModelType modelType: ACModelType.values()){
			if(acModelTypeString.toUpperCase().equals(modelType.toString()))
				return modelType;
		}

		throw new ParameterException(ErrorCode.INCOMPATIBILITY);
	}

}
