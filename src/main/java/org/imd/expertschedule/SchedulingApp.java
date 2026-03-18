package org.imd.expertschedule;

import org.imd.expertschedule.io.generator.GeneratorConfigPresets;
import org.imd.expertschedule.io.loader.ExpertPlanningSolutionLoader;
import org.imd.expertschedule.planner.cp.ExpertPlanningConstraintConfiguration;
import org.imd.expertschedule.planner.domain.ExpertSchedule;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.imd.expertschedule.planner.solution.PlannerParameters;
import org.imd.expertschedule.planner.solution.SolutionContext;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import java.io.IOException;
import java.nio.file.Path;

public class SchedulingApp {

    public static void main(String[] args) throws IOException {
        final Path dataDir = Path.of("data/expertschedule/");
        final ExpertPlanningSolutionLoader loader = new ExpertPlanningSolutionLoader();
        final SolutionContext context = loader.loadFromDirectory(dataDir, GeneratorConfigPresets.small().getFileName());
        final ExpertPlanningSolution unsolvedSolution = new ExpertPlanningSolution(new PlannerParameters(),
            new ExpertPlanningConstraintConfiguration(), context);

        final SolverConfig solverConfig = SolverConfig.createFromXmlResource(
                "org/imd/expertschedule/expert-schedule-solver-config.xml");
        solverConfig.withTerminationConfig(new TerminationConfig().withSecondsSpentLimit(15L));

        SolverFactory<ExpertPlanningSolution> solverFactory = SolverFactory.create(solverConfig);
        Solver<ExpertPlanningSolution> solver = solverFactory.buildSolver();

        ExpertPlanningSolution solution = solver.solve(unsolvedSolution);

        System.out.println("Best score: " + solution.getScore());
        for (ExpertSchedule schedule : solution.getExpertScheduleList()) {
            System.out.printf("Expert %d on %s: %d items%n",
                    schedule.getExpert().getId(),
                    schedule.getDate(),
                    solution.getScheduleItemList() != null ? solution.getScheduleItemList().size() : 0);
            if (solution.getScheduleItemList() != null) {
                for (ScheduleItem item : solution.getScheduleItemList()) {
                    Order order = item.getOrder();
                    if (order != null) {
                        System.out.println("OrderRef " + order.getId() +
                                " Slot: W" + (item.getExpertSchedule().getDate().getDayOfYear() / 7) +
                                " Day: " + item.getExpertSchedule().getDate().getDayOfWeek() +
                                " Time: " + item.getTimeSlot().getStartTime());
                    }
                }
            }
        }
    }
}
