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
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.solver.SolutionManager;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PartitionDiagnosticsTest {

    @Test
    void printPartitions_listsDueDateAndItemCountPerPartition() {
        ExpertPlanningSolution solution = buildTwoDueDateSolution();
        SolutionManager<ExpertPlanningSolution, HardMediumSoftScore> solutionManager = solutionManager();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            PartitionDiagnostics.printPartitions(
                    new ExpertSchedulePartitioner(),
                    solution,
                    "Test",
                    solutionManager::update);
        } finally {
            System.setOut(originalOut);
        }

        String output = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("dueDate=2026-03-02"));
        assertTrue(output.contains("dueDate=2026-03-03"));
        assertTrue(output.contains("items=1"));
        assertTrue(output.contains("2 partitions"));
    }

    private static SolutionManager<ExpertPlanningSolution, HardMediumSoftScore> solutionManager() {
        SolverConfig solverConfig = SolverConfig.createFromXmlResource(
                "org/imd/expertschedule/expert-schedule-solver-config.unpartitioned.xml");
        return SolutionManager.create(SolverFactory.create(solverConfig));
    }

    private static ExpertPlanningSolution buildTwoDueDateSolution() {
        Expert expert = expert(1);
        ExpertSchedule mondaySchedule = new ExpertSchedule(expert, LocalDate.of(2026, 3, 2));
        ExpertSchedule tuesdaySchedule = new ExpertSchedule(expert, LocalDate.of(2026, 3, 3));

        ScheduleItem mondayItem = scheduleItem(order(1, LocalDate.of(2026, 3, 2)));
        ScheduleItem tuesdayItem = scheduleItem(order(2, LocalDate.of(2026, 3, 3)));

        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setConstraintConfiguration(new ExpertPlanningConstraintConfiguration());
        solution.setExpertList(List.of(expert));
        solution.setExpertScheduleList(List.of(mondaySchedule, tuesdaySchedule));
        solution.setOrderList(List.of(mondayItem.getOrder(), tuesdayItem.getOrder()));
        solution.setTimeSlotList(List.of(new TimeSlot(LocalTime.of(9, 0))));
        solution.setScheduleItemList(List.of(mondayItem, tuesdayItem));
        return solution;
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

    private static ScheduleItem scheduleItem(Order order) {
        ScheduleItem item = new ScheduleItem();
        item.setOrder(order);
        return item;
    }
}
