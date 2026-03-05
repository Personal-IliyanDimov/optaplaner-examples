package com.example.expertschedule.planner.domain;

import com.example.expertschedule.planner.domain.refs.BackOfficeRef;
import com.example.expertschedule.planner.domain.refs.ExpertRef;
import com.example.expertschedule.planner.domain.time.Absence;
import com.example.expertschedule.planner.domain.time.Availability;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class Expert {

    private ExpertRef id;
    private String name;

    private BackOfficeRef backOfficeRef;
    private Location backOfficeLocation;
    private Set<Skill> skills;

    private List<Availability> availabilities;
    private List<Absence> absences;
}
