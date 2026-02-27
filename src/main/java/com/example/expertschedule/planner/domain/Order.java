package com.example.expertschedule.planner.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@PlanningEntity
public class Order {

    // Order that must be visited.
    private String code;
    private Customer customer;
    private Set<Skill> requiredSkills;

    @PlanningVariable(valueRangeProviderRefs = "expertRange")
    private Expert assignedExpert;

}
