package org.imd.expertschedule.io.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.imd.expertschedule.io.model.AbsenceData;
import org.imd.expertschedule.io.model.AvailabilityData;
import org.imd.expertschedule.io.model.BackOfficeData;
import org.imd.expertschedule.io.model.CustomerData;
import org.imd.expertschedule.io.model.ExpertData;
import org.imd.expertschedule.io.model.LocationData;
import org.imd.expertschedule.io.model.OrderData;
import org.imd.expertschedule.io.model.PlanningDatasetData;
import org.imd.expertschedule.io.model.SkillData;
import org.imd.expertschedule.io.model.TimeSlotData;
import org.imd.expertschedule.planner.domain.BackOffice;
import org.imd.expertschedule.planner.domain.Customer;
import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.Location;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.OrderPriority;
import org.imd.expertschedule.planner.domain.Skill;
import org.imd.expertschedule.planner.domain.refs.BackOfficeRef;
import org.imd.expertschedule.planner.domain.refs.CustomerRef;
import org.imd.expertschedule.planner.domain.refs.ExpertRef;
import org.imd.expertschedule.planner.domain.refs.OrderRef;
import org.imd.expertschedule.planner.domain.time.Absence;
import org.imd.expertschedule.planner.domain.time.Availability;
import org.imd.expertschedule.planner.domain.time.TimeSlot;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.imd.expertschedule.planner.solution.SolutionContext;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Loads planning data from a single JSON file (see {@link PlanningDatasetData}) and builds an {@link ExpertPlanningSolution}.
 */
public class ExpertPlanningSolutionLoader {
    private final ObjectMapper objectMapper;

    public ExpertPlanningSolutionLoader() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public ExpertPlanningSolutionLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SolutionContext load(Path datasetFile) throws IOException {
        PlanningDatasetData data = objectMapper.readValue(datasetFile.toFile(), PlanningDatasetData.class);
        return buildContext(data);
    }

    public SolutionContext loadFromDirectory(final Path dataDir,
                                                    final String fileName) throws IOException {
        Objects.requireNonNull(dataDir);
        Objects.requireNonNull(fileName);

        return load(dataDir.resolve(fileName));
    }

    private SolutionContext buildContext(PlanningDatasetData data) {
        Objects.requireNonNull(data.getSkills());
        Objects.requireNonNull(data.getBackOffices());
        Objects.requireNonNull(data.getCustomers());
        Objects.requireNonNull(data.getExperts());
        Objects.requireNonNull(data.getOrders());

        final Map<String, Skill> skillByName = buildSkillMap(data.getSkills());
        final Map<Long, BackOffice> backOfficeById = buildBackOfficeMap(data.getBackOffices());
        final Map<Long, Customer> customerById = buildCustomerMap(data.getCustomers());
        final Map<Long, Expert> expertById = buildExpertMap(data.getExperts(), skillByName, backOfficeById);
        final Map<Long, Order> orderById = buildOrderMap(data.getOrders(), skillByName, customerById);

        final SolutionContext result = new SolutionContext();
        result.setSkillList(new ArrayList<>(skillByName.values()));
        result.setExpertList(new ArrayList<>(expertById.values()));
        result.setCustomerList(new ArrayList<>(customerById.values()));
        result.setBackOfficeList(new ArrayList<>(backOfficeById.values()));
        result.setOrderList(new ArrayList<>(orderById.values()));

        return result;
    }



    private Map<String, Skill> buildSkillMap(List<SkillData> skillData) {
        Map<String, Skill> skillByName = new HashMap<>();
        for (SkillData sd : skillData) {
            Skill skill = new Skill();
            skill.setName(sd.getName());
            skillByName.put(sd.getName(), skill);
        }
        return skillByName;
    }

    private Map<Long, BackOffice> buildBackOfficeMap(List<BackOfficeData> backOfficeData) {
        Map<Long, BackOffice> backOfficeById = new HashMap<>();
        for (BackOfficeData bd : backOfficeData) {
            BackOffice bo = new BackOffice();
            BackOfficeRef bRef = new BackOfficeRef();
            bRef.setId(bd.getId());
            bo.setId(bRef);
            bo.setName(bd.getName());
            bo.setLocation(toLocation(bd.getLocation()));
            backOfficeById.put(bd.getId(), bo);
        }
        return backOfficeById;
    }

    private Map<Long, Customer> buildCustomerMap(List<CustomerData> customerData) {
        Map<Long, Customer> customerById = new HashMap<>();
        for (CustomerData cd : customerData) {
            CustomerRef cRef = new CustomerRef();
            cRef.setId(cd.getId());

            Customer customer = new Customer();
            customer.setId(cRef);
            customer.setName(cd.getName());
            customerById.put(cd.getId(), customer);
        }
        return customerById;
    }

    private Map<Long, Expert> buildExpertMap(final List<ExpertData> expertData,
                                             final Map<String, Skill> skillByName,
                                             final Map<Long, BackOffice> backOfficeById) {
        Map<Long, Expert> expertById = new HashMap<>();
        for (ExpertData ed : expertData) {
            ExpertRef eRef = new ExpertRef();
            eRef.setId(ed.getId());

            BackOfficeRef boRef = new BackOfficeRef();
            boRef.setId(ed.getBackOfficeId());

            Expert expert = new Expert();
            expert.setId(eRef);
            expert.setName(ed.getName());
            expert.setBackOffice(Objects.requireNonNull(backOfficeById.get(boRef.getId())));
            expert.setSkills(resolveSkills(ed.getSkills(), skillByName));
            if (ed.getAvailabilities() != null && !ed.getAvailabilities().isEmpty()) {
                expert.setAvailabilities(toAvailabilities(ed.getAvailabilities()));
            }
            if (ed.getAbsences() != null && !ed.getAbsences().isEmpty()) {
                expert.setAbsences(toAbsences(ed.getAbsences()));
            }
            expertById.put(ed.getId(), expert);
        }
        return expertById;
    }

    private Set<Skill> resolveSkills(List<String> skillNames, Map<String, Skill> skillByName) {
        final Set<Skill> result = new HashSet<>();
        if (skillNames != null) {
            for (String skillName : skillNames) {
                Skill skill = skillByName.get(skillName);
                result.add(Objects.requireNonNull(skill));
            }
        }

        return result;
    }

    private List<Availability> toAvailabilities(List<AvailabilityData> list) {
        List<Availability> result = new ArrayList<>();
        for (AvailabilityData ad : list) {
            Availability a = new Availability();
            a.setCalendarWeek(ad.getCalendarWeek());
            a.setDayOfWeek(ad.getDayOfWeek());
            a.setStartTime(ad.getStartTime());
            a.setEndTime(ad.getEndTime());
            result.add(a);
        }
        return result;
    }

    private List<Absence> toAbsences(List<AbsenceData> list) {
        List<Absence> result = new ArrayList<>();
        for (AbsenceData ad : list) {
            Absence a = new Absence();
            a.setCalendarWeek(ad.getCalendarWeek());
            a.setDayOfWeek(ad.getDayOfWeek());
            a.setStartTime(ad.getStartTime());
            a.setEndTime(ad.getEndTime());
            a.setReason(ad.getReason());
            result.add(a);
        }
        return result;
    }

    private Map<Long, Order> buildOrderMap(final List<OrderData> orderData,
                                           final Map<String, Skill> skillByName,
                                           final Map<Long, Customer> customerById) {
        Map<Long, Order> orderById = new HashMap<>();
        for (OrderData od : orderData) {
            final OrderRef oRef = new OrderRef();
            oRef.setId(od.getId());

            final Order order = new Order();
            order.setId(oRef);
            order.setCustomer(customerById.get(od.getCustomerId()));
            order.setLocation(toLocation(od.getLocation()));
            order.setDueDate(od.getDueDate());
            order.setPriority(parsePriority(od.getPriority()));
            order.setDiagnosisDuration(parseDuration(od.getDiagnosisDuration()));
            order.setRequiredSkills(resolveSkills(od.getRequiredSkills(), skillByName));
            orderById.put(od.getId(), order);
        }
        return orderById;
    }

    private static OrderPriority parsePriority(String s) {
        if (s == null) return OrderPriority.MEDIUM;
        return switch (s.toUpperCase(java.util.Locale.ROOT)) {
            case "HIGH" -> OrderPriority.HIGH;
            case "LOW" -> OrderPriority.LOW;
            default -> OrderPriority.MEDIUM;
        };
    }

    private static Period parsePeriod(String s) {
        if (s == null || s.isBlank()) return Period.ZERO;
        try {
            return Period.parse(s);
        } catch (Exception e) {
            return Period.ZERO;
        }
    }

    private static Duration parseDuration(String s) {
        if (s == null || s.isBlank()) return Duration.ZERO;
        try {
            return Duration.parse(s);
        } catch (Exception e) {
            return Duration.ZERO;
        }
    }

    private static TimeSlot toTimeSlot(TimeSlotData data) {
        if (data == null) return null;
        final TimeSlot slot = new TimeSlot();
        slot.setStartTime(data.getStartTime());
        return slot;
    }



    private Location toLocation(LocationData data) {
        if (data == null) return null;
        return new Location(data.getLatitude(), data.getLongitude());
    }
}
