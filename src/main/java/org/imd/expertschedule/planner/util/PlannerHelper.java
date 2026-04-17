package org.imd.expertschedule.planner.util;

import lombok.Getter;
import lombok.Setter;
import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.time.Absence;
import org.imd.expertschedule.planner.domain.time.Availability;
import org.imd.expertschedule.planner.domain.time.WeekPeriod;
import org.imd.expertschedule.planner.solution.PlannerParameters;
import org.jfree.data.time.Week;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PlannerHelper {

    public LocalDate calculateDate(final int year, final int calendarWeek, final int calendarWeekDay) {
        if (calendarWeek < 1) {
            throw new IllegalArgumentException("Calendar week must be greater than 0. ");
        }

        if ((calendarWeekDay < 1) || (calendarWeekDay > 7)) {
            throw new IllegalArgumentException("Calendar week day must be in [1,7] range. ");
        }

        final LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);
        final int fdoyWeekDay = firstDayOfYear.getDayOfWeek().getValue();

        LocalDate result;
        if (calendarWeek == 1) {
            final int deltaInDays = calendarWeekDay - fdoyWeekDay;

            if (deltaInDays > 0) {
                result = firstDayOfYear.plusDays(deltaInDays);
            } else {
                result = firstDayOfYear;
            }
        } else {
            final int deltaInDays = calendarWeekDay - fdoyWeekDay;

            result = firstDayOfYear
                .plusWeeks(calendarWeek - 1)
                .plusDays(deltaInDays);
        }

        return result;
    }

    public boolean duringLunchTime(final LocalTime testedLocalTime, final PlannerParameters.ExpertRelated expertRelated) {
        return lessOrEqual(expertRelated.getLunchStartTime(), testedLocalTime) &&
                lessOrEqual(testedLocalTime, expertRelated.getLunchEndTime());
    }

    public boolean overlapLunchTime(final DayInterval interval, final PlannerParameters.ExpertRelated expertRelated) {
        DayInterval lunch = new DayInterval(interval.getDate(),
                expertRelated.getLunchStartTime(), expertRelated.getLunchEndTime());
        return intersect(interval, lunch);
    }

    public boolean lessOrEqual(final LocalDate leftDate, final LocalDate rightDate) {
        return (leftDate.equals(rightDate) || leftDate.isBefore(rightDate));
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
            .filter(ea -> meetingInterval.getDate().equals(calculateDate(ea.getYear(), ea.getCalendarWeek(), ea.getWorkDay())))
            .filter(ea -> lessOrEqual(ea.getStartTime(), meetingInterval.getFrom()) && lessOrEqual(meetingInterval.getTo(), ea.getEndTime()))
            .toList();

        final List<Absence> absenceList = expertAbsences.stream()
                .filter(absence -> intersect(meetingInterval, new DayInterval(
                        calculateDate(absence.getYear(), absence.getCalendarWeek(), absence.getWorkDay()),
                        absence.getStartTime(), absence.getEndTime())))
                .toList();

        return (!availabilityList.isEmpty() && absenceList.isEmpty());
    }

    public boolean orderIsServable(final DayInterval meetingInterval, final List<Availability> customerAvailabilities) {
        final Optional<Availability> result = customerAvailabilities.stream()
            .filter(ca -> meetingInterval.getDate().equals(calculateDate(ca.getYear(), ca.getCalendarWeek(), ca.getWorkDay())))
            .filter(ca -> lessOrEqual(ca.getStartTime(), meetingInterval.getFrom()) && lessOrEqual(meetingInterval.getTo(), ca.getEndTime()))
            .findFirst();

        return result.isPresent();
    }

    public int countIntervalIntersects(Map<LocalDate, List<DayInterval>> dateToDayIntervalsMap) {
        int result = 0;

        for (Map.Entry<LocalDate, List<DayInterval>> entry : dateToDayIntervalsMap.entrySet()) {
            final List<DayInterval> expertDayIntervals = entry.getValue();
            if (expertDayIntervals != null && expertDayIntervals.size() > 1) {
                for (int i = 0; i < expertDayIntervals.size() - 1; i++) {
                    for (int j = i + 1; j < expertDayIntervals.size(); j++) {
                        if (intersect(expertDayIntervals.get(i), expertDayIntervals.get(j))) {
                            result++;
                        }
                    }
                }
            }
        }

        return result;
    }

    public int calculateDaysDifference(final LocalDate left, final LocalDate right) {
        final Period betweenPeriod = Period.between(left, right);
        final int result =  betweenPeriod.getDays() * -1;
        return result;
    }

    public Long calculateRealAvailability(final Availability availability, final List<Absence> absences) {
        long result = 0;

        if (availability == null) {
            return result;
        }

        int difference =  (availability.getEndTime().toSecondOfDay() - availability.getStartTime().toSecondOfDay()) / 60;
        result = (long) difference;

        if (absences == null || absences.isEmpty()) {
            return result;
        }

        WeekPeriod avAsWp = availability;
        for (Absence absence : absences) {
           IntersectResult intersectResult = intersect(avAsWp, absence);
            long delta = intersectResult.minutes();
           if (delta > 0) {
               result -= delta;
               avAsWp = intersectResult.wp();
           }
        }

        return result;
    }

    public IntersectResult intersect(final WeekPeriod p1, final WeekPeriod p2) {
        if (p1 == null || p2 == null) {
            return new IntersectResult( 0, null);
        }

        if ( p1.getYear() != p2.getYear() ||
             p1.getCalendarWeek() != p2.getCalendarWeek() ||
             p1.getWorkDay() != p2.getWorkDay()) {

            return new IntersectResult( 0, null);
        }

        final LocalTime overlapFrom = p1.getStartTime().isAfter(p2.getStartTime()) ? p1.getStartTime() : p2.getStartTime();
        final LocalTime overlapTo = p1.getEndTime().isBefore(p2.getEndTime()) ? p1.getEndTime() : p2.getEndTime();
        if (! lessOrEqual(overlapFrom, overlapTo)) {
            return new IntersectResult( 0, null);
        }

        final long overlapInMinutes = ChronoUnit.MINUTES.between(overlapFrom, overlapTo);
        final WeekPeriod wp = new WeekPeriod();
        wp.setYear(p1.getYear());
        wp.setCalendarWeek(p1.getCalendarWeek());
        wp.setWorkDay(p1.getWorkDay());
        wp.setStartTime(overlapFrom);
        wp.setEndTime(overlapTo);

        return new IntersectResult(overlapInMinutes, wp);
    }

    public record IntersectResult(long minutes, WeekPeriod wp) {
    }
}
