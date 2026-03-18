package org.imd.expertschedule.planner.solution;

import lombok.Getter;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@PlanningSolution
public class ExpertPlanningSolution {

    private final PlannerParameters plannerParameters;

    @ConstraintConfigurationProvider
    private final ExpertPlanningConstraintConfiguration constraintConfiguration;

    @ValueRangeProvider(id = "expertRange")
    @ProblemFactCollectionProperty
    private final List<Expert> expertList;

    @ValueRangeProvider(id = "orderRange")
    @ProblemFactCollectionProperty
    private final List<Order> orderList;

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

    public ExpertPlanningSolution(final PlannerParameters plannerParameters,
                                  final ExpertPlanningConstraintConfiguration constraintConfiguration,
                                  final SolutionContext context) {

        // initialize it
        this.plannerParameters = plannerParameters;
        this.constraintConfiguration = constraintConfiguration;
        this.expertList = context.getExpertList();
        this.orderList = context.getOrderList();
        this.scheduleItemList = initScheduleItems(orderList);
        this.expertScheduleList = initExpertSchedules(plannerParameters, expertList);
        this.score = HardMediumSoftScore.of(0,0,0);
    }



    private static List<ScheduleItem> initScheduleItems(final List<Order> orderList) {
        return orderList.stream()
                        .map(o -> new ScheduleItem(o))
                        .toList();
    }


    private List<ExpertSchedule> initExpertSchedules(final PlannerParameters plannerParameters,
                                                     final List<Expert> expertList) {
        final List<ExpertSchedule> result = new ArrayList<>();
        for (final Expert expert : expertList) {
            for (int day: plannerParameters.getExpertRelated().getWorkingDays()) {
                result.add(new ExpertSchedule(expert, calculateDate(plannerParameters.getPlannerRelated().getCalendarWeek(), day)));
            }
        }

        return result;
    }

    private LocalDate calculateDate(int calendarWeek, int wd) {
        return LocalDate.of(LocalDate.now().getYear(), 1, 1)
                .plusWeeks(calendarWeek - 1)
                .plusDays(wd - 1);
    }

    private List<TimeSlot> initTimeSlots(PlannerParameters plannerParameters) {
        final List<TimeSlot> result = new ArrayList<>();

//        LocalTime initialLocalTime = LocalTime.of(plannerParameters.getExpertRelated().);
//        for (String durationStr : metaData.getOrderDurations()) {
//            Duration duration = parseDuration(durationStr);
//            if (!duration.isZero()) {
//                TimeSlot slot = new TimeSlot();
//                slot.setStartTime(duration);
//                result.add(slot);
//            }
//        }

        return result;
    }
}
