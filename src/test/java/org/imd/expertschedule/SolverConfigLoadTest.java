package org.imd.expertschedule;

import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SolverConfigLoadTest {

    @Test
    void partitionedSolverConfig_buildsWithoutUnreachablePhaseError() {
        SolverConfig solverConfig = SolverConfig.createFromXmlResource(
                "org/imd/expertschedule/expert-schedule-solver-config.partitioned.xml");
        SolverFactory<ExpertPlanningSolution> solverFactory = SolverFactory.create(solverConfig);
        assertNotNull(solverFactory.buildSolver());
    }

    @Test
    void partitionedSolverConfig_solverTerminationHasNoBestScoreLimit() {
        SolverConfig solverConfig = SolverConfig.createFromXmlResource(
                "org/imd/expertschedule/expert-schedule-solver-config.partitioned.xml");
        TerminationConfig termination = solverConfig.getTerminationConfig();
        assertNotNull(termination);
        assertNull(termination.getBestScoreLimit(),
                "bestScoreLimit on solver termination is propagated to PART_THREAD and throws UnsupportedOperationException");
    }
}
