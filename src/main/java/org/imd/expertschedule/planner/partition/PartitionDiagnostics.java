package org.imd.expertschedule.planner.partition;

import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public final class PartitionDiagnostics {

    private PartitionDiagnostics() {
    }

    public static void printPartitions(
            ExpertSchedulePartitioner partitioner,
            ExpertPlanningSolution solution,
            String heading,
            Function<ExpertPlanningSolution, HardMediumSoftScore> scoreFunction) {
        List<ExpertPlanningSolution> partitions = partitioner.split(solution);
        System.out.println();
        System.out.println("=== " + heading + " (" + partitions.size() + " partitions) ===");
        System.out.printf(
                "  merged solution: uninitialized schedule items=%d / %d%n",
                countUninitialized(solution), solution.getScheduleItemList().size());
        for (int index = 0; index < partitions.size(); index++) {
            ExpertPlanningSolution partition = partitions.get(index);
            LocalDate dueDate = partitionDueDate(partition);
            int itemCount = partition.getScheduleItemList().size();
            int expertScheduleCount = partition.getExpertScheduleList().size();
            HardMediumSoftScore score = scoreFunction.apply(partition);
            System.out.printf(
                    "  [%d] dueDate=%s, items=%d, expertSchedules=%d, score=%s%n",
                    index, dueDate, itemCount, expertScheduleCount, score);
        }
        System.out.println();
    }

    public static int countUninitialized(ExpertPlanningSolution solution) {
        int count = 0;
        for (ScheduleItem item : solution.getScheduleItemList()) {
            if (item.getExpertSchedule() == null || item.getTimeSlot() == null) {
                count++;
            }
        }
        return count;
    }

    private static LocalDate partitionDueDate(ExpertPlanningSolution partition) {
        List<ScheduleItem> items = partition.getScheduleItemList();
        if (items.isEmpty()) {
            return null;
        }
        Order order = items.getFirst().getOrder();
        return order == null ? null : order.getDueDate();
    }
}
