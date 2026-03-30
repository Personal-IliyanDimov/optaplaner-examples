package org.imd.expertschedule.planner.util;

import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.time.Availability;
import org.imd.expertschedule.planner.solution.PlannerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlannerHelperTest {

    private PlannerHelper plannerHelper;
    private PlannerParameters plannerParameters;

    @BeforeEach
    void setUp() {
        plannerHelper = new PlannerHelper();
        final PlannerParameters plannerParameters = new PlannerParameters();
        plannerParameters.getPlannerRelated().setCalendarWeek(10);
        plannerParameters.getPlannerRelated().setWorkingDays(new int[] {1, 2, 3, 4, 5}); // Example lunch times
    }

    @Test
    void calculateDate_ValidResults() {
        int[] unvalidWorkingDays = new int[] {1,2,3};
        for (int uwd :unvalidWorkingDays){
            final LocalDate test = plannerHelper.calculateDate(2026, 1, uwd);
            assertTrue(test.isEqual(LocalDate.of(2026, 1, 1)));
        }

        final LocalDate jan1st = plannerHelper.calculateDate(2026, 1, 4);
        assertTrue(jan1st.isEqual(LocalDate.of(2026, 1, 1)));

        final LocalDate jan5th = plannerHelper.calculateDate(2026, 2, 1);
        assertTrue(jan1st.isEqual(LocalDate.of(2026, 1, 5)));


        final LocalDate jan6th = plannerHelper.calculateDate(2026, 2, 2);
        assertTrue(jan1st.isEqual(LocalDate.of(2026, 1, 6)));
    }

    @Test
    void testDuringLunchTime() {
        assertTrue(plannerHelper.duringLunchTime(LocalTime.of(12, 00), plannerParameters.getExpertRelated()));
        assertTrue(plannerHelper.duringLunchTime(LocalTime.of(12, 30), plannerParameters.getExpertRelated()));
        assertTrue(plannerHelper.duringLunchTime(LocalTime.of(12, 59), plannerParameters.getExpertRelated()));

        assertFalse(plannerHelper.duringLunchTime(LocalTime.of(11, 59), plannerParameters.getExpertRelated()));
        assertFalse(plannerHelper.duringLunchTime(LocalTime.of(13, 0), plannerParameters.getExpertRelated()));
    }

    @Test
    void testOverlapLunchTime() {
        DayInterval interval1 = new DayInterval(LocalDate.now(), LocalTime.of(12, 0), LocalTime.of(12, 30));
        assertTrue(plannerHelper.overlapLunchTime(interval1, plannerParameters.getExpertRelated()));

        DayInterval interval2 = new DayInterval(LocalDate.now(), LocalTime.of(11, 45), LocalTime.of(12, 15));
        assertTrue(plannerHelper.overlapLunchTime(interval2, plannerParameters.getExpertRelated()));

        DayInterval interval3 = new DayInterval(LocalDate.now(), LocalTime.of(12, 45), LocalTime.of(13, 15));
        assertTrue(plannerHelper.overlapLunchTime(interval3, plannerParameters.getExpertRelated()));

        DayInterval interval4 = new DayInterval(LocalDate.now(), LocalTime.of(11, 00), LocalTime.of(14, 00));
        assertTrue(plannerHelper.overlapLunchTime(interval4, plannerParameters.getExpertRelated()));
    }

    @Test
    void testLessOrEqual() {
        assertTrue(plannerHelper.lessOrEqual(LocalTime.of(10, 0), LocalTime.of(11, 0)));
        assertTrue(plannerHelper.lessOrEqual(LocalTime.of(10, 0), LocalTime.of(10, 0)));
        assertFalse(plannerHelper.lessOrEqual(LocalTime.of(11, 0), LocalTime.of(10, 0)));
    }

    @Test
    void testIntersect() {
        DayInterval i1 = new DayInterval(LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0));
        DayInterval i2 = new DayInterval(LocalDate.now(), LocalTime.of(9, 30), LocalTime.of(10, 30));
        assertTrue(plannerHelper.intersect(i1, i2));
    }

    @Test
    void testExpertIsAvailable() {
        // Mock Expert, Availability, Absence as needed
        Expert expert = new Expert();
        expert.setAvailabilities(List.of(createAvailability(1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))));
        DayInterval meeting = new DayInterval(plannerHelper.calculateDate(2026, 1, 1), LocalTime.of(10, 0), LocalTime.of(11, 0));
        assertTrue(plannerHelper.expertIsAvailable(meeting, expert));
    }

    @Test
    void testOrderIsServable() {
        List<Availability> availabilities = List.of(createAvailability(1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        DayInterval meeting = new DayInterval(plannerHelper.calculateDate(2026, 1, 1), LocalTime.of(10, 0), LocalTime.of(11, 0));
        assertTrue(plannerHelper.orderIsServable(meeting, availabilities));
    }

    private Availability createAvailability(int calendarWeek, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        final Availability result = new Availability();
        result.setCalendarWeek(calendarWeek);
        result.setWorkDay(dayOfWeek);
        result.setStartTime(startTime);
        result.setEndTime(endTime);

        return  result;
    }
}
