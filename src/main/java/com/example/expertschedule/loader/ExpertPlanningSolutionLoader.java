package com.example.expertschedule.loader;

import com.example.expertschedule.io.model.*;
import com.example.expertschedule.planner.domain.*;
import com.example.expertschedule.planner.domain.refs.CustomerRef;
import com.example.expertschedule.planner.domain.refs.ExpertRef;
import com.example.expertschedule.planner.domain.refs.OrderRef;
import com.example.expertschedule.planner.domain.time.Absence;
import com.example.expertschedule.planner.domain.time.Availability;
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

    public ExpertPlanningSolution load(Path datasetFile) throws IOException {
        PlanningDatasetData data = objectMapper.readValue(datasetFile.toFile(), PlanningDatasetData.class);
        return toDomain(
                data.getSkills() != null ? data.getSkills() : List.of(),
                data.getCustomers() != null ? data.getCustomers() : List.of(),
                data.getExperts() != null ? data.getExperts() : List.of(),
                data.getOrders() != null ? data.getOrders() : List.of(),
                data.getExpertSchedules() != null ? data.getExpertSchedules() : List.of()
        );
    }

    public ExpertPlanningSolution loadFromDirectory(Path dataDir) throws IOException {
        return load(dataDir.resolve(DEFAULT_FILENAME));
    }

    private ExpertPlanningSolution toDomain(List<SkillData> skillData,
                                            List<CustomerData> customerData,
                                            List<ExpertData> expertData,
                                            List<OrderData> orderData,
                                            List<ExpertScheduleData> expertScheduleData) {
        Map<String, Skill> skillByName = new HashMap<>();
        for (SkillData sd : skillData) {
            Skill skill = new Skill();
            skill.setName(sd.getName());
            skillByName.put(sd.getName(), skill);
        }

        Map<Long, Customer> customerById = new HashMap<>();
        for (CustomerData cd : customerData) {
            Customer customer = new Customer();
            CustomerRef cRef = new CustomerRef();
            cRef.setId(cd.getId());
            customer.setId(cRef);
            customer.setName(cd.getName());
            customerById.put(cd.getId(), customer);
        }

        Map<Long, Expert> expertById = new HashMap<>();
        for (ExpertData ed : expertData) {
            Expert expert = new Expert();
            ExpertRef eRef = new ExpertRef();
            eRef.setId(ed.getId());
            expert.setId(eRef);
            expert.setName(ed.getName());
            expert.setBackOfficeLocation(toLocation(ed.getBackOfficeLocation()));

            Set<Skill> skills = new HashSet<>();
            if (ed.getSkills() != null) {
                for (String skillName : ed.getSkills()) {
                    Skill skill = skillByName.get(skillName);
                    if (skill != null) skills.add(skill);
                }
            }
            expert.setSkills(skills);

            if (ed.getAvailabilities() != null && !ed.getAvailabilities().isEmpty()) {
                List<Availability> availabilities = new ArrayList<>();
                for (AvailabilityData ad : ed.getAvailabilities()) {
                    Availability a = new Availability();
                    a.setCalendarWeek(ad.getCalendarWeek());
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
                    a.setCalendarWeek(ad.getCalendarWeek());
                    a.setDayOfWeek(ad.getDayOfWeek());
                    a.setStartTime(ad.getStartTime());
                    a.setEndTime(ad.getEndTime());
                    a.setReason(ad.getReason());
                    absences.add(a);
                }
                expert.setAbsences(absences);
            }
            expertById.put(ed.getId(), expert);
        }

        Map<Long, Order> orderById = new HashMap<>();
        for (OrderData od : orderData) {
            Order order = new Order();
            OrderRef oRef = new OrderRef();
            oRef.setId(od.getId());
            order.setId(oRef);
            CustomerRef cRef = new CustomerRef();
            cRef.setId(od.getCustomerId());
            order.setCustomerRef(cRef);
            order.setLocation(toLocation(od.getLocation()));

            Set<Skill> required = new HashSet<>();
            if (od.getRequiredSkills() != null) {
                for (String skillName : od.getRequiredSkills()) {
                    Skill skill = skillByName.get(skillName);
                    if (skill != null) required.add(skill);
                }
            }
            order.setRequiredSkills(required);
            orderById.put(od.getId(), order);
        }

        List<ExpertSchedule> expertSchedules = new ArrayList<>();
        for (ExpertScheduleData esd : expertScheduleData) {
            ExpertSchedule es = new ExpertSchedule();
            ExpertRef eRef = new ExpertRef();
            eRef.setId(esd.getExpertId());
            es.setExpertRef(eRef);
            es.setDate(esd.getDate());
            if (esd.getItems() != null) {
                List<ScheduleItem> items = new ArrayList<>();
                for (ScheduleItemData sid : esd.getItems()) {
                    ScheduleItem si = new ScheduleItem();
                    si.setOrder(orderById.get(sid.getOrderId()));
                    si.setSequence(sid.getSequence());
                    items.add(si);
                }
                items.sort(Comparator.comparingInt(ScheduleItem::getSequence));
                es.setItems(items);
            } else {
                es.setItems(new ArrayList<>());
            }
            expertSchedules.add(es);
        }

        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setSkillList(new ArrayList<>(skillByName.values()));
        solution.setExpertList(new ArrayList<>(expertById.values()));
        solution.setCustomerList(new ArrayList<>(customerById.values()));
        solution.setOrderList(new ArrayList<>(orderById.values()));
        solution.setExpertScheduleList(expertSchedules);
        return solution;
    }

    private Location toLocation(LocationData data) {
        if (data == null) return null;
        return new Location(data.getLatitude(), data.getLongitude());
    }
}
