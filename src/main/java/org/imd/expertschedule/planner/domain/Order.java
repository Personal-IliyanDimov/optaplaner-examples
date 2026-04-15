package org.imd.expertschedule.planner.domain;

import org.imd.expertschedule.planner.domain.refs.CustomerRef;
import org.imd.expertschedule.planner.domain.refs.OrderRef;
import org.imd.expertschedule.planner.domain.time.Availability;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class Order {
    private OrderRef id;
    private Customer customer;
    private Location location;
    private Set<Skill> requiredSkills;
    private LocalDate dueDate;
    private OrderPriority priority;
    private Duration diagnosisDuration;
    private List<Availability> customerAvailabilities;
}
