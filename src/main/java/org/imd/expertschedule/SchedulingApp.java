package org.imd.expertschedule;

import org.imd.expertschedule.io.PlanningSolutionAssembly;
import org.imd.expertschedule.io.generator.GeneratorConfigPresets;
import org.imd.expertschedule.io.loader.ExpertPlanningSolutionLoader;
import org.imd.expertschedule.planner.cp.ExpertPlanningConstraintConfiguration;
import org.imd.expertschedule.planner.domain.printers.OrderDistributionPrinter;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.imd.expertschedule.planner.solution.PlannerParameters;
import org.imd.expertschedule.planner.domain.printers.ExpertSchedulesPrinter;
import org.imd.expertschedule.planner.solution.SolutionContext;
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
        final var loaderContext = loader.loadBundleFromDirectory(dataDir, GeneratorConfigPresets.small().getFileName());

        final PlannerParameters plannerParameters = PlanningSolutionAssembly.plannerParametersFromMetadata(loaderContext.metadata());
        final ExpertPlanningConstraintConfiguration constraintConfiguration = new ExpertPlanningConstraintConfiguration();
        final SolutionContext context = loaderContext.context();

        final ExpertPlanningSolution unsolvedSolution = new SolutionInitializer().initialize(plannerParameters,
                constraintConfiguration, context);

        final PlanningSolutionValidator validator = new PlanningSolutionValidator(plannerParameters);
        final Collection<PlanningSolutionValidator.Violation> validationResults = validator.validate(unsolvedSolution);
        if (! validationResults.isEmpty()) {
            validationResults.forEach(violation -> System.out.println("Validation violation: " + violation.getMessage()));
            return ;
        }

        final SolverConfig solverConfig = SolverConfig.createFromXmlResource(
                "org/imd/expertschedule/expert-schedule-solver-config.xml");
        // solverConfig.withTerminationConfig(new TerminationConfig().withSecondsSpentLimit(1200L));

        final SolverFactory<ExpertPlanningSolution> solverFactory = SolverFactory.create(solverConfig);
        final Solver<ExpertPlanningSolution> solver = solverFactory.buildSolver();
        final ExpertPlanningSolution solution = solver.solve(unsolvedSolution);

        final OrderDistributionPrinter odPrinter = new OrderDistributionPrinter();
        odPrinter.print(solution);

        final ExpertSchedulesPrinter printer = new ExpertSchedulesPrinter();
        printer.print(solution);
    }
}
