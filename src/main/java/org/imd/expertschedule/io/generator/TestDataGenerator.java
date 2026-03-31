package org.imd.expertschedule.io.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import org.imd.expertschedule.planner.domain.time.Availability;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Generates a single planning dataset file from a {@link GeneratorConfig}.
 * Use presets from {@link GeneratorConfigPresets} or pass a custom config.
 * <p>
 * Example: run with a preset name (default {@code small}): {@code ultrasmall}, {@code small},
 * {@code medium}, {@code large}, {@code extralarge}. Output goes under {@code data/expertschedule/}
 * using the preset's file name.
 */
public class TestDataGenerator {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) throws IOException {
        String presetName = args.length > 0 ? args[0].trim().toLowerCase(Locale.ROOT) : "ultrasmall";
        Path outputDir = Path.of("data/expertschedule/");
        Files.createDirectories(outputDir);

        final GeneratorConfig config = resolvePreset(presetName);
        final Path outputFile = outputDir.resolve(config.getFileName());
        generate(config, outputFile);

        System.out.println("Generated " + presetName + " dataset: " + outputFile.toAbsolutePath());
    }

    private static GeneratorConfig resolvePreset(String presetName) {
        return switch (presetName) {
            case "ultrasmall" -> GeneratorConfigPresets.ultrasmall();
            case "small" -> GeneratorConfigPresets.small();
            case "medium" -> GeneratorConfigPresets.medium();
            case "large" -> GeneratorConfigPresets.large();
            case "extralarge" -> GeneratorConfigPresets.extraLarge();
            default -> throw new IllegalArgumentException(
                    "Unknown preset '" + presetName
                            + "'. Use: ultrasmall | small | medium | large | extralarge");
        };
    }

    public static void generate(final GeneratorConfig config, final Path outputFile) throws IOException {
        PlanningDatasetData dataset = buildDataset(config);
        MAPPER.writeValue(outputFile.toFile(), dataset);
    }

    public static PlanningDatasetData buildDataset(GeneratorConfig config) {
        Random random = new Random();
        List<SkillData> skills = buildSkills(config.getNumSkills());
        List<BackOfficeData> backOffices = buildBackOffices(config.getNumOffices(), random);
        List<CustomerData> customers = buildCustomers(config.getNumCustomers());
        List<ExpertData> experts = buildExperts(config.getNumExperts(), skills, backOffices, config, random);
        List<OrderData> orders = buildOrders(config.getNumOrders(), customers, experts, config, random);

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

    private static List<BackOfficeData> buildBackOffices(int count, Random random) {
        List<BackOfficeData> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BackOfficeData b = new BackOfficeData();
            b.setId(i + 1);
            b.setName("BackOffice-" + (i + 1));
            b.setLocation(randomLocation(0, 10, 0, 5, random));
            list.add(b);
        }
        return list;
    }

    private static List<ExpertData> buildExperts(int count, List<SkillData> skills,
                                                  List<BackOfficeData> backOffices,
                                                  GeneratorConfig config,
                                                  Random random) {
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
            e.setSkills(pickSkillNamesFromSkillData(skills, random));

            if (i < config.getExpertsWithUndefaultAvailability()) {
                e.setAvailabilities(sampleUndefaultAvailabilities(calendarWeek, weekWorkingDays, random));
                e.setAbsences(new ArrayList<>());
            } else {
                e.setAvailabilities(sampleDefaultAvailabilities(calendarWeek, weekWorkingDays));
                e.setAbsences(new ArrayList<>());

                if (i + config.getExpertsWithAbsence() >= count) {
                    e.setAbsences(sampleAbsences(calendarWeek, weekWorkingDays, random));
                }
            }

            list.add(e);
        }
        return list;
    }

    private static List<String> pickSkillNamesFromSkillData(List<SkillData> skills, Random random) {
        if (skills.isEmpty()) return List.of("Electrical");
        int howMany = Math.max(1, random.nextInt(skills.size()) + 1);
        List<String> names = new ArrayList<>(skills.stream().map(SkillData::getName).toList());
        Collections.shuffle(names, random);
        return names.subList(0, Math.min(howMany, names.size()));
    }

    private static List<AvailabilityData> sampleUndefaultAvailabilities(int calendarWeek,
                                                                        int[] weekWorkingDays,
                                                                        Random random) {
        List<AvailabilityData> list = new ArrayList<>();
        for (int wd : weekWorkingDays) {
            AvailabilityData a = new AvailabilityData();
            a.setCalendarWeek(calendarWeek);
            a.setDayOfWeek(DayOfWeek.of(wd));
            a.setStartTime(LocalTime.of(9 + random.nextInt(2), random.nextInt(3)*15));
            a.setEndTime(LocalTime.of(13 + random.nextInt(2), 0));
            list.add(a);
        }
        return list;
    }

    private static List<AvailabilityData> sampleDefaultAvailabilities(int calendarWeek, int[] weekWorkingDays) {
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

    private static List<AbsenceData> sampleAbsences(int calendarWeek, int[] weekWorkingDays, Random random) {
        if (weekWorkingDays.length == 0) return List.of();
        int wd = weekWorkingDays[random.nextInt(weekWorkingDays.length)];
        AbsenceData a = new AbsenceData();
        a.setCalendarWeek(calendarWeek);
        a.setDayOfWeek(DayOfWeek.of(wd));
        a.setStartTime(LocalTime.of(9 + random.nextInt(1), random.nextInt(3)*15));
        a.setEndTime(LocalTime.of(12 - random.nextInt(1), 0));
        a.setReason("Leave");
        return List.of(a);
    }

    /**
     * Chooses a non-empty random subset of skills from a randomly picked generated expert, so every order can be
     * served by at least that expert (skill-wise).
     */
    private static List<String> pickRequiredSkillsSubsetFromExpert(List<ExpertData> experts, Random random) {
        List<ExpertData> withSkills = experts.stream()
                .filter(e -> e.getSkills() != null && !e.getSkills().isEmpty())
                .toList();
        if (withSkills.isEmpty()) {
            return List.of("Electrical");
        }
        ExpertData expert = withSkills.get(random.nextInt(withSkills.size()));
        List<String> pool = new ArrayList<>(expert.getSkills());
        int subsetSize = random.nextInt(pool.size()) + 1;
        Collections.shuffle(pool, random);
        return new ArrayList<>(pool.subList(0, subsetSize));
    }

    private static List<OrderData> buildOrders(final int count,
                                               final List<CustomerData> customers,
                                               final List<ExpertData> experts,
                                               final GeneratorConfig config,
                                               final Random random) {
        List<OrderData> list = new ArrayList<>();

        int[] weekWorkingDays = config.getWeekWorkingDays();
        List<LocalDate> planningDates = planningDates(config.getCalendarWeek(), weekWorkingDays);

        String[] priorities = config.getOrderPriorities();
        String[] durations = config.getOrderDurations();
        for (int i = 0; i < count; i++) {
            OrderData o = new OrderData();
            o.setId(i + 1);
            o.setCode("ORDER-" + (i + 1));
            o.setCustomerId(customers.get(random.nextInt(customers.size())).getId());
            o.setLocation(randomLocation(0, 20, 0, 20, random));
            o.setDueDate(planningDates.get(random.nextInt(planningDates.size())));
            o.setPriority(priorities[random.nextInt(priorities.length)]);
            o.setDiagnosisDuration(randomDiagnosisDuration(durations, random));
            o.setRequiredSkills(pickRequiredSkillsSubsetFromExpert(experts, random));
            o.setCustomerAvailabilities(
                    customerAvailabilitiesForPlanningWeek(config, weekWorkingDays, random));
            list.add(o);
        }
        return list;
    }

    private static final LocalTime CUSTOMER_AVAILABILITY_DAY_START = LocalTime.of(9, 0);
    private static final LocalTime CUSTOMER_AVAILABILITY_DAY_END = LocalTime.of(18, 0);

    /**
     * Matches {@link org.imd.expertschedule.planner.solution.PlannerParameters.ExpertRelated#getSlotDuration()}:
     * customer availability starts only on :00, :15, :30, :45.
     */
    private static final Duration SLOT_DURATION = Duration.of(15, ChronoUnit.MINUTES);

    /**
     * Per order: on each planning working day, customer is available for a contiguous window of length
     * {@link GeneratorConfig#getCustomerAvailabilityTimeWindowInMinutes()}, with start time aligned to
     * {@link #SLOT_DURATION} within 09:00–18:00 (same day bounds as generated expert availability) so
     * {@link org.imd.expertschedule.planner.util.PlannerHelper#orderIsServable} can still match feasible slots.
     */
    private static List<Availability> customerAvailabilitiesForPlanningWeek(
            GeneratorConfig config, int[] weekWorkingDays, Random random) {
        if (weekWorkingDays == null || weekWorkingDays.length == 0) {
            return List.of();
        }
        int calendarWeek = config.getCalendarWeek();
        int daySpanMinutes = (int) Duration.between(CUSTOMER_AVAILABILITY_DAY_START, CUSTOMER_AVAILABILITY_DAY_END).toMinutes();

        int requestedWindow = config.getCustomerAvailabilityTimeWindowInMinutes();
        int effectiveWindowMinutes = requestedWindow <= 0
                ? daySpanMinutes
                : Math.min(requestedWindow, daySpanMinutes);

        int latestStartOffset = daySpanMinutes - effectiveWindowMinutes;
        int slotMinutes = (int) SLOT_DURATION.toMinutes();

        List<Availability> list = new ArrayList<>(weekWorkingDays.length);
        for (int wd : weekWorkingDays) {
            int startOffset = pickSlotAlignedStartOffsetMinutes(latestStartOffset, slotMinutes, random);
            LocalTime start = CUSTOMER_AVAILABILITY_DAY_START.plusMinutes(startOffset);
            LocalTime end = start.plusMinutes(effectiveWindowMinutes);

            Availability a = new Availability();
            a.setCalendarWeek(calendarWeek);
            a.setWorkDay(wd);
            a.setStartTime(start);
            a.setEndTime(end);
            list.add(a);
        }
        return list;
    }

    /**
     * Random start offset from day start, in minutes, that is a multiple of {@code slotMinutes}
     * (so clock minute is 0, 15, 30, or 45) and fits before {@code latestStartOffset} inclusive.
     */
    private static int pickSlotAlignedStartOffsetMinutes(int latestStartOffset, int slotMinutes, Random random) {
        if (latestStartOffset <= 0) {
            return 0;
        }
        int maxAligned = (latestStartOffset / slotMinutes) * slotMinutes;
        int maxSlotIndex = maxAligned / slotMinutes;
        int slotIndex = random.nextInt(maxSlotIndex + 1);
        return slotIndex * slotMinutes;
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

    private static String randomDiagnosisDuration(final String[] orderDurations, Random random) {
        return orderDurations[random.nextInt(orderDurations.length)];
    }

    private static LocationData randomLocation(double minLat, double maxLat, double minLon, double maxLon, Random random) {
        LocationData loc = new LocationData();
        loc.setLatitude(minLat + (maxLat - minLat) * random.nextDouble());
        loc.setLongitude(minLon + (maxLon - minLon) * random.nextDouble());
        return loc;
    }
}
