package com.example.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExpertData {

    private String name;
    private LocationData backOfficeLocation;
    private List<String> skills;
}

