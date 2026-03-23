package org.imd.expertschedule.planner.cp;

import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.domain.time.Absence;
import org.imd.expertschedule.planner.domain.time.Availability;
import org.imd.expertschedule.planner.util.Pair;
import org.imd.expertschedule.planner.util.DayInterval;
import org.imd.expertschedule.planner.util.PlannerHelper;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Predicate;

public class ExpertPlanningConstraintProvider implements ConstraintProvider {

    private final PlannerHelper helper = new PlannerHelper();

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            matchExpertAvailability(constraintFactory),
            matchOrderAvailability(constraintFactory),
            matchExpertSkillsAndOrderSkillsHard(constraintFactory),
            matchExpertSkillsAndOrderSkillsSoft(constraintFactory),
            matchOrderDueDate(constraintFactory),
            fairlyDistributeScheduledItems(constraintFactory)
        };
    }

    private Constraint matchExpertAvailability(ConstraintFactory factory) {
        // hard constraint - hard penalize per (availability difference in hours between expert and time)
        return factory.forEach(ScheduleItem.class)
            .filter(this.populatedScheduleItem())
            .filter(si -> ! this.expertIsAvailable(si))
            .penalizeConfigurable(si -> -1)
            .asConstraint(ExpertPlanningConstraintConfiguration.WeightNames.EA_AVAILABILITY_CONFLICT);
    }

    private Predicate<ScheduleItem> populatedScheduleItem() {
        return si -> si.getExpertSchedule() != null && si.getTimeSlot() != null;
    }

    private boolean expertIsAvailable(final ScheduleItem si) {
        final List<Availability> customerAvailabilities = si.getOrder().getCustomerAvailabilities();
        final LocalDate meetingDate = si.getExpertSchedule().getDate();
        final LocalTime meetingStartTime = si.getTimeSlot().getStartTime();
        final Duration meetingDuration = si.getOrder().getDiagnosisDuration();
        final DayInterval meetingInterval = new DayInterval(meetingDate, meetingStartTime, meetingStartTime.plus(meetingDuration));
        return helper.expertIsAvailable(meetingInterval, si.getExpertSchedule().getExpert());
    }

    private static DayInterval findExpertAvailabilityRange(final Expert expert, final LocalDate meetingDate) {
        final List<Availability> expertAvailabilities = expert.getAvailabilities();
        final List<Absence> expertAbsences = expert.getAbsences();

        for (final Availability expertAvailability : expertAvailabilities) {
            final LocalDate expertAvailabilityDate  = calculateDate(expertAvailability.getCalendarWeek(),
                    expertAvailability.getDayOfWeek().getValue());
            if (meetingDate.equals(expertAvailabilityDate)) {

            }
        }
    }

    private static LocalDate calculateDate(int calendarWeek, int wd) {
        return LocalDate.of(LocalDate.now().getYear(), 1, 1)
                .plusWeeks(calendarWeek - 1)
                .plusDays(wd - 1);
    }


    private Constraint matchExpertSkillsAndOrderSkillsHard(ConstraintFactory factory) {
;
    }

    private Constraint matchExpertSkillsAndOrderSkillsSoft(ConstraintFactory factory) {

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
        return null;
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

    private static int extraSkillsCount(final Order order, final Expert expert) {
        if (!expertHasAllRequired(order, expert)) return 0;
        if (expert.getSkills() == null) return 0;
        if (order.getRequiredSkills() == null) return expert.getSkills().size();
        return expert.getSkills().size() - order.getRequiredSkills().size();
    }
}
