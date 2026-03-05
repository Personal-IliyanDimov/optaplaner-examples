package com.example.expertschedule.planner.domain;

import com.example.expertschedule.planner.domain.refs.ExpertRef;
import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@PlanningEntity
public class ExpertSchedule {
    private ExpertRef expertRef;
    private LocalDate date;
    private List<ScheduleItem> items;
}

