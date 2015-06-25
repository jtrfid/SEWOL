package de.uni.freiburg.iig.telematik.sewol.accesscontrol.graphic;

import de.invation.code.toval.graphic.dialog.AbstractEditCreateDialog;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.misc.soabase.SOABaseComboBox;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.acl.ACLModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.graphic.permission.PermissionDialog;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACMValidationException;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACModelProperties;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.properties.ACModelType;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.graphic.RoleLatticeDialog;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.graphic.RoleMembershipDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author stocker
 */
public class ACModelDialog<P extends ACModelProperties> extends AbstractEditCreateDialog<AbstractACModel<P>> {

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

    private Collection<SOABase> contextCandidates = null;

	//---------------------------------------------------
    protected ACModelDialog(Window owner, String acModelName, ACModelType targetModelType) throws Exception {
        super(owner, targetModelType, acModelName);
    }

    public ACModelDialog(Window owner, String acModelName, ACModelType targetModelType, SOABase context) throws Exception {
        this(owner, acModelName, targetModelType);
        getDialogObject().setContext(context);
    }

    public ACModelDialog(Window owner, String acModelName, ACModelType targetModelType, SOABase context, Collection<SOABase> contextCandidates) throws Exception {
        this(owner, acModelName, targetModelType, context);
        if (!contextCandidates.contains(context)) {
            throw new ParameterException(ParameterException.ErrorCode.INCONSISTENCY, "Context candidates must contain actually assigned context");
        }
        this.contextCandidates = contextCandidates;
    }

    public ACModelDialog(Window owner, AbstractACModel<P> acModel) throws Exception {
        super(owner, acModel);
    }

    public ACModelDialog(Window owner, AbstractACModel<P> acModel, Collection<SOABase> contextCandidates) throws Exception {
        this(owner, acModel);
        if (!contextCandidates.contains(acModel.getContext())) {
            throw new ParameterException(ParameterException.ErrorCode.INCONSISTENCY, "Context candidates must contain actually assigned context");
        }
        this.contextCandidates = contextCandidates;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected AbstractACModel newDialogObject(Object... parameters) {
        Validate.notNull(parameters);
        if (parameters.length != 2) {
            throw new ParameterException("Expected 2 parameters, but got " + parameters.length);
        }
        Validate.notNull(parameters[0]);
        Validate.notNull(parameters[1]);
        ACModelType targetModelType = null;
        try {
            targetModelType = (ACModelType) parameters[0];
        } catch (Exception ex) {
            throw new ParameterException("Cannot extract ac model type from parameter list.\nReason: " + ex);
        }
        String acModelName = null;
        try {
            acModelName = (String) parameters[1];
        } catch (Exception ex) {
            throw new ParameterException("Cannot extract ac model name from parameter list.\nReason: " + ex);
        }
        switch (targetModelType) {
            case ACL:
                return new ACLModel(acModelName);
            case RBAC:
                return new RBACModel(acModelName);
        }
        return null;
    }

    @Override
    public AbstractACModel<P> getDialogObject() {
        return (AbstractACModel<P>) super.getDialogObject();
    }

    @Override
    protected void setTitle() {
        if (editMode()) {
            setTitle("Edit Access Control Model");
        } else {
            setTitle("New Access Control Model");
        }
    }

    @Override
    protected boolean validateAndSetFieldValues() throws Exception {
        if (getDialogObject().getContext().isEmpty()) {
            throw new ParameterException("Empty context");
        }

        String acModelName = getFieldName().getText();
        if (acModelName == null || acModelName.isEmpty()) {
            throw new ParameterException("Empty name");
        }

        getDialogObject().setName(acModelName);

        try {
            getDialogObject().checkValidity();
        } catch (ACMValidationException e1) {
            throw new ParameterException("Invalid AC Model\nReason: " + e1.getMessage());
        }
        return true;
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
        if (getDialogObject() instanceof RBACModel) {
            topComponentsPanel.add(new JSeparator(JSeparator.HORIZONTAL));
            topComponentsPanel.add(getPanelRoles());
        }
        topComponentsPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        mainPanel().add(topComponentsPanel, BorderLayout.PAGE_START);

        mainPanel().add(getPanelTextArea(), BorderLayout.CENTER);
    }

    protected JPanel getPanelPermissions() {
        if (panelPermissions == null) {
            panelPermissions = new JPanel();
            BoxLayout l = new BoxLayout(panelPermissions, BoxLayout.LINE_AXIS);
            panelPermissions.setLayout(l);
            panelPermissions.add(Box.createHorizontalGlue());
            panelPermissions.add(getButtonEditPermissions());
            if (getDialogObject() instanceof RBACModel) {
                panelPermissions.add(getCheckBoxPropagateRights());
            }
            panelPermissions.add(Box.createHorizontalGlue());
        }
        return panelPermissions;
    }

    protected JPanel getPanelRoles() {
        if (panelRoles == null) {
            panelRoles = new JPanel();
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

    private JPanel getPanelContext() {
        JPanel contextPanel = new JPanel();
        BoxLayout contextPanelLayout = new BoxLayout(contextPanel, BoxLayout.LINE_AXIS);
        contextPanel.setLayout(contextPanelLayout);
        JLabel contextNameLabel = new JLabel("Context:", JLabel.RIGHT);
        contextNameLabel.setPreferredSize(LABEL_DIMENSION);
        contextNameLabel.setMinimumSize(LABEL_DIMENSION);
        contextNameLabel.setMaximumSize(LABEL_DIMENSION);
        contextPanel.add(contextNameLabel);
        if (contextCandidates != null) {
            contextPanel.add(getComboContext());
        } else {
            contextPanel.add(getFieldContextName());
        }
        contextPanel.add(getButtonEditContext());
        return contextPanel;
    }

    @SuppressWarnings("rawtypes")
    private JComboBox getComboContext() {
        if (comboContext == null) {
            comboContext = new SOABaseComboBox(contextCandidates);
            comboContext.setSelectedItem(getDialogObject().getContext());
            comboContext.setPreferredSize(FIELD_DIMENSION);
            comboContext.setMinimumSize(FIELD_DIMENSION);
        }
        return comboContext;
    }

    private JTextField getFieldName() {
        if (txtName == null) {
            txtName = new JTextField();
            txtName.setText(getDialogObject().getName());
            txtName.setColumns(10);
            txtName.setPreferredSize(FIELD_DIMENSION);
            txtName.setMinimumSize(FIELD_DIMENSION);
        }
        return txtName;
    }

    private JTextField getFieldContextName() {
        if (txtContextName == null) {
            txtContextName = new JTextField();
            txtContextName.setText(getDialogObject().getContext().getName());
            txtContextName.setColumns(10);
            txtContextName.setPreferredSize(FIELD_DIMENSION);
            txtContextName.setMinimumSize(FIELD_DIMENSION);
            txtContextName.setEditable(false);
        }
        return txtContextName;
    }

    @Override
    protected void prepareEditing() throws Exception {
        txtName.setText(getDialogObject().getName());
        txtContextName.setText(getDialogObject().getContext().getName());
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
    private JButton getButtonEditRoleMembership() {
        if (btnEditRoleMembership == null) {
            btnEditRoleMembership = new JButton("Edit role membership");
            btnEditRoleMembership.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        RoleMembershipDialog.showDialog(ACModelDialog.this, (RBACModel) getDialogObject());
                        updateTextArea();
                    } catch (Exception e1) {
                        internalException("Cannot launch role membership dialog.", e1);
                    }
                }
            });
        }
        return btnEditRoleMembership;
    }

    private JButton getButtonEditContext() {
        if (btnEditContext == null) {
            btnEditContext = new JButton("Edit");
            btnEditContext.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (getDialogObject() != null) {
                        SOABase context = contextCandidates != null ? comboContext.getSelectedItem() : getDialogObject().getContext();
                        try {
                            context.showDialog(ACModelDialog.this);
                            updateTextArea();
                        } catch (Exception e1) {
                            internalException("Cannot launch context dialog.", e1);
                        }
                    }
                }
            });
        }
        return btnEditContext;
    }

    private JButton getButtonEditPermissions() {
        if (btnEditPermissions == null) {
            btnEditPermissions = new JButton("Edit Permissions");
            btnEditPermissions.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (getDialogObject() == null) {
                        return;
                    }
                    try {
                        if (getDialogObject() instanceof ACLModel) {
                            PermissionDialog.showDialog(ACModelDialog.this, "Edit subject permissions", (ACLModel) getDialogObject());
                            updateTextArea();
                        } else {
//							System.out.println(((RBACModel) getDialogObject()).getRolePermissions().getContext());
                            PermissionDialog.showDialog(ACModelDialog.this, "Edit role permissions", ((RBACModel) getDialogObject()).getRolePermissions());
                        }
                    } catch (Exception ex) {
                        internalException("Cannot launch permission dialog.", ex);
                        return;
                    }
                    updateTextArea();
                }
            });
        }
        return btnEditPermissions;
    }

    private JCheckBox getCheckBoxPropagateRights() {
        if (chckbxPropagateRights == null) {
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

    private JButton getButtonEditRoleLattice() {
        if (btnEditRoleLattice == null) {
            btnEditRoleLattice = new JButton("Edit role lattice");
            btnEditRoleLattice.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        RoleLatticeDialog.showDialog(ACModelDialog.this, ((RBACModel) getDialogObject()).getRoleLattice());
                    } catch (Exception e1) {
                        internalException("Cannot launch role lattice dialog.", e1);
                        return;
                    }
                    updateTextArea();
                }
            });
        }
        return btnEditRoleLattice;
    }

    private void updateTextArea() {
        textArea.setText("");
        if (getDialogObject() != null) {
            textArea.setText(getDialogObject().toString());
        }
    }

    public static <P extends ACModelProperties> AbstractACModel<P> showDialog(String acModelName, ACModelType modelType) throws Exception {
        return showDialog(null, acModelName, modelType);
    }

    public static <P extends ACModelProperties> AbstractACModel<P> showDialog(Window owner, String acModelName, ACModelType modelType) throws Exception {
        ACModelDialog<P> acModelDialog = new ACModelDialog<P>(owner, acModelName, modelType);
        acModelDialog.setUpGUI();
        return acModelDialog.getDialogObject();
    }

    public static <P extends ACModelProperties> AbstractACModel<P> showDialog(String acModelName, ACModelType modelType, SOABase context) throws Exception {
        return showDialog(null, acModelName, modelType, context);
    }

    public static <P extends ACModelProperties> AbstractACModel<P> showDialog(Window owner, String acModelName, ACModelType modelType, SOABase context) throws Exception {
        ACModelDialog<P> acModelDialog = new ACModelDialog<P>(owner, acModelName, modelType, context);
        acModelDialog.setUpGUI();
        return acModelDialog.getDialogObject();
    }

    public static <P extends ACModelProperties> AbstractACModel<P> showDialog(String acModelName, ACModelType modelType, SOABase context, Collection<SOABase> contextCandidates) throws Exception {
        return showDialog(null, acModelName, modelType, context, contextCandidates);
    }

    public static <P extends ACModelProperties> AbstractACModel<P> showDialog(Window owner, String acModelName, ACModelType modelType, SOABase context, Collection<SOABase> contextCandidates) throws Exception {
        ACModelDialog<P> acModelDialog = new ACModelDialog<P>(owner, acModelName, modelType, context, contextCandidates);
        acModelDialog.setUpGUI();
        return acModelDialog.getDialogObject();
    }

    public static <P extends ACModelProperties> void showDialog(AbstractACModel<P> acModel) throws Exception {
        showDialog(null, acModel);
    }

    public static <P extends ACModelProperties> void showDialog(Window owner, AbstractACModel<P> acModel) throws Exception {
        ACModelDialog<P> acModelDialog = new ACModelDialog<P>(owner, acModel);
        acModelDialog.setUpGUI();
    }

    public static <P extends ACModelProperties> void showDialog(AbstractACModel<P> acModel, Collection<SOABase> contextCandidates) throws Exception {
        showDialog(null, acModel, contextCandidates);
    }

    public static <P extends ACModelProperties> void showDialog(Window owner, AbstractACModel<P> acModel, Collection<SOABase> contextCandidates) throws Exception {
        ACModelDialog<P> acModelDialog = new ACModelDialog<P>(owner, acModel, contextCandidates);
        acModelDialog.setUpGUI();
    }

    public static void main(String[] args) throws Exception {
////		ACModelDialog.showDialog(null, "ACModel1", ACModelType.ACL);
//		SOABase c1 = new SOABase("c1");
//		c1.setActivities(Arrays.asList("act1","act2","act3"));
//		SOABase c2 = new SOABase("c2");
////		DUContext du1 = new DUContext("du1");
////		du1.setActivities(Arrays.asList("act1","act2"));
////		
////		ACLModel m = new ACLModel("g", c1);
////		du1.setACModel(m);
////		ACModelDialog.showDialog(null, m);
//		ACModelDialog.showDialog(null, "ACModel1", ACModelType.RBAC, c1, Arrays.asList(c1,c2));
        @SuppressWarnings("rawtypes")
        AbstractACModel model = ACModelDialog.showDialog("Gerd", ACModelType.ACL);
        System.out.println(model);
    }

}
