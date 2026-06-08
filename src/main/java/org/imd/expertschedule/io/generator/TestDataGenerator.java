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
import org.imd.expertschedule.planner.util.PlannerHelper;

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
 */
public class TestDataGenerator {

    private static final PlannerHelper HELPER = new PlannerHelper();

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) throws IOException {
        String presetName = args.length > 0 ? args[0].trim().toLowerCase(Locale.ROOT) : "small";
        Path outputDir = Path.of("data/expertschedule/");
        Files.createDirectories(outputDir);

        final GeneratorConfig config = resolvePreset(presetName);
        final Path outputFile = outputDir.resolve(config.getFileName());
        generate(config, outputFile);

        System.out.println("Generated " + presetName + " dataset: " + outputFile.toAbsolutePath()
                + " (maxDistanceFromBackOfficeKm=" + config.getMaxDistanceFromBackOfficeKm() + ")");
    }

    private static GeneratorConfig resolvePreset(String presetName) {
        return switch (presetName) {
            case "ultrasmall" -> GeneratorConfigPresets.ultrasmall();
            case "small" -> GeneratorConfigPresets.small();
            case "medium" -> GeneratorConfigPresets.medium();
            case "large" -> GeneratorConfigPresets.large();
            case "extralarge" -> GeneratorConfigPresets.extraLarge();
            case "huge" -> GeneratorConfigPresets.huge();
            default -> throw new IllegalArgumentException(
                    "Unknown preset '" + presetName
                            + "'. Use: ultrasmall | small | medium | large | extralarge | huge");
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
        List<OrderData> orders = buildOrders(config.getNumOrders(), customers, experts, backOffices, config, random);

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
        int year = config.getYear();
        int calendarWeek = config.getCalendarWeek();
        int[] weekWorkingDays = config.getWeekWorkingDays();
        for (int i = 0; i < count; i++) {
            ExpertData e = new ExpertData();
            e.setId(i + 1);
            e.setName(i < names.length ? names[i] : "Expert-" + (i + 1));
            BackOfficeData office = backOffices.get(i % backOffices.size());
            e.setBackOfficeId(office.getId());
            e.setSkills(pickSkillNamesFromSkillData(skills, random));

            if (i < config.getExpertsWithUndefaultAvailability()) {
                e.setAvailabilities(sampleUndefaultAvailabilities(year, calendarWeek, weekWorkingDays));
                e.setAbsences(new ArrayList<>());
            } else {
                e.setAvailabilities(sampleDefaultAvailabilities(year, calendarWeek, weekWorkingDays));
                e.setAbsences(new ArrayList<>());

                if (i + config.getExpertsWithAbsence() >= count) {
                    e.setAbsences(sampleAbsences(year, calendarWeek, weekWorkingDays, random));
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

    private static List<AvailabilityData> sampleUndefaultAvailabilities(int year,
                                                                        int calendarWeek,
                                                                        int[] weekWorkingDays) {
        List<AvailabilityData> list = new ArrayList<>();
        for (int wd : weekWorkingDays) {
            AvailabilityData a = new AvailabilityData();
            a.setYear(year);
            a.setCalendarWeek(calendarWeek);
            a.setDayOfWeek(DayOfWeek.of(wd));
            a.setStartTime(LocalTime.of(9 + wd % 2, (wd % 3)*15));
            a.setEndTime(LocalTime.of(13 + wd % 2, 0));
            list.add(a);
        }
        return list;
    }

    private static List<AvailabilityData> sampleDefaultAvailabilities(int year, int calendarWeek, int[] weekWorkingDays) {
        List<AvailabilityData> list = new ArrayList<>();
        for (int wd : weekWorkingDays) {
            AvailabilityData a = new AvailabilityData();
            a.setYear(year);
            a.setCalendarWeek(calendarWeek);
            a.setDayOfWeek(DayOfWeek.of(wd));
            a.setStartTime(LocalTime.of(9, 0));
            a.setEndTime(LocalTime.of(18, 0));
            list.add(a);
        }
        return list;
    }

    private static List<AbsenceData> sampleAbsences(int year, int calendarWeek, int[] weekWorkingDays, Random random) {
        if (weekWorkingDays.length == 0) return List.of();
        int wd = weekWorkingDays[random.nextInt(weekWorkingDays.length)];
        AbsenceData a = new AbsenceData();
        a.setYear(year);
        a.setCalendarWeek(calendarWeek);
        a.setDayOfWeek(DayOfWeek.of(wd));
        a.setStartTime(LocalTime.of(9 + random.nextInt(1), random.nextInt(3)*15));
        a.setEndTime(LocalTime.of(12 - random.nextInt(1), 0));
        a.setReason("Leave");
        return List.of(a);
    }

    /**
     * Chooses a non-empty random subset of skills from a randomly picked generated expert, so every order can be
     * served by at least that expert (skill-wise). The same expert anchors the order location near their back office.
     */
    private static PickResult pickRequiredSkillsSubsetFromExpert(List<ExpertData> experts, int expertIndex, Random random) {
        ExpertData expert = experts.get(random.nextInt(experts.size()));
        List<String> expertSkills = new ArrayList<>(expert.getSkills());
        int subsetSize = random.nextInt(expertSkills.size()) + 1;
        Collections.shuffle(expertSkills, random);
        return new PickResult(expert, new ArrayList<>(expertSkills.subList(0, subsetSize)));
    }

    private record PickResult(ExpertData referenceExpert, List<String> requiredSkills) {}

    private static List<OrderData> buildOrders(final int count,
                                               final List<CustomerData> customers,
                                               final List<ExpertData> experts,
                                               final List<BackOfficeData> backOffices,
                                               final GeneratorConfig config,
                                               final Random random) {
        List<OrderData> list = new ArrayList<>();

        int[] weekWorkingDays = config.getWeekWorkingDays();
        List<LocalDate> planningDates = planningDates(config.getYear(), config.getCalendarWeek(), weekWorkingDays);

        String[] priorities = config.getOrderPriorities();
        String[] durations = config.getOrderDurations();
        for (int i = 0; i < count; i++) {
            PickResult pick = pickRequiredSkillsSubsetFromExpert(experts, i % experts.size(), random);
            OrderData o = new OrderData();
            o.setId(i + 1);
            o.setCode("ORDER-" + (i + 1));
            o.setCustomerId(customers.get(random.nextInt(customers.size())).getId());
            o.setLocation(orderLocationNearBackOffice(pick, backOffices, config, random));
            o.setDueDate(planningDates.get(i % planningDates.size()));
            o.setPriority(priorities[random.nextInt(priorities.length)]);
            o.setDiagnosisDuration(durations[i % (durations.length)]);
            o.setRequiredSkills(pick.requiredSkills());
            o.setCustomerAvailabilities(
                    customerAvailabilitiesForPlanningWeek(config, config.getYear(), weekWorkingDays, random));
            list.add(o);
        }
        return list;
    }

    private static LocationData orderLocationNearBackOffice(PickResult pick,
                                                            List<BackOfficeData> backOffices,
                                                            GeneratorConfig config,
                                                            Random random) {
        double maxKm = config.getMaxDistanceFromBackOfficeKm();

        LocationData anchor = resolveAnchorOfficeLocation(pick.referenceExpert(), backOffices);
        return randomLocationNear(anchor.getLatitude(), anchor.getLongitude(), maxKm, random);
    }

    private static LocationData resolveAnchorOfficeLocation(ExpertData referenceExpert, List<BackOfficeData> backOffices) {
        LocationData result = null;

        long oid = referenceExpert.getBackOfficeId();
        for (BackOfficeData b : backOffices) {
            if (b.getId() == oid && b.getLocation() != null) {
                result = b.getLocation();
            }
        }

        return result;
    }

    /** Earth mean radius for great-circle distance (km). */
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Uniform random point within a geographic disk of radius {@code maxRadiusKm} around ({@code centerLatDeg}, {@code centerLonDeg})
     * (great-circle distance).
     */
    static LocationData randomLocationNear(double centerLatDeg, double centerLonDeg, double maxRadiusKm, Random random) {

        double distKm = maxRadiusKm * Math.sqrt(random.nextDouble());
        double bearing = 2.0 * Math.PI * random.nextDouble();

        double lat1 = Math.toRadians(centerLatDeg);
        double lon1 = Math.toRadians(centerLonDeg);
        double angularDist = distKm / EARTH_RADIUS_KM;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(angularDist)
                + Math.cos(lat1) * Math.sin(angularDist) * Math.cos(bearing));
        double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(angularDist) * Math.cos(lat1),
                Math.cos(angularDist) - Math.sin(lat1) * Math.sin(lat2));

        LocationData loc = new LocationData();
        loc.setLatitude(Math.toDegrees(lat2));
        loc.setLongitude(Math.toDegrees(lon2));
        return loc;
    }

    /**
     * Great-circle distance between two WGS84-like coordinates in kilometers.
     */
    static double haversineKm(double lat1Deg, double lon1Deg, double lat2Deg, double lon2Deg) {
        double lat1 = Math.toRadians(lat1Deg);
        double lat2 = Math.toRadians(lat2Deg);
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(lon2Deg - lon1Deg);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2.0 * EARTH_RADIUS_KM * Math.asin(Math.min(1.0, Math.sqrt(h)));
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
            GeneratorConfig config, int year, int[] weekWorkingDays, Random random) {
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
            a.setYear(year);
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

    /** Dates in the planning window: one per working day (matches {@link PlannerHelper#calculateDate}). */
    private static List<LocalDate> planningDates(int year, int calendarWeek, int[] weekWorkingDays) {
        List<LocalDate> list = new ArrayList<>();
        for (int wd : weekWorkingDays) {
            list.add(HELPER.calculateDate(year, calendarWeek, wd));
        }
        return list;
    }

    private static LocationData randomLocation(double minLat, double maxLat, double minLon, double maxLon, Random random) {
        LocationData loc = new LocationData();
        loc.setLatitude(minLat + (maxLat - minLat) * random.nextDouble());
        loc.setLongitude(minLon + (maxLon - minLon) * random.nextDouble());
        return loc;
    }
}
