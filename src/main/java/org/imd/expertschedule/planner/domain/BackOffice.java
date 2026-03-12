package org.imd.expertschedule.planner.domain;

import org.imd.expertschedule.planner.domain.refs.BackOfficeRef;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BackOffice {

    private BackOfficeRef id;
    private String name;
    private Location location;
}
