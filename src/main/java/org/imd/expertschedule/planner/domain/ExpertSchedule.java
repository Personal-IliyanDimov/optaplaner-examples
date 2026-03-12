package org.imd.expertschedule.planner.domain;

import org.imd.expertschedule.planner.domain.refs.ExpertRef;
import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@PlanningEntity
public class ExpertSchedule {
    @PlanningVariable(valueRangeProviderRefs = "expertRefRange")
    private ExpertRef expertRef;

    @PlanningVariable(valueRangeProviderRefs = "planningDateRangeProvider")
    private LocalDate date;

    @PlanningEntityCollectionProperty
    private List<ScheduleItem> items;
}

