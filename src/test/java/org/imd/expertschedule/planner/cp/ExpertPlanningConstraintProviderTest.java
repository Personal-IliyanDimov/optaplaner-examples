package org.imd.expertschedule.planner.cp;

import org.imd.expertschedule.planner.domain.BackOffice;
import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.ExpertSchedule;
import org.imd.expertschedule.planner.domain.Location;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.OrderPriority;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.domain.Skill;
import org.imd.expertschedule.planner.domain.refs.BackOfficeRef;
import org.imd.expertschedule.planner.domain.refs.ExpertRef;
import org.imd.expertschedule.planner.domain.refs.OrderRef;
import org.imd.expertschedule.planner.domain.time.Availability;
import org.imd.expertschedule.planner.domain.time.TimeSlot;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.imd.expertschedule.planner.solution.PlannerParameters;
import org.imd.expertschedule.planner.util.DayInterval;
import org.imd.expertschedule.planner.util.PlannerHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.solver.SolutionManager;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpertPlanningConstraintProviderTest {

    private final PlannerHelper plannerHelper = new PlannerHelper();
    private PlannerParameters plannerParameters;

    private LocalDate anchorDate;

    private SolutionManager<ExpertPlanningSolution, HardMediumSoftScore> solutionManager;

    @BeforeEach
    void setUp() {
        plannerParameters = new PlannerParameters();
        plannerParameters.getPlannerRelated().setYear(2026);
        plannerParameters.getPlannerRelated().setCalendarWeek(2);
        plannerParameters.getPlannerRelated().setWorkingDays(new int[] { 1, 2, 3, 4, 5 });

        anchorDate = plannerHelper.calculateDate(2026, 2, 1);

        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(ExpertPlanningSolution.class)
                .withEntityClasses(ScheduleItem.class)
                .withConstraintProviderClass(ExpertPlanningConstraintProvider.class);
        solutionManager = SolutionManager.create(SolverFactory.create(solverConfig));
    }

    @Test
    void matchExpertAvailability_whenExpertNotAvailableForSlot_countsExpertAvailabilityConflict() {
        Expert expert = expertWithAvailabilities(skilled(skillJava()), avail(2026, 2, 1, LocalTime.of(14, 0), LocalTime.of(18, 0)));
        Order order = order(anchorDate, diagHour(60), customerAvailWide());
        ExpertSchedule schedule = new ExpertSchedule(expert, anchorDate);
        ScheduleItem si = scheduleItem(order, schedule, LocalTime.of(10, 0));

        ExpertPlanningSolution solution =
                wrapSolution(List.of(si), expert, schedule, ts(LocalTime.of(10, 0)));

        ScoreExplanation<ExpertPlanningSolution, HardMediumSoftScore> explanation =
                solutionManager.explain(solution);
        assertTrue(
                constraintMatchTotal(explanation, ExpertPlanningConstraintConfiguration.WeightNames.EA_AVAILABILITY_CONFLICT)
                        .hasMatches());
        assertHardNegative(explanation, ExpertPlanningConstraintConfiguration.WeightNames.EA_AVAILABILITY_CONFLICT);
    }

    @Test
    void matchExpertAvailabilityNoLunchOverlap_whenAppointmentSpansWorkingLunch_breaksExpertLunchConflict() {
        Expert expert = expertWithAvailabilities(skilled(skillJava()), avail(2026, 2, 1, LocalTime.of(9, 0), LocalTime.of(18, 0)));
        Order order = order(anchorDate, Duration.ofMinutes(120), customerAvail(LocalTime.of(9, 0), LocalTime.of(18, 0)));
        ExpertSchedule schedule = new ExpertSchedule(expert, anchorDate);
        ScheduleItem si = scheduleItem(order, schedule, LocalTime.of(11, 45));

        ExpertPlanningSolution solution =
                wrapSolution(List.of(si), expert, schedule, ts(LocalTime.of(11, 45)));

        ScoreExplanation<ExpertPlanningSolution, HardMediumSoftScore> explanation =
                solutionManager.explain(solution);
        assertTrue(
                constraintMatchTotal(explanation, ExpertPlanningConstraintConfiguration.WeightNames.EA_LUNCH_TIME_CONFLICT)
                        .hasMatches());
        assertHardNegative(explanation, ExpertPlanningConstraintConfiguration.WeightNames.EA_LUNCH_TIME_CONFLICT);
    }

    @Test
    void matchNoOverlapsExpertOtherMeetings_whenTwoVisitsIntersectSameExpert_countsOverlapConflict() {
        Expert expert = expertWithAvailabilities(skilled(skillJava()), avail(2026, 2, 1, LocalTime.of(9, 0), LocalTime.of(18, 0)));
        Order orderEarly = ordered(anchorDate, Duration.ofMinutes(60), skillJava(),
                List.of(cust(LocalTime.of(9, 0), LocalTime.of(12, 0))), 101L);
        Order orderLate = ordered(anchorDate, Duration.ofMinutes(60), skillJava(),
                List.of(cust(LocalTime.of(9, 0), LocalTime.of(13, 0))), 102L);

        ExpertSchedule schedule = new ExpertSchedule(expert, anchorDate);

        ScheduleItem si1 = scheduleItem(orderEarly, schedule, LocalTime.of(9, 0));
        ScheduleItem si2 = scheduleItem(orderLate, schedule, LocalTime.of(9, 30));

        ExpertPlanningSolution solution = wrapSolution(
                List.of(si1, si2),
                expert,
                List.of(schedule),
                List.of(ts(LocalTime.of(9, 0)), ts(LocalTime.of(9, 30))));

        ScoreExplanation<ExpertPlanningSolution, HardMediumSoftScore> explanation =
                solutionManager.explain(solution);
        ConstraintMatchTotals overlap =
                constraintMatchTotal(explanation, ExpertPlanningConstraintConfiguration.WeightNames.OVERLAPS_WITH_OTHER_MEETING_CONFLICT);
        assertTrue(overlap.hasMatches());
        assertHardNegative(explanation, ExpertPlanningConstraintConfiguration.WeightNames.OVERLAPS_WITH_OTHER_MEETING_CONFLICT);

        LocalDate d = anchorDate;
        Map<LocalDate, List<DayInterval>> grouped = Map.of(d, List.of(
                new DayInterval(d, LocalTime.of(9, 0), LocalTime.of(9, 59)),
                new DayInterval(d, LocalTime.of(9, 30), LocalTime.of(10, 29))));
        assertEquals(1, plannerHelper.countIntervalIntersects(grouped));
    }

    @Test
    void matchOrderAvailability_whenCustomerWindowsMissVisit_countsOrderAvailabilityConflict() {
        Expert expert = expertWithAvailabilities(skilled(skillJava()), avail(2026, 2, 1, LocalTime.of(9, 0), LocalTime.of(18, 0)));
        Order order = order(anchorDate, diagHour(60), customerAvail(LocalTime.of(14, 0), LocalTime.of(18, 0)));
        ExpertSchedule schedule = new ExpertSchedule(expert, anchorDate);
        ScheduleItem si = scheduleItem(order, schedule, LocalTime.of(10, 0));

        ExpertPlanningSolution solution =
                wrapSolution(List.of(si), expert, schedule, ts(LocalTime.of(10, 0)));

        ScoreExplanation<ExpertPlanningSolution, HardMediumSoftScore> explanation =
                solutionManager.explain(solution);
        assertTrue(
                constraintMatchTotal(explanation, ExpertPlanningConstraintConfiguration.WeightNames.OA_AVAILABILITY_CONFLICT)
                        .hasMatches());
        assertHardNegative(explanation, ExpertPlanningConstraintConfiguration.WeightNames.OA_AVAILABILITY_CONFLICT);
    }

    @Test
    void matchExpertSkillsAndOrderSkills_whenExpertMissingRequiredSkill_countsSkillsConflict() {
        Skill needed = skillJava();
        Expert expert = expertWithAvailabilities(Set.of(), avail(2026, 2, 1, LocalTime.of(9, 0), LocalTime.of(18, 0)));
        Order order = ordered(anchorDate, diagHour(60), needed, customerAvailWide(), 201L);
        ExpertSchedule schedule = new ExpertSchedule(expert, anchorDate);
        ScheduleItem si = scheduleItem(order, schedule, LocalTime.of(10, 0));

        ExpertPlanningSolution solution =
                wrapSolution(List.of(si), expert, schedule, ts(LocalTime.of(10, 0)));

        ScoreExplanation<ExpertPlanningSolution, HardMediumSoftScore> explanation =
                solutionManager.explain(solution);
        assertTrue(
                constraintMatchTotal(explanation, ExpertPlanningConstraintConfiguration.WeightNames.ES_VS_OS_SKILL_CONFLICT)
                        .hasMatches());
        assertHardNegative(explanation, ExpertPlanningConstraintConfiguration.WeightNames.ES_VS_OS_SKILL_CONFLICT);
    }

    @Test
    void matchOrderDueDate_whenVisitAfterDueDate_countsDueDateConflictByPriority() {
        LocalDate scheduled = plannerHelper.calculateDate(2026, 2, 2);
        LocalDate dueDate = plannerHelper.calculateDate(2026, 2, 1);
        Skill needed = skillJava();
        Expert expert = expertWithAvailabilities(skilled(needed),
                availForDate(scheduled, LocalTime.of(9, 0), LocalTime.of(18, 0)));
        Order order = ordered(dueDate, diagHour(60), needed, customerAvailFor(scheduled), 301L);
        order.setPriority(OrderPriority.HIGH);
        ExpertSchedule schedule = new ExpertSchedule(expert, scheduled);
        ScheduleItem si = scheduleItem(order, schedule, LocalTime.of(10, 0));

        ExpertPlanningSolution solution =
                wrapSolution(List.of(si), expert, schedule, ts(LocalTime.of(10, 0)));

        ScoreExplanation<ExpertPlanningSolution, HardMediumSoftScore> explanation =
                solutionManager.explain(solution);
        ConstraintMatchTotals due =
                constraintMatchTotal(explanation, ExpertPlanningConstraintConfiguration.WeightNames.O_DUE_DATE_CONFLICT);
        assertTrue(due.hasMatches());

        PlannerHelper helper = new PlannerHelper();
        int delay = helper.calculateDaysDifference(scheduled, dueDate);
        assertEquals(1, delay);
        assertHardScoreEquals(
                explanation, ExpertPlanningConstraintConfiguration.WeightNames.O_DUE_DATE_CONFLICT,
                -(OrderPriority.HIGH.getLevel() * delay));
    }

    @Test
    void matchExpertLimitTravelDistancePerDay_whenPairwiseRouteExceedsLimit_countsTravelConflict() {
        Location office = location(52.3676, 4.9041);
        Expert expert =
                expertWithOffice(office, skilled(skillJava()),
                        avail(2026, 2, 1, LocalTime.of(9, 0), LocalTime.of(18, 0)));

        Order farWest = geoOrder(401L, location(-33.8688, 151.2093));
        Order farEast = geoOrder(402L, location(35.6762, 139.6503));

        ExpertSchedule schedule = new ExpertSchedule(expert, anchorDate);

        ScheduleItem si1 = scheduleItem(farWest, schedule, LocalTime.of(9, 0));
        ScheduleItem si2 = scheduleItem(farEast, schedule, LocalTime.of(11, 0));

        ExpertPlanningSolution solution =
                wrapSolutionGeo(List.of(si1, si2), expert, List.of(schedule), geoTimeSlots(LocalTime.of(9, 0), LocalTime.of(11, 0)));

        ScoreExplanation<ExpertPlanningSolution, HardMediumSoftScore> explanation =
                solutionManager.explain(solution);

        PlannerHelper planner = new PlannerHelper();
        Integer travel = planner.calculateTotalTravelDistance(List.of(si1, si2));
        assertTrue(travel != null && travel > plannerParameters.getTravelRelated().getMaxTravelDistancePerDay());

        assertTrue(
                constraintMatchTotal(explanation, ExpertPlanningConstraintConfiguration.WeightNames.EL_TD_PD_CONFLICT)
                        .hasMatches());
        assertHardNegative(explanation, ExpertPlanningConstraintConfiguration.WeightNames.EL_TD_PD_CONFLICT);
    }

    @Test
    void fairlyDistributePerExpertPerPeriodScheduledItems_whenExpertsImbalanced_penalizesSoftBalance() {
        Skill needed = skillJava();
        Expert a = labeledExpert(1L, "A",
                skilled(needed),
                avail(2026, 2, 1, LocalTime.of(9, 0), LocalTime.of(18, 0)));
        Expert b = labeledExpert(2L, "B",
                skilled(needed),
                avail(2026, 2, 1, LocalTime.of(9, 0), LocalTime.of(18, 0)));

        ExpertSchedule sa = new ExpertSchedule(a, anchorDate);
        ExpertSchedule sb = new ExpertSchedule(b, anchorDate);

        Order longVisit = ordered(anchorDate, Duration.ofMinutes(180), needed, customerAvailWide(), 501L);
        Order shortVisit = ordered(anchorDate, Duration.ofMinutes(30), needed, customerAvailWide(), 502L);

        ScheduleItem heavy = scheduleItem(longVisit, sa, LocalTime.of(9, 0));
        ScheduleItem light = scheduleItem(shortVisit, sb, LocalTime.of(9, 0));

        ExpertPlanningSolution solution = wrapSolution(
                List.of(heavy, light),
                List.of(a, b),
                List.of(sa, sb),
                List.of(ts(LocalTime.of(9, 0))));

        ScoreExplanation<ExpertPlanningSolution, HardMediumSoftScore> explanation =
                solutionManager.explain(solution);
        assertTrue(
                constraintMatchTotal(explanation, ExpertPlanningConstraintConfiguration.WeightNames.FD_PE_PP_SI_CONFLICT)
                        .hasMatches());
        assertSoftNegative(explanation, ExpertPlanningConstraintConfiguration.WeightNames.FD_PE_PP_SI_CONFLICT);
    }

    @Test
    void fairlyDistributePerExpertPerDayScheduledItems_whenExpertsImbalancedSameDay_penalizesSoftBalance() {
        Skill needed = skillJava();
        Expert a = labeledExpert(3L, "C",
                skilled(needed),
                avail(2026, 2, 1, LocalTime.of(9, 0), LocalTime.of(18, 0)));
        Expert b = labeledExpert(4L, "D",
                skilled(needed),
                avail(2026, 2, 1, LocalTime.of(9, 0), LocalTime.of(18, 0)));

        ExpertSchedule sa = new ExpertSchedule(a, anchorDate);
        ExpertSchedule sb = new ExpertSchedule(b, anchorDate);

        Order longVisit = ordered(anchorDate, Duration.ofMinutes(180), needed, customerAvailWide(), 601L);
        Order shortVisit = ordered(anchorDate, Duration.ofMinutes(30), needed, customerAvailWide(), 602L);

        ScheduleItem heavy = scheduleItem(longVisit, sa, LocalTime.of(9, 0));
        ScheduleItem light = scheduleItem(shortVisit, sb, LocalTime.of(9, 0));

        ExpertPlanningSolution solution = wrapSolution(
                List.of(heavy, light),
                List.of(a, b),
                List.of(sa, sb),
                List.of(ts(LocalTime.of(9, 0))));

        ScoreExplanation<ExpertPlanningSolution, HardMediumSoftScore> explanation =
                solutionManager.explain(solution);
        assertTrue(
                constraintMatchTotal(explanation, ExpertPlanningConstraintConfiguration.WeightNames.FD_PE_PD_SI_CONFLICT)
                        .hasMatches());
        assertSoftNegative(explanation, ExpertPlanningConstraintConfiguration.WeightNames.FD_PE_PD_SI_CONFLICT);
    }

    private Expert expertWithAvailabilities(Set<Skill> skills, Availability window) {
        Expert expert = new Expert();
        ExpertRef ref = new ExpertRef();
        ref.setId(10L);
        expert.setId(ref);
        expert.setBackOffice(backOffice(location(0.0, 0.0)));
        expert.setSkills(skills);
        expert.setAvailabilities(List.of(window));
        expert.setAbsences(List.of());
        return expert;
    }

    private Expert expertWithOffice(Location officeLoc, Set<Skill> skills, Availability window) {
        Expert expert = new Expert();
        ExpertRef ref = new ExpertRef();
        ref.setId(20L);
        expert.setId(ref);
        expert.setBackOffice(backOffice(officeLoc));
        expert.setSkills(skills);
        expert.setAvailabilities(List.of(window));
        expert.setAbsences(List.of());
        return expert;
    }

    private Expert labeledExpert(long id, String name, Set<Skill> skills, Availability window) {
        Expert expert = new Expert();
        ExpertRef ref = new ExpertRef();
        ref.setId(id);
        expert.setId(ref);
        expert.setName(name);
        expert.setBackOffice(backOffice(location(0.0, 0.0)));
        expert.setSkills(skills);
        expert.setAvailabilities(List.of(window));
        expert.setAbsences(List.of());
        return expert;
    }

    private Set<Skill> skilled(Skill s) {
        return Set.of(s);
    }

    private BackOffice backOffice(Location loc) {
        BackOffice bo = new BackOffice();
        BackOfficeRef id = new BackOfficeRef();
        id.setId(1L);
        bo.setId(id);
        bo.setLocation(loc);
        return bo;
    }

    private Location location(double lat, double lon) {
        return new Location(lat, lon);
    }

    private Availability avail(int year, int cw, int workDay, LocalTime from, LocalTime to) {
        Availability a = new Availability();
        a.setYear(year);
        a.setCalendarWeek(cw);
        a.setWorkDay(workDay);
        a.setStartTime(from);
        a.setEndTime(to);
        return a;
    }

    private Availability availForDate(LocalDate date, LocalTime from, LocalTime to) {
        LinkedHashMap<LocalDate, int[]> map = buildDateToYcwDay();
        int[] ycwDay = map.get(date);
        return avail(ycwDay[0], ycwDay[1], ycwDay[2], from, to);
    }

    private LinkedHashMap<LocalDate, int[]> buildDateToYcwDay() {
        LinkedHashMap<LocalDate, int[]> map = new LinkedHashMap<>();
        for (int day : plannerParameters.getPlannerRelated().getWorkingDays()) {
            LocalDate d = plannerHelper.calculateDate(
                    plannerParameters.getPlannerRelated().getYear(),
                    plannerParameters.getPlannerRelated().getCalendarWeek(),
                    day);
            map.put(d, new int[] {
                    plannerParameters.getPlannerRelated().getYear(),
                    plannerParameters.getPlannerRelated().getCalendarWeek(),
                    day
            });
        }
        return map;
    }

    private List<Availability> customerAvailWide() {
        return List.of(cust(LocalTime.of(9, 0), LocalTime.of(18, 0)));
    }

    private List<Availability> customerAvailFor(LocalDate date) {
        LinkedHashMap<LocalDate, int[]> map = buildDateToYcwDay();
        int[] ycwDay = map.get(date);
        Availability a = new Availability();
        a.setYear(ycwDay[0]);
        a.setCalendarWeek(ycwDay[1]);
        a.setWorkDay(ycwDay[2]);
        a.setStartTime(LocalTime.of(9, 0));
        a.setEndTime(LocalTime.of(18, 0));
        return List.of(a);
    }

    private Availability cust(LocalTime from, LocalTime to) {
        return avail(2026, 2, 1, from, to);
    }

    private List<Availability> customerAvail(LocalTime from, LocalTime to) {
        return List.of(cust(from, to));
    }

    private Order order(LocalDate due, Duration diagnosis, List<Availability> customerAv) {
        return ordered(due, diagnosis, skillJava(), customerAv, 1L);
    }

    private Order ordered(LocalDate due, Duration diagnosis, Skill required, List<Availability> customerAv, long orderId) {
        Order order = new Order();
        OrderRef ref = new OrderRef();
        ref.setId(orderId);
        order.setId(ref);
        order.setDueDate(due);
        order.setDiagnosisDuration(diagnosis);
        order.setRequiredSkills(Set.of(required));
        order.setCustomerAvailabilities(customerAv);
        order.setPriority(OrderPriority.LOW);
        order.setLocation(location(0.1, 0.1));
        return order;
    }

    private Order geoOrder(long orderId, Location loc) {
        Order order = ordered(anchorDate, diagHour(60), skillJava(), customerAvailWide(), orderId);
        order.setLocation(loc);
        return order;
    }

    private Duration diagHour(int minutes) {
        return Duration.ofMinutes(minutes);
    }

    private Skill skillJava() {
        Skill s = new Skill();
        s.setName("java");
        return s;
    }

    private TimeSlot ts(LocalTime start) {
        return new TimeSlot(start);
    }

    private List<TimeSlot> geoTimeSlots(LocalTime... starts) {
        List<TimeSlot> list = new ArrayList<>();
        for (LocalTime s : starts) {
            list.add(ts(s));
        }
        return list;
    }

    private ScheduleItem scheduleItem(Order order, ExpertSchedule schedule, LocalTime start) {
        ScheduleItem si = new ScheduleItem();
        si.setOrder(order);
        si.setExpertSchedule(schedule);
        si.setTimeSlot(ts(start));
        return si;
    }

    private ExpertPlanningSolution wrapSolution(
            List<ScheduleItem> items,
            Expert expert,
            ExpertSchedule schedule,
            TimeSlot slot) {
        return wrapSolution(items, List.of(expert), List.of(schedule), List.of(slot));
    }

    private ExpertPlanningSolution wrapSolution(
            List<ScheduleItem> items,
            Expert expert,
            List<ExpertSchedule> schedules,
            List<TimeSlot> slots) {
        return wrapSolution(items, List.of(expert), schedules, slots);
    }

    private ExpertPlanningSolution wrapSolution(
            List<ScheduleItem> items,
            List<Expert> experts,
            List<ExpertSchedule> schedules,
            List<TimeSlot> slots) {
        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setConstraintConfiguration(new ExpertPlanningConstraintConfiguration());
        solution.setScore(HardMediumSoftScore.ZERO);
        solution.setScheduleItemList(items);
        solution.setExpertList(experts);
        solution.setOrderList(items.stream().map(ScheduleItem::getOrder).toList());
        solution.setExpertScheduleList(schedules);
        solution.setTimeSlotList(slots);
        return solution;
    }

    private ExpertPlanningSolution wrapSolutionGeo(
            List<ScheduleItem> items,
            Expert expert,
            List<ExpertSchedule> schedules,
            List<TimeSlot> slots) {
        return wrapSolution(items, List.of(expert), schedules, slots);
    }

    private static ConstraintMatchTotals constraintMatchTotal(
            ScoreExplanation<ExpertPlanningSolution, HardMediumSoftScore> explanation,
            String constraintName) {
        for (ConstraintMatchTotal<HardMediumSoftScore> total : explanation.getConstraintMatchTotalMap().values()) {
            if (constraintName.equals(total.getConstraintName())) {
                return new ConstraintMatchTotals(total.getConstraintMatchCount(), total.getScore());
            }
        }
        return new ConstraintMatchTotals(0, HardMediumSoftScore.ZERO);
    }

    private static void assertHardNegative(
            ScoreExplanation<ExpertPlanningSolution, HardMediumSoftScore> explanation,
            String constraintName) {
        ConstraintMatchTotals t = constraintMatchTotal(explanation, constraintName);
        assertTrue(t.hasMatches());
        assertTrue(t.score().hardScore() < 0);
    }

    private static void assertSoftNegative(
            ScoreExplanation<ExpertPlanningSolution, HardMediumSoftScore> explanation,
            String constraintName) {
        ConstraintMatchTotals t = constraintMatchTotal(explanation, constraintName);
        assertTrue(t.hasMatches());
        assertTrue(t.score().softScore() < 0);
    }

    private static void assertHardScoreEquals(
            ScoreExplanation<ExpertPlanningSolution, HardMediumSoftScore> explanation,
            String constraintName,
            int expectedHard) {
        ConstraintMatchTotals t = constraintMatchTotal(explanation, constraintName);
        assertTrue(t.hasMatches());
        assertEquals(expectedHard, t.score().hardScore());
    }

    private record ConstraintMatchTotals(int matchCount, HardMediumSoftScore score) {
        boolean hasMatches() {
            return matchCount > 0;
        }
    }
}
