package com.example.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExpertData {

    private long id;
    private String name;
    private LocationData backOfficeLocation;
    private List<String> skills;
    private List<AvailabilityData> availabilities;
    private List<AbsenceData> absences;
}
