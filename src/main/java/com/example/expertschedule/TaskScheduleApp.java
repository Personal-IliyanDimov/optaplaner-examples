package com.example.expertschedule;

import com.example.expertschedule.loader.ExpertPlanningSolutionLoader;
import com.example.expertschedule.planner.cp.TaskScheduleConstraintProvider;
import com.example.expertschedule.planner.domain.ExpertSchedule;
import com.example.expertschedule.planner.domain.Order;
import com.example.expertschedule.planner.domain.ScheduleItem;
import com.example.expertschedule.planner.solution.ExpertPlanningSolution;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import java.io.IOException;
import java.nio.file.Path;

public class TaskScheduleApp {

    public static void main(String[] args) throws IOException {
        Path dataDir = Path.of("data");
        ExpertPlanningSolutionLoader loader = new ExpertPlanningSolutionLoader();
        ExpertPlanningSolution problem = loader.loadFromDirectory(dataDir);

        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(ExpertPlanningSolution.class)
                .withEntityClasses(ExpertSchedule.class)
                .withConstraintProviderClass(TaskScheduleConstraintProvider.class)
                .withTerminationConfig(new TerminationConfig().withSecondsSpentLimit(15L));

        SolverFactory<ExpertPlanningSolution> solverFactory = SolverFactory.create(solverConfig);
        Solver<ExpertPlanningSolution> solver = solverFactory.buildSolver();

        ExpertPlanningSolution solution = solver.solve(problem);

        System.out.println("Best score: " + solution.getScore());
        for (ExpertSchedule schedule : solution.getExpertScheduleList()) {
            System.out.printf("Expert %d on %s: %d items%n",
                    schedule.getExpertRef().getId(),
                    schedule.getDate(),
                    schedule.getItems() != null ? schedule.getItems().size() : 0);
            if (schedule.getItems() != null) {
                for (ScheduleItem item : schedule.getItems()) {
                    Order order = item.getOrder();
                    if (order != null) {
                        System.out.printf("  seq %d -> order %d at (%.2f, %.2f)%n",
                                item.getSequence(),
                                order.getId().getId(),
                                order.getLocation() != null ? order.getLocation().getLatitude() : 0,
                                order.getLocation() != null ? order.getLocation().getLongitude() : 0);
                    }
                }
            }
        }
    }
}
