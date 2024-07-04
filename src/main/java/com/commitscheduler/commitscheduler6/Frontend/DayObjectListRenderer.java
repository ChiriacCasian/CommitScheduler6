package com.commitscheduler.commitscheduler6.Frontend;

import com.commitscheduler.commitscheduler6.PersistanceStateVariables;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;

class DayObjectListRenderer extends JPanel {
    DayObjectListRenderer(DayObject dayObject, PersistanceStateVariables state) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        Dimension buttonDimension = new Dimension(60, 30);
        if (state.getDayList().size() == 1) {
            JButton invButton = new JButton("");
            invButton.setMaximumSize(new Dimension(20, 0));
            invButton.setPreferredSize(new Dimension(20, 0));
            invButton.setBackground(JBColor.background());
            this.add(Box.createHorizontalGlue());
            this.add(invButton) ;
        }else if(state.getDayList().indexOf(dayObject) != 0) {
            this.add(Box.createHorizontalGlue());
        }

        // Create a button for the first action
        JButton button1 = new JButton("-");
        button1.setFont(new Font(button1.getFont().getName(), button1.getFont().getStyle(), 20));
        button1.setVerticalTextPosition(SwingConstants.CENTER);

        button1.setPreferredSize(buttonDimension);
        button1.setMaximumSize(buttonDimension);

        button1.addActionListener(e -> { /// minus button
            if(dayObject.getNrOfCommits() == 0) {
                JOptionPane.showMessageDialog(null,
                        "No commits to remove.", "No commits", JOptionPane.ERROR_MESSAGE);
                return;
            }
            else {
                dayObject.setNrOfCommits(dayObject.getNrOfCommits() - 1);
                state.lastDayObjectPlusOneCommit() ;
            }
            System.out.println("MINUS on " + dayObject);
        });
        if(dayObject.getNrOfCommits() == 0){
            button1.setForeground(Color.RED);
        }
        this.add(button1);

        // Create a button for the second action
        JButton button2 = new JButton("+");
        button2.setFont(new Font(button1.getFont().getName(), button1.getFont().getStyle(), 20));
        button2.setVerticalTextPosition(SwingConstants.CENTER);

        button2.setPreferredSize(buttonDimension);
        button2.setMaximumSize(buttonDimension);

        button2.addActionListener(e -> { /// plus button
            if(dayObject.getNrOfCommits() >= state.getMaxCommits()) {
                JOptionPane.showMessageDialog(null,
                        "Daily commits limit reached, modify the maximum number of allowed commits and retry.", "Too many commits", JOptionPane.ERROR_MESSAGE);
                return;
            }
            else {
                dayObject.setNrOfCommits(dayObject.getNrOfCommits() + 1);
                state.lastDayObjectMinusOneCommit() ;
            }
            System.out.println("PLUS on " + dayObject);
        });
        if(dayObject.getNrOfCommits() == state.getMaxCommits()){
            button2.setForeground(Color.RED);
        }
        this.add(button2);

        if(state.getDayList().size() == 1 ||
                state.getDayList().indexOf(dayObject) != state.getDayList().size() - 1) {
            this.add(Box.createHorizontalGlue());
        }
    }
}