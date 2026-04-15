package org.imd.expertschedule.planner.domain.compare;

import org.imd.expertschedule.planner.domain.time.TimeSlot;
import org.imd.expertschedule.planner.solution.PlannerParameters;
import org.imd.expertschedule.planner.util.PlannerHelper;

import java.time.LocalTime;
import java.util.Comparator;

public final class TimeSlotStrengthComparator implements Comparator<TimeSlot> {

    private final PlannerHelper helper = new PlannerHelper();
    private final PlannerParameters.ExpertRelated expertRelated = new PlannerParameters().getExpertRelated();

    @Override
    public int compare(final TimeSlot a, final TimeSlot b) {
        final int aAvLength = calculateAvailabilityLength(a.getStartTime());
        final int bAvLength = calculateAvailabilityLength(b.getStartTime());

        return Integer.compare(aAvLength, bAvLength);
    }

    private int calculateAvailabilityLength(LocalTime time) {
        int result = 0;
        if (helper.lessOrEqual(expertRelated.getWorkingDayStartTime(), time) && helper.lessOrEqual(time, expertRelated.getLunchStartTime())) {
            result = expertRelated.getLunchStartTime().toSecondOfDay() - time.toSecondOfDay();
        }
        else if (helper.lessOrEqual(expertRelated.getLunchEndTime(), time) && helper.lessOrEqual(time, expertRelated.getWorkingDayEndTime())){
            result = expertRelated.getWorkingDayEndTime().toSecondOfDay() - time.toSecondOfDay();
        }

        return result;
    }
}
