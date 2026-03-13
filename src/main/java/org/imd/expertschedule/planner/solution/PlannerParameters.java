package org.imd.expertschedule.planner.solution;

import lombok.Getter;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Getter
public class PlannerParameters {
    @Getter
    private final class ExpertRelated {
        public final LocalTime workingDayStartTime = LocalTime.of(9, 0);
        public final LocalTime workingDayEndTime = LocalTime.of(18, 0);
        public final Duration slotDuration = Duration.of(15, ChronoUnit.MINUTES);
        public final LocalTime lunchStartTime = LocalTime.of(12, 0);
        public final LocalTime lunchEndTime = LocalTime.of(13, 0);
    }
}
