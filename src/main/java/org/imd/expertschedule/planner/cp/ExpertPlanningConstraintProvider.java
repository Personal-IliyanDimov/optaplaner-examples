package org.imd.expertschedule.planner.cp;

import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.domain.Skill;
import org.imd.expertschedule.planner.domain.refs.ExpertRef;
import org.imd.expertschedule.planner.domain.time.Availability;
import org.imd.expertschedule.planner.util.DayInterval;
import org.imd.expertschedule.planner.util.Pair;
import org.imd.expertschedule.planner.util.PlannerHelper;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;

public class ExpertPlanningConstraintProvider implements ConstraintProvider {
    private static final int FIXED_PENALTY = 1;

    private final PlannerHelper helper = new PlannerHelper();

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                matchExpertAvailability(constraintFactory),
                matchNoOverlapsExpertOtherMeetings(constraintFactory),
                matchOrderAvailability(constraintFactory),
                matchExpertSkillsAndOrderSkills(constraintFactory),
                matchOrderDueDate(constraintFactory),
                fairlyDistributePerExpertPerPeriodScheduledItems(constraintFactory),
                fairlyDistributePerExpertPerDayScheduledItems(constraintFactory)
        };
    }

    private Constraint matchExpertAvailability(ConstraintFactory factory) {
        // hard constraint - hard penalize per (availability difference in hours between expert and time)
        return factory.forEach(ScheduleItem.class)
                .filter(this.populatedScheduleItem())
                .filter(si -> !this.expertIsAvailable(si))
                .penalizeConfigurable(si -> FIXED_PENALTY)
                .asConstraint(ExpertPlanningConstraintConfiguration.WeightNames.EA_AVAILABILITY_CONFLICT);
    }

    private Predicate<ScheduleItem> populatedScheduleItem() {
        return si -> si.getExpertSchedule() != null && si.getTimeSlot() != null;
    }

    private boolean expertIsAvailable(final ScheduleItem si) {
        final List<Availability> customerAvailabilities = si.getOrder().getCustomerAvailabilities();
        final DayInterval meetingInterval = extractDayInterval(si);
        return helper.expertIsAvailable(meetingInterval, si.getExpertSchedule().getExpert());
    }

    private static DayInterval extractDayInterval(ScheduleItem si) {
        final LocalDate meetingDate = si.getExpertSchedule().getDate();
        final LocalTime meetingStartTime = si.getTimeSlot().getStartTime();
        final Duration meetingDuration = si.getOrder().getDiagnosisDuration();
        final DayInterval meetingInterval = new DayInterval(meetingDate, meetingStartTime,
            meetingStartTime.plus(meetingDuration).minusMinutes(1));
        return meetingInterval;
    }

    private Constraint matchNoOverlapsExpertOtherMeetings(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(ScheduleItem.class)
                .filter(this.populatedScheduleItem())
                .groupBy(si -> si.getExpertSchedule().getExpert().getId(), OverlapDetector.buildCollector(
                 si -> si.getExpertSchedule().getExpert().getId(), si -> extractDayInterval(si)))
                .penalizeConfigurable(buildMatchWeigher())
                .asConstraint(ExpertPlanningConstraintConfiguration.WeightNames.OVERLAPS_WITH_OTHER_MEETING_CONFLICT);
    }

    private ToIntBiFunction<ExpertRef, OverlapDetector.OverlapData> buildMatchWeigher() {
        return (er, od) -> {
            final List<DayInterval> expertDayIntervals = od.extractExpertDayIntervals(er);
            final Map<LocalDate, List<DayInterval>> dateToDayIntervalsMap = expertDayIntervals.stream()
                    .collect(Collectors.groupingBy(DayInterval::getDate));

            return helper.countIntervalIntersects(dateToDayIntervalsMap);
        };
    }


    private Constraint matchOrderAvailability(ConstraintFactory factory) {
        // hard constraint - hard penalize per (availability difference in hours between expert and time)
        return factory.forEach(ScheduleItem.class)
                .filter(this.populatedScheduleItem())
                .filter(si -> !this.orderIsServable(si))
                .penalizeConfigurable(si -> FIXED_PENALTY)
                .asConstraint(ExpertPlanningConstraintConfiguration.WeightNames.OA_AVAILABILITY_CONFLICT);
    }

    private boolean orderIsServable(ScheduleItem si) {
        final List<Availability> customerAvailabilities = si.getOrder().getCustomerAvailabilities();
        final DayInterval meetingInterval = extractDayInterval(si);
        return helper.orderIsServable(meetingInterval, customerAvailabilities);
    }

    private Constraint matchExpertSkillsAndOrderSkills(ConstraintFactory factory) {
        // hard constraint - hard penalize lack of skills between order and expert
        return factory.forEach(ScheduleItem.class)
                .filter(this.populatedScheduleItem())
                .map(si -> new Pair<ScheduleItem, Integer>
                        (si, skillDifference(si.getExpertSchedule().getExpert(), si.getOrder())))
                .filter(pair -> pair.getRight() > 0)
                .penalizeConfigurable(pair -> FIXED_PENALTY)
                .asConstraint(ExpertPlanningConstraintConfiguration.WeightNames.ES_VS_OS_SKILL_CONFLICT);
    }

    private int skillDifference(final Expert expert, final Order order) {
        final Set<Skill> expertSkills = expert.getSkills();
        final Set<Skill> orderSkills = order.getRequiredSkills();

        final Set<Skill> missingSkills = new HashSet<>(orderSkills);
        missingSkills.removeAll(expertSkills);
        return missingSkills.size();
    }

    private Constraint matchOrderDueDate(ConstraintFactory factory) {
        // hard constraint - scheduled item date must be before (or on) order due date.
        // hard penalize - priority level * days delayed
        return factory.forEach(ScheduleItem.class)
                .filter(this.populatedScheduleItem())
                .map(si -> new Pair<ScheduleItem, Integer>(si, calculateDelayInDays(si)))
                .filter(pair -> pair.getRight() > 0)
                .penalizeConfigurable(pair ->
                     (pair.getLeft().getOrder().getPriority().getLevel()) * FIXED_PENALTY)
                .asConstraint(ExpertPlanningConstraintConfiguration.WeightNames.O_DUE_DATE_CONFLICT);
    }

    private int calculateDelayInDays(final ScheduleItem si) {
        final LocalDate scheduledDate = si.getExpertSchedule().getDate();
        final LocalDate orderDueDate = si.getOrder().getDueDate();

        return helper.calculateDaysDifference(scheduledDate, orderDueDate);
    }

    private Constraint fairlyDistributePerExpertPerPeriodScheduledItems(ConstraintFactory factory) {
        // medium constraint - penalize imbalance of total diagnosis minutes per expert (whole horizon)
        return factory
                .forEach(ScheduleItem.class)
                .filter(this.populatedScheduleItem())
                .groupBy(FairnessDetector.loadBalance(si -> si.getExpertSchedule().getExpert(),
                                               si -> BigInteger.valueOf(si.getOrder().getDiagnosisDuration().toMinutes())))
                .penalizeConfigurable(result -> fairnessPenaltyMinutes(result))
                .asConstraint(ExpertPlanningConstraintConfiguration.WeightNames.FD_PE_PP_SI_CONFLICT);
    }

    private Constraint fairlyDistributePerExpertPerDayScheduledItems(ConstraintFactory factory) {
        return factory
                .forEach(ScheduleItem.class)
                .filter(this.populatedScheduleItem())
                .groupBy(si -> si.getExpertSchedule().getDate(),
                        FairnessDetector.loadBalance(si -> si.getExpertSchedule().getExpert(),
                                si -> BigInteger.valueOf(si.getOrder().getDiagnosisDuration().toMinutes())))
                .penalizeConfigurable((ignoredDate, result) -> fairnessPenaltyMinutes(result))
                .asConstraint(ExpertPlanningConstraintConfiguration.WeightNames.FD_PE_PD_SI_CONFLICT);
    }

    private static int fairnessPenaltyMinutes(FairnessDetector.LoadBalanceData result) {
        return result.getZeroDeviationSquaredSumRoot()
                .min(BigInteger.valueOf(Integer.MAX_VALUE))
                .intValue();
    }


    private static int missingRequiredSkillsCount(final Order order, final Expert expert) {
        if (order.getRequiredSkills() == null) return 0;
        if (expert.getSkills() == null) return order.getRequiredSkills().size();
        return (int) order.getRequiredSkills().stream()
                .filter(skill -> !expert.getSkills().contains(skill))
                .count();
    }

    private static boolean expertHasAllRequired(final Order order, final Expert expert) {
        if (order.getRequiredSkills() == null) return true;
        if (expert.getSkills() == null) return false;
        return expert.getSkills().containsAll(order.getRequiredSkills());
    }
}
