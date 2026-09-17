package com.launcher.core.architecture.resource.model;

import com.launcher.core.resource.model.PlannedResource;
import com.launcher.core.resource.model.ResourceSetPlan;
import com.launcher.model.manifest.ResourceEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceSetPlanTest {

    @Test
    void should_create_resource_set_plan() {
        //given
        PlannedResource firstResource = getResource("firstResource");
        PlannedResource secondResource = getResource("secondResource");
        List<PlannedResource> resources = List.of(firstResource, secondResource);

        //when
        ResourceSetPlan result = new ResourceSetPlan(resources);

        //then
        assertEquals(
                List.of(firstResource, secondResource),
                result.resources()
        );
    }

    @Test
    void should_reject_resources_mutation_from_accessor() {
        //given
        PlannedResource firstResource = getResource("firstResource");
        PlannedResource secondResource = getResource("secondResource");
        List<PlannedResource> resources = new ArrayList<>(List.of(firstResource));

        ResourceSetPlan resourceSetPlan = new ResourceSetPlan(resources);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> resourceSetPlan.resources().add(secondResource)
        );
    }

    @Test
    void should_create_immutable_resources() {
        //given
        PlannedResource firstResource = getResource("firstResource");
        PlannedResource secondResource = getResource("secondResource");
        List<PlannedResource> resources = new ArrayList<>(List.of(firstResource));

        ResourceSetPlan resourceSetPlan = new ResourceSetPlan(resources);

        //when
        resources.add(secondResource);

        //then
        assertEquals(
                List.of(firstResource),
                resourceSetPlan.resources()
        );
    }

    @Test
    void should_reject_null_resource() {
        //given
        List<PlannedResource> resources = new ArrayList<>();
        resources.add(null);

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ResourceSetPlan(resources)
        );

        assertEquals("resource", exception.getMessage());
    }

    @Test
    void should_reject_null_resources() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ResourceSetPlan(null)
        );

        assertEquals("resources", exception.getMessage());
    }

    private PlannedResource getResource(String resourceName) {
        return new PlannedResource(
                new ResourceEntry(
                        resourceName + ".jar",
                        resourceName + "-sha256",
                        123L,
                        "https://test-url.com/%s.jar".formatted(resourceName)
                ),
                Path.of(resourceName)
        );
    }
}
