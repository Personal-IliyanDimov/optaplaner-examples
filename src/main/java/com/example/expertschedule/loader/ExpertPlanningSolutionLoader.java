package com.example.expertschedule.loader;

import com.example.expertschedule.io.model.*;
import com.example.expertschedule.planner.domain.time.Absence;
import com.example.expertschedule.planner.domain.time.Availability;
import com.example.expertschedule.planner.domain.Customer;
import com.example.expertschedule.planner.domain.Expert;
import com.example.expertschedule.planner.domain.Location;
import com.example.expertschedule.planner.domain.Order;
import com.example.expertschedule.planner.domain.Skill;
import com.example.expertschedule.planner.solution.ExpertPlanningSolution;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Loads planning data from a single JSON file (see {@link PlanningDatasetData}) and builds an {@link ExpertPlanningSolution}.
 */
public class ExpertPlanningSolutionLoader {

    private static final String DEFAULT_FILENAME = "dataset.json";

    private final ObjectMapper objectMapper;

    public ExpertPlanningSolutionLoader() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public ExpertPlanningSolutionLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Loads from a single dataset file (e.g. data/dataset.json).
     */
    public ExpertPlanningSolution load(Path datasetFile) throws IOException {
        PlanningDatasetData data = objectMapper.readValue(datasetFile.toFile(), PlanningDatasetData.class);
        return toDomain(
                data.getSkills() != null ? data.getSkills() : List.of(),
                data.getCustomers() != null ? data.getCustomers() : List.of(),
                data.getExperts() != null ? data.getExperts() : List.of(),
                data.getOrders() != null ? data.getOrders() : List.of()
        );
    }

    /**
     * Loads from the default file "dataset.json" inside the given directory.
     */
    public ExpertPlanningSolution loadFromDirectory(Path dataDir) throws IOException {
        return load(dataDir.resolve(DEFAULT_FILENAME));
    }

    private ExpertPlanningSolution toDomain(List<SkillData> skillData,
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

            if (ed.getAvailabilities() != null && !ed.getAvailabilities().isEmpty()) {
                List<Availability> availabilities = new ArrayList<>();
                for (AvailabilityData ad : ed.getAvailabilities()) {
                    Availability a = new Availability();
                    a.setExpert(expert);
                    a.setDayOfWeek(ad.getDayOfWeek());
                    a.setStartTime(ad.getStartTime());
                    a.setEndTime(ad.getEndTime());
                    availabilities.add(a);
                }
                expert.setAvailabilities(availabilities);
            }
            if (ed.getAbsences() != null && !ed.getAbsences().isEmpty()) {
                List<Absence> absences = new ArrayList<>();
                for (AbsenceData ad : ed.getAbsences()) {
                    Absence a = new Absence();
                    a.setExpert(expert);
                    a.setStartDate(ad.getStartDate());
                    a.setEndDate(ad.getEndDate());
                    a.setReason(ad.getReason());
                    absences.add(a);
                }
                expert.setAbsences(absences);
            }

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

        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setSkillList(new ArrayList<>(skillByName.values()));
        solution.setExpertList(experts);
        solution.setCustomerList(new ArrayList<>(customerByName.values()));
        solution.setOrderList(orders);
        return solution;
    }

    private Location toLocation(LocationData data) {
        if (data == null) {
            return null;
        }
        return new Location(data.getLatitude(), data.getLongitude());
    }
}
