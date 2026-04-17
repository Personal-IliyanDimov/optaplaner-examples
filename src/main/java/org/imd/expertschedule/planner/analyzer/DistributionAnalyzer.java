package org.imd.expertschedule.planner.analyzer;

import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.Skill;
import org.imd.expertschedule.planner.domain.time.Availability;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.imd.expertschedule.planner.util.PlannerHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistributionAnalyzer {

    private final PlannerHelper plannerHelper = new PlannerHelper();

    /**
     * Extracts skill distribution info base on orders.
     *
     * @param solution
     * @return
     */
    public List<SkillDistribution> orderSkillDistributionBasedOnDueDate(final ExpertPlanningSolution solution) {
        final List<SkillDistribution> result = new ArrayList<>();

        // initialize
        final List<LocalDate> distinctDueDates = extractDistinctOrderDueDates(solution);

        // populate
        distinctDueDates.forEach(dueDate -> {
            result.add(new SkillDistribution(dueDate, new HashMap<>()));
        });

        for (final Order order : solution.getOrderList()) {
            for (SkillDistribution sd : result) {
                if (sd.getDueDate().equals(order.getDueDate())) {
                    final Map<String, Long> skillToMinutes = sd.getSkillToMinutes();

                    final long visitMinutes = order.getDiagnosisDuration().toMinutes();
                    for (Skill skill : order.getRequiredSkills()) {
                        skillToMinutes.merge(skill.getName(), visitMinutes, Long::sum);
                    }

                    break;
                }
            }
        }

        return result;
    }

    private static List<LocalDate> extractDistinctOrderDueDates(ExpertPlanningSolution solution) {
        final List<LocalDate> distinctDueDates = solution.getOrderList().stream().
                map(order -> order.getDueDate()).distinct().sorted(LocalDate::compareTo).toList();
        return distinctDueDates;
    }

    public List<SkillDistribution> expertSkillDistributionBasedOnDueDate(ExpertPlanningSolution solution) {
        final List<SkillDistribution> result = new ArrayList<>();

        // initialize
        final List<LocalDate> distinctDueDates = extractDistinctOrderDueDates(solution);

        // populate
        distinctDueDates.forEach(dueDate -> {
            result.add(new SkillDistribution(dueDate, new HashMap<>()));
        });

        for (final Expert expert : solution.getExpertList()) {
            for (Availability availability: expert.getAvailabilities()) {
                final Long intervalInMinutes = plannerHelper.calculateRealAvailability(availability, expert.getAbsences());

                for (SkillDistribution sd : result) {
                    if (sd.getDueDate().equals(sd.getDueDate())) {
                        final Map<String, Long> skillToMinutes = sd.getSkillToMinutes();

                        final long visitMinutes = order.getDiagnosisDuration().toMinutes();
                        for (Skill skill : order.getRequiredSkills()) {
                            skillToMinutes.merge(skill.getName(), visitMinutes, Long::sum);
                        }

                        break;
                    }
                }
            }
        }

        return result;
    }
}
