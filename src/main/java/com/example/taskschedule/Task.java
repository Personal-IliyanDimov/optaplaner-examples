package com.example.taskschedule;

import lombok.AllArgsConstructor;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@PlanningEntity
public class Task {

    private String name;
    private int durationInHours;

    @PlanningVariable
    private Employee assignedEmployee;

    public Task(String name, int durationInHours) {
        this.name = name;
        this.durationInHours = durationInHours;
    }
}
