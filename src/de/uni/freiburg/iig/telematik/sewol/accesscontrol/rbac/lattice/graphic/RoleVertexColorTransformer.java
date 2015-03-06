package de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.graphic;
import java.awt.Color;
import java.awt.Paint;

import org.apache.commons.collections15.Transformer;


public class RoleVertexColorTransformer implements Transformer<String, Paint>{

	@Override
	public Paint transform(String input) {
		return new Color(190, 231, 255);
	}

}
