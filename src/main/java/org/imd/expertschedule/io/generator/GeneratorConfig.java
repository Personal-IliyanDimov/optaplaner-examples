package org.imd.expertschedule.io.generator;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

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
    private int expertsPerOfficeWithUndefaultAvailability;
    private int expertsPerOfficeWithAbsence;

    private String[] orderPriorities;
    private Duration[] orderDurations;

    private int customerAvailabilityTimeWindowInMinutes = 240;

    private int year;
    private int calendarWeek;
    private int[] weekWorkingDays;

    private double maxDistanceFromBackOfficeKm;

    private int totalExperts;
    private int totalOrders;

    private int avarageTravelDurationInMinutes = 30;
}
