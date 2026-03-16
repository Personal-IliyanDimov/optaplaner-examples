package org.imd.expertschedule.planner.solution;

import org.imd.expertschedule.planner.cp.ExpertPlanningConstraintConfiguration;
import org.imd.expertschedule.planner.domain.ExpertSchedule;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.domain.refs.ExpertRef;
import org.imd.expertschedule.planner.domain.refs.OrderRef;
import lombok.Getter;
import lombok.Setter;
import org.imd.expertschedule.planner.domain.time.TimeSlot;
import org.optaplanner.core.api.domain.constraintweight.ConstraintConfigurationProvider;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@PlanningSolution
public class ExpertPlanningSolution {

    private SolutionContext context;

    private PlannerParameters plannerParameters;

    @ValueRangeProvider(id = "timeSlotRange")
    @ProblemFactCollectionProperty
    private List<TimeSlot> timeSlotList;

    @ValueRangeProvider(id = "expertRefRange")
    @ProblemFactCollectionProperty
    private List<ExpertRef> expertRefList;

    @ValueRangeProvider(id = "orderRefRange")
    @ProblemFactCollectionProperty
    private List<OrderRef> orderRefList;

    @ValueRangeProvider(id = "expertScheduleRange")
    @ProblemFactCollectionProperty
    private List<ExpertSchedule> expertScheduleList;

    @PlanningEntityCollectionProperty
    private List<ScheduleItem> scheduleItemList;

    @ConstraintConfigurationProvider
    private ExpertPlanningConstraintConfiguration constraintConfiguration;

    @PlanningScore
    private HardMediumSoftScore score;
}
