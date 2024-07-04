package com.commitscheduler.commitscheduler6.Frontend;

import com.commitscheduler.commitscheduler6.CommitObject;
import com.commitscheduler.commitscheduler6.ConfigStateOnStartup;
import com.commitscheduler.commitscheduler6.GitListenerStartup;
import com.commitscheduler.commitscheduler6.PersistanceStateVariables;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CustomBranchesTableModel extends AbstractTableModel {
    private final String[] columnNames;
    public final List<Pair<String, String>> data;
    private Project project ;
    private PersistanceStateVariables state ;
    private CustomCommitsTableModel commitModel ;

    public CustomBranchesTableModel(List<Pair<String, String>> data, String[] columnNames,
                                    Project project, PersistanceStateVariables state,
                                    CustomCommitsTableModel commitModel){
        this.data = data;
        this.columnNames = columnNames;
        this.project = project ;
        this.state = state ;
        this.commitModel = commitModel ;
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
        try {
            return getAttribute(data.get(rowIndex), columnIndex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 4 || columnIndex == 3; // Only the "Scheduled push time" column is editable
    }
    public Pair<String, String> getDataAtRow(int rowIndex) {
        return data.get(rowIndex) ;
    }
    public Project getProject() {
        return project;
    }
    public PersistanceStateVariables getState() {
        return state;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        //data.get(rowIndex).setAttribute(columnIndex) = aValue;
        setAttribute(aValue, data.get(rowIndex), columnIndex);
        fireTableCellUpdated(rowIndex, columnIndex);
    }
    private Object getAttribute(Pair<String, String> branch, int columnIndex) throws IOException, InterruptedException {
        switch (columnIndex) {
            case 0:
                return branch.getFirst() ; /// remote
            case 1:
                return branch.getSecond() ; /// local
            case 2:
                return nrOfUnpushedCommits(project, branch.getFirst(), branch.getSecond()) ;
            case 3:
                return "push" ;
            default:
                return null;
        }
    }
    private void setAttribute(Object AValue, Pair<String, String> branch, int columnIndex) {
        //TO DO
        return;
    }
    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 3:
                return JButton.class;
            default:
                return Object.class;
        }
    }
    public int nrOfUnpushedCommits(Project project, String remoteBranch,
                                          String localBranch) throws IOException, InterruptedException {
        commitModel.updateData(state);
        String sha1s = ConfigStateOnStartup.runCommand(project,
                "git log " + remoteBranch + ".." + localBranch + " --pretty=format:%H");
        if(sha1s.isEmpty())return 0 ;
        return Arrays.stream(sha1s.split("\n")).toList().size();
    }
}