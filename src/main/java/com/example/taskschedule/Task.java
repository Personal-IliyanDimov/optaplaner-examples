package com.example.taskschedule;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class Task {

    private String name;
    private int durationInHours;

    @PlanningVariable
    private Employee assignedEmployee;

    public Task() {
    }

    public Task(String name, int durationInHours) {
        this.name = name;
        this.durationInHours = durationInHours;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDurationInHours() {
        return durationInHours;
    }

    public void setDurationInHours(int durationInHours) {
        this.durationInHours = durationInHours;
    }

    public Employee getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(Employee assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    @Override
    public String toString() {
        return name + "(" + durationInHours + "h)";
    }
}

