package org.imd.expertschedule;

import org.imd.expertschedule.io.generator.GeneratorConfigPresets;
import org.imd.expertschedule.io.loader.ExpertPlanningSolutionLoader;
import org.imd.expertschedule.planner.cp.ExpertPlanningConstraintConfiguration;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.imd.expertschedule.planner.solution.PlannerParameters;
import org.imd.expertschedule.planner.solution.SolutionContext;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolutionPrinter;
import org.imd.expertschedule.planner.solution.SolutionInitializer;
import org.imd.expertschedule.planner.validator.PlanningSolutionValidator;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public class SchedulingApp {

    public static void main(String[] args) throws IOException {
        final Path dataDir = Path.of("data/expertschedule/");
        final ExpertPlanningSolutionLoader loader = new ExpertPlanningSolutionLoader();
        final SolutionContext context = loader.loadFromDirectory(dataDir, GeneratorConfigPresets.ultrasmall().getFileName());
        final PlannerParameters plannerParameters = new PlannerParameters();
        plannerParameters.getPlannerRelated().setCalendarWeek(10);
        plannerParameters.getPlannerRelated().setWorkingDays(new int[] {1, 2, 3, 4, 5});

        final SolutionInitializer solutionInitializer = new SolutionInitializer();
        final ExpertPlanningSolution unsolvedSolution = solutionInitializer.initialize(plannerParameters,
                new ExpertPlanningConstraintConfiguration(), context);

        final PlanningSolutionValidator validator = new PlanningSolutionValidator(plannerParameters);
        final Collection<PlanningSolutionValidator.Violation> validationResults = validator.validate(unsolvedSolution);
        if (! validationResults.isEmpty()) {
            validationResults.forEach(violation -> System.out.println("Validation violation: " + violation.getMessage()));
            return ;
        }

        final SolverConfig solverConfig = SolverConfig.createFromXmlResource(
                "org/imd/expertschedule/expert-schedule-solver-config.xml");
        solverConfig.withTerminationConfig(new TerminationConfig().withSecondsSpentLimit(15L));

        SolverFactory<ExpertPlanningSolution> solverFactory = SolverFactory.create(solverConfig);
        Solver<ExpertPlanningSolution> solver = solverFactory.buildSolver();

        ExpertPlanningSolution solution = solver.solve(unsolvedSolution);

        ExpertPlanningSolutionPrinter printer = new ExpertPlanningSolutionPrinter();
        printer.print(solution);
    }
}
