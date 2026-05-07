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
    private int numExperts;
    private int numCustomers;
    private int numOrders;

    private int numOffices;
    private int expertsWithUndefaultAvailability;
    private int expertsWithAbsence;

    private String[] orderPriorities;
    private String[] orderDurations;

    private int customerAvailabilityTimeWindowInMinutes = 240;

    private int year;
    private int calendarWeek;
    private int[] weekWorkingDays;

    private double maxDistanceFromBackOfficeKm = 50.0;
}
