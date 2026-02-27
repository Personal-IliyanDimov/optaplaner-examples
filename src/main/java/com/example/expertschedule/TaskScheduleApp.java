package com.example.expertschedule;

import com.example.expertschedule.planner.cp.TaskScheduleConstraintProvider;
import com.example.expertschedule.planner.domain.Customer;
import com.example.expertschedule.planner.domain.Expert;
import com.example.expertschedule.planner.domain.Location;
import com.example.expertschedule.planner.domain.Skill;
import com.example.expertschedule.planner.domain.Order;
import com.example.expertschedule.planner.solution.ExpertPlanningSolution;
import com.example.expertschedule.io.model.CustomerData;
import com.example.expertschedule.io.model.ExpertData;
import com.example.expertschedule.io.model.LocationData;
import com.example.expertschedule.io.model.OrderData;
import com.example.expertschedule.io.model.SkillData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class TaskScheduleApp {

    public static void main(String[] args) throws IOException {

        Path dataDir = Path.of("data");
        ObjectMapper mapper = new ObjectMapper();

        List<SkillData> skillData = readList(mapper, dataDir.resolve("skills.json").toFile(), new TypeReference<>() {});
        List<CustomerData> customerData = readList(mapper, dataDir.resolve("customers.json").toFile(), new TypeReference<>() {});
        List<ExpertData> expertData = readList(mapper, dataDir.resolve("experts.json").toFile(), new TypeReference<>() {});
        List<OrderData> orderData = readList(mapper, dataDir.resolve("orders.json").toFile(), new TypeReference<>() {});

        ExpertPlanningSolution problem = toDomain(skillData, customerData, expertData, orderData);

        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(ExpertPlanningSolution.class)
                .withEntityClasses(Order.class)
                .withConstraintProviderClass(TaskScheduleConstraintProvider.class)
                .withTerminationConfig(new TerminationConfig().withSecondsSpentLimit(5L));

        SolverFactory<ExpertPlanningSolution> solverFactory = SolverFactory.create(solverConfig);
        Solver<ExpertPlanningSolution> solver = solverFactory.buildSolver();

        ExpertPlanningSolution solution = solver.solve(problem);

        System.out.println("Best score: " + solution.getScore());
        for (Order order : solution.getOrderList()) {
            System.out.printf("  %s -> %s at %s%n",
                    order.getCode(),
                    order.getAssignedExpert() == null ? "unassigned" : order.getAssignedExpert().getName(),
                    order.getCustomer() == null ? "n/a" : order.getCustomer().getName());
        }
    }

    private static ExpertPlanningSolution toDomain(List<SkillData> skillData,
                                                   List<CustomerData> customerData,
                                                   List<ExpertData> expertData,
                                                   List<OrderData> orderData) {

        Map<String, Skill> skillByName = new HashMap<>();
        for (SkillData sd : skillData) {
            Skill skill = new Skill();
            skill.setName(sd.getName());
            skillByName.put(sd.getName(), skill);
        }

        Map<String, Customer> customerByName = new HashMap<>();
        for (CustomerData cd : customerData) {
            Customer customer = new Customer();
            customer.setName(cd.getName());
            customer.setLocation(toLocation(cd.getLocation()));
            customerByName.put(cd.getName(), customer);
        }

        List<Expert> experts = new ArrayList<>();
        for (ExpertData ed : expertData) {
            Expert expert = new Expert();
            expert.setName(ed.getName());
            expert.setBackOfficeLocation(toLocation(ed.getBackOfficeLocation()));

            Set<Skill> skills = new HashSet<>();
            if (ed.getSkills() != null) {
                for (String skillName : ed.getSkills()) {
                    Skill skill = skillByName.get(skillName);
                    if (skill != null) {
                        skills.add(skill);
                    }
                }
            }
            expert.setSkills(skills);
            experts.add(expert);
        }

        List<Order> orders = new ArrayList<>();
        for (OrderData od : orderData) {
            Order order = new Order();
            order.setCode(od.getCode());
            order.setCustomer(customerByName.get(od.getCustomer()));

            Set<Skill> required = new HashSet<>();
            if (od.getRequiredSkills() != null) {
                for (String skillName : od.getRequiredSkills()) {
                    Skill skill = skillByName.get(skillName);
                    if (skill != null) {
                        required.add(skill);
                    }
                }
            }
            order.setRequiredSkills(required);
            orders.add(order);
        }

        ExpertPlanningSolution schedule = new ExpertPlanningSolution();
        schedule.setSkillList(new ArrayList<>(skillByName.values()));
        schedule.setExpertList(experts);
        schedule.setCustomerList(new ArrayList<>(customerByName.values()));
        schedule.setOrderList(orders);
        return schedule;
    }

    private static Location toLocation(LocationData data) {
        if (data == null) {
            return null;
        }
        return new Location(data.getLatitude(), data.getLongitude());
    }

    private static <T> List<T> readList(ObjectMapper mapper, File file, TypeReference<List<T>> type) throws IOException {
        return mapper.readValue(file, type);
    }
}


