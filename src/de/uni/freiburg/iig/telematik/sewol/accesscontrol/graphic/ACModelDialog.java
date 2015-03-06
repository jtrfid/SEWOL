package de.uni.freiburg.iig.telematik.sewol.accesscontrol.graphic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import de.invation.code.toval.graphic.dialog.AbstractDialog;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.misc.soabase.SOABaseComboBox;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.acl.ACLModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.graphic.permission.PermissionDialog;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACModelType;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.graphic.RoleLatticeDialog;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.graphic.RoleMembershipDialog;

public class ACModelDialog extends AbstractDialog {
	
	private static final long serialVersionUID = -2689725669752188740L;
	
	private static final Dimension LABEL_DIMENSION = new Dimension(80, 20);
	private static final Dimension FIELD_DIMENSION = new Dimension(80, 20);
	private static final Dimension TEXT_AREA_DIMENSION = new Dimension(400, 300);
	
	private JTextArea textArea = null;
	private JTextField txtName = null;
	private JTextField txtContextName = null;
	private JButton btnEditPermissions = null;
	private JButton btnEditRoleLattice = null;
	private JButton btnEditRoleMembership = null;
	private JButton btnEditContext = null;
	private SOABaseComboBox comboContext = null;
	
	private JPanel panelPermissions = null;
	private JPanel panelRoles = null;
	
//	private JButton btnEditRoleMembership = null;
//	private JButton btnEditRoleLattice = null;
//	private JButton btnEditPermissions = null;

	private JCheckBox chckbxPropagateRights;
	
	@SuppressWarnings("rawtypes")
	private AbstractACModel originalACModel;
	private ACModelType modelType = null;
	private Collection<SOABase> contextCandidates = null;
	
	//---------------------------------------------------
	

	public ACModelDialog(Window owner, String acModelName, ACModelType targetModelType) throws Exception {
		super(owner);
		Validate.notNull(acModelName);
		Validate.notNull(targetModelType);
		switch (targetModelType) {
		case ACL:
			setDialogObject(new ACLModel(acModelName));
			break;
		case RBAC:
			setDialogObject(new RBACModel(acModelName));
			break;
		}
	}
	
	public ACModelDialog(Window owner, String acModelName, ACModelType targetModelType, SOABase context) throws Exception {
		this(owner, acModelName, targetModelType);
		getDialogObject().setContext(context);
	}
	
	public ACModelDialog(Window owner, String acModelName, ACModelType targetModelType, SOABase context, Collection<SOABase> contextCandidates) throws Exception {
		this(owner, acModelName, targetModelType, context);
		if(!contextCandidates.contains(context))
			throw new ParameterException(ErrorCode.INCONSISTENCY, "Context candidates must contain actually assigned context");
		this.contextCandidates = contextCandidates;
	}
	
	@SuppressWarnings("rawtypes")
	public ACModelDialog(Window owner, AbstractACModel acModel) throws Exception {
		super(owner, true);
		this.originalACModel = acModel;
		setDialogObject(originalACModel.clone());
	}
	
	@SuppressWarnings("rawtypes")
	public ACModelDialog(Window owner, AbstractACModel acModel, Collection<SOABase> contextCandidates) throws Exception {
		this(owner, acModel);
		if(!contextCandidates.contains(acModel.getContext()))
			throw new ParameterException(ErrorCode.INCONSISTENCY, "Context candidates must contain actually assigned context");
		this.contextCandidates = contextCandidates;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public AbstractACModel getDialogObject(){
		return (AbstractACModel) super.getDialogObject();
	}

	@Override
	protected void setTitle() {
		if(editMode){
			setTitle("Edit Access Control Model");
		} else {
			setTitle("New Access Control Model");
		}
	}
	
	@Override
	protected void addComponents() throws Exception {
		mainPanel().setLayout(new BorderLayout());
		
		JPanel topComponentsPanel = new JPanel();
		BoxLayout layout = new BoxLayout(topComponentsPanel, BoxLayout.PAGE_AXIS);
		topComponentsPanel.setLayout(layout);
		topComponentsPanel.add(Box.createVerticalStrut(10));
		JPanel namePanel = new JPanel();
		BoxLayout namePanelLayout = new BoxLayout(namePanel, BoxLayout.LINE_AXIS);
		namePanel.setLayout(namePanelLayout);
		JLabel nameLabel = new JLabel("Name:", JLabel.RIGHT);
		nameLabel.setPreferredSize(LABEL_DIMENSION);
		nameLabel.setMinimumSize(LABEL_DIMENSION);
		nameLabel.setMaximumSize(LABEL_DIMENSION);
		namePanel.add(nameLabel);
		namePanel.add(getFieldName());
		topComponentsPanel.add(namePanel);
		topComponentsPanel.add(Box.createVerticalStrut(10));
		topComponentsPanel.add(getPanelContext());
		topComponentsPanel.add(Box.createVerticalStrut(10));
		topComponentsPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		topComponentsPanel.add(getPanelPermissions());
		if(getDialogObject() instanceof RBACModel){
			topComponentsPanel.add(new JSeparator(JSeparator.HORIZONTAL));
			topComponentsPanel.add(getPanelRoles());
		}
		topComponentsPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		mainPanel().add(topComponentsPanel, BorderLayout.PAGE_START);
		
		mainPanel().add(getPanelTextArea(), BorderLayout.CENTER);
	}
	
	protected JPanel getPanelPermissions(){
		if(panelPermissions == null) {
			panelPermissions= new JPanel();
			BoxLayout l = new BoxLayout(panelPermissions, BoxLayout.LINE_AXIS);
			panelPermissions.setLayout(l);
			panelPermissions.add(Box.createHorizontalGlue());
			panelPermissions.add(getButtonEditPermissions());
			if(getDialogObject() instanceof RBACModel){
				panelPermissions.add(getCheckBoxPropagateRights());
			}
			panelPermissions.add(Box.createHorizontalGlue());
		}
		return panelPermissions;
	}
	
	protected JPanel getPanelRoles(){
		if(panelRoles == null) {
			panelRoles= new JPanel();
			BoxLayout l = new BoxLayout(panelRoles, BoxLayout.LINE_AXIS);
			panelRoles.setLayout(l);
			panelRoles.add(Box.createHorizontalGlue());
			panelRoles.add(getButtonEditRoleLattice());
			panelRoles.add(getButtonEditRoleMembership());
			panelRoles.add(Box.createHorizontalGlue());
		}
		return panelRoles;
	}
	
	private JPanel getPanelTextArea() {
		JPanel textAreaPanel = new JPanel(new BorderLayout());
		textAreaPanel.setPreferredSize(TEXT_AREA_DIMENSION);
		textAreaPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		textArea = new JTextArea();
		textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
		updateTextArea();
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
		textAreaPanel.add(scrollPane, BorderLayout.CENTER);
		return textAreaPanel;
	}

	private JPanel getPanelContext(){
		JPanel contextPanel = new JPanel();
		BoxLayout contextPanelLayout = new BoxLayout(contextPanel, BoxLayout.LINE_AXIS);
		contextPanel.setLayout(contextPanelLayout);
		JLabel contextNameLabel = new JLabel("Context:", JLabel.RIGHT);
		contextNameLabel.setPreferredSize(LABEL_DIMENSION);
		contextNameLabel.setMinimumSize(LABEL_DIMENSION);
		contextNameLabel.setMaximumSize(LABEL_DIMENSION);
		contextPanel.add(contextNameLabel);
		if(contextCandidates != null){
			contextPanel.add(getComboContext());
		} else {
			contextPanel.add(getFieldContextName());
		}
		contextPanel.add(getButtonEditContext());
		return contextPanel;
	}
	
	private JComboBox getComboContext(){
		if(comboContext == null){
			comboContext = new SOABaseComboBox(contextCandidates);
			comboContext.setSelectedItem(getDialogObject().getContext());
			comboContext.setPreferredSize(FIELD_DIMENSION);
			comboContext.setMinimumSize(FIELD_DIMENSION);
		}
		return comboContext;
	}
	
	private JTextField getFieldName(){
		if(txtName == null){
			txtName = new JTextField();
			txtName.setText(AbstractACModel.DEFAULT_AC_MODEL_NAME);
			txtName.setColumns(10);
			txtName.setPreferredSize(FIELD_DIMENSION);
			txtName.setMinimumSize(FIELD_DIMENSION);
		}
		return txtName;
	}
	
	private JTextField getFieldContextName(){
		if(txtContextName == null){
			txtContextName = new JTextField();
			txtContextName.setText(getDialogObject().getContext().getName());
			txtContextName.setColumns(10);
			txtContextName.setPreferredSize(FIELD_DIMENSION);
			txtContextName.setMinimumSize(FIELD_DIMENSION);
		}
		return txtContextName;
	}
	
//	public ACModelDialog(Window owner, Context context) {
//		.add(getButtonEditPermissions());
//		
//		JScrollPane scrollPane = new JScrollPane();
//		scrollPane.setBounds(20, 120, 360, 200);
//		scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
//		contentPanel.add(scrollPane);
//
//		textArea = new JTextArea();
//		scrollPane.setViewportView(textArea);
//		textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
//		textArea.setBorder(new EmptyBorder(0, 0, 0, 0));
//		
//		contentPanel.add(chckbxPropagateRights);
//		
//		btnEditRoleMembership = new JButton("Edit role membership");
//		btnEditRoleMembership.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				new RoleMembershipDialog(ACModelDialog.this, (RBACModel) acModel);
//				updateTextArea();
//			}
//		});
//		btnEditRoleMembership.setEnabled(false);
//		
//		contentPanel.add(btnEditRoleMembership);
//		
//		contentPanel.add(getButtonEditRoleLattice());
//		
//		updateModelType();
//	}
	
	private JButton getButtonEditRoleMembership(){
		if(btnEditRoleMembership == null){
			btnEditRoleMembership = new JButton("Edit role membership");
			btnEditRoleMembership.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						RoleMembershipDialog.showDialog(ACModelDialog.this, (RBACModel) getDialogObject());
						updateTextArea();
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(ACModelDialog.this, "Cannot launch role membership dialog.\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}
		return btnEditRoleMembership;
	}
	
	private JButton getButtonEditContext(){
		if(btnEditContext == null){
			btnEditContext = new JButton("Edit");
			btnEditContext.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(getDialogObject() != null){
						SOABase context = contextCandidates != null ? comboContext.getSelectedItem() : getDialogObject().getContext();
						try {
							context.showDialog(ACModelDialog.this);
							updateTextArea();
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(ACModelDialog.this, "Cannot launch context dialog.\nReason:" + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});
		}
		return btnEditContext;
	}
	
	private JButton getButtonEditPermissions(){
		if(btnEditPermissions == null){
			btnEditPermissions = new JButton("Edit Permissions");
			btnEditPermissions.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(getDialogObject() == null)
						return;
					try {
						if(getDialogObject() instanceof ACLModel){
							PermissionDialog.showDialog(ACModelDialog.this, "Edit subject permissions", (ACLModel) getDialogObject());
							updateTextArea();
						} else {
//							System.out.println(((RBACModel) getDialogObject()).getRolePermissions().getContext());
							PermissionDialog.showDialog(ACModelDialog.this, "Edit role permissions", ((RBACModel) getDialogObject()).getRolePermissions());
						}
					}catch(Exception ex){
						JOptionPane.showMessageDialog(ACModelDialog.this, "<html>Cannot launch permission dialog.<br>Reason: "+ex.getMessage()+"</html>", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
					}
					updateTextArea();
				}
			});
		}
		return btnEditPermissions;
	}
	
	private JCheckBox getCheckBoxPropagateRights(){
		if(chckbxPropagateRights == null){
			chckbxPropagateRights = new JCheckBox("Propagate rights along lattice");
			chckbxPropagateRights.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if ((getDialogObject() != null) && (getDialogObject().getType() == ACModelType.RBAC)) {
						((RBACModel) getDialogObject()).setRightsPropagation(chckbxPropagateRights.isSelected());
						updateTextArea();
					}
				}

			});
		}
		return chckbxPropagateRights;
	}
	
	private JButton getButtonEditRoleLattice(){
		if(btnEditRoleLattice == null){
			btnEditRoleLattice = new JButton("Edit role lattice");
			btnEditRoleLattice.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						RoleLatticeDialog.showDialog(ACModelDialog.this, ((RBACModel) getDialogObject()).getRoleLattice());
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(ACModelDialog.this, "<html>Cannot launch role lattice dialog:<br>Reason: "+e1.getMessage()+"</html>", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
					}
					updateTextArea();
				}
			});
		}
		return btnEditRoleLattice;
	}
	
	private void updateTextArea(){
		textArea.setText("");
		if(getDialogObject() != null){
			textArea.setText(getDialogObject().toString());
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static AbstractACModel showDialog(Window owner, String acModelName, ACModelType modelType) throws Exception {
		ACModelDialog acModelDialog = new ACModelDialog(owner, acModelName, modelType);
		acModelDialog.setUpGUI();
		return acModelDialog.getDialogObject();
	}
	
	@SuppressWarnings("rawtypes")
	public static AbstractACModel showDialog(Window owner, String acModelName, ACModelType modelType, SOABase context) throws Exception {
		ACModelDialog acModelDialog = new ACModelDialog(owner, acModelName, modelType, context);
		acModelDialog.setUpGUI();
		return acModelDialog.getDialogObject();
	}
	
	@SuppressWarnings("rawtypes")
	public static AbstractACModel showDialog(Window owner, String acModelName, ACModelType modelType, SOABase context, Collection<SOABase> contextCandidates) throws Exception {
		ACModelDialog acModelDialog = new ACModelDialog(owner, acModelName, modelType, context, contextCandidates);
		acModelDialog.setUpGUI();
		return acModelDialog.getDialogObject();
	}
	
	@SuppressWarnings("rawtypes")
	public static void showDialog(Window owner, AbstractACModel acModel) throws Exception {
		ACModelDialog acModelDialog = new ACModelDialog(owner, acModel);
		acModelDialog.setUpGUI();
	}
	
	@SuppressWarnings("rawtypes")
	public static void showDialog(Window owner, AbstractACModel acModel, Collection<SOABase> contextCandidates) throws Exception {
		ACModelDialog acModelDialog = new ACModelDialog(owner, acModel, contextCandidates);
		acModelDialog.setUpGUI();
	}
	
	public static void main(String[] args) throws Exception {
//		ACModelDialog.showDialog(null, "ACModel1", ACModelType.ACL);
		SOABase c1 = new SOABase("c1");
		c1.setActivities(Arrays.asList("act1","act2","act3"));
		SOABase c2 = new SOABase("c2");
//		DUContext du1 = new DUContext("du1");
//		du1.setActivities(Arrays.asList("act1","act2"));
//		
//		ACLModel m = new ACLModel("g", c1);
//		du1.setACModel(m);
//		ACModelDialog.showDialog(null, m);
		ACModelDialog.showDialog(null, "ACModel1", ACModelType.RBAC, c1, Arrays.asList(c1,c2));
	}
	
	

}
