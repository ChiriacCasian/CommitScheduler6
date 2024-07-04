package com.commitscheduler.commitscheduler6.Frontend;

import com.commitscheduler.commitscheduler6.CommitObject;
import com.commitscheduler.commitscheduler6.PersistanceStateVariables;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class CustomCommitsTableModel extends AbstractTableModel {
    private final String[] columnNames;
    public List<CommitObject> data;
    PersistanceStateVariables state ;
    public CustomCommitsTableModel(List<CommitObject> data, String[] columnNames, PersistanceStateVariables state){
        this.data = data;
        this.columnNames = columnNames;
        this.state = state ;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return getAttribute(data.get(rowIndex), columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Only the "Scheduled push time" column is editable
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        setAttribute(aValue, data.get(rowIndex), columnIndex);
        fireTableCellUpdated(rowIndex, columnIndex);
    }
    private Object getAttribute(CommitObject commitObject, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return commitObject.getCommitMessage() ;
            case 1:
                return commitObject.getRemoteBranchName() + "/" + commitObject.getLocalBranchName();
            case 2:
                return String.valueOf(commitObject.getCommitTime());
            default:
                return null;
        }
    }
    private void setAttribute(Object AValue, CommitObject commitObject, int columnIndex) {
        //TO DO
        return;
    }
    public boolean hasCommits() {
        return !data.isEmpty();
    }
    public void updateData(PersistanceStateVariables state){
        this.data = state.getCommits() ;
        fireTableDataChanged();
    }
}