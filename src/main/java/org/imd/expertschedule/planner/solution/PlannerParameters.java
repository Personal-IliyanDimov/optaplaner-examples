package org.imd.expertschedule.planner.solution;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Getter
public class PlannerParameters {

    private PlannerRelated plannerRelated = new ExpertRelated();
    private ExpertRelated expertRelated = new ExpertRelated();

    @Getter
    private class ExpertRelated {
        private final LocalTime workingDayStartTime = LocalTime.of(9, 0);
        private final LocalTime workingDayEndTime = LocalTime.of(18, 0);
        private final Duration slotDuration = Duration.of(15, ChronoUnit.MINUTES);
        private final LocalTime lunchStartTime = LocalTime.of(12, 0);
        private final LocalTime lunchEndTime = LocalTime.of(13, 0);
    }

    @Getter
    @Setter
    private class PlannerRelated {
        private int calendarWeek;
        private int[] workingDays;
    }
}
