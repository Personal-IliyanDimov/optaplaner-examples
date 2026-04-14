package org.imd.expertschedule.planner.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.imd.expertschedule.planner.domain.refs.OrderRef;
import org.imd.expertschedule.planner.domain.time.TimeSlot;
import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.time.Period;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@PlanningEntity
public class ScheduleItem {

    private Order order;

    @PlanningVariable(valueRangeProviderRefs = "expertScheduleRange")
    private ExpertSchedule expertSchedule;

    @PlanningVariable(valueRangeProviderRefs = "timeSlotRange")
    private TimeSlot timeSlot;
}
