package com.launcher.core.architecture.resource;

import com.launcher.core.resource.ResourcePathResolver;
import com.launcher.core.resource.ResourceSetConflictException;
import com.launcher.core.resource.ResourceSetPlanner;
import com.launcher.core.resource.SafeResourcePathResolver;
import com.launcher.core.resource.UnsafeResourcePathException;
import com.launcher.core.resource.model.PlannedResource;
import com.launcher.core.resource.model.ResourceSetPlan;
import com.launcher.model.manifest.ResourceEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceSetPlannerTest {
    private final ResourcePathResolver resolver = new SafeResourcePathResolver();
    private final ResourceSetPlanner resourceSetPlanner = new ResourceSetPlanner(resolver);

    private static final Path DEFAULT_GAME_DIRECTORY_PATH = Path.of("default_game_directory");

    @Test
    void should_not_mutate_initial_resource_entries() {
        //given
        ResourceEntry firstResource = getResourceEntry("firstResource");
        ResourceEntry secondResource = getResourceEntry("firstResource");
        ResourceEntry thirdResource = getResourceEntry("firstResource");

        List<ResourceEntry> resourceEntries = new ArrayList<>(List.of(
                firstResource,
                secondResource,
                thirdResource
        ));

        //when
        ResourceSetPlan result = resourceSetPlanner.plan(
                resourceEntries,
                DEFAULT_GAME_DIRECTORY_PATH
        );

        //then
        List<PlannedResource> expectedPlannedResources = getPlannedResources(
                firstResource
        );

        assertEquals(expectedPlannedResources, result.resources());
        assertEquals(
                List.of(
                        firstResource,
                        secondResource,
                        thirdResource
                ),
                resourceEntries
        );
    }

    @Test
    void should_reject_conflicting_resources_when_paths_resolve_to_same_target() {
        //given
        ResourceEntry firstResource = getResourceEntry("libraries/firstResource", "first-sha256");
        ResourceEntry secondResource = getResourceEntry("libraries/./firstResource", "second-sha256");

        //when & then
        ResourceSetConflictException exception = assertThrows(
                ResourceSetConflictException.class,
                () -> resourceSetPlanner.plan(
                        List.of(firstResource, secondResource),
                        DEFAULT_GAME_DIRECTORY_PATH
                )
        );

        Path expectedPath = resolver.resolve(DEFAULT_GAME_DIRECTORY_PATH, firstResource.path());

        String expectedMessage = "Conflicting resources for target '%s': '%s' and '%s'".formatted(
                expectedPath,
                firstResource.path(),
                secondResource.path()
        );

        assertEquals(
                expectedMessage,
                exception.getMessage()
        );

        assertSame(
                firstResource,
                exception.getFirstResource()
        );

        assertSame(
                secondResource,
                exception.getConflictingResource()
        );

        assertEquals(
                expectedPath,
                exception.getTargetPath()
        );
    }

    @Test
    void should_preserve_unique_resource_order_when_duplicates_are_interleaved() {
        //given
        ResourceEntry firstResource = getResourceEntry("firstResource");
        ResourceEntry secondResource = getResourceEntry("secondResource");
        ResourceEntry thirdResource = getResourceEntry("firstResource");
        ResourceEntry fourthResource = getResourceEntry("thirdResource");
        ResourceEntry fifthResource = getResourceEntry("secondResource");
        ResourceEntry sixthResource = getResourceEntry("thirdResource");

        //when
        ResourceSetPlan result = resourceSetPlanner.plan(
                List.of(
                        firstResource,
                        secondResource,
                        thirdResource,
                        fourthResource,
                        fifthResource,
                        sixthResource
                ),
                DEFAULT_GAME_DIRECTORY_PATH
        );

        //then
        List<PlannedResource> expectedPlannedResources = getPlannedResources(
                firstResource,
                secondResource,
                fourthResource
        );

        assertEquals(
                expectedPlannedResources,
                result.resources()
        );

        assertSame(firstResource, result.resources().get(0).resource());
        assertSame(secondResource, result.resources().get(1).resource());
        assertSame(fourthResource, result.resources().get(2).resource());
    }

    @Test
    void should_fail_when_url_has_different_value() {
        //given
        ResourceEntry firstResource = getResourceEntryWithDifferentUrl("https://first-url.com");
        ResourceEntry secondResource = getResourceEntryWithDifferentUrl("https://second-url.com");

        //when & then
        ResourceSetConflictException exception = assertThrows(
                ResourceSetConflictException.class,
                () -> resourceSetPlanner.plan(
                        List.of(firstResource, secondResource),
                        DEFAULT_GAME_DIRECTORY_PATH
                )
        );

        assertSame(
                firstResource,
                exception.getFirstResource()
        );

        assertSame(
                secondResource,
                exception.getConflictingResource()
        );

        Path expectedPath = resolver.resolve(DEFAULT_GAME_DIRECTORY_PATH, firstResource.path());

        assertEquals(
                expectedPath,
                exception.getTargetPath()
        );
    }

    @Test
    void should_fail_when_size_has_different_value() {
        //given
        ResourceEntry firstResource = getResourceEntryWithDifferentSize(123L);
        ResourceEntry secondResource = getResourceEntryWithDifferentSize(321L);

        //when & then
        ResourceSetConflictException exception = assertThrows(
                ResourceSetConflictException.class,
                () -> resourceSetPlanner.plan(
                        List.of(firstResource, secondResource),
                        DEFAULT_GAME_DIRECTORY_PATH
                )
        );

        assertSame(
                firstResource,
                exception.getFirstResource()
        );

        assertSame(
                secondResource,
                exception.getConflictingResource()
        );

        Path expectedPath = resolver.resolve(DEFAULT_GAME_DIRECTORY_PATH, firstResource.path());

        assertEquals(
                expectedPath,
                exception.getTargetPath()
        );
    }

    @Test
    void should_fail_when_sha256_has_different_value() {
        //given
        ResourceEntry firstResource = getResourceEntryWithDifferentSha256("first-sha256");
        ResourceEntry secondResource = getResourceEntryWithDifferentSha256("second-sha256");

        //when & then
        ResourceSetConflictException exception = assertThrows(
                ResourceSetConflictException.class,
                () -> resourceSetPlanner.plan(
                        List.of(firstResource, secondResource),
                        DEFAULT_GAME_DIRECTORY_PATH
                )
        );

        assertSame(
                firstResource,
                exception.getFirstResource()
        );

        assertSame(
                secondResource,
                exception.getConflictingResource()
        );

        Path expectedPath = resolver.resolve(DEFAULT_GAME_DIRECTORY_PATH, firstResource.path());

        assertEquals(
                expectedPath,
                exception.getTargetPath()
        );
    }

    @Test
    void should_reject_unsafe_path() {
        //given
        ResourceEntry firstResource = getResourceEntry("../libraries/example");
        ResourceEntry secondResource = getResourceEntry("libraries/./example");

        //when & then
        UnsafeResourcePathException exception = assertThrows(
                UnsafeResourcePathException.class,
                () -> resourceSetPlanner.plan(
                        List.of(firstResource, secondResource),
                        DEFAULT_GAME_DIRECTORY_PATH
                )
        );

        assertEquals(
                "Resource path escapes base directory",
                exception.getMessage()
        );
    }

    @Test
    void should_merge_compatible_resources_when_paths_resolve_to_same_target() {
        //given
        ResourceEntry firstResource = getResourceEntry("libraries/example");
        ResourceEntry secondResource = getResourceEntry("libraries/./example");

        //when
        ResourceSetPlan result = resourceSetPlanner.plan(
                List.of(firstResource, secondResource),
                DEFAULT_GAME_DIRECTORY_PATH
        );

        //then
        assertEquals(
                List.of(
                        new PlannedResource(
                                firstResource,
                                resolver.resolve(DEFAULT_GAME_DIRECTORY_PATH, firstResource.path())
                        )
                ),
                result.resources()
        );

        assertSame(
                firstResource,
                result.resources().getFirst().resource()
        );
    }

    @Test
    void should_keep_first_resource_when_duplicates_are_compatible() {
        //given
        ResourceEntry firstResource = getResourceEntry("firstResource");
        ResourceEntry secondResource = getResourceEntry("firstResource");

        //when
        ResourceSetPlan result = resourceSetPlanner.plan(
                List.of(firstResource, secondResource),
                DEFAULT_GAME_DIRECTORY_PATH
        );

        //then
        List<PlannedResource> expectedPlannedResource = getPlannedResources(firstResource);

        assertEquals(
                expectedPlannedResource,
                result.resources()
        );

        assertSame(
                firstResource,
                result.resources().getFirst().resource()
        );
    }

    @Test
    void should_return_empty_plan_when_resources_are_empty() {
        //given & when
        ResourceSetPlan result = resourceSetPlanner.plan(List.of(), DEFAULT_GAME_DIRECTORY_PATH);

        //then
        assertEquals(List.of(), result.resources());
    }

    @Test
    void should_reject_null_game_directory() {
        //when & then
        assertThrows(
                NullPointerException.class,
                () -> resourceSetPlanner.plan(List.of(), null)
        );
    }

    @Test
    void should_reject_null_resource() {
        //given
        List<ResourceEntry> resources = new ArrayList<>();
        resources.add(null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> resourceSetPlanner.plan(resources, DEFAULT_GAME_DIRECTORY_PATH));
    }

    @Test
    void should_reject_null_resources() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> resourceSetPlanner.plan(null, DEFAULT_GAME_DIRECTORY_PATH)
        );

        assertEquals("resources", exception.getMessage());
    }

    @Test
    void should_reject_null_resource_path_resolver() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ResourceSetPlanner(null)
        );

        assertEquals("resourcePathResolver", exception.getMessage());
    }

    private ResourceEntry getResourceEntryWithDifferentUrl(
            String url
    ) {
        return getResourceEntry(
                "resource.jar",
                "sha256",
                123L,
                url
        );
    }

    private ResourceEntry getResourceEntryWithDifferentSize(
            long size
    ) {
        return getResourceEntry(
                "resource.jar",
                "sha256",
                size,
                "https://testurl.com/resource.jar"
        );
    }

    private ResourceEntry getResourceEntryWithDifferentSha256(
            String sha256
    ) {
        return getResourceEntry(
                "resource.jar",
                sha256,
                123L,
                "https://testurl.com/resource.jar"
        );
    }

    private ResourceEntry getResourceEntry(String resource) {
        return getResourceEntry(
                resource,
                "sha256",
                123L,
                "https://testurl.com/resource.jar"
        );
    }

    private ResourceEntry getResourceEntry(String resource, String sha256) {
        return getResourceEntry(
                resource,
                sha256,
                123L,
                "https://testurl.com/resource.jar"
        );
    }

    private ResourceEntry getResourceEntry(
            String resource,
            String sha256,
            long size,
            String url
    ) {
        return new ResourceEntry(
                resource,
                sha256,
                size,
                url
        );
    }

    private List<PlannedResource> getPlannedResources(ResourceEntry... resources) {
        return Arrays.stream(resources)
                .map(resource -> new PlannedResource(
                       resource,
                       resolver.resolve(DEFAULT_GAME_DIRECTORY_PATH, resource.path())
                ))
                .toList();
    }
}
