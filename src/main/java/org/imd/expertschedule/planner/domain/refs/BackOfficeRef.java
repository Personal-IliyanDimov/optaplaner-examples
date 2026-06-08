package org.imd.expertschedule.planner.domain.refs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class BackOfficeRef implements Comparable<BackOfficeRef> {

    @NonNull
    private final long id;

    @Override
    public int compareTo(BackOfficeRef o) {
        return Long.compare(this.id, o.id);
    }
}
