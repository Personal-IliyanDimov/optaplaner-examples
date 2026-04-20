package org.imd.expertschedule.planner.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Pair<A,B> {
    private final A left;
    private final B right;
}
