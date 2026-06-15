package org.imd.expertschedule.io.generator;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for generating a planning dataset.
 * Use presets from {@link GeneratorConfigPresets} or construct with setters.
 */
@Getter
@Setter
public class GeneratorConfig {

    private String fileName;

    private int numSkills;
    private int numCustomers;

    private int numOffices;
    private int expertsPerBackOffice;
    private int ordersPerExpert;
    /** Per-office: how many of the first experts in each office receive non-default (reduced) availability. */
    private int expertsWithUndefaultAvailability;
    /** Per-office: how many of the last experts in each office receive an absence entry. */
    private int expertsWithAbsence;

    private String[] orderPriorities;
    private String[] orderDurations;

    private int customerAvailabilityTimeWindowInMinutes = 240;

    private int year;
    private int calendarWeek;
    private int[] weekWorkingDays;

    private double maxDistanceFromBackOfficeKm;

    /** @deprecated used only by {@link TestDataGeneratorBackUp}; new code uses {@link #expertsPerBackOffice}. */
    @Deprecated
    private int numExperts;

    /** @deprecated used only by {@link TestDataGeneratorBackUp}; new code uses {@link #ordersPerExpert}. */
    @Deprecated
    private int numOrders;
}
