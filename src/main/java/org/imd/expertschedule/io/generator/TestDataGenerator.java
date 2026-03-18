package org.imd.expertschedule.io.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.imd.expertschedule.io.model.AbsenceData;
import org.imd.expertschedule.io.model.AvailabilityData;
import org.imd.expertschedule.io.model.BackOfficeData;
import org.imd.expertschedule.io.model.CustomerData;
import org.imd.expertschedule.io.model.ExpertData;
import org.imd.expertschedule.io.model.ExpertScheduleData;
import org.imd.expertschedule.io.model.LocationData;
import org.imd.expertschedule.io.model.OrderData;
import org.imd.expertschedule.io.model.PlanningDatasetData;
import org.imd.expertschedule.io.model.SkillData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates a single planning dataset file from a {@link GeneratorConfig}.
 * Use presets from {@link GeneratorConfigPresets} or pass a custom config.
 * <p>
 * Example: run with args {@code small}, {@code medium}, or {@code large} to generate data/dataset.json
 * with the corresponding preset. Default is {@code small}.
 */
public class TestDataGenerator {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) throws IOException {
        String presetName = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "small";
        Path outputDir = Path.of("data/expertschedule/");
        Files.createDirectories(outputDir);

        final GeneratorConfig config = GeneratorConfigPresets.small();
        final Path outputFile = outputDir.resolve(config.getFileName());
        generate(config, outputFile);

        System.out.println("Generated " + presetName + " dataset: " + outputFile.toAbsolutePath());
    }

    public static void generate(final GeneratorConfig config, final Path outputFile) throws IOException {
        PlanningDatasetData dataset = buildDataset(config);
        MAPPER.writeValue(outputFile.toFile(), dataset);
    }

    public static PlanningDatasetData buildDataset(GeneratorConfig config) {
        List<SkillData> skills = buildSkills(config.getNumSkills());
        List<BackOfficeData> backOffices = buildBackOffices(config.getNumOffices());
        List<CustomerData> customers = buildCustomers(config.getNumCustomers());
        List<ExpertData> experts = buildExperts(config.getNumExperts(), skills, backOffices, config);
        List<OrderData> orders = buildOrders(config.getNumOrders(), customers, skills, config);

        PlanningDatasetData dataset = new PlanningDatasetData();
        dataset.setMetadata(config);
        dataset.setSkills(skills);
        dataset.setBackOffices(backOffices);
        dataset.setCustomers(customers);
        dataset.setExperts(experts);
        dataset.setOrders(orders);
        return dataset;
    }

    private static List<SkillData> buildSkills(int count) {
        List<SkillData> list = new ArrayList<>();
        String[] names = {"Electrical", "Painting-Scratches", "Engine-Petrol",
                          "Engine-Diesel", "Exclusive", "Total Damage", "Security",
                          "Painting-Part", "Tires", "Glass", "Interior", "Exterior",
                          "Software", "Hardware", "Other"};

        for (int i = 0; i < count; i++) {
            SkillData s = new SkillData();
            s.setName(i < names.length ? names[i] : "Skill-" + (i + 1));
            list.add(s);
        }
        return list;
    }

    private static List<CustomerData> buildCustomers(int count) {
        List<CustomerData> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CustomerData c = new CustomerData();
            c.setId(i + 1);
            c.setName("Customer-" + (i + 1));
            list.add(c);
        }
        return list;
    }

    private static List<BackOfficeData> buildBackOffices(int count) {
        List<BackOfficeData> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BackOfficeData b = new BackOfficeData();
            b.setId(i + 1);
            b.setName("BackOffice-" + (i + 1));
            b.setLocation(randomLocation(0, 10, 0, 5));
            list.add(b);
        }
        return list;
    }

    private static List<ExpertData> buildExperts(int count, List<SkillData> skills,
                                                  List<BackOfficeData> backOffices,
                                                  GeneratorConfig config) {
        List<ExpertData> list = new ArrayList<>();
        String[] names = {"Alice", "Bob", "Carol", "Dave", "Eve", "Frank", "Grace",
                          "Henry", "Ivy", "Jack", "Kate", "Leo", "Mia", "Noah",
                          "Olivia"};
        int calendarWeek = config.getCalendarWeek();
        int[] weekWorkingDays = config.getWeekWorkingDays() != null ? config.getWeekWorkingDays() : new int[]{1, 2, 3, 4, 5};
        for (int i = 0; i < count; i++) {
            ExpertData e = new ExpertData();
            e.setId(i + 1);
            e.setName(i < names.length ? names[i] : "Expert-" + (i + 1));
            BackOfficeData office = backOffices.get(i % backOffices.size());
            e.setBackOfficeId(office.getId());
            e.setSkills(pickSkillNamesFromSkillData(skills));

            if (i < config.getExpertsWithAvailability()) {
                e.setAvailabilities(sampleAvailabilities(calendarWeek, weekWorkingDays));
            }
            if (i < config.getExpertsWithAbsence()) {
                e.setAbsences(sampleAbsences(calendarWeek, weekWorkingDays));
            }
            list.add(e);
        }
        return list;
    }

    private static List<String> pickSkillNamesFromSkillData(List<SkillData> skills) {
        if (skills.isEmpty()) return List.of("Electrical");
        int howMany = Math.max(1, ThreadLocalRandom.current().nextInt(skills.size()) + 1);
        List<String> names = new ArrayList<>(skills.stream().map(SkillData::getName).toList());
        Collections.shuffle(names);
        return names.subList(0, Math.min(howMany, names.size()));
    }

    private static List<AvailabilityData> sampleAvailabilities(int calendarWeek, int[] weekWorkingDays) {
        List<AvailabilityData> list = new ArrayList<>();
        for (int wd : weekWorkingDays) {
            AvailabilityData a = new AvailabilityData();
            a.setCalendarWeek(calendarWeek);
            a.setDayOfWeek(DayOfWeek.of(wd));
            a.setStartTime(LocalTime.of(9, 0));
            a.setEndTime(LocalTime.of(18, 0));
            list.add(a);
        }
        return list;
    }

    private static List<AbsenceData> sampleAbsences(int calendarWeek, int[] weekWorkingDays) {
        if (weekWorkingDays.length == 0) return List.of();
        int wd = weekWorkingDays[ThreadLocalRandom.current().nextInt(weekWorkingDays.length)];
        AbsenceData a = new AbsenceData();
        a.setCalendarWeek(calendarWeek);
        a.setDayOfWeek(DayOfWeek.of(wd));
        a.setStartTime(LocalTime.of(0, 0));
        a.setEndTime(LocalTime.of(23, 59));
        a.setReason("Leave");
        return List.of(a);
    }

    private static List<OrderData> buildOrders(final int count,
                                               final List<CustomerData> customers,
                                               final List<SkillData> skills,
                                               final GeneratorConfig config) {
        List<OrderData> list = new ArrayList<>();
        List<String> skillNames = skills.stream().map(SkillData::getName).toList();
        if (skillNames.isEmpty()) skillNames = List.of("Electrical");

        int[] weekWorkingDays = config.getWeekWorkingDays();
        List<LocalDate> planningDates = planningDates(config.getCalendarWeek(), weekWorkingDays);

        ThreadLocalRandom r = ThreadLocalRandom.current();
        String[] priorities = config.getOrderPriorities();
        String[] durations = config.getOrderDurations();
        for (int i = 0; i < count; i++) {
            OrderData o = new OrderData();
            o.setId(i + 1);
            o.setCode("ORDER-" + (i + 1));
            o.setCustomerId(customers.get(r.nextInt(customers.size())).getId());
            o.setLocation(randomLocation(0, 20, 0, 20));
            o.setDueDate(planningDates.get(r.nextInt(planningDates.size())));
            o.setPriority(priorities[r.nextInt(priorities.length)]);
            o.setDiagnosisDuration(randomDiagnosisDuration(durations));
            int skillCount = Math.max(1, r.nextInt(2) + 1);
            Set<String> required = new HashSet<>();
            while (required.size() < skillCount && required.size() < skillNames.size()) {
                required.add(skillNames.get(r.nextInt(skillNames.size())));
            }
            o.setRequiredSkills(new ArrayList<>(required));
            list.add(o);
        }
        return list;
    }

    /** Dates in the planning window: one per working day in the given calendar week (matches loader's calculateDate). */
    private static List<LocalDate> planningDates(int calendarWeek, int[] weekWorkingDays) {
        List<LocalDate> list = new ArrayList<>();
        for (int wd : weekWorkingDays) {
            list.add(dateInPlanningWeek(calendarWeek, wd));
        }
        return list;
    }

    /** Monday of week 1 = Jan 1; wd 1..7 = day of week. Matches loader's calculateDate. */
    private static LocalDate dateInPlanningWeek(int calendarWeek, int wd) {
        return LocalDate.of(LocalDate.now().getYear(), 1, 1)
                .plusWeeks(calendarWeek - 1)
                .plusDays(wd - 1);
    }

    private static String randomDiagnosisDuration(final String[] orderDurations) {
        return orderDurations[ThreadLocalRandom.current().nextInt(orderDurations.length)];
    }

    private static LocationData randomLocation(double minLat, double maxLat, double minLon, double maxLon) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        LocationData loc = new LocationData();
        loc.setLatitude(minLat + (maxLat - minLat) * r.nextDouble());
        loc.setLongitude(minLon + (maxLon - minLon) * r.nextDouble());
        return loc;
    }
}
