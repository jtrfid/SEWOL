package de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.graphic;
import org.apache.commons.collections15.Transformer;


public class RoleEdgeLabeller implements Transformer<String,String>{

	@Override
	public String transform(String input) {
//		return input.replaceAll("-", " dominates ");
		return "";
	}

}
