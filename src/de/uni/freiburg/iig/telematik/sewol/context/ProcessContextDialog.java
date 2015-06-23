package de.uni.freiburg.iig.telematik.sewol.context;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import de.invation.code.toval.misc.soabase.SOABaseDialog;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.graphic.ACModelDialog;
import de.uni.freiburg.iig.telematik.sewol.context.du.DataUsageDialog;



public class ProcessContextDialog extends SOABaseDialog {

	private static final long serialVersionUID = 1356633338243797124L;
	
	private JButton btnSetACModel;
	private JButton btnSetDataUsage;

	public ProcessContextDialog(Window owner) throws Exception {
		super(owner);
	}
	
	public ProcessContextDialog(Window owner, ProcessContext context) throws Exception {
		super(owner, context);
	}
	
	@Override
	protected ProcessContext getDialogObject() {
		return (ProcessContext) super.getDialogObject();
	}
	
	@Override
	protected Component getComponentsExtensionPanel() {
		JPanel extensionPanel = new JPanel(new BorderLayout());
		
		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(1, 3, 10, 0));
		JPanel leftmostPanel = new JPanel(new BorderLayout());
		leftmostPanel.add(getSetDataUsageButton(), BorderLayout.CENTER);
		gridPanel.add(leftmostPanel);
		gridPanel.add(new JPanel());
		gridPanel.add(new JPanel());
		extensionPanel.add(gridPanel, BorderLayout.CENTER);
		JPanel restPanel = new JPanel(new BorderLayout(0,10));
		restPanel.add(new JSeparator(), BorderLayout.PAGE_START);
		restPanel.add(getACModelPanel(), BorderLayout.CENTER);
		extensionPanel.add(restPanel, BorderLayout.PAGE_END);
		return extensionPanel;
	}
	
	private JButton getSetDataUsageButton(){
		if(btnSetDataUsage == null){
			btnSetDataUsage = new JButton("Set data usage");
			btnSetDataUsage.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						DataUsageDialog.showDialog(ProcessContextDialog.this, getDialogObject());
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(ProcessContextDialog.this, "Cannot launch data usage dialog.\nReason: " + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}
		return btnSetDataUsage;
	}
	
	private JPanel getACModelPanel(){
		JPanel acModelPanel = new JPanel();
		BoxLayout layout = new BoxLayout(acModelPanel, BoxLayout.LINE_AXIS);
		acModelPanel.setLayout(layout);
		
		acModelPanel.add(new JLabel("Access Control Model:"));
		JTextField txtACModelName = new JTextField();
		txtACModelName.setEditable(false);
		txtACModelName.setColumns(10);
		acModelPanel.add(txtACModelName);
		acModelPanel.add(getSetACModelButton());
		acModelPanel.add(Box.createGlue());
		return acModelPanel;
	}
	
	private JButton getSetACModelButton(){
		if(btnSetACModel == null){
			btnSetACModel = new JButton("Set/Edit");
			btnSetACModel.setEnabled(false);
			btnSetACModel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						AbstractACModel<?> acModel = getDialogObject().getACModel();
						if(acModel != null){
							try {
								ACModelDialog.showDialog(ProcessContextDialog.this, acModel);
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(ProcessContextDialog.this, "Cannot launch access control model dialog.\nReason:" + e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
							}
						} else {
							// ask for model type | provide list with possible acModels?
							//						-> new constructor with acModels parameter
							// Set acmodel of context
						}
					} catch (ParameterException e1) {
						JOptionPane.showMessageDialog(ProcessContextDialog.this, e1.getMessage(), "Cannot set access control model.", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			btnSetACModel.setBounds(342, 371, 98, 29);
		}
		return btnSetACModel;
	}
	
	
	//------- STARTUP ---------------------------------------------------------------------------------------------------------------

	public static ProcessContext showDialog(Window parentWindow) throws Exception {
		ProcessContextDialog contextDialog = new ProcessContextDialog(parentWindow);
		contextDialog.setUpGUI();
		return contextDialog.getDialogObject();
	}

	public static boolean showDialog(Window parentWindow, ProcessContext context) throws Exception {
		ProcessContextDialog contextDialog = new ProcessContextDialog(parentWindow, context);
		contextDialog.setUpGUI();
                return contextDialog.getDialogObject() != null;
	}

	public static void main(String[] args) throws Exception {
		ProcessContext c = new ProcessContext("GerdContext");
		c.setActivities(Arrays.asList("act1","act2"));
		ProcessContextDialog.showDialog(null, c);
		System.out.println(c);
	}
}
