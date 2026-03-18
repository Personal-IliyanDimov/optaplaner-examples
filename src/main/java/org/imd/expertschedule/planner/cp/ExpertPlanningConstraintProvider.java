package org.imd.expertschedule.planner.cp;

import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.domain.time.Availability;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.function.Predicate;

public class ExpertPlanningConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                matchExpertAvailabilityAndOrderAvailability(constraintFactory),
                matchExpertSkillsAndOrderSkillsHard(constraintFactory),
                matchExpertSkillsAndOrderSkillsSoft(constraintFactory),
                matchOrderDueDate(constraintFactory),
                fairlyDistributeScheduledItems(constraintFactory)
        };
    }

    private Constraint matchExpertAvailabilityAndOrderAvailability(ConstraintFactory factory) {
        // hard constraint - hard penalize per (availability difference in hours between expert and order)
        return factory.forEach(ScheduleItem.class)
                .filter(this.populatedScheduleItem())
                .map(si -> si.getExpertSchedule())
                .join(Expert.class, Joiners.equal(
                        si -> si.getExpertSchedule().getExpertRef(),
                        expert -> expert.getId()))
                .filter((si, expert) -> availabilityDifferenceHours(si, expert) > 0)
                .penalizeConfigurable((si, expert) -> availabilityDifferenceHours(si, expert))
                .asConstraint(ExpertPlanningConstraintConfiguration.WeightNames.EA_VS_OA_AVAILABILITY_CONFLICT);
    }

    private Predicate<ScheduleItem> populatedScheduleItem() {
        return si -> si.getExpertSchedule() != null && si.getTimeSlot() != null;
    }

    private Constraint matchExpertSkillsAndOrderSkillsHard(ConstraintFactory factory) {
        // hard constraint - if order requires skills expert doesn't have, hard penalize by number of missing skills
        return factory.forEach(ScheduleItem.class)
                .filter(si -> si.getOrderRef() != null && si.getExpertSchedule() != null)
                .join(Order.class, Joiners.equal(si -> si.getOrderRef().getId(), order -> order.getId().getId()))
                .join(Expert.class, Joiners.equal(
                        (ScheduleItem si, Order o) -> si.getExpertSchedule().getExpertRef().getId(),
                        (Expert e) -> e.getId().getId()))
                .filter((si, order, expert) -> missingRequiredSkillsCount(order, expert) > 0)
                .penalize(ExpertPlanningConstraintConfiguration.WeightNames.ES_VS_OS_SKILL_CONFLICT,
                        HardMediumSoftScore.ONE_HARD,
                        (si, order, expert) -> missingRequiredSkillsCount(order, expert));
    }

    private Constraint matchExpertSkillsAndOrderSkillsSoft(ConstraintFactory factory) {
        // soft penalize - if expert has more skills than order requires, soft penalize by number of "extra" (overqualified)
        return factory.forEach(ScheduleItem.class)
                .filter(si -> si.getOrderRef() != null && si.getExpertSchedule() != null)
                .join(Order.class, Joiners.equal(si -> si.getOrderRef().getId(), order -> order.getId().getId()))
                .join(Expert.class, Joiners.equal(
                        (ScheduleItem si, Order o) -> si.getExpertSchedule().getExpertRef().getId(),
                        (Expert e) -> e.getId().getId()))
                .filter((si, order, expert) -> expertHasAllRequired(order, expert) && extraSkillsCount(order, expert) > 0)
                .penalize("ExpertSkills Vs OrderSkills (overqualified)",
                        HardMediumSoftScore.ONE_SOFT,
                        (si, order, expert) -> extraSkillsCount(order, expert));
    }

    private Constraint matchOrderDueDate(ConstraintFactory factory) {
        // hard constraint - scheduled item date must be before (or on) order due date.
        // hard penalize - priority level * days delayed
        return factory.forEach(ScheduleItem.class)
                .filter(si -> si.getOrderRef() != null && si.getExpertSchedule() != null)
                .join(Order.class, Joiners.equal(si -> si.getOrderRef().getId(), order -> order.getId().getId()))
                .filter((si, order) -> si.getExpertSchedule().getDate().isAfter(order.getDueDate()))
                .penalize(ExpertPlanningConstraintConfiguration.WeightNames.O_DUE_DATE_CONFLICT,
                        HardMediumSoftScore.ONE_HARD,
                        (si, order) -> (int) (order.getPriority().getLevel() * java.time.temporal.ChronoUnit.DAYS.between(order.getDueDate(), si.getExpertSchedule().getDate())));
    }

    private Constraint fairlyDistributeScheduledItems(ConstraintFactory factory) {
        // medium constraint - penalize imbalance (more items on one expert schedule = higher medium penalty)
        return factory.forEach(ScheduleItem.class)
                .filter(si -> si.getExpertSchedule() != null)
                .groupBy(ScheduleItem::getExpertSchedule, ConstraintCollectors.count())
                .penalize(ExpertPlanningConstraintConfiguration.WeightNames.FD_SI_CONFLICT,
                        HardMediumSoftScore.ONE_MEDIUM,
                        (expertSchedule, count) -> count * count);
    }

    private static int availabilityDifferenceHours(final ScheduleItem si, final Expert expert) {
        if (expert.getAvailabilities() == null || expert.getAvailabilities().isEmpty()) {
            return 24;
        }
        int calendarWeek = si.getExpertSchedule().getDate().get(WeekFields.ISO.weekOfWeekBasedYear());
        java.time.DayOfWeek dayOfWeek = si.getExpertSchedule().getDate().getDayOfWeek();
        LocalTime scheduledTime = si.getTimeSlot().getStartTime();
        for (Availability a : expert.getAvailabilities()) {
            if (a.getCalendarWeek() == calendarWeek && a.getDayOfWeek() == dayOfWeek) {
                if (!scheduledTime.isBefore(a.getStartTime()) && !scheduledTime.isAfter(a.getEndTime())) {
                    return 0;
                }
                long minutesBefore = java.time.temporal.ChronoUnit.MINUTES.between(a.getStartTime(), scheduledTime);
                long minutesAfter = java.time.temporal.ChronoUnit.MINUTES.between(scheduledTime, a.getEndTime());
                if (minutesBefore > 0 && minutesAfter > 0) {
                    return (int) Math.min((minutesBefore + 59) / 60, (minutesAfter + 59) / 60);
                }
                if (minutesBefore > 0) return (int) (minutesBefore + 59) / 60;
                if (minutesAfter > 0) return (int) (minutesAfter + 59) / 60;
            }
        }
        return 24;
    }

    private static int missingRequiredSkillsCount(Order order, Expert expert) {
        if (order.getRequiredSkills() == null) return 0;
        if (expert.getSkills() == null) return order.getRequiredSkills().size();
        return (int) order.getRequiredSkills().stream()
                .filter(skill -> !expert.getSkills().contains(skill))
                .count();
    }

    private static boolean expertHasAllRequired(Order order, Expert expert) {
        if (order.getRequiredSkills() == null) return true;
        if (expert.getSkills() == null) return false;
        return expert.getSkills().containsAll(order.getRequiredSkills());
    }

    private static int extraSkillsCount(Order order, Expert expert) {
        if (!expertHasAllRequired(order, expert)) return 0;
        if (expert.getSkills() == null) return 0;
        if (order.getRequiredSkills() == null) return expert.getSkills().size();
        return expert.getSkills().size() - order.getRequiredSkills().size();
    }
}
