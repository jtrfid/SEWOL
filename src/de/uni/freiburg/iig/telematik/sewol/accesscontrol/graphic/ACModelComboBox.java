package de.uni.freiburg.iig.telematik.sewol.accesscontrol.graphic;

import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;

@SuppressWarnings("rawtypes")
public class ACModelComboBox extends JComboBox {

	private static final long serialVersionUID = -7002182269586188L;

	public ACModelComboBox(Collection<AbstractACModel> acModels){
		super();
		Validate.notNull(acModels);
		setModel((new DefaultComboBoxModel(acModels.toArray())));
		setRenderer(new ACModelCellRenderer());
	}

	@Override
	public AbstractACModel getSelectedItem() {
		return (AbstractACModel) super.getSelectedItem();
	}
	
	

}
