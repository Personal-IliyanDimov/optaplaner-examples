package org.imd.expertschedule.planner.validator;


import org.imd.expertschedule.planner.analyzer.DistributionAnalyzer;
import org.imd.expertschedule.planner.analyzer.SkillDistribution;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.imd.expertschedule.planner.solution.PlannerParameters;
import org.imd.expertschedule.planner.util.PlannerHelper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SkillsSupplyAndDemandValidator {

    private final DistributionAnalyzer distributionAnalyzer;

    public SkillsSupplyAndDemandValidator(DistributionAnalyzer distributionAnalyzer) {
        this.distributionAnalyzer = distributionAnalyzer;
    }

    public void validate(final ExpertPlanningSolution solution,
                         final Collection<Violation> violations) {

        final List<SkillDistribution> demandedSkillDistributions = distributionAnalyzer.orderSkillDistributionBasedOnDueDate(solution);
        final List<SkillDistribution> suppliedSkillDistributions = distributionAnalyzer.expertSkillDistributionBasedOnDueDate(solution);

        for  (final SkillDistribution demandedSkillDistribution : demandedSkillDistributions) {
            for   (final SkillDistribution suppliedSkillDistribution : suppliedSkillDistributions) {
                if (suppliedSkillDistribution.getDueDate().equals(demandedSkillDistribution.getDueDate()))  {
                    final Map<String, Long> demandedSkillToMinutes = demandedSkillDistribution.getSkillToMinutes();
                    final Map<String, Long> suppliedSkillToMinutes = suppliedSkillDistribution.getSkillToMinutes();

                    for (String skillName : demandedSkillToMinutes.keySet()) {
                        final long demandMinutes = demandedSkillToMinutes.get(skillName);
                        final long supplyMinutes = suppliedSkillToMinutes.getOrDefault(skillName, 0L);

                        if (supplyMinutes < demandMinutes) {
                            violations.add(new Violation("On " + demandedSkillDistribution.getDueDate() + ", skill " + skillName + " is under-supplied. " +
                                    "Demand: " + demandMinutes + " minutes, Supply: " + supplyMinutes + " minutes."));
                        }
                    }

                    break;
                }
            }
        }
    }
}
