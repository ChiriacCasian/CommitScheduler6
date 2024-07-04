package com.commitscheduler.commitscheduler6;

import com.commitscheduler.commitscheduler6.Frontend.DayObject;
import com.intellij.openapi.module.impl.scopes.LibraryScope;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import git4idea.GitCommit;
import net.sf.cglib.core.Local;


import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PersistanceStateVariables {
    private String patToken = "PAT token not set yet !";
    private String sshKey = "ssh key not set yet !";
    private String httpLink = "http link not set yet !";
    private boolean gitProtocol = true; /// currently assuming ssh protocol
    private List<CommitObject> commits = new ArrayList<>() ;
    private List<Pair<String, String>> branches = new ArrayList<>() ;
    private List<DayObject> dayList = new ArrayList<>() ;
    private int minCommits = 0 ; /// defaults
    private int maxCommits = 10 ; /// defaults
    private int delayCommitPush = 1 ;/// 1 min default
    public void generateDayList(){ /// all started at 5:35 /// does not take into account commits already pushed today
        System.out.println("Generating dayList");
        int nrOfCommits = commits.size();
        System.out.println("nrOfCommits = " + nrOfCommits);
        if(nrOfCommits == 0)return;
        dayList.clear();
        DayObject firstDay = new DayObject(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(0),
                minCommits + (int)(Math.random()*(maxCommits - minCommits)));
        dayList.add(firstDay);
        nrOfCommits -= firstDay.getNrOfCommits();
        while(nrOfCommits > 0){
            DayObject newDayObject = new DayObject(dayList.get(dayList.size() - 1).getBeginning().plusDays(1),
                    dayList.get(dayList.size() - 1).getEnd().plusDays(1),
                    minCommits + (int)(Math.random()*(maxCommits - minCommits))) ;
            dayList.add(newDayObject);
            nrOfCommits -= newDayObject.getNrOfCommits();
        }
        saveDayList();
    }
    public void updateDayList(){ /// everytime a commit is added or pushed
        /// when a commit is pushed(removed) we do firstDay.nrOfCommits -- (this is done in CommitNowMain)
        /// we only remove first day when LocalDateTime.now() > firstDay.end
        /// when a commit is added(commited) we check if commits.size() < sumOfAllDayObjects.nrOfCommits
        /// if so we don t do anything, if commits.size got bigger we add another dayObject and update graph
        /// if enough commits have been pushed manually we start deleting days from the
        if(dayList.isEmpty()){
            generateDayList();
            return ;
        }
        System.out.println("Updating dayList");
        while(!dayList.isEmpty() && LocalDateTime.now().isAfter(dayList.get(0).getEnd())){ /// maybe application has been closed for a while
            dayList.remove(0);
        }
        while(dayList != null && !withoutLastDay(dayList).isEmpty() && commits.size() <= sumOfDaysInCommits(withoutLastDay(dayList))){
            System.out.println("removing"+dayList.size());
            dayList = withoutLastDay(dayList) ;
        }
        if(commits.size() <= sumOfDaysInCommits(dayList)) return ;
        /// add another day sequence from above
        DayObject newDayObject = new DayObject(dayList.get(dayList.size() - 1).getBeginning().plusDays(1),
                dayList.get(dayList.size() - 1).getEnd().plusDays(1),
                minCommits + (int)(Math.random()*(maxCommits - minCommits))) ;
        dayList.add(newDayObject);
        saveDayList();
    }
    public void lastDayObjectMinusOneCommit(){
        if(dayList.isEmpty())return ;
        if(dayList.get(dayList.size() - 1).getNrOfCommits() <= 0){
            dayList.remove(dayList.size() - 1);
            lastDayObjectMinusOneCommit();
            return ;
        }
        dayList.get(dayList.size() - 1).setNrOfCommits(dayList.get(dayList.size() - 1).getNrOfCommits() - 1);
        saveDayList();
    }
    public void lastDayObjectPlusOneCommit(){
        if(dayList.isEmpty())return ;
        if(dayList.get(dayList.size() - 1).getNrOfCommits() >= maxCommits){
            dayList.add(new DayObject(dayList.get(dayList.size() - 1).getBeginning().plusDays(1),
                    dayList.get(dayList.size() - 1).getEnd().plusDays(1),
                    minCommits + (int)(Math.random()*(maxCommits - minCommits)))) ;
            if(dayList.get(dayList.size() - 1).getNrOfCommits() == 0)
                lastDayObjectPlusOneCommit();
            /// if it added a new dayObject with more than 0 commits we are done otherwise add 1 commit to that
            return ;
        }
        dayList.get(dayList.size() - 1).setNrOfCommits(dayList.get(dayList.size() - 1).getNrOfCommits() + 1);
        saveDayList();
    }
    private int sumOfDaysInCommits(List<DayObject> dayList) {
        if(dayList == null)return 0 ;
        if(dayList.isEmpty())return 0 ;
        int rez = 0 ;
        for(DayObject day : dayList){
            rez += day.getNrOfCommits();
        }

        return rez;
    }
    private List<DayObject> withoutLastDay(List<DayObject> dayList) {
        if(dayList == null)return  new ArrayList<>();
        List<DayObject> auxDayList = new ArrayList<>(dayList);
        if(auxDayList.isEmpty())return new ArrayList<>();
        auxDayList.remove(auxDayList.size() - 1);
        return auxDayList;
    }

    public int getNrOfCommitsToday(){
        if(dayList.isEmpty()){
            generateDayList();
        }
        if(dayList.isEmpty())return 0 ;
        return dayList.get(0).getNrOfCommits();
    }
    public void saveDayList(){
        /// save dayList to file
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("dayList.ser")))) {
            oos.writeObject(dayList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void loadDayList() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("dayList.ser")))) {
            List<DayObject> auxDayList = (List<DayObject>) ois.readObject();
            if(auxDayList != null && !auxDayList.isEmpty()) dayList = auxDayList ;
            else generateDayList();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void registerPush(){
        if(dayList.isEmpty()) return ; /// this should never happen
        if(dayList.get(0).getNrOfCommits() <= 0) return ;
        dayList.get(0).setNrOfCommits(dayList.get(0).getNrOfCommits() - 1);
        updateDayList();
    }
    public List<DayObject> getDayList(){
        if(dayList.isEmpty()) generateDayList();
        return dayList;
    }
    public void addCommit(CommitObject commit){
        if(commits.contains(commit)) return ;
        commits.add(commit);
        sortCommitsAfterDate();
    }
    public void removeCommit(CommitObject commit){
        commits.remove(commit);
    }
    public void removeAllCommits(){
        commits.clear();
    }
    public void removeCommitBySha1(String sha1){
        commits.removeIf(commit -> commit.getCommitSha1().equals(sha1));
    }
    public void sortCommitsAfterDate(){
        commits.sort(Comparator.comparing(CommitObject::getCommitTime));
    }
    public List<CommitObject> getCommits(){
        return commits ;
    }
    public void addBranch(String remoteBranch, String localBranch){
        if(!branches.contains(new Pair<>(remoteBranch, localBranch)))branches.add(new Pair<String, String>(remoteBranch, localBranch));
    }
    public List<Pair<String, String>> getBranches(){
        return branches ;
    }
    public boolean getCurrentProtocol(Project project) throws IOException, InterruptedException {
        gitProtocol = ConfigGitProtocol.getProjectProtocol(project, branches.get(0).getFirst().split("/")[0]);
        String link = ConfigGitProtocol.getLink(project, branches.get(0).getFirst().split("/")[0]);
        if(gitProtocol) {
            sshKey = link ;
        }
        else {
            httpLink = link ;
        }
        return gitProtocol;
    }

    public String getPatToken() {
        return patToken;
    }

    public void setPatToken(String patToken) {
        this.patToken = patToken;
    }

    public String getSshKey() {
        return sshKey;
    }

    public void setSshKey(String sshKey) {
        this.sshKey = sshKey;
    }
    public boolean getProtocol() {
        return gitProtocol ;
    }

    public String getHttpLink() {
        return httpLink;
    }

    public void setHttpLink(String httpLink) {
        this.httpLink = httpLink;
    }
    public void setMinCommits(int minCommits) {
        this.minCommits = minCommits;
    }

    public void setMaxCommits(int maxCommits) {
        this.maxCommits = maxCommits;
    }

    public void setDelayCommitPush(int delayCommitPush) {
        this.delayCommitPush = delayCommitPush;
    }

    public int getMinCommits() {
        return minCommits;
    }

    public int getMaxCommits() {
        return maxCommits;
    }

    public int getDelayCommitPush() {
        return delayCommitPush;
    }
}
