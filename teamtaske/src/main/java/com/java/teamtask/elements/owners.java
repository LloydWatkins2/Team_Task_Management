package com.java.teamtask.elements;

import java.util.*;

public class owners implements projectsManage{
    private users Users;
    private projects Projects;
    public Owner(user Users,project Projects) {
        this.Users = Users;
        this.Projects = Projects;
    }
    @Override
    public void deleteMember(member Member) {
        
    }
    @Override
    public void addMember(users User) {

    }
    @Override
    public void assignTask(member Member, tasks Task, List<Tasks> Tasks) {
        
    }

    @Override
    public void unassignTask(member Member, tasks Task, List<Tasks> Tasks) {
        
    }
    @Override
    public void updateProject() {
       
    }

    @Override
    public void deleteProject() {
       
    }

    @Override
    public void archiveProject() {
        
    }

    public String viewProjectInfo() {
        
    }

    public Member[] viewMembers() {
        
    }

    public String viewMemberInfo(member Member) {
        
    }

    public double viewOverallProgress() {
        
    }

    public double viewMemberProgress(member Member) {
        
    }

    public String viewTask() {
        
    }

    public String checkWork() {
        
    }

    public String commentTasks(tasks Tasks, String comment) {
        
    }
}