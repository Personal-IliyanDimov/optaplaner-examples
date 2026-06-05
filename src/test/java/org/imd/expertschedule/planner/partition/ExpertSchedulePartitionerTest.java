package org.imd.expertschedule.planner.partition;

import org.imd.expertschedule.planner.cp.ExpertPlanningConstraintConfiguration;
import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.ExpertSchedule;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.domain.refs.ExpertRef;
import org.imd.expertschedule.planner.domain.refs.OrderRef;
import org.imd.expertschedule.planner.domain.time.TimeSlot;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpertSchedulePartitionerTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 3, 2);
    private static final LocalDate TUESDAY = LocalDate.of(2026, 3, 3);
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 3, 4);

    @Test
    void splitWorkingSolution_groupsItemsByDueDate() {
        Expert expert = expert(1);
        ExpertSchedule mondaySchedule = new ExpertSchedule(expert, MONDAY);
        ExpertSchedule tuesdaySchedule = new ExpertSchedule(expert, TUESDAY);
        ExpertSchedule wednesdaySchedule = new ExpertSchedule(expert, WEDNESDAY);

        ScheduleItem mondayDue = scheduleItem(order(1, MONDAY), mondaySchedule, LocalTime.of(9, 0));
        ScheduleItem tuesdayDue = scheduleItem(order(2, TUESDAY), tuesdaySchedule, LocalTime.of(10, 0));
        ScheduleItem alsoTuesdayDue = scheduleItem(order(3, TUESDAY), null, null);

        ExpertPlanningSolution original = wrapSolution(
                List.of(expert),
                List.of(mondaySchedule, tuesdaySchedule, wednesdaySchedule),
                List.of(mondayDue, tuesdayDue, alsoTuesdayDue));

        List<ExpertPlanningSolution> parts = split(original);

        assertEquals(2, parts.size());
        assertEquals(MONDAY, dueDateOf(parts.get(0)));
        assertEquals(TUESDAY, dueDateOf(parts.get(1)));
        assertEquals(1, parts.get(0).getScheduleItemList().size());
        assertEquals(2, parts.get(1).getScheduleItemList().size());
    }

    @Test
    void splitWorkingSolution_eachScheduleItemInExactlyOnePartition() {
        Expert expertA = expert(1);
        Expert expertB = expert(2);
        List<ExpertSchedule> schedules = List.of(
                new ExpertSchedule(expertA, MONDAY),
                new ExpertSchedule(expertA, TUESDAY),
                new ExpertSchedule(expertB, MONDAY),
                new ExpertSchedule(expertB, TUESDAY));

        List<ScheduleItem> items = List.of(
                scheduleItem(order(1, MONDAY), schedules.get(0), LocalTime.of(9, 0)),
                scheduleItem(order(2, TUESDAY), schedules.get(1), LocalTime.of(10, 0)),
                scheduleItem(order(3, TUESDAY), schedules.get(3), LocalTime.of(11, 0)));

        ExpertPlanningSolution original = wrapSolution(
                List.of(expertA, expertB), schedules, items);

        List<ExpertPlanningSolution> parts = split(original);

        Set<Long> originalIds = new HashSet<>();
        for (ScheduleItem item : original.getScheduleItemList()) {
            originalIds.add(item.getPlanningId());
        }

        Set<Long> seen = new HashSet<>();
        for (ExpertPlanningSolution part : parts) {
            for (ScheduleItem item : part.getScheduleItemList()) {
                assertTrue(seen.add(item.getPlanningId()), "duplicate schedule item id in partitions");
            }
        }
        assertEquals(originalIds, seen);
    }

    @Test
    void splitWorkingSolution_allExpertsInEveryPartition() {
        Expert expertA = expert(1);
        Expert expertB = expert(2);
        ExpertPlanningSolution original = wrapSolution(
                List.of(expertA, expertB),
                List.of(new ExpertSchedule(expertA, MONDAY), new ExpertSchedule(expertB, MONDAY)),
                List.of(scheduleItem(order(1, MONDAY), null, null)));

        List<ExpertPlanningSolution> parts = split(original);

        for (ExpertPlanningSolution part : parts) {
            assertEquals(original.getExpertList(), part.getExpertList());
        }
    }

    @Test
    void splitWorkingSolution_expertSchedulesOnlyUpToPartitionDueDate() {
        Expert expert = expert(1);
        ExpertSchedule mondaySchedule = new ExpertSchedule(expert, MONDAY);
        ExpertSchedule tuesdaySchedule = new ExpertSchedule(expert, TUESDAY);
        ExpertSchedule wednesdaySchedule = new ExpertSchedule(expert, WEDNESDAY);

        ExpertPlanningSolution original = wrapSolution(
                List.of(expert),
                List.of(mondaySchedule, tuesdaySchedule, wednesdaySchedule),
                List.of(
                        scheduleItem(order(1, MONDAY), null, null),
                        scheduleItem(order(2, TUESDAY), null, null)));

        List<ExpertPlanningSolution> parts = split(original);

        assertEquals(2, parts.size());
        assertEquals(1, parts.get(0).getExpertScheduleList().size());
        assertTrue(parts.get(0).getExpertScheduleList().stream().allMatch(s -> !s.getDate().isAfter(MONDAY)));

        assertEquals(1, parts.get(1).getExpertScheduleList().size());
        assertTrue(parts.get(1).getExpertScheduleList().stream().allMatch(s -> !s.getDate().isAfter(TUESDAY)));
    }

    @Test
    void splitWorkingSolution_sharesTimeSlotsAndConstraintConfiguration() {
        ExpertPlanningSolution original = wrapSolution(
                List.of(expert(1)),
                List.of(new ExpertSchedule(expert(1), MONDAY)),
                List.of(scheduleItem(order(1, MONDAY), null, null)));

        List<ExpertPlanningSolution> parts = split(original);

        for (ExpertPlanningSolution part : parts) {
            assertEquals(original.getTimeSlotList(), part.getTimeSlotList());
            assertEquals(original.getConstraintConfiguration(), part.getConstraintConfiguration());
        }
    }

    private static LocalDate dueDateOf(ExpertPlanningSolution part) {
        return part.getScheduleItemList().getFirst().getOrder().getDueDate();
    }

    private static List<ExpertPlanningSolution> split(ExpertPlanningSolution original) {
        return new ExpertSchedulePartitioner().split(original);
    }

    private static Expert expert(long id) {
        ExpertRef ref = new ExpertRef();
        ref.setId(id);
        Expert expert = new Expert();
        expert.setId(ref);
        return expert;
    }

    private static Order order(long id, LocalDate dueDate) {
        OrderRef orderRef = new OrderRef();
        orderRef.setId(id);
        Order order = new Order();
        order.setId(orderRef);
        order.setDueDate(dueDate);
        return order;
    }

    private static ScheduleItem scheduleItem(Order order, ExpertSchedule expertSchedule, LocalTime startTime) {
        ScheduleItem item = new ScheduleItem();
        item.setOrder(order);
        item.setExpertSchedule(expertSchedule);
        if (startTime != null) {
            item.setTimeSlot(new TimeSlot(startTime));
        }
        return item;
    }

    private static ExpertPlanningSolution wrapSolution(
            List<Expert> experts,
            List<ExpertSchedule> schedules,
            List<ScheduleItem> items) {
        List<TimeSlot> timeSlots = List.of(
                new TimeSlot(LocalTime.of(9, 0)),
                new TimeSlot(LocalTime.of(10, 0)),
                new TimeSlot(LocalTime.of(11, 0)));

        List<Order> orders = new ArrayList<>();
        for (ScheduleItem item : items) {
            if (!orders.contains(item.getOrder())) {
                orders.add(item.getOrder());
            }
        }

        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setConstraintConfiguration(new ExpertPlanningConstraintConfiguration());
        solution.setExpertList(experts);
        solution.setExpertScheduleList(schedules);
        solution.setOrderList(orders);
        solution.setTimeSlotList(timeSlots);
        solution.setScheduleItemList(items);
        return solution;
    }
}
