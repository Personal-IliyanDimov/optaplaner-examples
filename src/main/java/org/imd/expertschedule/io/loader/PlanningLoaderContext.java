package org.imd.expertschedule.io.loader;

import org.imd.expertschedule.io.generator.GeneratorConfig;
import org.imd.expertschedule.planner.solution.SolutionContext;

public record PlanningLoaderContext(SolutionContext context, GeneratorConfig metadata) {}
