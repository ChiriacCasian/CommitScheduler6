package com.commitscheduler.commitscheduler6;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.*;

public class ConfigDialog extends DialogWrapper {

    private JPanel contentPane;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JButton applyButton ;
    private JButton pushCommitButton ;
    private PersistanceStateVariables state ;
    private Project project ;

    public ConfigDialog(PersistanceStateVariables state, Project project) {
        super(true); // use current window as parent
        init();
        setTitle("Plugin Configuration");
        this.state = state ;
        this.project = project ;
        initializeFields();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        textField1 = new JTextField();
        textField2 = new JTextField();

        textField3 = new JTextField();
        textField4 = new JTextField();
        //checkBox1 = new JCheckBox("Enable Feature", true);
        applyButton = new JButton("Apply");
        pushCommitButton = new JButton("PushCommit");
        textField3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                textField3.setText("");
            }
        });
        textField4.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                textField4.setText("");
            }
        });
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveState();
            }
        });
        pushCommitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CommitNowMain.main(state, project);
                    System.out.println("Pushed commit");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        contentPane.add(new JLabel("ssh key"));
        contentPane.add(textField1);
        contentPane.add(new JLabel("PAT auth token"));
        contentPane.add(textField2);

        contentPane.add(new JLabel("remote branch name"));
        contentPane.add(textField3);
        contentPane.add(new JLabel("local branch name"));
        contentPane.add(textField4);
        contentPane.add(applyButton);
        contentPane.add(pushCommitButton);

        return contentPane;
    }
    private void initializeFields() {
        textField1.setText(state.getPatToken());
        textField2.setText(state.getSshKey());
        textField3.setText(state.getRemoteBranchName());
        textField4.setText(state.getLocalBranchName());
    }

    public void saveState() {
        state.setPatToken(textField1.getText());
        state.setSshKey(textField2.getText());
        state.setRemoteBranchName(textField3.getText());
        state.setLocalBranchName(textField4.getText());
    }

    public String getConfigItem1() {
        return textField1.getText();
    }
    public String getConfigItem2() {
        return textField2.getText();
    }
    public String getConfigItem3() {
        return textField3.getText();
    }
    public String getConfigItem4() {
        return textField4.getText();
    }
}
