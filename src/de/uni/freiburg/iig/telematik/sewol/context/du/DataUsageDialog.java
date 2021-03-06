package de.uni.freiburg.iig.telematik.sewol.context.du;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.invation.code.toval.graphic.dialog.AbstractDialog;
import de.invation.code.toval.graphic.dialog.ValueChooserDialog;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.sewol.context.process.ProcessContext;

public class DataUsageDialog extends AbstractDialog {

    private static final long serialVersionUID = 9017322681121907900L;
    private static final Dimension PREFERRED_SIZE_LEFT_PANEL = new Dimension(180, 350);

    private JList activityList = null;
    private DataUsageTable dataUsageTable = null;
    private ProcessContext context = null;
    private JButton addDUButton = null;
    private JButton removeDUButton = null;

    public DataUsageDialog(Window owner, ProcessContext context) {
        super(owner, ButtonPanelLayout.CENTERED);
        if (!context.containsActivities()) {
            throw new ParameterException("Context does not contain any " + context.getActivityDescriptorPlural().toLowerCase());
        }
        if (!context.containsObjects()) {
            throw new ParameterException("Context does not contain any " + context.getObjectDescriptorPlural().toLowerCase());
        }
        this.context = context;
        setIncludeCancelButton(false);
    }

    @Override
    protected void setTitle() {
        setTitle("Activity Data Usage");
    }

    @Override
    protected void addComponents() throws Exception {
        mainPanel().setLayout(new BorderLayout(5, 0));

        JPanel leftPanel = new JPanel(new BorderLayout(0, 5));
        leftPanel.setPreferredSize(PREFERRED_SIZE_LEFT_PANEL);
        leftPanel.setMinimumSize(PREFERRED_SIZE_LEFT_PANEL);
        JLabel lblActivities = new JLabel("Activities:");
        leftPanel.add(lblActivities, BorderLayout.PAGE_START);
        JScrollPane scrollPaneActivities = new JScrollPane(getActivityList());
        scrollPaneActivities.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        leftPanel.add(scrollPaneActivities, BorderLayout.CENTER);
        mainPanel().add(leftPanel, BorderLayout.LINE_START);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 5));
        JLabel lblDataUsage = new JLabel("Attributes and usage modes:");
        rightPanel.add(lblDataUsage, BorderLayout.PAGE_START);
        JScrollPane scrollPaneDataUsage = new JScrollPane(getDataUsageTable());
        scrollPaneDataUsage.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPaneDataUsage.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        rightPanel.add(scrollPaneDataUsage);
        updateDataUsageList(getActivityList().getSelectedValue().toString());
        JPanel buttonPanel = new JPanel();
        BoxLayout layout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
        buttonPanel.setLayout(layout);
        buttonPanel.add(getAddDUButton());
        buttonPanel.add(getRemoveDUButton());
        buttonPanel.add(Box.createHorizontalGlue());
        rightPanel.add(buttonPanel, BorderLayout.PAGE_END);
        mainPanel().add(rightPanel, BorderLayout.CENTER);
    }

    private JButton getAddDUButton() {
        if (addDUButton == null) {
            addDUButton = new JButton("Add");
            addDUButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<String> attributes = null;
                    try {
                        attributes = ValueChooserDialog.showDialog(DataUsageDialog.this, "Add new attribute to activity", DataUsageDialog.this.context.getAttributes());
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(DataUsageDialog.this, "<html>Cannot launch value chooser dialog.<br>Reason: " + e1.getMessage() + "</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (attributes != null && !attributes.isEmpty()) {
                        dataUsageTable.getModel().addElement(attributes.get(0));
                    }
                    dataUsageTable.repaint();
                }
            });
        }
        return addDUButton;
    }

    private JButton getRemoveDUButton() {
        if (removeDUButton == null) {
            removeDUButton = new JButton("Remove");
            removeDUButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeSelectedDataUsages();
                }
            });
        }
        return removeDUButton;
    }

    private void removeSelectedDataUsages() {
        if (dataUsageTable.getSelectedRow() >= 0) {
            String attribute = dataUsageTable.getModel().getValueAt(dataUsageTable.getSelectedRow(), 0).toString();
            dataUsageTable.getModel().removeElement(attribute);
            try {
                context.removeDataUsageFor(activityList.getSelectedValue().toString(), attribute);
            } catch (ParameterException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    private JList getActivityList() {
        if (activityList == null) {
            activityList = new JList();
            activityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            activityList.setCellRenderer(new DataUsageListRenderer(context));
            activityList.setFixedCellHeight(20);
            activityList.setVisibleRowCount(10);
            activityList.setBorder(null);

            DefaultListModel listModel = new DefaultListModel();
            for (String activity : context.getActivities()) {
                listModel.addElement(activity);
            }
            activityList.setModel(listModel);

            activityList.setSelectedIndex(0);

            activityList.addListSelectionListener(
                    new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            if ((e.getValueIsAdjusting() == false) && (activityList.getSelectedValue() != null)) {
                                updateDataUsageList(activityList.getSelectedValue().toString());
                                getDataUsageTable().requestFocusInWindow();
                            }
                        }
                    }
            );
            activityList.addKeyListener(new KeyListener() {

                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        removeSelectedDataUsages();
                    }
                }

                @Override
                public void keyPressed(KeyEvent e) {
                }
            });
        }
        return activityList;
    }

    private DataUsageTable getDataUsageTable() {
        if (dataUsageTable == null) {
            dataUsageTable = new DataUsageTable(context);
            dataUsageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            dataUsageTable.setBorder(null);
        }
        return dataUsageTable;
    }

    private void updateDataUsageList(String selectedActivity) {
        try {
            getDataUsageTable().update(selectedActivity);
        } catch (ParameterException e) {
            throw new RuntimeException(e);
        }
    }

    

    public static void showDialog(Window owner, ProcessContext context) throws Exception {
        DataUsageDialog dialog = new DataUsageDialog(owner, context);
        dialog.setUpGUI();
    }

}
