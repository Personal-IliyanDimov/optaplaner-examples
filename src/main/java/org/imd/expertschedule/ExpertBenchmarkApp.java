package org.imd.expertschedule;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.slf4j.LoggerFactory;

import java.io.File;

public final class ExpertBenchmarkApp {

    private static final String BENCHMARK_CONFIG_RESOURCE =
            "org/imd/expertschedule/benchmark/expert-schedule-benchmark-config.xml";

    public static void main(String[] args) throws Exception {
        reducePlannerLogging();

        if (hasFlag(args, "--help") || hasFlag(args, "-h")) {
            System.out.println("Expert scheduling benchmark (OptaPlanner).");
            System.out.println("Writes reports under local/benchmark/ (see benchmark XML).");
            System.out.println("After the run, open index.html inside the printed folder.");
            System.out.println("Options: --help  this text");
            System.out.println("Gradle: ./gradlew benchmark");
            return;
        }

        PlannerBenchmarkFactory factory = PlannerBenchmarkFactory.createFromXmlResource(BENCHMARK_CONFIG_RESOURCE);
        PlannerBenchmark benchmark = factory.buildPlannerBenchmark();
        File reportFolder = benchmark.benchmark();

        System.out.println("Benchmark finished.");
        System.out.println("Report folder: " + reportFolder.getAbsolutePath());
    }

    private static void reducePlannerLogging() {
        var factory = LoggerFactory.getILoggerFactory();
        if (factory instanceof LoggerContext ctx) {
            ctx.getLogger("org.optaplanner").setLevel(Level.WARN);
        }
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String a : args) {
            if (flag.equalsIgnoreCase(a)) {
                return true;
            }
        }
        return false;
    }
}
