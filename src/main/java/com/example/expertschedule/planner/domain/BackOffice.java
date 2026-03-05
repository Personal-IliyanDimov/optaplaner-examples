package com.example.expertschedule.planner.domain;

import com.example.expertschedule.planner.domain.refs.BackOfficeRef;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BackOffice {

    private BackOfficeRef id;
    private String name;
    private Location location;
}
