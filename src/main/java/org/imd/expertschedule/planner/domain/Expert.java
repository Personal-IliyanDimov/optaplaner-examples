package org.imd.expertschedule.planner.domain;

import org.imd.expertschedule.planner.domain.refs.BackOfficeRef;
import org.imd.expertschedule.planner.domain.refs.ExpertRef;
import org.imd.expertschedule.planner.domain.time.Absence;
import org.imd.expertschedule.planner.domain.time.Availability;
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
