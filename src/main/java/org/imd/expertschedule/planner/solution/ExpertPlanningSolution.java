package org.imd.expertschedule.planner.solution;

import lombok.Getter;
import lombok.Setter;
import org.imd.expertschedule.planner.cp.ExpertPlanningConstraintConfiguration;
import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.ExpertSchedule;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.domain.time.TimeSlot;
import org.optaplanner.core.api.domain.constraintweight.ConstraintConfigurationProvider;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

import java.util.List;

@Getter
@Setter
@PlanningSolution
public class ExpertPlanningSolution {

    @ConstraintConfigurationProvider
    private ExpertPlanningConstraintConfiguration constraintConfiguration;

    @ValueRangeProvider(id = "expertRange")
    @ProblemFactCollectionProperty
    private List<Expert> expertList;

    @ValueRangeProvider(id = "orderRange")
    @ProblemFactCollectionProperty
    private List<Order> orderList;

    @PlanningEntityCollectionProperty
    private List<ScheduleItem> scheduleItemList;

    @ValueRangeProvider(id = "expertScheduleRange")
    @ProblemFactCollectionProperty
    private List<ExpertSchedule> expertScheduleList;

    @ValueRangeProvider(id = "timeSlotRange")
    @ProblemFactCollectionProperty
    private List<TimeSlot> timeSlotList;

    @PlanningScore
    private HardMediumSoftScore score;
}
