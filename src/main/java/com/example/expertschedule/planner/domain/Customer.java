package com.example.expertschedule.planner.domain;

import com.example.expertschedule.planner.domain.refs.CustomerRef;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer {
    private CustomerRef id;
    private String name;
}

