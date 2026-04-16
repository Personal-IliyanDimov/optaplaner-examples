package org.imd.expertschedule.planner.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.imd.expertschedule.planner.domain.compare.ExpertScheduleStrengthComparator;
import org.imd.expertschedule.planner.domain.compare.ScheduleItemDifficultyComparator;
import org.imd.expertschedule.planner.domain.compare.TimeSlotStrengthComparator;
import org.imd.expertschedule.planner.domain.time.TimeSlot;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@PlanningEntity(difficultyComparatorClass = ScheduleItemDifficultyComparator.class)
public class ScheduleItem {

    private Order order;

    @PlanningVariable(
            valueRangeProviderRefs = "expertScheduleRange",
            strengthComparatorClass = ExpertScheduleStrengthComparator.class)
    private ExpertSchedule expertSchedule;

    @PlanningVariable(
            valueRangeProviderRefs = "timeSlotRange",
            strengthComparatorClass = TimeSlotStrengthComparator.class)
    private TimeSlot timeSlot;

    @PlanningId
    @EqualsAndHashCode.Include
    public Long getPlanningId() {
        return order == null || order.getId() == null ? null : order.getId().getId();
    }
}
