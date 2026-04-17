package org.imd.expertschedule.planner.util;

import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.time.Availability;
import org.imd.expertschedule.planner.domain.time.WeekPeriod;
import org.imd.expertschedule.planner.solution.PlannerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlannerHelperTest {

    private PlannerHelper plannerHelper;
    private PlannerParameters plannerParameters;

    @BeforeEach
    void setUp() {
        plannerHelper = new PlannerHelper();
        plannerParameters = new PlannerParameters();
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
        assertTrue(jan5th.isEqual(LocalDate.of(2026, 1, 5)));


        final LocalDate jan6th = plannerHelper.calculateDate(2026, 2, 2);
        assertTrue(jan6th.isEqual(LocalDate.of(2026, 1, 6)));
    }

    @Test
    void testDuringLunchTime() {
        assertTrue(plannerHelper.duringLunchTime(LocalTime.of(12, 0), plannerParameters.getExpertRelated()));
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

        DayInterval interval4 = new DayInterval(LocalDate.now(), LocalTime.of(11, 30), LocalTime.of(12, 30));
        assertTrue(plannerHelper.overlapLunchTime(interval4, plannerParameters.getExpertRelated()));

        // Neither endpoint falls inside lunch, but the interval still covers 12:00–12:59
        DayInterval interval5 = new DayInterval(LocalDate.now(), LocalTime.of(11, 45), LocalTime.of(13, 15));
        assertTrue(plannerHelper.overlapLunchTime(interval5, plannerParameters.getExpertRelated()));

        DayInterval interval6 = new DayInterval(LocalDate.now(), LocalTime.of(13, 0), LocalTime.of(14, 0));
        assertFalse(plannerHelper.overlapLunchTime(interval6, plannerParameters.getExpertRelated()));
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
    void countIntervalIntersects_emptyMap_returnsZero() {
        assertEquals(0, plannerHelper.countIntervalIntersects(Map.of()));
    }

    @Test
    void countIntervalIntersects_fewerThanTwoIntervalsPerDay_returnsZero() {
        LocalDate d = LocalDate.of(2026, 3, 10);
        Map<LocalDate, List<DayInterval>> map = new HashMap<>();
        map.put(d, List.of());
        map.put(LocalDate.of(2026, 3, 11), List.of(iv(d, 9, 0, 10, 0)));
        assertEquals(0, plannerHelper.countIntervalIntersects(map));
    }

    @Test
    void countIntervalIntersects_twoDisjointIntervalsSameDay_returnsZero() {
        LocalDate d = LocalDate.of(2026, 3, 10);
        Map<LocalDate, List<DayInterval>> map = Map.of(d,
                List.of(iv(d, 9, 0, 10, 0), iv(d, 11, 0, 12, 0)));
        assertEquals(0, plannerHelper.countIntervalIntersects(map));
    }

    @Test
    void countIntervalIntersects_twoOverlappingIntervalsSameDay_returnsOne() {
        LocalDate d = LocalDate.of(2026, 3, 10);
        Map<LocalDate, List<DayInterval>> map = Map.of(d,
                List.of(iv(d, 9, 0, 10, 0), iv(d, 9, 30, 10, 30)));
        assertEquals(1, plannerHelper.countIntervalIntersects(map));
    }

    @Test
    void countIntervalIntersects_threeIntervals_oneOverlapPair_countsOne() {
        LocalDate d = LocalDate.of(2026, 3, 10);
        Map<LocalDate, List<DayInterval>> map = Map.of(d,
                List.of(
                        iv(d, 9, 0, 10, 0),
                        iv(d, 9, 30, 10, 30),
                        iv(d, 14, 0, 15, 0)));
        assertEquals(1, plannerHelper.countIntervalIntersects(map));
    }

    @Test
    void countIntervalIntersects_threeIntervals_allPairsOverlap_countsThree() {
        LocalDate d = LocalDate.of(2026, 3, 10);
        Map<LocalDate, List<DayInterval>> map = Map.of(d,
                List.of(
                        iv(d, 9, 0, 11, 0),
                        iv(d, 10, 0, 12, 0),
                        iv(d, 11, 0, 13, 0)));
        assertEquals(3, plannerHelper.countIntervalIntersects(map));
    }

    @Test
    void countIntervalIntersects_twoDays_sumsAcrossDays() {
        LocalDate d1 = LocalDate.of(2026, 3, 10);
        LocalDate d2 = LocalDate.of(2026, 3, 11);
        Map<LocalDate, List<DayInterval>> map = new HashMap<>();
        map.put(d1, List.of(iv(d1, 9, 0, 10, 0), iv(d1, 9, 30, 10, 30)));
        map.put(d2, List.of(iv(d2, 14, 0, 15, 0), iv(d2, 14, 30, 15, 30)));
        assertEquals(2, plannerHelper.countIntervalIntersects(map));
    }

    @Test
    void countIntervalIntersects_differentDates_noCountAcrossDates() {
        LocalDate d1 = LocalDate.of(2026, 3, 10);
        LocalDate d2 = LocalDate.of(2026, 3, 11);
        Map<LocalDate, List<DayInterval>> map = new HashMap<>();
        map.put(d1, List.of(iv(d1, 9, 0, 17, 0)));
        map.put(d2, List.of(iv(d2, 9, 0, 17, 0)));
        assertEquals(0, plannerHelper.countIntervalIntersects(map));
    }

    private static DayInterval iv(LocalDate date, int fromH, int fromM, int toH, int toM) {
        return new DayInterval(date, LocalTime.of(fromH, fromM), LocalTime.of(toH, toM));
    }

    @Test
    void testExpertIsAvailable() {
        // Mock Expert, Availability, Absence as needed
        Expert expert = new Expert();
        expert.setAvailabilities(List.of(createAvailability(1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))));
        expert.setAbsences(List.of());
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
        result.setYear(2026);
        result.setCalendarWeek(calendarWeek);
        result.setWorkDay(dayOfWeek.getValue());
        result.setStartTime(startTime);
        result.setEndTime(endTime);

        return result;
    }

    @Test
    void calculateDaysDifference() {
        assertEquals(0, plannerHelper.calculateDaysDifference(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 1)));
        assertEquals(-1, plannerHelper.calculateDaysDifference(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2)));
        assertEquals(1, plannerHelper.calculateDaysDifference(LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 1)));
    }

    @Test
    void testIntersectForWeekPeriods() {

        assertEquals(0, plannerHelper.intersect(buildWeekPeriod(2026, 12, 1, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                                                         buildWeekPeriod(2025, 12, 1, LocalTime.of(10, 0), LocalTime.of(13, 0))).minutes());

        assertEquals(0, plannerHelper.intersect(buildWeekPeriod(2026, 12, 1, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                                                         buildWeekPeriod(2025, 11, 1, LocalTime.of(10, 0), LocalTime.of(13, 0))).minutes());

        assertEquals(0, plannerHelper.intersect(buildWeekPeriod(2026, 12, 1, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                                                         buildWeekPeriod(2025, 12, 2, LocalTime.of(10, 0), LocalTime.of(13, 0))).minutes());

        // regular cases

        assertEquals(120, plannerHelper.intersect(buildWeekPeriod(2026, 12, 1, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                                                           buildWeekPeriod(2026, 12, 1, LocalTime.of(10, 0), LocalTime.of(13, 0))).minutes());

        assertEquals(0, plannerHelper.intersect(buildWeekPeriod(2026, 12, 1, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                                                         buildWeekPeriod(2026, 12, 1, LocalTime.of(11, 0), LocalTime.of(12, 0))).minutes());

        assertEquals(60, plannerHelper.intersect(buildWeekPeriod(2026, 12, 1, LocalTime.of(10, 0), LocalTime.of(11, 0)),
                                                          buildWeekPeriod(2026, 12, 1, LocalTime.of(9, 0), LocalTime.of(12, 0))).minutes());

        assertEquals(60, plannerHelper.intersect(buildWeekPeriod(2026, 12, 1, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                                                          buildWeekPeriod(2026, 12, 1, LocalTime.of(10, 0), LocalTime.of(11, 0))).minutes());

        // exceptional cases

        assertEquals(0, plannerHelper.intersect(buildWeekPeriod(2026, 12, 1, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                                                           buildWeekPeriod(2026, 12, 1, LocalTime.of(10, 0), LocalTime.of(10, 0))).minutes());

        assertEquals(0, plannerHelper.intersect(buildWeekPeriod(2026, 12, 1, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                                                         buildWeekPeriod(2026, 12, 1, LocalTime.of(11, 0), LocalTime.of(11, 0))).minutes());

        assertEquals(0, plannerHelper.intersect(buildWeekPeriod(2026, 12, 1, LocalTime.of(10, 0), LocalTime.of(11, 0)),
                                                         buildWeekPeriod(2026, 12, 1, LocalTime.of(9, 0), LocalTime.of(9, 0))).minutes());

        assertEquals(0, plannerHelper.intersect(buildWeekPeriod(2026, 12, 1, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                                                         buildWeekPeriod(2026, 12, 1, LocalTime.of(10, 0), LocalTime.of(10, 0))).minutes());

    }

    private WeekPeriod buildWeekPeriod(int year,
                                       int calendarWeek,
                                       int workDay,
                                       LocalTime startTime,
                                       LocalTime endTime) {
        final WeekPeriod weekPeriod = new WeekPeriod();
        weekPeriod.setYear(year);
        weekPeriod.setCalendarWeek(calendarWeek);
        weekPeriod.setWorkDay(workDay);
        weekPeriod.setStartTime(startTime);
        weekPeriod.setEndTime(endTime);

        return weekPeriod;
    }
}
