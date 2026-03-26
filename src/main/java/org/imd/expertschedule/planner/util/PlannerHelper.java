package org.imd.expertschedule.planner.util;

import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.time.Absence;
import org.imd.expertschedule.planner.domain.time.Availability;
import org.imd.expertschedule.planner.solution.PlannerParameters;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class PlannerHelper {

    public LocalDate calculateDate(final int calendarWeek, final int wd) {
        return LocalDate.of(LocalDate.now().getYear(), 1, 1)
                .plusWeeks(calendarWeek - 1)
                .plusDays(wd - 1);
    }

    public boolean duringLunchTime(final LocalTime testedLocalTime, final PlannerParameters.ExpertRelated expertRelated) {
        return lessOrEqual(expertRelated.getLunchStartTime(), testedLocalTime) &&
                lessOrEqual(testedLocalTime, expertRelated.getLunchEndTime());
    }

    public boolean overlapLunchTime(final DayInterval interval, final PlannerParameters.ExpertRelated expertRelated) {
        return duringLunchTime(interval.getFrom(), expertRelated) || duringLunchTime(interval.getTo(), expertRelated);
    }


    public boolean lessOrEqual(final LocalTime leftTime, final LocalTime rightTime) {
        return (leftTime.equals(rightTime) || leftTime.isBefore(rightTime));
    }

    public boolean intersect(final DayInterval i1, final DayInterval i2) {
        return i1.getDate().equals(i2.getDate()) &&
               ((lessOrEqual(i1.getFrom(), i2.getTo()) && (lessOrEqual(i2.getTo(), i1.getTo()))) ||
                (lessOrEqual(i2.getFrom(), i1.getTo()) && (lessOrEqual(i1.getFrom(), i2.getFrom()))));
    }

    public boolean expertIsAvailable(final DayInterval meetingInterval,
                                     final Expert expert) {

        final List<Availability> expertAvailabilities = expert.getAvailabilities();
        final List<Absence> expertAbsences = expert.getAbsences();

        final List<Availability> availabilityList = expertAvailabilities.stream()
            .filter(ea -> meetingInterval.getDate().equals(calculateDate(ea.getCalendarWeek(), ea.getDayOfWeek().getValue())))
            .filter(ea -> lessOrEqual(ea.getStartTime(), meetingInterval.getFrom()) && lessOrEqual(meetingInterval.getTo(), ea.getEndTime()))
            .toList();

        final List<Absence> absenceList = expertAbsences.stream()
                .filter(absence -> intersect(meetingInterval, new DayInterval(calculateDate(absence.getCalendarWeek(),
                        absence.getDayOfWeek().getValue()), absence.getStartTime(), absence.getEndTime())))
                .toList();

        return (!availabilityList.isEmpty() && absenceList.isEmpty());
    }

    public boolean orderIsServable(final DayInterval meetingInterval, final List<Availability> customerAvailabilities) {
        final Optional<Availability> result = customerAvailabilities.stream()
            .filter(ca -> meetingInterval.getDate().equals(calculateDate(ca.getCalendarWeek(), ca.getDayOfWeek().getValue())))
            .filter(ca -> lessOrEqual(ca.getStartTime(), meetingInterval.getFrom()) && lessOrEqual(meetingInterval.getTo(), ca.getEndTime()))
            .findFirst();

        return result.isPresent();
    }
}
