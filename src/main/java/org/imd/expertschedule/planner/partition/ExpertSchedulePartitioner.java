package org.imd.expertschedule.planner.partition;

import org.imd.expertschedule.planner.cp.ExpertPlanningConstraintConfiguration;
import org.imd.expertschedule.planner.domain.BackOffice;
import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.ExpertSchedule;
import org.imd.expertschedule.planner.domain.Location;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.domain.refs.BackOfficeRef;
import org.imd.expertschedule.planner.domain.time.TimeSlot;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.imd.expertschedule.planner.util.DistanceCalculator;
import org.imd.expertschedule.planner.util.Pair;
import org.imd.expertschedule.planner.util.PlannerHelper;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.partitionedsearch.partitioner.SolutionPartitioner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ExpertSchedulePartitioner implements SolutionPartitioner<ExpertPlanningSolution> {
    private final PlannerHelper helper = new PlannerHelper();
    private final DistanceCalculator distanceCalculator = new DistanceCalculator();

    @Override
    public List<ExpertPlanningSolution> splitWorkingSolution(
            ScoreDirector<ExpertPlanningSolution> scoreDirector, Integer runnablePartThreadLimit) {
        return split(scoreDirector.getWorkingSolution());
    }

    private List<ExpertPlanningSolution> split(ExpertPlanningSolution originalSolution) {
        final List<ScheduleItem> originalScheduleItemList = originalSolution.getScheduleItemList();
        final List<TimeSlot> originalTimeSlotList = originalSolution.getTimeSlotList();
        final List<Expert> originalSolutionExpertList = originalSolution.getExpertList();
        final ExpertPlanningConstraintConfiguration originalConstraintConfiguration =
                originalSolution.getConstraintConfiguration();
        final List<ExpertSchedule> originalExpertScheduleList = originalSolution.getExpertScheduleList();

        List<BackOffice> backOffices = collectDistinctBackOffices(originalSolutionExpertList);
        if (backOffices.isEmpty()) {
            throw new IllegalStateException("Cannot partition solution with no back offices on experts.");
        }

        List<PartitionInfo> partitionGroups =
                toPartitionGroups(originalScheduleItemList, backOffices);
        if (partitionGroups.isEmpty()) {
            throw new IllegalStateException("Cannot partition solution with no schedule items.");
        }

        List<ExpertPlanningSolution> partitionList = new ArrayList<>(partitionGroups.size());
        PartitionInfo previousPartition;
        PartitionInfo partition;

        for (int i = 0; i < partitionGroups.size(); i ++) {
            previousPartition = (i == 0) ? null : partitionGroups.get(i - 1);

            partition = partitionGroups.get(i);
            final PartitionKey partitionKey = previousPartition.key();

            final LocalDate partitionStartDateNi = (previousPartition == null) ? LocalDate.MIN : partitionKey.dueDate();
            List<ScheduleItem> partitionItems = partition.data();

            final List<Expert> partitionExperts = filterExpertsForPartition(originalSolutionExpertList, partitionKey.backOfficeRef());

            final ExpertPlanningSolution epsParition = createPartition(
                    originalConstraintConfiguration,
                    originalTimeSlotList,
                    partitionExperts,
                    buildExpertSchedulesForPartition(originalExpertScheduleList, partitionStartDateNi, partitionKey),
                    copyScheduleItems(partitionItems));

            partitionList.add(epsParition);
        }

        return partitionList;
    }

    private ExpertPlanningSolution createPartition(
            ExpertPlanningConstraintConfiguration constraintConfiguration,
            List<TimeSlot> originalTimeSlotList,
            List<Expert> expertList,
            List<ExpertSchedule> expertScheduleList,
            List<ScheduleItem> scheduleItemList) {
        final ExpertPlanningSolution partition = new ExpertPlanningSolution();
        partition.setConstraintConfiguration(constraintConfiguration);
        partition.setTimeSlotList(originalTimeSlotList);
        partition.setExpertList(new ArrayList<>(expertList));
        partition.setExpertScheduleList(expertScheduleList);
        partition.setOrderList(collectOrders(scheduleItemList));
        partition.setScheduleItemList(scheduleItemList);
        partition.setScore(HardMediumSoftScore.ZERO);

        return partition;
    }

    private List<PartitionInfo> toPartitionGroups(
            final List<ScheduleItem> scheduleItems,
            final List<BackOffice> backOffices) {
        final Map<PartitionKey, List<ScheduleItem>> pgToItems = new HashMap<>();
        for (final ScheduleItem si : scheduleItems) {
            final Order order = si.getOrder();
            BackOfficeRef closestOfficeRef = closestBackOfficeId(order, backOffices);
            PartitionKey key = new PartitionKey(order.getDueDate(), closestOfficeRef);
            pgToItems.computeIfAbsent(key, ignored -> new ArrayList<>()).add(si);
        }

        List<PartitionInfo> result = pgToItems.
            entrySet().stream().
            map(entry ->
                new PartitionInfo(entry.getKey(), entry.getValue())
        ).toList();

        Collections.sort(result, Comparator.comparing(p -> p.key().dueDate));

        return result;
    }

    private BackOfficeRef closestBackOfficeId(final Order order, final List<BackOffice> backOffices) {
        final Location orderLocation = Objects.requireNonNull(order.getLocation(), "order location");

        BackOffice closest = null;
        double minDistance = Double.MAX_VALUE;

        for (final BackOffice office : backOffices) {
            double distance = distanceCalculator.calculateDistance(orderLocation, office.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                closest = office;
            }
        }

        return closest.getId();
    }

    private List<BackOffice> collectDistinctBackOffices(List<Expert> experts) {
        Map<BackOfficeRef, BackOffice> byId = new LinkedHashMap<>();
        for (Expert expert : experts) {
            BackOffice backOffice = expert.getBackOffice();
            if (backOffice != null && backOffice.getId() != null) {
                byId.putIfAbsent(backOffice.getId(), backOffice);
            }
        }
        return new ArrayList<>(byId.values());
    }

    private List<Expert> filterExpertsForPartition(List<Expert> experts, BackOfficeRef backOfficeRef) {
        return experts.stream()
                .filter(expert -> expert.getBackOffice().getId() == backOfficeRef)
                .toList();
    }

    private List<ExpertSchedule> buildExpertSchedulesForPartition(
            final List<ExpertSchedule> originalExpertScheduleList,
            final LocalDate partitionStartDateNi,
            final PartitionKey key) {
        final LocalDate partitionDueDate = key.dueDate();
        final BackOfficeRef backOfficeRef = key.backOfficeRef;

        final List<ExpertSchedule> partitionExpertSchedules = new ArrayList<>();
        for (ExpertSchedule originalSchedule : originalExpertScheduleList) {
            Expert expert = originalSchedule.getExpert();
                if ((helper.less(partitionStartDateNi, originalSchedule.getDate()) &&
                     helper.lessOrEqual(originalSchedule.getDate(), partitionDueDate)) &&
                     expert.getBackOffice().getId() == backOfficeRef) {
                partitionExpertSchedules.add(new ExpertSchedule(expert, originalSchedule.getDate()));
            }
        }
        return partitionExpertSchedules;
    }

    private List<Order> collectOrders(List<ScheduleItem> scheduleItems) {
        Set<Order> orders = new LinkedHashSet<>();
        for (ScheduleItem item : scheduleItems) {
            orders.add(item.getOrder());
        }

        return new ArrayList<>(orders);
    }

    private List<ScheduleItem> copyScheduleItems(final List<ScheduleItem> originalItems) {
        List<ScheduleItem> partitionItems = new ArrayList<>(originalItems.size());
        for (ScheduleItem originalItem : originalItems) {
            final ScheduleItem partitionItem = new ScheduleItem();
            partitionItem.setOrder(originalItem.getOrder());
            partitionItem.setExpertSchedule(originalItem.getExpertSchedule());
            partitionItem.setTimeSlot(originalItem.getTimeSlot());

            partitionItems.add(partitionItem);
        }
        return partitionItems;
    }

    private record PartitionInfo(PartitionKey key, List<ScheduleItem> data) {
    }

    private record PartitionKey(LocalDate dueDate, BackOfficeRef backOfficeRef) implements Comparable<PartitionKey> {

        @Override
        public int compareTo(PartitionKey other) {
            int byDate = dueDate.compareTo(other.dueDate);
            if (byDate != 0) {
                return byDate;
            }

            return backOfficeRef.compareTo(other.backOfficeRef);
        }
    }
}
