package org.imd.expertschedule.planner.validator;

import org.imd.expertschedule.planner.analyzer.DistributionAnalyzer;
import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.Skill;
import org.imd.expertschedule.planner.domain.refs.ExpertRef;
import org.imd.expertschedule.planner.domain.refs.OrderRef;
import org.imd.expertschedule.planner.domain.time.Availability;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.imd.expertschedule.planner.util.PlannerHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillsSupplyAndDemandValidatorTest {

    private static final int YEAR = 2026;
    private static final int CALENDAR_WEEK = 10;

    private PlannerHelper plannerHelper;
    private LocalDate planningMonday;
    private LocalDate planningTuesday;
    private LocalDate planningWednesday;

    @BeforeEach
    void setUp() {
        plannerHelper = new PlannerHelper();
        planningMonday = plannerHelper.calculateDate(YEAR, CALENDAR_WEEK, 1);
        planningTuesday = plannerHelper.calculateDate(YEAR, CALENDAR_WEEK, 2);
        planningWednesday = plannerHelper.calculateDate(YEAR, CALENDAR_WEEK, 3);
    }

    @Test
    void validate_whenDemandExceedsSupplyOnSameDueDate_addsViolation() {
        Skill electrical = new Skill();
        electrical.setName("Electrical");

        Order order = new Order();
        OrderRef orderRef = new OrderRef();
        orderRef.setId(1L);
        order.setId(orderRef);
        order.setDueDate(planningMonday);
        order.setDiagnosisDuration(Duration.ofMinutes(600));
        order.setRequiredSkills(Set.of(electrical));

        Availability availability = new Availability();
        availability.setYear(YEAR);
        availability.setCalendarWeek(CALENDAR_WEEK);
        availability.setWorkDay(1);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(11, 0));

        Expert expert = new Expert();
        ExpertRef expertRef = new ExpertRef();
        expertRef.setId(1L);
        expert.setId(expertRef);
        expert.setSkills(Set.of(electrical));
        expert.setAvailabilities(List.of(availability));
        expert.setAbsences(Collections.emptyList());

        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setOrderList(List.of(order));
        solution.setExpertList(List.of(expert));

        SkillsSupplyAndDemandValidator validator =
                new SkillsSupplyAndDemandValidator(new DistributionAnalyzer(plannerHelper));
        List<Violation> violations = new ArrayList<>();
        validator.validate(solution, violations);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("under-supplied")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Electrical")));
    }

    @Test
    void validate_whenSupplyCoversDemand_noUnderSupplyViolation() {
        Skill electrical = new Skill();
        electrical.setName("Electrical");

        Order order = new Order();
        OrderRef orderRef = new OrderRef();
        orderRef.setId(1L);
        order.setId(orderRef);
        order.setDueDate(planningMonday);
        order.setDiagnosisDuration(Duration.ofMinutes(60));
        order.setRequiredSkills(Set.of(electrical));

        Availability availability = new Availability();
        availability.setYear(YEAR);
        availability.setCalendarWeek(CALENDAR_WEEK);
        availability.setWorkDay(1);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(12, 0));

        Expert expert = new Expert();
        ExpertRef expertRef = new ExpertRef();
        expertRef.setId(1L);
        expert.setId(expertRef);
        expert.setSkills(Set.of(electrical));
        expert.setAvailabilities(List.of(availability));
        expert.setAbsences(Collections.emptyList());

        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setOrderList(List.of(order));
        solution.setExpertList(List.of(expert));

        SkillsSupplyAndDemandValidator validator =
                new SkillsSupplyAndDemandValidator(new DistributionAnalyzer(plannerHelper));
        List<Violation> violations = new ArrayList<>();
        validator.validate(solution, violations);

        assertFalse(violations.stream().anyMatch(v -> v.getMessage().contains("under-supplied")));
    }

    @Test
    void validate_twoDueDates_supplyOnlyOnEarlierDate_doesNotCoverLaterDueDate() {
        Skill skill = skill("Electrical");
        Order orderMonday = order(1L, planningMonday, Duration.ofMinutes(60), skill);
        Order orderTuesday = order(2L, planningTuesday, Duration.ofMinutes(60), skill);

        // All availability lands in the Monday bucket (first matching due date); Tuesday demand sees no supply.
        Expert expert = expert(1L, Set.of(skill),
                List.of(availability(YEAR, CALENDAR_WEEK, 1, LocalTime.of(9, 0), LocalTime.of(12, 0))));

        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setOrderList(List.of(orderMonday, orderTuesday));
        solution.setExpertList(List.of(expert));

        List<Violation> violations = new ArrayList<>();
        new SkillsSupplyAndDemandValidator(new DistributionAnalyzer(plannerHelper)).validate(solution, violations);

        long underSupplyCount = violations.stream().filter(v -> v.getMessage().contains("under-supplied")).count();
        assertEquals(1, underSupplyCount);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains(planningTuesday.toString())));
        assertFalse(violations.stream().anyMatch(v -> v.getMessage().contains(planningMonday.toString())
                && v.getMessage().contains("under-supplied")));
    }

    @Test
    void validate_twoDueDates_eachDateGetsOwnAvailability_noViolations() {
        Skill skill = skill("Electrical");
        Order orderMonday = order(1L, planningMonday, Duration.ofMinutes(60), skill);
        Order orderTuesday = order(2L, planningTuesday, Duration.ofMinutes(60), skill);

        Expert expert = expert(1L, Set.of(skill), List.of(
                availability(YEAR, CALENDAR_WEEK, 1, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                availability(YEAR, CALENDAR_WEEK, 2, LocalTime.of(9, 0), LocalTime.of(10, 0))));

        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setOrderList(List.of(orderMonday, orderTuesday));
        solution.setExpertList(List.of(expert));

        List<Violation> violations = new ArrayList<>();
        new SkillsSupplyAndDemandValidator(new DistributionAnalyzer(plannerHelper)).validate(solution, violations);

        assertFalse(violations.stream().anyMatch(v -> v.getMessage().contains("under-supplied")));
    }

    @Test
    void validate_threeDueDates_middleDateMissesSupply_otherDatesOk() {
        Skill skill = skill("Plumbing");
        Order mon = order(1L, planningMonday, Duration.ofMinutes(60), skill);
        Order tue = order(2L, planningTuesday, Duration.ofMinutes(60), skill);
        Order wed = order(3L, planningWednesday, Duration.ofMinutes(60), skill);

        Expert expert = expert(1L, Set.of(skill), List.of(
                availability(YEAR, CALENDAR_WEEK, 1, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                availability(YEAR, CALENDAR_WEEK, 3, LocalTime.of(9, 0), LocalTime.of(10, 0))));

        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setOrderList(List.of(mon, tue, wed));
        solution.setExpertList(List.of(expert));

        List<Violation> violations = new ArrayList<>();
        new SkillsSupplyAndDemandValidator(new DistributionAnalyzer(plannerHelper)).validate(solution, violations);

        assertEquals(1, violations.stream().filter(v -> v.getMessage().contains("under-supplied")).count());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains(planningTuesday.toString())));
    }

    @Test
    void validate_threeDueDates_singleEarlyAvailabilityOnlyFlowsToFirstDueDate() {
        Skill skill = skill("Electrical");
        Order mon = order(1L, planningMonday, Duration.ofMinutes(60), skill);
        Order tue = order(2L, planningTuesday, Duration.ofMinutes(60), skill);
        Order wed = order(3L, planningWednesday, Duration.ofMinutes(60), skill);

        // 120 minutes on Monday only → Monday bucket is satisfied; Tuesday and Wednesday stay empty.
        Expert expert = expert(1L, Set.of(skill),
                List.of(availability(YEAR, CALENDAR_WEEK, 1, LocalTime.of(9, 0), LocalTime.of(11, 0))));

        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setOrderList(List.of(mon, tue, wed));
        solution.setExpertList(List.of(expert));

        List<Violation> violations = new ArrayList<>();
        new SkillsSupplyAndDemandValidator(new DistributionAnalyzer(plannerHelper)).validate(solution, violations);

        assertEquals(2, violations.stream().filter(v -> v.getMessage().contains("under-supplied")).count());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains(planningTuesday.toString())));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains(planningWednesday.toString())));
        assertFalse(violations.stream().anyMatch(v -> v.getMessage().contains(planningMonday.toString())
                && v.getMessage().contains("under-supplied")));
    }

    private static Skill skill(String name) {
        Skill s = new Skill();
        s.setName(name);
        return s;
    }

    private static Order order(long id, LocalDate dueDate, Duration diagnosisDuration, Skill... skills) {
        Order order = new Order();
        OrderRef orderRef = new OrderRef();
        orderRef.setId(id);
        order.setId(orderRef);
        order.setDueDate(dueDate);
        order.setDiagnosisDuration(diagnosisDuration);
        order.setRequiredSkills(Set.copyOf(Arrays.asList(skills)));
        return order;
    }

    private static Expert expert(long id, Set<Skill> skills, List<Availability> availabilities) {
        Expert expert = new Expert();
        ExpertRef expertRef = new ExpertRef();
        expertRef.setId(id);
        expert.setId(expertRef);
        expert.setSkills(skills);
        expert.setAvailabilities(availabilities);
        expert.setAbsences(Collections.emptyList());
        return expert;
    }

    private static Availability availability(int year, int calendarWeek, int workDay,
                                             LocalTime start, LocalTime end) {
        Availability a = new Availability();
        a.setYear(year);
        a.setCalendarWeek(calendarWeek);
        a.setWorkDay(workDay);
        a.setStartTime(start);
        a.setEndTime(end);
        return a;
    }
}
