package com.example.expertschedule.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class Expert {

    private String name;

    // Expert's back office location, from where travel starts.
    private Location backOfficeLocation;
    private Set<Skill> skills;

}
