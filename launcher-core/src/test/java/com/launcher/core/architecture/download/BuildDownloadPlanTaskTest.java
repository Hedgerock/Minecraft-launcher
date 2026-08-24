package com.launcher.core.architecture.download;

import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.BuildDownloadPlanTask;
import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.result.Result;
import com.launcher.core.verification.model.ResourceVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.ResourceEntry;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildDownloadPlanTaskTest {
    private final DownloadPlanBuilder builder = new DownloadPlanBuilder();

    private LaunchContext getContext(boolean withVerificationPlan) {
        LaunchContext launchContext = new LaunchContext(
                new LauncherConfiguration(
                        URI.create("currentPath"),
                        Path.of("")
                )
        );

        if (withVerificationPlan) {
            launchContext.setVerificationPlan(getVerificationPlan());
        }

        return launchContext;
    }

    private VerificationPlan getVerificationPlan() {
        return new VerificationPlan(
            List.of(
                    new ResourceVerificationResult(getResourceEntry(), VerificationStatus.MISSING)
            )
        );
    }

    private ResourceEntry getResourceEntry() {
        return new ResourceEntry(
                "missing-path.jar",
                "sha256-missing-path.jar",
                321L,
                "https://test-url.com/missing-path.jar"
        );
    }

    @Test
    void should_store_download_plan_when_verification_plan_exists() {
        //given
        LaunchContext context = getContext(true);
        BuildDownloadPlanTask planTask = new BuildDownloadPlanTask(builder);
        List<ResourceEntry> expectedFiles = List.of(getResourceEntry());

        //when
        Result result = planTask.execute(context);

        //then
        assertTrue(result.success());
        assertNotNull(context.getDownloadPlan());

        List<ResourceEntry> actualFiles = context.getDownloadPlan().resources();

        assertEquals(expectedFiles, actualFiles);
    }

    @Test
    void should_fail_when_verification_plan_is_missing() {
        //given
        LaunchContext context = getContext(false);
        BuildDownloadPlanTask planTask = new BuildDownloadPlanTask(builder);

        //when
        Result result = planTask.execute(context);

        //then
        assertFalse(result.success());
        assertNull(context.getDownloadPlan());
    }

}
