package org.imd.expertschedule.planner.solution;

import org.imd.expertschedule.planner.cp.ExpertPlanningConstraintConfiguration;
import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.ExpertSchedule;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.domain.time.TimeSlot;
import org.imd.expertschedule.planner.util.PlannerHelper;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SolutionInitializer {

    private final PlannerHelper helper = new PlannerHelper();

    public ExpertPlanningSolution initialize(final PlannerParameters plannerParameters,
                                             final ExpertPlanningConstraintConfiguration constraintConfiguration,
                                             final SolutionContext context) {

        final ExpertPlanningSolution result = new ExpertPlanningSolution();
        result.setConstraintConfiguration(constraintConfiguration);
        result.setExpertList(context.getExpertList());
        result.setOrderList(context.getOrderList());
        result.setScheduleItemList(initScheduleItems(context.getOrderList()));
        result.setExpertScheduleList(initExpertSchedules(plannerParameters, context.getExpertList()));
        result.setTimeSlotList(initTimeSlots(plannerParameters));
        result.setScore(HardMediumSoftScore.of(0,0,0));

        return result;
    }

    private List<ScheduleItem> initScheduleItems(final List<Order> orderList) {
        return orderList.stream()
                .map(o -> {
                    final ScheduleItem r = new ScheduleItem();
                    r.setOrder(o);
                    return r;
                })
                .toList();
    }

    private List<ExpertSchedule> initExpertSchedules(final PlannerParameters plannerParameters,
                                                     final List<Expert> expertList) {
        final List<ExpertSchedule> result = new ArrayList<>();
        for (final Expert expert : expertList) {
            for (int day: plannerParameters.getPlannerRelated().getWorkingDays()) {
                result.add(new ExpertSchedule(expert, helper.calculateDate(plannerParameters.getPlannerRelated().getCalendarWeek(), day)));
            }
        }

        return result;
    }

    private List<TimeSlot> initTimeSlots(PlannerParameters plannerParameters) {
        final List<TimeSlot> result = new ArrayList<>();

        PlannerParameters.ExpertRelated expertRelated = plannerParameters.getExpertRelated();

        LocalTime currentLocalTime = expertRelated.getWorkingDayStartTime();
        final LocalTime endLocalTime = expertRelated.getWorkingDayEndTime();

        while (currentLocalTime.isBefore(endLocalTime)) {
            if (! helper.duringLunchTime(currentLocalTime, expertRelated)) {
                result.add(new TimeSlot(currentLocalTime));
            }

            currentLocalTime = currentLocalTime.plus(expertRelated.getSlotDuration());
        }

        return result;
    }
}
