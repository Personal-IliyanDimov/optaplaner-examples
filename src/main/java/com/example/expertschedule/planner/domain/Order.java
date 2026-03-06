package com.example.expertschedule.planner.domain;

import com.example.expertschedule.planner.domain.refs.CustomerRef;
import com.example.expertschedule.planner.domain.refs.OrderRef;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@PlanningEntity
public class Order {
    private OrderRef id;
    private CustomerRef customerRef;
    private Location location;
    private Set<Skill> requiredSkills;
    private LocalDate dueDate;
    private OrderPriority priority;
    private Duration diagnosisDuration;
}
