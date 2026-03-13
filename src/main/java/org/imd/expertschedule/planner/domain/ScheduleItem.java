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

    private OrderRef orderRef;

    @PlanningVariable(valueRangeProviderRefs = "expertScheduleRange")
    private ExpertSchedule expertSchedule;

    @PlanningVariable(valueRangeProviderRefs = "timeSlotRange")
    private TimeSlot timeSlot;
}

