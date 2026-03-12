package org.imd.expertschedule.planner.domain;

import org.imd.expertschedule.planner.domain.refs.CustomerRef;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer {
    private CustomerRef id;
    private String name;
}

