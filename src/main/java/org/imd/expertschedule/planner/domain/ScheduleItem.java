package org.imd.expertschedule.planner.domain;

import org.imd.expertschedule.planner.domain.refs.OrderRef;
import org.imd.expertschedule.planner.domain.time.TimeSlot;
import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.time.Period;

@Getter
@Setter
@PlanningEntity
public class ScheduleItem {
    private Period travelDuration;

    @PlanningVariable(valueRangeProviderRefs = "orderRefRange")
    private OrderRef orderRef;

    private TimeSlot slot;
}

