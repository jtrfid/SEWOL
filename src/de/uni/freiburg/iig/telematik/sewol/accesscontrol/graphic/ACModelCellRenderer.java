package de.uni.freiburg.iig.telematik.sewol.accesscontrol.graphic;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;

@SuppressWarnings("rawtypes")
public class ACModelCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 4710491454568527897L;

	public ACModelCellRenderer() {
		super();
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if(value instanceof AbstractACModel){
			setText(((AbstractACModel) value).getName());
		} else {
			setText("undef.");
		}
		return this;
	}


}
