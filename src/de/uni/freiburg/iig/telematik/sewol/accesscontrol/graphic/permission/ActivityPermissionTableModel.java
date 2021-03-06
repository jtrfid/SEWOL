package de.uni.freiburg.iig.telematik.sewol.accesscontrol.graphic.permission;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.acl.ACLModel;


public class ActivityPermissionTableModel extends AbstractTableModel implements ItemListener {
	
	private static final long serialVersionUID = -4658501093720360981L;
	
	private static final Dimension DEFAULT_CELL_SIZE = new Dimension(10,10);
	
	private List<String> colNames = null;
	private List<String> rowNames = null;
	private ACLModel aclModel = null;
	private ActivityPermissionPanel[][] permissionPanels = null;
	private List<ItemListener> itemListeners = new ArrayList<ItemListener>();
	
	public ActivityPermissionTableModel(ACLModel aclModel){
		rowNames = new ArrayList<String>(aclModel.getContext().getSubjects());
		Collections.sort(rowNames);
		colNames = new ArrayList<String>(aclModel.getContext().getActivities());
		Collections.sort(colNames);
		this.aclModel = aclModel;

		permissionPanels = new ActivityPermissionPanel[rowNames.size()][colNames.size()];
		for(int i=0; i<rowNames.size(); i++){
			for(int j=0; j<colNames.size(); j++){
				permissionPanels[i][j] = new ActivityPermissionPanel(rowNames.get(i) + " - " + colNames.get(j));
				permissionPanels[i][j].addItemListener(this);
				try {
					permissionPanels[i][j].setPermission(aclModel.isAuthorizedForTransaction(rowNames.get(i), colNames.get(j)));
				} catch (CompatibilityException e) {
					e.printStackTrace();
				} catch (ParameterException e) {
					e.printStackTrace();
				}
			}	
		}
		update();
	}
	
	public String getRowName(int index){
		return rowNames.get(index);
	}
	
	public Dimension preferredCellSize(){
		if(permissionPanels.length > 0)
			return permissionPanels[0][0].getPreferredSize();
		return DEFAULT_CELL_SIZE;
	}
	
	public void reset(){
		for(int i=0; i<rowNames.size(); i++){
			for(int j=0; j<colNames.size(); j++){
				permissionPanels[i][j].setPermission(false);
			}
		}
		fireTableDataChanged();
	}
	
	public void update(){
		for(int i=0; i<rowNames.size(); i++){
			for(int j=0; j<colNames.size(); j++){
				try {
					permissionPanels[i][j].setPermission(aclModel.isAuthorizedForTransaction(rowNames.get(i), colNames.get(j)));
				} catch (CompatibilityException e) {
					e.printStackTrace();
				} catch (ParameterException e) {
					e.printStackTrace();
				}
			}
		}
		fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		return rowNames.size();
	}

	@Override
	public int getColumnCount() {
		return colNames.size()+1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0)
			return rowNames.get(rowIndex);
		return permissionPanels[rowIndex][columnIndex-1];
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
    }

	@Override
	public String getColumnName(int column) {
		if(column == 0)
			return "";
		return colNames.get(column-1);
	}

    public boolean isCellEditable(int row, int col) {
    	if(col == 0)
    		return false;
    	return true;
    }
    
    public void addItemListener(ItemListener listener){
		itemListeners.add(listener);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		for(ItemListener listener: itemListeners)
			listener.itemStateChanged(e);
	}

	
}