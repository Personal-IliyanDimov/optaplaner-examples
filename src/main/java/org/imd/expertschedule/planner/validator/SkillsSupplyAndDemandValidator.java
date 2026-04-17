package org.imd.expertschedule.planner.validator;


import org.imd.expertschedule.planner.analyzer.DistributionAnalyzer;
import org.imd.expertschedule.planner.analyzer.SkillDistribution;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;

import java.util.Collection;
import java.util.HashMap;
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

        if (demandedSkillDistributions.size() == 0) {
            return ;
        }

        final SkillDistribution runningDeltaSkillDistribution =
            new SkillDistribution(demandedSkillDistributions.getLast().getDueDate(), new HashMap<> ());

        for (final SkillDistribution suppliedSkillDistribution : suppliedSkillDistributions) {
            suppliedSkillDistribution.getSkillToMinutes().entrySet().forEach(entry -> {
                final String skillName = entry.getKey();
                final long demandMinutes = entry.getValue();
                final long deltaMinutes = runningDeltaSkillDistribution.getSkillToMinutes().getOrDefault(skillName, 0L) + demandMinutes;
                runningDeltaSkillDistribution.getSkillToMinutes().put(skillName, deltaMinutes);
            });

            for (final SkillDistribution demandedSkillDistribution : demandedSkillDistributions) {
                if (suppliedSkillDistribution.getDueDate().equals(demandedSkillDistribution.getDueDate()))  {
                    final Map<String, Long> demandedSkillToMinutes = demandedSkillDistribution.getSkillToMinutes();
                    final Map<String, Long> runningDeltaSkillToMinutes = runningDeltaSkillDistribution.getSkillToMinutes();

                    for (String skillName : demandedSkillToMinutes.keySet()) {
                        final long demandMinutes = demandedSkillToMinutes.get(skillName);
                        final long runningDeltaMinutes = runningDeltaSkillToMinutes.getOrDefault(skillName, 0L);

                        if (runningDeltaMinutes < demandMinutes) {
                            violations.add(new Violation("On " + demandedSkillDistribution.getDueDate() + ", skill " + skillName + " is under-supplied. " +
                                    "Demand: " + demandMinutes + " minutes, Running Delta: " + runningDeltaMinutes + " minutes."));
                        }

                        runningDeltaSkillDistribution.getSkillToMinutes().put(skillName, runningDeltaMinutes - demandMinutes);
                    }

                    break;
                }
            }
        }
    }
}
