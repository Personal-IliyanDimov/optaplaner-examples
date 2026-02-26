package com.example.taskschedule;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

@PlanningSolution
public class TaskSchedule {

    @ProblemFactCollectionProperty
    private List<Employee> employeeList;

    @PlanningEntityCollectionProperty
    private List<Task> taskList;

    @PlanningScore
    private HardSoftScore score;

    public TaskSchedule() {
    }

    public TaskSchedule(List<Employee> employeeList, List<Task> taskList) {
        this.employeeList = employeeList;
        this.taskList = taskList;
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }
}

