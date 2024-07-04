package com.commitscheduler.commitscheduler6.Frontend;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DayObject implements Serializable {
    private final LocalDateTime beginningOfDay;
    private final LocalDateTime endOfDay;
    private int scheduledCommits; /// means how many more commits are due today

    public DayObject(LocalDateTime beginningOfDay, LocalDateTime endOfDay, int scheduledCommits) {
        this.beginningOfDay = beginningOfDay;
        this.endOfDay = endOfDay;
        this.scheduledCommits = scheduledCommits;
    }

    public int getNrOfCommits() {
        return scheduledCommits;
    }

    public void setNrOfCommits(int scheduledCommits) {
        this.scheduledCommits = scheduledCommits;
    }

    public LocalDateTime getBeginning() {
        return beginningOfDay;
    }

    public LocalDateTime getEnd() {
        return endOfDay;
    }
    public String toString(){
        return beginningOfDay.toString() + " - " + endOfDay.toString() + " : " + scheduledCommits;
    }
}
