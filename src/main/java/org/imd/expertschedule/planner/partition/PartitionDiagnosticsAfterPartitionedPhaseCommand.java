package org.imd.expertschedule.planner.partition;

import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.api.solver.SolutionManager;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.impl.phase.custom.CustomPhaseCommand;

public class PartitionDiagnosticsAfterPartitionedPhaseCommand implements CustomPhaseCommand<ExpertPlanningSolution> {

    private static final String SOLVER_CONFIG_RESOURCE =
            "org/imd/expertschedule/expert-schedule-solver-config.partitioned.xml";

    private final ExpertSchedulePartitioner partitioner = new ExpertSchedulePartitioner();

    @Override
    public void changeWorkingSolution(ScoreDirector<ExpertPlanningSolution> scoreDirector) {
        SolutionManager<ExpertPlanningSolution, HardMediumSoftScore> solutionManager = solutionManager();
        PartitionDiagnostics.printPartitions(
                partitioner,
                scoreDirector.getWorkingSolution(),
                "After partitioned search phase (merged working solution, per-partition isolated score)",
                solutionManager::update);
    }

    private static SolutionManager<ExpertPlanningSolution, HardMediumSoftScore> solutionManager() {
        SolverConfig solverConfig = SolverConfig.createFromXmlResource(SOLVER_CONFIG_RESOURCE);
        return SolutionManager.create(SolverFactory.create(solverConfig));
    }
}
