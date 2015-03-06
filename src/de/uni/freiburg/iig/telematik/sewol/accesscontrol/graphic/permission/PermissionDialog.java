package de.uni.freiburg.iig.telematik.sewol.accesscontrol.graphic.permission;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.invation.code.toval.graphic.dialog.AbstractDialog;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.acl.ACLModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;
import de.uni.freiburg.iig.telematik.sewol.context.ProcessContext;



public class PermissionDialog extends AbstractDialog {
	
	private static final long serialVersionUID = -5216821409053567193L;
	public static final Dimension PREFERRED_SIZE = new Dimension(500, 540);
	public static final int DEFAULT_LABEL_HEIGHT = 24;
	
	private JCheckBox chckbxDeriveAttributePermissions;
	private JComboBox viewComboBox;
	private PermissionTable aclTable;
	
	private ACLModel aclModel;

	@SuppressWarnings("rawtypes")
	public PermissionDialog(Window owner, String title, AbstractACModel acModel) throws Exception {
		super(owner, true, ButtonPanelLayout.CENTERED);
		setTitle(title);
		setIncludeCancelButton(false);
		if(!acModel.getContext().containsActivities())
			throw new ParameterException(ErrorCode.EMPTY, "Access control model does not contain any " + acModel.getContext().getActivityDescriptorPlural().toLowerCase());
		if(!acModel.getContext().containsSubjects())
			throw new ParameterException(ErrorCode.EMPTY, "Access control model does not contain any " + acModel.getContext().getSubjectDescriptorPlural().toLowerCase());
		if(acModel instanceof ACLModel){
			this.aclModel = (ACLModel) acModel;
		} else if(acModel instanceof RBACModel){
			this.aclModel = ((RBACModel) acModel).getRolePermissions();
		}
	}
	
	@Override
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}

	@Override
	protected void addComponents() throws Exception {
		mainPanel().setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		BoxLayout topPanelLayout = new BoxLayout(topPanel, BoxLayout.LINE_AXIS);
		topPanel.setLayout(topPanelLayout);
		topPanel.add(getViewBox());
		if(aclModel.getContext() instanceof ProcessContext)
			topPanel.add(getDeriveBox());
		topPanel.add(Box.createHorizontalGlue());
		mainPanel().add(topPanel, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane(getPermissionTable());
		mainPanel().add(scrollPane, BorderLayout.CENTER);
		
//		aclModel = aclTable.getACLModel();
	}
	
	protected JCheckBox getDeriveBox(){
		if(chckbxDeriveAttributePermissions == null){
			chckbxDeriveAttributePermissions = new JCheckBox("Derive Attribute Permissions");
			chckbxDeriveAttributePermissions.setEnabled(aclModel.getContext().containsObjects());
			chckbxDeriveAttributePermissions.setSelected(false);
			chckbxDeriveAttributePermissions.setToolTipText("<html>In activated state, object permissions are <br>" +
															"derived automatically on basis of activity permissions.<br><br>" +
															"<b>Note:</b> This setting is only effective within this dialog.<br>" +
															"In case data usage information of activities are changed<br>" +
															"in a related context, this will not affect the object<br>" +
															"permissions of the connected access control model.<br>" +
															"This behavior is intentional and guarantees the independence<br>" +
															"of access control models from contexts and their reuse.</html>");
			chckbxDeriveAttributePermissions.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					aclTable.setDeriveAttributePermissions(chckbxDeriveAttributePermissions.isSelected());
				}
			});
		}
		return chckbxDeriveAttributePermissions;
	}
	
	protected JComboBox getViewBox(){
		if(viewComboBox == null){
			viewComboBox = new JComboBox();
			viewComboBox.addItem("Activity Permissions");
			if(aclModel.getContext().containsObjects()){
				viewComboBox.addItem("Attribute Permissions");
			}
			viewComboBox.setSelectedIndex(0);
			viewComboBox.setPreferredSize(new Dimension(200, DEFAULT_LABEL_HEIGHT));
			viewComboBox.setMaximumSize(new Dimension(200, DEFAULT_LABEL_HEIGHT));
			viewComboBox.addItemListener(new ItemListener(){

				@Override
				public void itemStateChanged(ItemEvent e) {
					try {
						if(viewComboBox.getSelectedIndex() == 0){
							aclTable.setView(PermissionTable.VIEW.TRANSACTION);
						} else {
							aclTable.setView(PermissionTable.VIEW.OBJECT);
						}
					} catch (ParameterException e1) {
						e1.printStackTrace();
					}
				}
				
			});
		}
		return viewComboBox;
	}

	@Override
	protected void setTitle() {}

	private PermissionTable getPermissionTable() throws ParameterException{
		if(aclTable == null){
			aclTable = new PermissionTable(aclModel);
	        aclTable.setFillsViewportHeight(true);
		}
		return aclTable;
	}

	public ACLModel getACLModel(){
		return aclModel;
	}
	
	public static ACLModel showDialog(Window owner, String title, ACLModel aclModel) throws Exception{
		PermissionDialog dialog = new PermissionDialog(owner, title, aclModel);
		dialog.setUpGUI();
		return dialog.getACLModel();
	}
	
	public static void main(String[] args) throws Exception {
		SOABase context = new SOABase("c1");
		context.setActivities(Arrays.asList("act1","act2","act3"));
		context.setSubjects(Arrays.asList("sub1","sub2","sub3"));
		context.setObjects(Arrays.asList("ob1","ob2","ob3"));
		ACLModel ac = new ACLModel("acl1", context);
		PermissionDialog.showDialog(null, "title", ac);
	}

}
