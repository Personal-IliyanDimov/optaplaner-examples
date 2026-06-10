package org.imd.expertschedule.planner.partition;

import org.imd.expertschedule.planner.cp.ExpertPlanningConstraintConfiguration;
import org.imd.expertschedule.planner.domain.BackOffice;
import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.ExpertSchedule;
import org.imd.expertschedule.planner.domain.Location;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.domain.refs.BackOfficeRef;
import org.imd.expertschedule.planner.domain.refs.ExpertRef;
import org.imd.expertschedule.planner.domain.refs.OrderRef;
import org.imd.expertschedule.planner.domain.time.TimeSlot;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpertSchedulePartitionerTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 3, 2);
    private static final LocalDate TUESDAY = LocalDate.of(2026, 3, 3);

    private final ExpertSchedulePartitioner partitioner = new ExpertSchedulePartitioner();

    @Test
    void splitWorkingSolution_groupsItemsByDueDateAndClosestBackOffice() {
        BackOffice o1 = office(1, 0.0, 0.0);
        BackOffice o2 = office(2, 10.0, 10.0);
        Expert e1 = expert(1, o1);
        Expert e2 = expert(2, o2);

        ExpertSchedule e1Monday = new ExpertSchedule(e1, MONDAY);
        ExpertSchedule e2Monday = new ExpertSchedule(e2, MONDAY);
        ExpertSchedule e1Tuesday = new ExpertSchedule(e1, TUESDAY);

        ScheduleItem orderNearO1Monday = scheduleItem(order(1, MONDAY, 0.1, 0.1), e1Monday, LocalTime.of(9, 0));
        ScheduleItem orderNearO2Monday = scheduleItem(order(2, MONDAY, 9.9, 9.9), e2Monday, LocalTime.of(10, 0));
        ScheduleItem orderNearO1Tuesday = scheduleItem(order(3, TUESDAY, 0.2, 0.2), null, null);

        ExpertPlanningSolution original = wrapSolution(
                List.of(e1, e2),
                List.of(e1Monday, e2Monday, e1Tuesday),
                List.of(orderNearO1Monday, orderNearO2Monday, orderNearO1Tuesday));

        List<ExpertPlanningSolution> partitions = sortedPartitions(split(original));

        assertEquals(3, partitions.size());
        assertPartition(partitions.get(0), MONDAY, 1L, 1, List.of(1L));
        assertPartition(partitions.get(1), MONDAY, 2L, 1, List.of(2L));
        assertPartition(partitions.get(2), TUESDAY, 1L, 1, List.of(1L));
    }

    @Test
    void splitWorkingSolution_eachScheduleItemInExactlyOnePartition() {
        BackOffice o1 = office(1, 0.0, 0.0);
        BackOffice o2 = office(2, 10.0, 10.0);
        Expert e1 = expert(1, o1);
        Expert e2 = expert(2, o2);

        List<ExpertSchedule> schedules = List.of(
                new ExpertSchedule(e1, MONDAY),
                new ExpertSchedule(e1, TUESDAY),
                new ExpertSchedule(e2, MONDAY),
                new ExpertSchedule(e2, TUESDAY));

        List<ScheduleItem> items = List.of(
                scheduleItem(order(1, MONDAY, 0.1, 0.1), schedules.get(0), LocalTime.of(9, 0)),
                scheduleItem(order(2, TUESDAY, 0.2, 0.2), schedules.get(1), LocalTime.of(10, 0)),
                scheduleItem(order(3, TUESDAY, 9.8, 9.8), schedules.get(3), LocalTime.of(11, 0)));

        ExpertPlanningSolution original = wrapSolution(List.of(e1, e2), schedules, items);
        List<ExpertPlanningSolution> partitions = split(original);

        Set<Long> originalOrderIds = new HashSet<>();
        for (ScheduleItem item : original.getScheduleItemList()) {
            originalOrderIds.add(item.getPlanningId());
        }

        Set<Long> partitionedOrderIds = new HashSet<>();
        for (ExpertPlanningSolution partition : partitions) {
            for (ScheduleItem item : partition.getScheduleItemList()) {
                assertTrue(partitionedOrderIds.add(item.getPlanningId()), "duplicate schedule item id in partitions");
            }
        }
        assertEquals(originalOrderIds, partitionedOrderIds);
    }

    @Test
    void splitWorkingSolution_onlyExpertsFromPartitionOffice() {
        BackOffice o1 = office(1, 0.0, 0.0);
        BackOffice o2 = office(2, 10.0, 10.0);
        Expert e1 = expert(1, o1);
        Expert e2 = expert(2, o2);

        ExpertPlanningSolution original = wrapSolution(
                List.of(e1, e2),
                List.of(new ExpertSchedule(e1, MONDAY), new ExpertSchedule(e2, MONDAY)),
                List.of(scheduleItem(order(1, MONDAY, 0.1, 0.1), null, null)));

        List<ExpertPlanningSolution> partitions = split(original);

        assertEquals(1, partitions.size());
        assertEquals(List.of(1L), expertIds(partitions.getFirst()));
    }

    @Test
    void splitWorkingSolution_expertSchedules_includeMondayScheduleForO1() {
        BackOffice o1 = office(1, 0.0, 0.0);
        Expert e1 = expert(1, o1);
        ExpertSchedule e1Monday = new ExpertSchedule(e1, MONDAY);
        ExpertSchedule e1Tuesday = new ExpertSchedule(e1, TUESDAY);

        ExpertPlanningSolution original = wrapSolution(
                List.of(e1),
                List.of(e1Monday, e1Tuesday),
                List.of(scheduleItem(order(1, MONDAY, 0.1, 0.1), null, null)));

        ExpertPlanningSolution mondayO1Partition = split(original).getFirst();

        assertEquals(1, mondayO1Partition.getExpertScheduleList().size());
        assertEquals(MONDAY, mondayO1Partition.getExpertScheduleList().getFirst().getDate());
        assertEquals("E1", mondayO1Partition.getExpertScheduleList().getFirst().getExpert().getName());
        assertEquals("O1", mondayO1Partition.getExpertScheduleList().getFirst().getExpert().getBackOffice().getName());
    }

    @Test
    void splitWorkingSolution_expertSchedules_includeMondayScheduleForO2() {
        BackOffice o2 = office(2, 10.0, 10.0);
        Expert e2 = expert(2, o2);
        ExpertSchedule e2Monday = new ExpertSchedule(e2, MONDAY);

        ExpertPlanningSolution original = wrapSolution(
                List.of(e2),
                List.of(e2Monday),
                List.of(scheduleItem(order(1, MONDAY, 9.9, 9.9), null, null)));

        ExpertPlanningSolution mondayO2Partition = split(original).getFirst();

        assertEquals(1, mondayO2Partition.getExpertScheduleList().size());
        assertEquals(MONDAY, mondayO2Partition.getExpertScheduleList().getFirst().getDate());
        assertEquals("E2", mondayO2Partition.getExpertScheduleList().getFirst().getExpert().getName());
        assertEquals("O2", mondayO2Partition.getExpertScheduleList().getFirst().getExpert().getBackOffice().getName());
    }

    @Test
    void splitWorkingSolution_sharesTimeSlotsAndConstraintConfiguration() {
        BackOffice o1 = office(1, 0.0, 0.0);
        Expert e1 = expert(1, o1);

        ExpertPlanningSolution original = wrapSolution(
                List.of(e1),
                List.of(new ExpertSchedule(e1, MONDAY)),
                List.of(scheduleItem(order(1, MONDAY, 0.1, 0.1), null, null)));

        List<ExpertPlanningSolution> partitions = split(original);

        for (ExpertPlanningSolution partition : partitions) {
            assertEquals(original.getTimeSlotList(), partition.getTimeSlotList());
            assertEquals(original.getConstraintConfiguration(), partition.getConstraintConfiguration());
        }
    }

    private void assertPartition(
            ExpertPlanningSolution partition,
            LocalDate dueDate,
            long officeId,
            int itemCount,
            List<Long> expectedExpertIds) {
        assertEquals(dueDate, partition.getScheduleItemList().getFirst().getOrder().getDueDate());
        assertEquals(officeId, partition.getExpertList().getFirst().getBackOffice().getId().getId());
        assertEquals(itemCount, partition.getScheduleItemList().size());
        assertEquals(expectedExpertIds, expertIds(partition));
    }

    private List<ExpertPlanningSolution> sortedPartitions(List<ExpertPlanningSolution> partitions) {
        return partitions.stream()
                .sorted(Comparator
                        .comparing((ExpertPlanningSolution partition) ->
                                partition.getScheduleItemList().getFirst().getOrder().getDueDate())
                        .thenComparing(partition ->
                                partition.getExpertList().getFirst().getBackOffice().getId().getId()))
                .toList();
    }

    private List<Long> expertIds(ExpertPlanningSolution partition) {
        return partition.getExpertList().stream().map(expert -> expert.getId().getId()).toList();
    }

    private List<ExpertPlanningSolution> split(ExpertPlanningSolution original) {
        return partitioner.split(original);
    }

    private BackOffice office(long id, double lat, double lon) {
        BackOffice office = new BackOffice();
        office.setId(new BackOfficeRef(id));
        office.setName("O" + id);
        office.setLocation(new Location(lat, lon));
        return office;
    }

    private Expert expert(long id, BackOffice office) {
        ExpertRef expertRef = new ExpertRef();
        expertRef.setId(id);
        Expert expert = new Expert();
        expert.setId(expertRef);
        expert.setName("E" + id);
        expert.setBackOffice(office);
        return expert;
    }

    private Order order(long id, LocalDate dueDate, double lat, double lon) {
        OrderRef orderRef = new OrderRef();
        orderRef.setId(id);
        Order order = new Order();
        order.setId(orderRef);
        order.setDueDate(dueDate);
        order.setLocation(new Location(lat, lon));
        return order;
    }

    private ScheduleItem scheduleItem(Order order, ExpertSchedule expertSchedule, LocalTime startTime) {
        ScheduleItem item = new ScheduleItem();
        item.setOrder(order);
        item.setExpertSchedule(expertSchedule);
        if (startTime != null) {
            item.setTimeSlot(new TimeSlot(startTime));
        }
        return item;
    }

    private ExpertPlanningSolution wrapSolution(
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
