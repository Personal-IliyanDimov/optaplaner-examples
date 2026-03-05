package com.example.expertschedule.io.generator;

import com.example.expertschedule.io.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates a single planning dataset file from a {@link GeneratorConfig}.
 * Use presets from {@link GeneratorConfigPresets} or pass a custom config.
 * <p>
 * Example: run with args {@code small}, {@code medium}, or {@code large} to generate data/dataset.json
 * with the corresponding preset. Default is {@code small}.
 */
public class TestDataGenerator {

    private static final String DEFAULT_FILENAME = "dataset.json";
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) throws IOException {
        String presetName = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "small";
        Path outputDir = Path.of("data");
        Files.createDirectories(outputDir);
        Path outputFile = outputDir.resolve(DEFAULT_FILENAME);

        GeneratorConfig config = presetFromName(presetName);
        generate(config, outputFile);

        System.out.println("Generated " + presetName + " dataset: " + outputFile.toAbsolutePath());
    }

    /**
     * Generates a dataset from the given config and writes it to a single JSON file.
     */
    public static void generate(GeneratorConfig config, Path outputFile) throws IOException {
        PlanningDatasetData dataset = buildDataset(config);
        MAPPER.writeValue(outputFile.toFile(), dataset);
    }

    public static PlanningDatasetData buildDataset(GeneratorConfig config) {
        List<SkillData> skills = buildSkills(config.getNumSkills());
        List<CustomerData> customers = buildCustomers(config.getNumCustomers());
        List<ExpertData> experts = buildExperts(config.getNumExperts(), skills,
                config.getExpertsWithAvailability(), config.getExpertsWithAbsence());
        List<OrderData> orders = buildOrders(config.getNumOrders(), customers, skills);

        PlanningDatasetData dataset = new PlanningDatasetData();
        dataset.setSkills(skills);
        dataset.setCustomers(customers);
        dataset.setExperts(experts);
        dataset.setOrders(orders);
        return dataset;
    }

    private static GeneratorConfig presetFromName(String name) {
        return switch (name) {
            case "medium" -> GeneratorConfigPresets.medium();
            case "large" -> GeneratorConfigPresets.large();
            default -> GeneratorConfigPresets.small();
        };
    }

    private static List<SkillData> buildSkills(int count) {
        List<SkillData> list = new ArrayList<>();
        String[] names = {"Electrical", "Plumbing", "Networking", "HVAC", "Carpentry", "Painting", "Security", "Landscaping"};
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
            c.setName("Customer-" + (i + 1));
            c.setLocation(randomLocation(0, 20, 0, 20));
            list.add(c);
        }
        return list;
    }

    private static List<ExpertData> buildExperts(int count, List<SkillData> skills,
                                                  int withAvailability, int withAbsence) {
        List<ExpertData> list = new ArrayList<>();
        String[] names = {"Alice", "Bob", "Carol", "Dave", "Eve", "Frank", "Grace", "Henry", "Ivy", "Jack", "Kate", "Leo", "Mia", "Noah", "Olivia"};
        for (int i = 0; i < count; i++) {
            ExpertData e = new ExpertData();
            e.setName(i < names.length ? names[i] : "Expert-" + (i + 1));
            e.setBackOfficeLocation(randomLocation(0, 10, 0, 5));
            e.setSkills(pickSkillNamesFromSkillData(skills));

            if (i < withAvailability) {
                e.setAvailabilities(sampleAvailabilities());
            }
            if (i < withAbsence) {
                e.setAbsences(sampleAbsences());
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

    private static List<AvailabilityData> sampleAvailabilities() {
        List<AvailabilityData> list = new ArrayList<>();
        for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
            AvailabilityData a = new AvailabilityData();
            a.setDayOfWeek(day);
            a.setStartTime(LocalTime.of(8, 0));
            a.setEndTime(LocalTime.of(17, 0));
            list.add(a);
        }
        return list;
    }

    private static List<AbsenceData> sampleAbsences() {
        AbsenceData a = new AbsenceData();
        a.setStartDate(LocalDate.now().plusDays(7));
        a.setEndDate(LocalDate.now().plusDays(9));
        a.setReason("Leave");
        return List.of(a);
    }

    private static List<OrderData> buildOrders(int count, List<CustomerData> customers, List<SkillData> skills) {
        List<OrderData> list = new ArrayList<>();
        List<String> skillNames = skills.stream().map(SkillData::getName).toList();
        if (skillNames.isEmpty()) skillNames = List.of("Electrical");
        for (int i = 0; i < count; i++) {
            OrderData o = new OrderData();
            o.setCode("ORDER-" + (i + 1));
            o.setCustomer(customers.get(ThreadLocalRandom.current().nextInt(customers.size())).getName());
            int skillCount = Math.max(1, ThreadLocalRandom.current().nextInt(2) + 1);
            Set<String> required = new HashSet<>();
            while (required.size() < skillCount && required.size() < skillNames.size()) {
                required.add(skillNames.get(ThreadLocalRandom.current().nextInt(skillNames.size())));
            }
            o.setRequiredSkills(new ArrayList<>(required));
            list.add(o);
        }
        return list;
    }

    private static LocationData randomLocation(double minLat, double maxLat, double minLon, double maxLon) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        LocationData loc = new LocationData();
        loc.setLatitude(minLat + (maxLat - minLat) * r.nextDouble());
        loc.setLongitude(minLon + (maxLon - minLon) * r.nextDouble());
        return loc;
    }
}
