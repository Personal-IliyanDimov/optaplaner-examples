package org.imd.expertschedule.io.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.imd.expertschedule.io.PlanningSolutionAssembly;
import org.imd.expertschedule.io.loader.ExpertPlanningSolutionLoader;
import org.imd.expertschedule.planner.cp.ExpertPlanningConstraintConfiguration;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.imd.expertschedule.planner.solution.SolutionInitializer;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

public class ExpertPlanningSolutionFileIO implements SolutionFileIO<ExpertPlanningSolution> {

    @Override
    public String getInputFileExtension() {
        return "json";
    }

    @Override
    public ExpertPlanningSolution read(final File inputSolutionFile) {
        try {
            var loaderContext = new ExpertPlanningSolutionLoader().loadBundle(inputSolutionFile.toPath());
            var plannerParameters = PlanningSolutionAssembly.plannerParametersFromMetadata(loaderContext.metadata());
            return new SolutionInitializer().initialize(plannerParameters,
                new ExpertPlanningConstraintConfiguration(), loaderContext.context());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read dataset: " + inputSolutionFile, e);
        }
    }

    @Override
    public void write(final ExpertPlanningSolution solution, final File outputSolutionFile) {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        ObjectNode root = mapper.createObjectNode();
        root.put("score", solution.getScore() != null ? solution.getScore().toString() : null);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(outputSolutionFile, root);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
