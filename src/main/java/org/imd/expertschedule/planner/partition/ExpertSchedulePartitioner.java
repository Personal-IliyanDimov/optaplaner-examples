package org.imd.expertschedule.planner.partition;

import org.imd.expertschedule.planner.cp.ExpertPlanningConstraintConfiguration;
import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.ExpertSchedule;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.domain.time.TimeSlot;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.imd.expertschedule.planner.util.Pair;
import org.imd.expertschedule.planner.util.PlannerHelper;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.partitionedsearch.partitioner.SolutionPartitioner;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ExpertSchedulePartitioner implements SolutionPartitioner<ExpertPlanningSolution> {

    private final PlannerHelper helper = new PlannerHelper();

    @Override
    public List<ExpertPlanningSolution> splitWorkingSolution(
            ScoreDirector<ExpertPlanningSolution> scoreDirector, Integer runnablePartThreadLimit) {
        return split(scoreDirector.getWorkingSolution());
    }

    List<ExpertPlanningSolution> split(ExpertPlanningSolution originalSolution) {
        final List<ScheduleItem> originalScheduleItemList = originalSolution.getScheduleItemList();
        final List<TimeSlot> originalTimeSlotList = originalSolution.getTimeSlotList();
        final List<Expert> originalSolutionExpertList = originalSolution.getExpertList();
        final ExpertPlanningConstraintConfiguration originalConstraintConfiguration =
                originalSolution.getConstraintConfiguration();
        final List<ExpertSchedule> originalExpertScheduleList = originalSolution.getExpertScheduleList();

        List<Pair<LocalDate, List<ScheduleItem>>> itemsByDueDate = groupScheduleItemsByDueDate(originalScheduleItemList);
        if (itemsByDueDate.isEmpty()) {
            throw new IllegalStateException("Cannot partition solution with no schedule items.");
        }

        List<ExpertPlanningSolution> partitionList = new ArrayList<>(itemsByDueDate.size());
        Pair<LocalDate, List<ScheduleItem>> previousPair;
        Pair<LocalDate, List<ScheduleItem>> pair;

        for (int i = 0; i < itemsByDueDate.size(); i ++) {
            previousPair = (i == 0) ? null : itemsByDueDate.get(i - 1);
            pair = itemsByDueDate.get(i);
            LocalDate partitionStartDate = (previousPair == null) ? LocalDate.MIN : previousPair.getLeft().plusDays(1);
            LocalDate partitionDueDate = pair.getLeft();
            List<ScheduleItem> partitionItems = pair.getRight();

            final ExpertPlanningSolution partSolution = createPartition(
                originalConstraintConfiguration,
                originalTimeSlotList,
                originalSolutionExpertList,
                buildExpertSchedulesUpToDueDate(originalExpertScheduleList, partitionStartDate, partitionDueDate),
                copyScheduleItems(partitionItems));

            partitionList.add(partSolution);
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

    private  List<Pair<LocalDate, List<ScheduleItem>>> groupScheduleItemsByDueDate(List<ScheduleItem> scheduleItems) {
        final Map<LocalDate, List<ScheduleItem>> itemsByDueDate = new TreeMap<>();
        for (ScheduleItem item : scheduleItems) {
            final Order order = item.getOrder();
            itemsByDueDate.computeIfAbsent(order.getDueDate(), ignored -> new ArrayList<>()).add(item);
        }

        final  List<Pair<LocalDate, List<ScheduleItem>>> result = new ArrayList<>();
        itemsByDueDate.entrySet().stream().forEach(entry -> {
            result.add(new Pair<>(entry.getKey(), entry.getValue()));
        });

        Collections.sort(result, Comparator.comparing(Pair::getLeft));
        return result;
    }


    private List<ExpertSchedule> buildExpertSchedulesUpToDueDate(
            final List<ExpertSchedule> originalExpertScheduleList,
            final LocalDate partitionStartDate,
            final LocalDate partitionDueDate) {

        List<ExpertSchedule> partitionExpertSchedules = new ArrayList<>();
        for (ExpertSchedule originalSchedule : originalExpertScheduleList) {
            if (originalSchedule.getDate() == null) {
                throw new IllegalStateException("Original expert schedule has null date. ");
            }
            if ( helper.lessOrEqual(partitionStartDate, originalSchedule.getDate()) &&
                 helper.lessOrEqual(originalSchedule.getDate(), partitionDueDate) ) {
                partitionExpertSchedules.add(new ExpertSchedule(originalSchedule.getExpert(), originalSchedule.getDate()));
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
            partitionItem.setExpertSchedule(originalItem.getExpertSchedule() );
            partitionItem.setTimeSlot(originalItem.getTimeSlot());

            partitionItems.add(partitionItem);
        }
        return partitionItems;
    }
}
