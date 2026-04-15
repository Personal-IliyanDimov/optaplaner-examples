package org.imd.expertschedule.planner.domain.compare;

import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.ExpertSchedule;

import java.time.LocalDate;
import java.util.Comparator;

public final class ExpertScheduleStrengthComparator implements Comparator<ExpertSchedule> {

    private final Comparator<ExpertSchedule> esComparator = (es1, es2) -> {
        int result = Comparator
           .nullsFirst(Comparator.comparingLong(ExpertScheduleStrengthComparator::expertSkills))
           .compare(es1.getExpert(), es2.getExpert());

        if  (result != 0) {
            return result;
        }

        result = Comparator
                .nullsFirst(LocalDate::compareTo)
                .compare(es2.getDate(), es2.getDate());
        return result;
    };

    @Override
    public int compare(ExpertSchedule es1, ExpertSchedule es2) {
        return Comparator.nullsFirst(esComparator).compare(es1, es2);
    }

    private static long expertSkills(Expert expert) {
        if (expert == null || expert.getSkills() == null) {
            return 0L;
        }
        return expert.getSkills().size();
    }
}
