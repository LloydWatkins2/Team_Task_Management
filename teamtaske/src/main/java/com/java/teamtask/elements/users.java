package com.java.teamtask.elements;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class users{
protected int userId;
    protected String name;
    protected String password;
    protected String email;
    protected Map<Projects, ProjectsAssignments> relatedProjects;

    public users(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.relatedProjects = new HashMap<>();
    }

    public void deleteAccount() {
        
    }

    public void updateAccount() {
        
    }

    public Projects createProject() {
       
    }

    public List<Projects> viewProjects() {
       
    }

    public String viewProjectStatus(Projects project) {
        
    }

    public void action(Projects project) {
       
    }
}