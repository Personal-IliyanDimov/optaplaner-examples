package com.example.expertschedule.planner.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class Expert {

    private String name;

    // Expert's back office location, from where travel starts.
    private Location backOfficeLocation;
    private Set<Skill> skills;

    // One schedule per day for this expert.
    private List<ExpertSchedule> schedules;
}
