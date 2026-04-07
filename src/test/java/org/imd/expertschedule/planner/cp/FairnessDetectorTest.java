package org.imd.expertschedule.planner.cp;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FairnessDetectorTest {

    private record Row(String group, Integer weight) {}

    private static UniConstraintCollector<Row, FairnessDetector.LoadBalanceData, FairnessDetector.LoadBalanceData> collector() {
        return FairnessDetector.loadBalance(Row::group, Row::weight);
    }

    @Test
    void emptyAccumulator_reportsZeroRoot() {
        UniConstraintCollector<Row, FairnessDetector.LoadBalanceData, FairnessDetector.LoadBalanceData> collector = collector();
        FairnessDetector.LoadBalanceData data = collector.supplier().get();

        assertEquals(BigInteger.ZERO, data.getSsdFromMean());
    }

    @Test
    void singleGroup_rootEqualsIntegerSqrtOfSquaredTotal() {
        UniConstraintCollector<Row, FairnessDetector.LoadBalanceData, FairnessDetector.LoadBalanceData> collector = collector();
        FairnessDetector.LoadBalanceData data = collector.supplier().get();
        var acc = collector.accumulator();

        acc.apply(data, new Row("A", 3));
        acc.apply(data, new Row("A", 4));

        assertEquals(BigInteger.valueOf(0), data.getSsdFromMean());
    }

    @Test
    void threeGroups_BestCase() {
        UniConstraintCollector<Row, FairnessDetector.LoadBalanceData, FairnessDetector.LoadBalanceData> collector = collector();
        FairnessDetector.LoadBalanceData data = collector.supplier().get();
        var acc = collector.accumulator();

        acc.apply(data, new Row("A", 3));
        acc.apply(data, new Row("B", 3));
        acc.apply(data, new Row("C", 3));

        assertEquals(BigInteger.valueOf(0), data.getSsdFromMean());
    }

    @Test
    void threeGroups_RegularCase() {
        UniConstraintCollector<Row, FairnessDetector.LoadBalanceData, FairnessDetector.LoadBalanceData> collector = collector();
        FairnessDetector.LoadBalanceData data = collector.supplier().get();
        var acc = collector.accumulator();

        acc.apply(data, new Row("A", 5));
        acc.apply(data, new Row("B", 3));
        acc.apply(data, new Row("C", 1));

        assertEquals(BigInteger.valueOf(8), data.getSsdFromMean());
    }

    @Test
    void fourGroups_BestCase() {
        UniConstraintCollector<Row, FairnessDetector.LoadBalanceData, FairnessDetector.LoadBalanceData> collector = collector();
        FairnessDetector.LoadBalanceData data = collector.supplier().get();
        var acc = collector.accumulator();

        acc.apply(data, new Row("A", 3));
        acc.apply(data, new Row("B", 3));
        acc.apply(data, new Row("C", 3));
        acc.apply(data, new Row("D", 3));

        assertEquals(BigInteger.valueOf(0), data.getSsdFromMean());
    }

    @Test
    void fourGroups_RegularCase() {
        UniConstraintCollector<Row, FairnessDetector.LoadBalanceData, FairnessDetector.LoadBalanceData> collector = collector();
        FairnessDetector.LoadBalanceData data = collector.supplier().get();
        var acc = collector.accumulator();

        acc.apply(data, new Row("A", 6));
        acc.apply(data, new Row("B", 3));
        acc.apply(data, new Row("C", 2));
        acc.apply(data, new Row("D", 1));

        assertEquals(BigInteger.valueOf(14), data.getSsdFromMean());
    }

    @Test
    void fourGroups_RawData_RealCase() {
        UniConstraintCollector<Row, FairnessDetector.LoadBalanceData, FairnessDetector.LoadBalanceData> collector = collector();
        FairnessDetector.LoadBalanceData data = collector.supplier().get();
        var acc = collector.accumulator();

        acc.apply(data, new Row("A", 1*60));
        acc.apply(data, new Row("A", 2*60));
        acc.apply(data, new Row("A", 3*60));

        acc.apply(data, new Row("B", 1*60));
        acc.apply(data, new Row("B", 2*60));

        acc.apply(data, new Row("C", 1*60));
        acc.apply(data, new Row("C", 1*60));

        acc.apply(data, new Row("D", 1*60));

        assertEquals(BigInteger.valueOf(14*60*60), data.getSsdFromMean());
    }

    @Test
    void undoRestoresMetric() {
        UniConstraintCollector<Row, FairnessDetector.LoadBalanceData, FairnessDetector.LoadBalanceData> collector = collector();
        FairnessDetector.LoadBalanceData data = collector.supplier().get();
        var acc = collector.accumulator();

        Runnable undo1 = acc.apply(data, new Row("A", 3));
        assertEquals(BigInteger.ZERO, data.getSsdFromMean());

        Runnable undo2 = acc.apply(data, new Row("B", 5));
        assertEquals(BigInteger.valueOf(2), data.getSsdFromMean());

        Runnable undo3 = acc.apply(data, new Row("C", 1));
        assertEquals(BigInteger.valueOf(8), data.getSsdFromMean());

        undo3.run();
        assertEquals(BigInteger.valueOf(2), data.getSsdFromMean());

        undo2.run();
        assertEquals(BigInteger.ZERO, data.getSsdFromMean());

        undo1.run();
        assertEquals(BigInteger.ZERO, data.getSsdFromMean());
    }
}
