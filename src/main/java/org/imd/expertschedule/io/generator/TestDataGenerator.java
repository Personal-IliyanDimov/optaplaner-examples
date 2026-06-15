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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
        List<CustomerData> customers = buildCustomers(config.getNumCustomers());
        List<BackOfficeData> backOffices = buildBackOffices(config.getNumOffices(), random);

        List<ExpertData> allExperts = new ArrayList<>();
        List<OrderData> allOrders = new ArrayList<>();
        int expertIdSeq = 1;
        int orderIdSeq = 1;

        for (BackOfficeData backOffice : backOffices) {
            List<ExpertData> officeExperts = buildExpertsForBackOffice(
                    expertIdSeq, config.getExpertsPerBackOffice(), skills, backOffice, config, random);
            expertIdSeq += officeExperts.size();
            allExperts.addAll(officeExperts);

            for (ExpertData expert : officeExperts) {
                List<OrderData> expertOrders = buildOrdersForExpert(
                        orderIdSeq, expert, customers, backOffice, config, random);
                orderIdSeq += expertOrders.size();
                allOrders.addAll(expertOrders);
            }
        }

        PlanningDatasetData dataset = new PlanningDatasetData();
        dataset.setMetadata(config);
        dataset.setSkills(skills);
        dataset.setBackOffices(backOffices);
        dataset.setCustomers(customers);
        dataset.setExperts(allExperts);
        dataset.setOrders(allOrders);

        return dataset;
    }

    // ─── builders ────────────────────────────────────────────────────────────

    private static List<SkillData> buildSkills(int count) {
        String[] knownNames = {"Electrical", "Painting-Scratches", "Engine-Petrol",
                               "Engine-Diesel", "Exclusive", "Total Damage", "Security",
                               "Painting-Part", "Tires", "Glass", "Interior", "Exterior",
                               "Software", "Hardware", "Other"};
        List<SkillData> skills = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            SkillData skill = new SkillData();
            skill.setName(i < knownNames.length ? knownNames[i] : "Skill-" + (i + 1));
            skills.add(skill);
        }
        return skills;
    }

    private static List<CustomerData> buildCustomers(int count) {
        List<CustomerData> customers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CustomerData customer = new CustomerData();
            customer.setId(i + 1);
            customer.setName("Customer-" + (i + 1));
            customers.add(customer);
        }
        return customers;
    }

    private static List<BackOfficeData> buildBackOffices(int count, Random random) {
        List<BackOfficeData> backOffices = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BackOfficeData backOffice = new BackOfficeData();
            backOffice.setId(i + 1);
            backOffice.setName("BackOffice-" + (i + 1));
            backOffice.setLocation(randomLocationInBounds(0, 10, 0, 5, random));
            backOffices.add(backOffice);
        }
        return backOffices;
    }

    private static List<ExpertData> buildExpertsForBackOffice(int startId, int count,
            List<SkillData> skills, BackOfficeData backOffice, GeneratorConfig config, Random random) {
        List<ExpertData> experts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            experts.add(buildExpert(startId + i, i, skills, backOffice, config, random));
        }
        return experts;
    }

    private static ExpertData buildExpert(int id, int indexInOffice,
            List<SkillData> skills, BackOfficeData backOffice, GeneratorConfig config, Random random) {
        String[] knownNames = {"Alice", "Bob", "Carol", "Dave", "Eve", "Frank", "Grace",
                               "Henry", "Ivy", "Jack", "Kate", "Leo", "Mia", "Noah", "Olivia"};
        int year = config.getYear();
        int calendarWeek = config.getCalendarWeek();
        int[] weekWorkingDays = config.getWeekWorkingDays();
        int count = config.getExpertsPerBackOffice();

        ExpertData expert = new ExpertData();
        expert.setId(id);
        expert.setName(indexInOffice < knownNames.length ? knownNames[indexInOffice] : "Expert-" + id);
        expert.setBackOfficeId(backOffice.getId());
        expert.setSkills(randomSkillNamesFrom(skills, random));
        expert.setAvailabilities(indexInOffice < config.getExpertsWithUndefaultAvailability()
                ? reducedDayAvailabilities(year, calendarWeek, weekWorkingDays)
                : fullDayAvailabilities(year, calendarWeek, weekWorkingDays));
        expert.setAbsences(indexInOffice + config.getExpertsWithAbsence() >= count
                ? randomLeaveAbsences(year, calendarWeek, weekWorkingDays, random)
                : new ArrayList<>());
        return expert;
    }

    // ─── order generation: per day, driven by expert availability ────────────

    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_START = LocalTime.of(13, 0);
    private static final LocalTime AFTERNOON_END = LocalTime.of(16, 0);

    private static List<OrderData> buildOrdersForExpert(int startId, ExpertData expert,
            List<CustomerData> customers, BackOfficeData backOffice, GeneratorConfig config, Random random) {
        List<OrderData> orders = new ArrayList<>();
        int orderIdSeq = startId;
        int[] weekWorkingDays = config.getWeekWorkingDays();
        List<LocalDate> planningDates = planningDatesForWeek(config.getYear(), config.getCalendarWeek(), weekWorkingDays);

        for (int i = 0; i < weekWorkingDays.length; i++) {
            int wd = weekWorkingDays[i];
            AvailabilityData availability = findAvailabilityForDay(expert, wd);
            if (availability == null) continue;

            List<AbsenceData> dayAbsences = findAbsencesForDay(expert, wd);
            LocalDate date = planningDates.get(i);

            OrderData morning = tryBuildMorningOrder(orderIdSeq, expert, customers, backOffice, availability, dayAbsences, date, config, random);
            if (morning != null) { orders.add(morning); orderIdSeq++; }

            OrderData afternoon = tryBuildAfternoonOrder(orderIdSeq, expert, customers, backOffice, availability, dayAbsences, date, config, random);
            if (afternoon != null) { orders.add(afternoon); orderIdSeq++; }
        }
        return orders;
    }

    private static OrderData tryBuildMorningOrder(int orderId, ExpertData expert,
            List<CustomerData> customers, BackOfficeData backOffice,
            AvailabilityData availability, List<AbsenceData> dayAbsences,
            LocalDate date, GeneratorConfig config, Random random) {
        LocalTime windowEnd = minTime(availability.getEndTime(), LUNCH_START);
        if (!windowEnd.isAfter(availability.getStartTime())) return null;

        int effectiveMinutes = effectiveMinutesInWindow(availability.getStartTime(), windowEnd, dayAbsences);
        String duration = morningDurationFor(effectiveMinutes, random);
        if (duration == null) return null;

        return buildOrderForSlot(orderId, expert, customers, backOffice, date, availability.getStartTime(), duration, config, random);
    }

    private static OrderData tryBuildAfternoonOrder(int orderId, ExpertData expert,
            List<CustomerData> customers, BackOfficeData backOffice,
            AvailabilityData availability, List<AbsenceData> dayAbsences,
            LocalDate date, GeneratorConfig config, Random random) {
        LocalTime windowStart = maxTime(availability.getStartTime(), AFTERNOON_START);
        LocalTime windowEnd = minTime(availability.getEndTime(), AFTERNOON_END);
        if (!windowEnd.isAfter(windowStart)) return null;

        int effectiveMinutes = effectiveMinutesInWindow(windowStart, windowEnd, dayAbsences);
        String duration = afternoonDurationFor(effectiveMinutes);
        if (duration == null) return null;

        return buildOrderForSlot(orderId, expert, customers, backOffice, date, windowStart, duration, config, random);
    }

    private static OrderData buildOrderForSlot(int orderId, ExpertData expert,
            List<CustomerData> customers, BackOfficeData backOffice,
            LocalDate date, LocalTime eventStart, String duration,
            GeneratorConfig config, Random random) {
        String[] priorities = config.getOrderPriorities();
        int wd = date.getDayOfWeek().getValue();

        OrderData order = new OrderData();
        order.setId(orderId);
        order.setCode("ORDER-" + orderId);
        order.setCustomerId(customers.get(random.nextInt(customers.size())).getId());
        order.setLocation(randomLocationNear(
                backOffice.getLocation().getLatitude(), backOffice.getLocation().getLongitude(),
                config.getMaxDistanceFromBackOfficeKm(), random));
        order.setDueDate(date);
        order.setPriority(priorities[random.nextInt(priorities.length)]);
        order.setDiagnosisDuration(duration);
        order.setRequiredSkills(pickRequiredSkillsSubset(expert.getSkills(), random));
        order.setCustomerAvailabilities(customerAvailabilityForOrder(config.getYear(), config.getCalendarWeek(), wd, eventStart));
        return order;
    }

    // ─── duration rules ───────────────────────────────────────────────────────

    /** Before-lunch slot: 3h+ → PT1H30M/PT2H, 2h+ → PT1H/PT1H30M, 1h+ → PT30M, else null. */
    private static String morningDurationFor(int effectiveMinutes, Random random) {
        if (effectiveMinutes >= 180) return random.nextBoolean() ? "PT1H30M" : "PT2H";
        if (effectiveMinutes >= 120) return random.nextBoolean() ? "PT1H" : "PT1H30M";
        if (effectiveMinutes >= 60) return "PT30M";
        return null;
    }

    /** 13:00–16:00 slot: full 3h → PT2H, 1.5h+ → PT1H30M, 1h+ → PT1H, 30m+ → PT30M, else null. */
    private static String afternoonDurationFor(int effectiveMinutes) {
        if (effectiveMinutes >= 180) return "PT2H";
        if (effectiveMinutes >= 90) return "PT1H30M";
        if (effectiveMinutes >= 60) return "PT1H";
        if (effectiveMinutes >= 30) return "PT30M";
        return null;
    }

    // ─── availability / absence lookups ──────────────────────────────────────

    private static AvailabilityData findAvailabilityForDay(ExpertData expert, int wd) {
        if (expert.getAvailabilities() == null) return null;
        return expert.getAvailabilities().stream()
                .filter(a -> a.getDayOfWeek() == DayOfWeek.of(wd))
                .findFirst().orElse(null);
    }

    private static List<AbsenceData> findAbsencesForDay(ExpertData expert, int wd) {
        if (expert.getAbsences() == null) return List.of();
        return expert.getAbsences().stream()
                .filter(a -> a.getDayOfWeek() == DayOfWeek.of(wd))
                .toList();
    }

    /** Total available minutes in [windowStart, windowEnd] minus any absence overlap. */
    private static int effectiveMinutesInWindow(LocalTime windowStart, LocalTime windowEnd, List<AbsenceData> absences) {
        int minutes = (int) Duration.between(windowStart, windowEnd).toMinutes();
        for (AbsenceData absence : absences) {
            LocalTime overlapStart = maxTime(absence.getStartTime(), windowStart);
            LocalTime overlapEnd = minTime(absence.getEndTime(), windowEnd);
            if (overlapEnd.isAfter(overlapStart)) {
                minutes -= (int) Duration.between(overlapStart, overlapEnd).toMinutes();
            }
        }
        return Math.max(0, minutes);
    }

    // ─── customer availability ────────────────────────────────────────────────

    private static final LocalTime CUSTOMER_AVAILABILITY_DAY_START = LocalTime.of(9, 0);
    private static final LocalTime CUSTOMER_AVAILABILITY_DAY_END = LocalTime.of(18, 0);

    /** Customer window = [eventStart − 1h, eventStart + 1h], clamped to 09:00–18:00. */
    private static List<Availability> customerAvailabilityForOrder(int year, int calendarWeek, int wd, LocalTime eventStart) {
        LocalTime start = maxTime(eventStart.minusHours(1), CUSTOMER_AVAILABILITY_DAY_START);
        LocalTime end = minTime(eventStart.plusHours(1), CUSTOMER_AVAILABILITY_DAY_END);

        Availability availability = new Availability();
        availability.setYear(year);
        availability.setCalendarWeek(calendarWeek);
        availability.setWorkDay(wd);
        availability.setStartTime(start);
        availability.setEndTime(end);
        return List.of(availability);
    }

    // ─── skill helpers ────────────────────────────────────────────────────────

    private static List<String> randomSkillNamesFrom(List<SkillData> skills, Random random) {
        if (skills.isEmpty()) return List.of("Electrical");
        int howMany = Math.max(1, random.nextInt(skills.size()) + 1);
        List<String> names = new ArrayList<>(skills.stream().map(SkillData::getName).toList());
        Collections.shuffle(names, random);
        return names.subList(0, Math.min(howMany, names.size()));
    }

    private static List<String> pickRequiredSkillsSubset(List<String> expertSkills, Random random) {
        if (expertSkills == null || expertSkills.isEmpty()) return List.of("Electrical");
        List<String> copy = new ArrayList<>(expertSkills);
        int subsetSize = random.nextInt(copy.size()) + 1;
        Collections.shuffle(copy, random);
        return new ArrayList<>(copy.subList(0, subsetSize));
    }

    // ─── availability / absence builders ─────────────────────────────────────

    private static List<AvailabilityData> reducedDayAvailabilities(int year, int calendarWeek, int[] weekWorkingDays) {
        List<AvailabilityData> availabilities = new ArrayList<>();
        for (int wd : weekWorkingDays) {
            AvailabilityData availability = new AvailabilityData();
            availability.setYear(year);
            availability.setCalendarWeek(calendarWeek);
            availability.setDayOfWeek(DayOfWeek.of(wd));
            availability.setStartTime(LocalTime.of(9 + wd % 2, (wd % 3) * 15));
            availability.setEndTime(LocalTime.of(13 + wd % 2, 0));
            availabilities.add(availability);
        }
        return availabilities;
    }

    private static List<AvailabilityData> fullDayAvailabilities(int year, int calendarWeek, int[] weekWorkingDays) {
        List<AvailabilityData> availabilities = new ArrayList<>();
        for (int wd : weekWorkingDays) {
            AvailabilityData availability = new AvailabilityData();
            availability.setYear(year);
            availability.setCalendarWeek(calendarWeek);
            availability.setDayOfWeek(DayOfWeek.of(wd));
            availability.setStartTime(LocalTime.of(9, 0));
            availability.setEndTime(LocalTime.of(18, 0));
            availabilities.add(availability);
        }
        return availabilities;
    }

    private static List<AbsenceData> randomLeaveAbsences(int year, int calendarWeek, int[] weekWorkingDays, Random random) {
        if (weekWorkingDays.length == 0) return List.of();
        int wd = weekWorkingDays[random.nextInt(weekWorkingDays.length)];
        AbsenceData absence = new AbsenceData();
        absence.setYear(year);
        absence.setCalendarWeek(calendarWeek);
        absence.setDayOfWeek(DayOfWeek.of(wd));
        absence.setStartTime(LocalTime.of(9 + random.nextInt(2), random.nextInt(3) * 15));
        absence.setEndTime(LocalTime.of(12 + random.nextInt(2), 0));
        absence.setReason("Leave");
        return List.of(absence);
    }

    // ─── location helpers ─────────────────────────────────────────────────────

    /** Earth mean radius for great-circle distance (km). */
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Uniform random point within a geographic disk of radius {@code maxRadiusKm} around
     * ({@code centerLatDeg}, {@code centerLonDeg}) (great-circle distance).
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

        LocationData location = new LocationData();
        location.setLatitude(Math.toDegrees(lat2));
        location.setLongitude(Math.toDegrees(lon2));
        return location;
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

    private static List<LocalDate> planningDatesForWeek(int year, int calendarWeek, int[] weekWorkingDays) {
        List<LocalDate> dates = new ArrayList<>();
        for (int wd : weekWorkingDays) {
            dates.add(HELPER.calculateDate(year, calendarWeek, wd));
        }
        return dates;
    }

    private static LocationData randomLocationInBounds(double minLat, double maxLat,
            double minLon, double maxLon, Random random) {
        LocationData location = new LocationData();
        location.setLatitude(minLat + (maxLat - minLat) * random.nextDouble());
        location.setLongitude(minLon + (maxLon - minLon) * random.nextDouble());
        return location;
    }

    private static LocalTime minTime(LocalTime a, LocalTime b) {
        return a.isBefore(b) ? a : b;
    }

    private static LocalTime maxTime(LocalTime a, LocalTime b) {
        return a.isAfter(b) ? a : b;
    }
}
