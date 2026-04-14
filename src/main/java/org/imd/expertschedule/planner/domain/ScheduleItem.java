package org.imd.expertschedule.planner.domain;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.imd.expertschedule.planner.domain.time.TimeSlot;
import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

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

//    @PlanningId
//    public Long getPlanningId() {
//        return order == null || order.getId() == null ? null : order.getId().getId();
//    }
}
