package com.example.expertschedule.generator;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderData {

    private String code;
    private String customer;
    private List<String> requiredSkills;
}

