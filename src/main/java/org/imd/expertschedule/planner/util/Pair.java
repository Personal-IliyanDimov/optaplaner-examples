package org.imd.expertschedule.planner.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pair<A,B> {
    private final A left;
    private final B right;
}
