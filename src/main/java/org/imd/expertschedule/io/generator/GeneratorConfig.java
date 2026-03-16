package org.imd.expertschedule.io.generator;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuration for generating a planning dataset.
 * Use presets from {@link GeneratorConfigPresets} or build custom with {@link #builder()}.
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
    private int expertsWithAvailability;
    private int expertsWithAbsence;

    private String[] orderPriorities;
    private String[] orderDurations;

    private int calendarWeek;
    private int[] weekWorkingDays;
}
