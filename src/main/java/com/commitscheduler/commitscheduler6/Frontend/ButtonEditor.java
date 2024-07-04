package com.commitscheduler.commitscheduler6.Frontend;

import com.commitscheduler.commitscheduler6.CommitNowMain;
import com.commitscheduler.commitscheduler6.CommitObject;
import com.commitscheduler.commitscheduler6.ConfigStateOnStartup;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

class ButtonEditor extends DefaultCellEditor {
    protected JButton button;

    private String label;

    private boolean isPushed;
    private JTable table ;
    private JBTable commitTable ;

    public ButtonEditor(JCheckBox checkBox, JBTable commitTabel) {
        super(checkBox);
        button = new JButton();
        button.setOpaque(true);
        this.commitTable = commitTabel ;
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
            }
        });
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        label = (value == null) ? "" : value.toString();
        this.table = table ;
        button.setText(label);
        isPushed = true;
        return button;
    }

    public Object getCellEditorValue() {
        if (isPushed) {
            // Get the CommitObject associated with the row
            /// here handle the push() for a specific branch
            CustomBranchesTableModel model = (CustomBranchesTableModel) table.getModel();
            Pair<String, String> pair = model.getDataAtRow(table.getSelectedRow()) ;
            try {
                int commitCode = CommitNowMain.main(model.getState(), model.getProject(), pair.first.split("/")[0], pair.second) ;
                if(commitCode == 1) {
                    /// make a popup saying that we just pushed the commit successfully
                    JOptionPane.showMessageDialog(null, "Commit was pushed successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                else if(commitCode == 0){
                    JOptionPane.showMessageDialog(null, "No commits to push, branch is up to date", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    JOptionPane.showMessageDialog(null, "Commit was not pushed successfully", "Failure", JOptionPane.ERROR_MESSAGE);
                }
                ConfigStateOnStartup.configStateOnStartup(model.getProject());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            model.getState().updateDayList();

            model.fireTableDataChanged();
            ((CustomCommitsTableModel)commitTable.getModel()).updateData(model.getState());
        }
        isPushed = false;
        return new String(label);
    }

    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }

    protected void fireEditingStopped() {
        super.fireEditingStopped();
    }
}