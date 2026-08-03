package com.launcher.core.architecture.verification;

import com.launcher.core.architecture.support.RecordVerificationService;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.result.Result;
import com.launcher.core.task.LauncherTask;
import com.launcher.core.verification.VerificationService;
import com.launcher.core.verification.VerifyFilesTask;
import com.launcher.core.verification.model.FileVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.FileEntry;
import com.launcher.model.manifest.LoaderInfo;
import com.launcher.model.manifest.Manifest;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VerifyFilesTaskTest {

    private LaunchContext getContext(boolean withManifest) {
        LaunchContext context = new LaunchContext(
                new LauncherConfiguration(
                        URI.create("currentPath"),
                        Path.of("")
                )
        );

        if (withManifest) {
            context.setManifest(getManifest());
        }

        return context;
    }

    private Manifest getManifest() {
        return new Manifest(
                "1.12.2",
                new LoaderInfo(
                        "Forge",
                        "1.0.0"
                ),
                List.of()
        );
    }

    private VerificationPlan getVerificationPlan() {
        return new VerificationPlan(
                List.of(
                        new FileVerificationResult(
                                new FileEntry(
                                        "test_path",
                                        "sha256",
                                        123L,
                                        "https://test.com"
                                ),
                                VerificationStatus.VALID
                        )
                )
        );
    }

    @Test
    void should_store_verification_plan_when_manifest_is_loaded() {
        //given
        VerificationPlan plan = getVerificationPlan();
        LaunchContext context = getContext(true);
        VerificationService verificationService = new RecordVerificationService(plan);
        LauncherTask task = new VerifyFilesTask(verificationService);

        //when
        Result result = task.execute(context);

        //then
        assertSame(plan, context.getVerificationPlan());
        assertTrue(result.success());
    }

    @Test
    void should_fail_when_manifest_is_not_loaded() {
        //given
        LaunchContext context = getContext(false);
        VerificationService verificationService = new RecordVerificationService(getVerificationPlan());
        LauncherTask task = new VerifyFilesTask(verificationService);

        //when
        Result result = task.execute(context);

        //then

        assertFalse(result.success());
    }
}
