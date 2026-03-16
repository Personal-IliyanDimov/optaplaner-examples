package org.imd.expertschedule.io.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.imd.expertschedule.io.generator.GeneratorConfig;
import org.imd.expertschedule.io.model.AbsenceData;
import org.imd.expertschedule.io.model.AvailabilityData;
import org.imd.expertschedule.io.model.BackOfficeData;
import org.imd.expertschedule.io.model.CustomerData;
import org.imd.expertschedule.io.model.ExpertData;
import org.imd.expertschedule.io.model.LocationData;
import org.imd.expertschedule.io.model.OrderData;
import org.imd.expertschedule.io.model.PlanningDatasetData;
import org.imd.expertschedule.io.model.ScheduleItemData;
import org.imd.expertschedule.io.model.SkillData;
import org.imd.expertschedule.io.model.TimeSlotData;
import org.imd.expertschedule.planner.domain.BackOffice;
import org.imd.expertschedule.planner.domain.Customer;
import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.ExpertSchedule;
import org.imd.expertschedule.planner.domain.Location;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.OrderPriority;
import org.imd.expertschedule.planner.domain.ScheduleItem;
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
import java.time.LocalDate;
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

    public ExpertPlanningSolution load(Path datasetFile) throws IOException {
        PlanningDatasetData data = objectMapper.readValue(datasetFile.toFile(), PlanningDatasetData.class);
        return toDomain(
                data.getSkills() != null ? data.getSkills() : List.of(),
                data.getBackOffices() != null ? data.getBackOffices() : List.of(),
                data.getCustomers() != null ? data.getCustomers() : List.of(),
                data.getExperts() != null ? data.getExperts() : List.of(),
                data.getOrders() != null ? data.getOrders() : List.of(),
                data.getMetadata()
        );
    }

    public ExpertPlanningSolution loadFromDirectory(final Path dataDir,
                                                    final String fileName) throws IOException {
        Objects.requireNonNull(dataDir);
        Objects.requireNonNull(fileName);

        return load(dataDir.resolve(fileName));
    }

    private ExpertPlanningSolution toDomain(List<SkillData> skillData,
                                            List<BackOfficeData> backOfficeData,
                                            List<CustomerData> customerData,
                                            List<ExpertData> expertData,
                                            List<OrderData> orderData,
                                            GeneratorConfig metaData) {
        Map<String, Skill> skillByName = buildSkillMap(skillData);
        Map<Long, BackOffice> backOfficeById = buildBackOfficeMap(backOfficeData);
        Map<Long, Customer> customerById = buildCustomerMap(customerData);
        Map<Long, Expert> expertById = buildExpertMap(expertData, skillByName);
        Map<Long, Order> orderById = buildOrderMap(orderData, skillByName);
        List<ExpertSchedule> expertSchedules = buildExpertScheduleList(metaData,expertData);

        SolutionContext context = buildContext(skillByName, backOfficeById, customerById, expertById, orderById);
        return buildSolution(expertById, orderById, expertSchedules, context);
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
            Customer customer = new Customer();
            CustomerRef cRef = new CustomerRef();
            cRef.setId(cd.getId());
            customer.setId(cRef);
            customer.setName(cd.getName());
            customerById.put(cd.getId(), customer);
        }
        return customerById;
    }

    private Map<Long, Expert> buildExpertMap(List<ExpertData> expertData, Map<String, Skill> skillByName) {
        Map<Long, Expert> expertById = new HashMap<>();
        for (ExpertData ed : expertData) {
            Expert expert = new Expert();
            ExpertRef eRef = new ExpertRef();
            eRef.setId(ed.getId());
            expert.setId(eRef);
            expert.setName(ed.getName());
            BackOfficeRef boRef = new BackOfficeRef();
            boRef.setId(ed.getBackOfficeId());
            expert.setBackOfficeRef(boRef);
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
        Set<Skill> skills = new HashSet<>();
        if (skillNames != null) {
            for (String skillName : skillNames) {
                Skill skill = skillByName.get(skillName);
                if (skill != null) skills.add(skill);
            }
        }
        return skills;
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

    private Map<Long, Order> buildOrderMap(List<OrderData> orderData, Map<String, Skill> skillByName) {
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
            order.setDueDate(od.getDueDate());
            order.setPriority(parsePriority(od.getPriority()));
            order.setDiagnosisDuration(parseDuration(od.getDiagnosisDuration()));
            order.setRequiredSkills(resolveSkills(od.getRequiredSkills(), skillByName));
            orderById.put(od.getId(), order);
        }
        return orderById;
    }

    private List<ExpertSchedule> buildExpertScheduleList(final GeneratorConfig metaData,
                                                         final List<ExpertData> expertData) {
        final int calendarWeek = metaData.getCalendarWeek();
        final int[] weekWorkingDays = metaData.getWeekWorkingDays();

        final List<ExpertSchedule> expertSchedules = new ArrayList<>();

        for (final ExpertData expert: expertData) {
            for (int wd: weekWorkingDays) {
                ExpertRef eRef = new ExpertRef();
                eRef.setId(expert.getId());

                ExpertSchedule es = new ExpertSchedule();
                es.setExpertRef(eRef);
                es.setDate(calculateDate(calendarWeek, wd));

                expertSchedules.add(es);
            }
        }
        return expertSchedules;
    }

    private LocalDate calculateDate(int calendarWeek, int wd) {
        return LocalDate.of(LocalDate.now().getYear(), 1, 1)
                        .plusWeeks(calendarWeek - 1)
                        .plusDays(wd - 1);
    }

    private List<ScheduleItem> toScheduleItems(List<ScheduleItemData> items) {
        final List<ScheduleItem> result = new ArrayList<>();
        for (ScheduleItemData sid : items) {
            ScheduleItem si = new ScheduleItem();
            OrderRef oRef = new OrderRef();
            oRef.setId(sid.getOrderId());
            si.setOrderRef(oRef);
            result.add(si);
        }
        return result;
    }

    private SolutionContext buildContext(Map<String, Skill> skillByName,
                                         Map<Long, BackOffice> backOfficeById,
                                         Map<Long, Customer> customerById,
                                         Map<Long, Expert> expertById,
                                         Map<Long, Order> orderById) {
        SolutionContext context = new SolutionContext();
        context.setSkillList(new ArrayList<>(skillByName.values()));
        context.setBackOfficeList(new ArrayList<>(backOfficeById.values()));
        context.setCustomerList(new ArrayList<>(customerById.values()));
        context.setExpertList(new ArrayList<>(expertById.values()));
        context.setOrderList(new ArrayList<>(orderById.values()));
        return context;
    }

    private ExpertPlanningSolution buildSolution(Map<Long, Expert> expertById,
                                                  Map<Long, Order> orderById,
                                                  List<ExpertSchedule> expertSchedules,
                                                  SolutionContext context) {
        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setExpertRefList(new ArrayList<>(expertById.values().stream().map(Expert::getId).toList()));
        solution.setOrderRefList(new ArrayList<>(orderById.values().stream().map(Order::getId).toList()));
        solution.setExpertScheduleList(expertSchedules);
        solution.setContext(context);
        return solution;
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
