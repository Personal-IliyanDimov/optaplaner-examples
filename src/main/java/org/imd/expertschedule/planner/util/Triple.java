package org.imd.expertschedule.planner.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Triple<A,B, C> {
    public A first;
    public B second;
    public C third;
}
