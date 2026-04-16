package org.imd.expertschedule.planner.domain.compare;

import org.imd.expertschedule.planner.domain.time.TimeSlot;
import org.imd.expertschedule.planner.solution.PlannerParameters;
import org.imd.expertschedule.planner.util.PlannerHelper;

import java.time.LocalTime;
import java.util.Comparator;

public final class TimeSlotStrengthComparator implements Comparator<TimeSlot> {

    private final Comparator<TimeSlot> tsComparator = new InternalComparator();

    @Override
    public int compare(final TimeSlot a, final TimeSlot b) {
        return Comparator.nullsFirst(tsComparator).compare(a, b);
    }


    private static class InternalComparator implements Comparator<TimeSlot> {
        private final TSHelper tsHelper = new TSHelper();
        @Override
        public int compare(TimeSlot ts1, TimeSlot ts2) {
            final int aAvLength = tsHelper.calculateAvailabilityLength(ts1.getStartTime());
            final int bAvLength = tsHelper.calculateAvailabilityLength(ts2.getStartTime());

            return Integer.compare(aAvLength, bAvLength);
        }
    }

    private static class TSHelper {
        private final PlannerHelper helper = new PlannerHelper();
        private final PlannerParameters.ExpertRelated expertRelated = new PlannerParameters().getExpertRelated();

        public int calculateAvailabilityLength(LocalTime time) {
            if (time == null) {
                return 0;
            }

            int result = 0;

            if (helper.lessOrEqual(expertRelated.getWorkingDayStartTime(), time) && helper.lessOrEqual(time, expertRelated.getLunchStartTime())) {
                result = expertRelated.getLunchStartTime().toSecondOfDay() - time.toSecondOfDay();
            }
            else if (helper.lessOrEqual(expertRelated.getLunchEndTime(), time) && helper.lessOrEqual(time, expertRelated.getWorkingDayEndTime())) {
                result = expertRelated.getWorkingDayEndTime().toSecondOfDay() - time.toSecondOfDay();
            }

            return result;
        }
    }
}
