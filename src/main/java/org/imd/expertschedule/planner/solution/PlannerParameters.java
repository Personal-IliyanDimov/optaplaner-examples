package org.imd.expertschedule.planner.solution;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Getter
public class PlannerParameters {

    private PlannerRelated plannerRelated = new PlannerRelated();
    private ExpertRelated expertRelated = new ExpertRelated();

    @Getter
    public static class ExpertRelated {
        private final LocalTime workingDayStartTime = LocalTime.of(9, 0);
        private final LocalTime workingDayEndTime = LocalTime.of(18, 0);
        private final Duration slotDuration = Duration.of(15, ChronoUnit.MINUTES);
        private final LocalTime lunchStartTime = LocalTime.of(12, 0);
        private final LocalTime lunchEndTime = LocalTime.of(12, 59);
    }

    @Getter
    @Setter
    public static class PlannerRelated {
        private int year;
        private int calendarWeek;
        private int[] workingDays;
    }
}
