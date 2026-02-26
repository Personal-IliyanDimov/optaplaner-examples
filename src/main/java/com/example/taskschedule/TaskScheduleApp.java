package com.example.taskschedule;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import java.util.Arrays;
import java.util.List;

public class TaskScheduleApp {

    public static void main(String[] args) {

        List<Employee> employees = Arrays.asList(
                createEmployee("Alice"),
                createEmployee("Bob")
        );

        List<Task> tasks = Arrays.asList(
                createTask("Task A", 3),
                createTask("Task B", 2),
                createTask("Task C", 5)
        );


        TaskSchedule problem = new TaskSchedule();
        problem.setEmployeeList(employees);
        problem.setTaskList(tasks);

        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(TaskSchedule.class)
                .withEntityClasses(Task.class)
                .withConstraintProviderClass(TaskScheduleConstraintProvider.class)
                .withTerminationConfig(new TerminationConfig().withSecondsSpentLimit(5L));

        SolverFactory<TaskSchedule> solverFactory = SolverFactory.create(solverConfig);
        Solver<TaskSchedule> solver = solverFactory.buildSolver();

        TaskSchedule solution = solver.solve(problem);

        System.out.println("Best score: " + solution.getScore());
        for (Task task : solution.getTaskList()) {
            System.out.printf("  %s -> %s%n",
                              task.getName(),
                              task.getAssignedEmployee() == null ? "unassigned" : task.getAssignedEmployee().getName());
        }
    }

    private static Employee createEmployee(String name) {
        Employee employee = new Employee();
        employee.setName(name);

        return employee;
    }

    private static Task createTask(String name, int duration) {
        Task task = new Task();
        task.setName(name);
        task.setDurationInHours(duration);

        return task;
    }
}


