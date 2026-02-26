package com.example.expertschedule.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class TestDataGenerator {

    public static void main(String[] args) throws IOException {
        Path dataDir = Path.of("data");
        Files.createDirectories(dataDir);

        // Skills
        SkillData electrical = new SkillData();
        electrical.setName("Electrical");
        SkillData plumbing = new SkillData();
        plumbing.setName("Plumbing");
        SkillData networking = new SkillData();
        networking.setName("Networking");
        List<SkillData> skills = Arrays.asList(electrical, plumbing, networking);

        // Customers
        CustomerData customerX = new CustomerData();
        customerX.setName("Customer X");
        customerX.setLocation(location(1, 1));

        CustomerData customerY = new CustomerData();
        customerY.setName("Customer Y");
        customerY.setLocation(location(9, 1));

        CustomerData customerZ = new CustomerData();
        customerZ.setName("Customer Z");
        customerZ.setLocation(location(5, 5));

        List<CustomerData> customers = Arrays.asList(customerX, customerY, customerZ);

        // Experts
        ExpertData expertAlice = new ExpertData();
        expertAlice.setName("Alice");
        expertAlice.setBackOfficeLocation(location(0, 0));
        expertAlice.setSkills(Arrays.asList("Electrical", "Networking"));

        ExpertData expertBob = new ExpertData();
        expertBob.setName("Bob");
        expertBob.setBackOfficeLocation(location(10, 0));
        expertBob.setSkills(Arrays.asList("Plumbing"));

        List<ExpertData> experts = Arrays.asList(expertAlice, expertBob);

        // Orders
        OrderData order1 = new OrderData();
        order1.setCode("ORDER-1");
        order1.setCustomer("Customer X");
        order1.setRequiredSkills(Arrays.asList("Electrical"));

        OrderData order2 = new OrderData();
        order2.setCode("ORDER-2");
        order2.setCustomer("Customer Y");
        order2.setRequiredSkills(Arrays.asList("Plumbing"));

        OrderData order3 = new OrderData();
        order3.setCode("ORDER-3");
        order3.setCustomer("Customer Z");
        order3.setRequiredSkills(Arrays.asList("Electrical", "Networking"));

        List<OrderData> orders = Arrays.asList(order1, order2, order3);

        ObjectMapper mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);

        mapper.writeValue(dataDir.resolve("skills.json").toFile(), skills);
        mapper.writeValue(dataDir.resolve("customers.json").toFile(), customers);
        mapper.writeValue(dataDir.resolve("experts.json").toFile(), experts);
        mapper.writeValue(dataDir.resolve("orders.json").toFile(), orders);

        System.out.println("Test data generated into directory: " + dataDir.toAbsolutePath());
    }

    private static LocationData location(double latitude, double longitude) {
        LocationData location = new LocationData();
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }
}

