package com.commitscheduler.commitscheduler6.Frontend;

import com.commitscheduler.commitscheduler6.ConfigGitProtocol;
import com.commitscheduler.commitscheduler6.PersistanceStateVariables;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

public class ConfigDialog extends DialogWrapper {
    private JPanel contentPane;
    private JLabel httpsLabel;
    private JLabel sshLabel;
    private JTextField httpsLinkTextField;
    private JTextField sshLinkTextField;
    private JTextField httpsInputField;
    private JTextField sshInputField;
    private JButton applyButton ;
    private PersistanceStateVariables state ;
    private Project project ;
    boolean gitProtocol ;
    JToggleButton toggleButton ;
    JFrame branchesFrame;
    JBTable branchesTable;
    JFrame commitsFrame;
    JBTable commitsTable;
    XYChart chart ;
    private JTextField minDailyCommitsTextField;
    private JTextField maxDailyCommitsTextField;
    private JTextField delayCommitPush;
    private JPanel chartPanel ;
    private JButton refreshButton ;
    private JButton applyProtocolButton ;

    public ConfigDialog(PersistanceStateVariables state, Project project) throws IOException, InterruptedException {
        super(true); // use current window as parent
        setTitle("Commit Scheduler Configuration");
        this.state = state ;
        this.project = project ;
        gitProtocol = state.getCurrentProtocol(project);/// true means ssh, false means https
        state.loadDayList();
        init();
        initializeToggleButton() ;
        initializeFields();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        httpsLabel = new JLabel(" Https Pat token");
        httpsLinkTextField = new JTextField();
        httpsLinkTextField.setEditable(false);
        sshLabel = new JLabel(" Ssh link");
        sshLinkTextField = new JTextField();
        sshLinkTextField.setEditable(false);
        toggleButton = new JToggleButton();
        httpsInputField = new JTextField();
        sshInputField = new JTextField();
        applyButton = new JButton("Apply");
        chart = new XYChartBuilder().width(600).height(200).build();
        chartPanel = new XChartPanel<>(chart) ;
        refreshButton = new JButton("Refresh Chart");
        applyProtocolButton = new JButton("Apply Protocol");

        minDailyCommitsTextField = new JTextField();
        maxDailyCommitsTextField = new JTextField();
        delayCommitPush = new JTextField();

        commitsFrame = new JFrame();
        commitsFrame.setTitle("Available branches");
        String[] commitsColumnNames = {"Commit Title", "Remote/Local Branch", "Time of Commit"} ;
        commitsTable = new JBTable(new CustomCommitsTableModel(state.getCommits(), commitsColumnNames, state));
        if (!((CustomCommitsTableModel) commitsTable.getModel()).hasCommits()) {
            System.out.println("No commits found");
            commitsTable.setPreferredScrollableViewportSize(new Dimension(500, 50));
        }
        commitsTable.setBounds(30, 40, 200, 300);
        JBScrollPane spForCommits = new JBScrollPane(commitsTable);
        commitsFrame.add(spForCommits);
        commitsFrame.setSize(500, 200);
        ////////////////////////////////////////
        branchesFrame = new JFrame();
        branchesFrame.setTitle("UnPushed Commits");
        String[] branchesColumnNames = {"remote branch", "local branch", "nr of Commits", "push"} ;
        branchesTable = new JBTable(new CustomBranchesTableModel(state.getBranches(),
                branchesColumnNames, project, state,(CustomCommitsTableModel) commitsTable.getModel()));
        branchesTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        branchesTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), commitsTable));
        branchesTable.setBounds(30, 40, 200, 300);
        JBScrollPane spForBranches = new JBScrollPane(branchesTable);
        branchesFrame.add(spForBranches);
        branchesFrame.setSize(500, 200);
        generateChart();

        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("ssh".equals(toggleButton.getText())) {
                    toggleButton.setText("https");
                    sshLabel.setVisible(false);
                    sshInputField.setVisible(false);

                    httpsLabel.setVisible(true);
                    httpsInputField.setVisible(true);
                    gitProtocol = false;
                } else {
                    toggleButton.setText("ssh");
                    httpsLabel.setVisible(false);
                    httpsInputField.setVisible(false);

                    sshLabel.setVisible(true);
                    sshInputField.setVisible(true);
                    gitProtocol = true;
                }
            }
        });
        /**
         * applies https/ssh git protocol configurations
         * applies min/max daily commits
         * applies delay between commit and push
         */
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveMinMaxAndDelayToState() ;
                initializeFields();
            }
        });
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateChart();
            }
        });
        applyProtocolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveState();
                try {
                    ConfigGitProtocol.setProtocol(gitProtocol, state, project);
                    Pair<String, Boolean> connectionStatus = ConfigGitProtocol.testConnection(project);
                    if(connectionStatus.getSecond()) {
                        if(gitProtocol) {
                            sshLinkTextField.setForeground(JBColor.GREEN);
                            sshLinkTextField.setForeground(JBColor.GREEN);
                        } else {
                            httpsLinkTextField.setForeground(JBColor.GREEN);
                            httpsLinkTextField.setForeground(JBColor.GREEN);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Connection failed + \n" +
                                connectionStatus.getFirst(), "Error", JOptionPane.ERROR_MESSAGE);
                        if(gitProtocol) {
                            sshLinkTextField.setForeground(JBColor.RED);
                            sshLinkTextField.setForeground(JBColor.RED);
                        } else {
                            httpsLinkTextField.setForeground(JBColor.RED);
                            httpsLinkTextField.setForeground(JBColor.RED);
                        }
                    }
                } catch (IOException | InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                initializeFields();
            }
        });

        /// automatic configuration :
        /// default push delay : 1h
        /// daily pushed commits (range) : min 5 & max 12 (it will choose randomly how many for today)
        /// make this a cool slider with live update of watch
        /// add FORCE PUSH ALL button (to push all commits from all branches)
        /// add a watch : enough commits for 23 days, that says considering the daily density of the
        /// commits you have chosen and the nr of commits, you have enough for 23 days,
        /// this is state.getCommits().size() / ((max - min) / 2)
        JLabel httpsLabel2 = new JLabel(" Https remote link") ;
        JLabel sshLabel2 = new JLabel(" SSh remote link") ;
        JLabel currentlyUsingLabel = new JLabel(" Currently using : ") ;
        contentPane.add(httpsLabel2);
        contentPane.add(httpsLinkTextField);
        contentPane.add(sshLabel2);
        contentPane.add(sshLinkTextField);
        contentPane.add(currentlyUsingLabel);

        contentPane.add(toggleButton) ;

        contentPane.add(httpsLabel);
        contentPane.add(httpsInputField);
        contentPane.add(sshLabel);
        contentPane.add(sshInputField);

        contentPane.add(applyProtocolButton);

        contentPane.add(spForCommits) ;
        contentPane.add(Box.createVerticalStrut(10));
        contentPane.add(spForBranches) ;
        contentPane.add(chartPanel, BorderLayout.CENTER) ;
        contentPane.add(refreshButton) ;


        contentPane.add(new JLabel(" Min daily commits"), BorderLayout.WEST);
        contentPane.add(minDailyCommitsTextField, BorderLayout.WEST);
        contentPane.add(new JLabel(" Max daily commits"), BorderLayout.WEST);
        contentPane.add(maxDailyCommitsTextField, BorderLayout.WEST);

        contentPane.add(applyButton);

        setAlignmentLeft(httpsLabel2, sshLabel2, httpsLinkTextField, sshLabel, sshLinkTextField, currentlyUsingLabel,
                toggleButton, httpsLabel, httpsInputField, sshLabel, sshInputField, applyProtocolButton, spForCommits,
                spForBranches, chartPanel, refreshButton, minDailyCommitsTextField, maxDailyCommitsTextField, applyButton);


        return contentPane;
    }
    private void setAlignmentLeft(JComponent... components) {
        for (JComponent component : components) {
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
    }

    private void initializeFields() {
        httpsInputField.setText(state.getPatToken());
        sshInputField.setText(state.getSshKey());
        if(httpsInputField.getText().equals("PAT token not set yet !")) httpsLinkTextField.setForeground(JBColor.RED);
        if(sshLinkTextField.getText().equals("ssh key not set yet !")) sshLinkTextField.setForeground(JBColor.RED);
        httpsLinkTextField.setText(state.getHttpLink());
        sshLinkTextField.setText(state.getSshKey());

        minDailyCommitsTextField.setText(String.valueOf(state.getMinCommits()));
        maxDailyCommitsTextField.setText(String.valueOf(state.getMaxCommits()));
        delayCommitPush.setText(String.valueOf(state.getDelayCommitPush()));
    }
    private void initializeToggleButton(){
        if(gitProtocol) { /// means ssh
            sshLabel.setVisible(true);
            sshInputField.setVisible(true);

            httpsLabel.setVisible(false);
            httpsInputField.setVisible(false);
            toggleButton.setText("ssh");
        } else { /// means https
            httpsLabel.setVisible(true);
            httpsInputField.setVisible(true);

            sshLabel.setVisible(false);
            sshInputField.setVisible(false);
            toggleButton.setText("https");
        }
    }

    public void saveState() {
        state.setPatToken(httpsInputField.getText());
        state.setSshKey(sshInputField.getText());
    }

    private void saveMinMaxAndDelayToState(){
        /// if one of them has changed we recompute and refresh the whole chart
        if(state.getMinCommits() != Integer.parseInt(minDailyCommitsTextField.getText()) ||
                state.getMaxCommits() != Integer.parseInt(maxDailyCommitsTextField.getText())) {
            int min = Integer.parseInt(minDailyCommitsTextField.getText()) ;
            int max = Integer.parseInt(maxDailyCommitsTextField.getText()) ;
            if(min > max || min < 0 || max < 0) {
                JOptionPane.showMessageDialog(null, "Min commits must be smaller than max commits and both must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            state.setMinCommits(min) ;
            state.setMaxCommits(max) ;
            state.generateDayList();
            generateChart();
        }
        state.setMinCommits(Integer.parseInt(minDailyCommitsTextField.getText()));
        state.setMaxCommits(Integer.parseInt(maxDailyCommitsTextField.getText()));
        state.setDelayCommitPush(Integer.parseInt(delayCommitPush.getText()));
    }
    public void generateChart(){
        Styler styler = chart.getStyler();
        JBColor dark_grey = (JBColor) JBColor.background(); /// Color.Dark_Gray
        styler.setPlotBorderColor(JBColor.GRAY);
        chartPanel.removeAll() ;///////////////!!!!
        // Set the color of the grid lines
        chart.getStyler().setChartBackgroundColor(dark_grey); // Set the background color
        chart.getStyler().setPlotBackgroundColor(dark_grey); // Set the plot background color
        chart.getStyler().setPlotGridLinesColor(dark_grey); // Set the plot grid lines color
        chart.getStyler().setPlotGridLinesColor(dark_grey);

        chart.getStyler().setAxisTickLabelsColor(JBColor.black); // Set the color of axis labels
        chart.getStyler().setAxisTickMarksColor(JBColor.ORANGE); // Set the color of axis tick marks
        chart.getStyler().setXAxisDecimalPattern("#");
        chart.getStyler().setYAxisDecimalPattern("#");

        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(new XChartPanel<>(chart), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        chart.removeSeries("Scheduled commits per day");
        chart.getStyler().setLegendVisible(false);
        AtomicInteger g = new AtomicInteger();
        int[] dayNr = state.getDayList().stream().mapToInt(i -> g.incrementAndGet()).toArray();
        int[] nrOfCommits = state.getDayList().stream().mapToInt(i -> i.getNrOfCommits()).toArray();
        if(dayNr.length != 0 && nrOfCommits.length != 0)
            chart.addSeries("Scheduled commits per day", dayNr, nrOfCommits);
        state.saveDayList();
        for (DayObject dayObject : state.getDayList()) {
            DayObjectListRenderer renderer = new DayObjectListRenderer(dayObject, state);
            buttonPanel.add(renderer);
        }
        chartPanel.add(buttonPanel, BorderLayout.PAGE_END);
        chartPanel.revalidate();
        chartPanel.repaint();
        branchesTable.revalidate();
        branchesTable.repaint();
    }
    public String getHttpsLink() {
        return httpsLinkTextField.getText();
    }
    public String getSshLink() {
        return sshLinkTextField.getText();
    }
    public String getPatToken() {
        return httpsInputField.getText();
    }
    public String getSshInputedKey() {
        return sshInputField.getText();
    }
}
