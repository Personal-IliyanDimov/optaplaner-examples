package org.imd.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExpertData {

    private long id;
    private String name;
    /** Id of the back office this expert belongs to. */
    private long backOfficeId;
    /** Location of the back office (convenience; can be derived from back office). */
    private LocationData backOfficeLocation;
    private List<String> skills;
    private List<AvailabilityData> availabilities;
    private List<AbsenceData> absences;
}
