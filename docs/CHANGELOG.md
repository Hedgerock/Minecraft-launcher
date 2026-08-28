## Unreleased

### Added

- Added launch argument placeholder resolution for game launch commands
- Added classpath entries to launch info manifest metadata
- Added classpath placeholder resolution for game launch commands 
- Added configurable Java executable metadata for game launch commands
- Added minimal manifest library entries metadata
- Added real HTTP GET support for manifest loading
- Added structured download failure context
- Documented the decision to use manifest resources as the verification flow source
- Documented the minimal library metadata scope
- Documented resource path safety rules
- Added safe resource path resolution through a shared `ResourсePathResolver`
- Documented the boundary between raw library manifest metadata and selected runtime library entries
- Added a runtime library selection between manifest JSON library metadata and selected `LibraryEntry` models
- Added `LibraryArtifactMetadata` to separate downloadable library artifact metadata from runtime library metadata
- Documented the runtime environment boundary for future library selection
- Added minimal runtime environment model for library selection
- Added `RuntimeEnvironmentProvider` and system-based runtime environment detection
- Added minimal OS-specific library rules for runtime library selection
- Added manifest JSON mapping for OS-specific library rules
- Documented library classifiers and natives boundary for library selection
- Added `LibraryClassifiersMetadata` for library classifier artifacts
- Added `LibraryNativesMetadata` for native classifier mapping

### Changed

- Changed game classpath building to prefer manifest libraries with launchInfo classpath fallback
- Accepted the download planning architecture decision
- Clarified downloader error handling documentation
- Documented the current download progress event limitation
- Documented the planned library metadata strategy
- Documented the planned library verification strategy
- Documented manifest resource projection
- Changed verification and download plan contracts to use resource entries
- Verification flow now uses manifest resources as its source
- Verification and download services now resolve manifest resource paths through the shared safe resolver
- Changed game classpath building to resolve classpath entries through the shared safe `ResourcePathResolver`
- Moved `DirectoryProvider` ownership from `LauncherServices` to application assembly
- Changed manifest JSON mapping to convert library JSON into `RuntimeLibraryMetadata` before 
  selecting runtime `LibraryEntry` models
- Changed `RuntimeLibraryMetadata` to reference `LibraryArtifactMetadata` instead of storing artifact fields directly
- Changed runtime library selection to receive `RuntimeEnvironment` as an explicit input
- Changed runtime library selection to filter libraries by OS-specific rules
- Changed `RuntimeLibraryMetadata` to include classifiers and natives metadata

---

## v0.3.0 – Launch Runtime Stabilization

### Added

- Added operation lifecycle events for operation start, successful completion and failure
- Added LaunchOperation lifecycle contract documentation
- Added factory support for VERIFY_FILES operation
- Added status helper methods to VerificationPlan
- Added FileVerifier contract for verifying individual manifest files
- Added HashService.sha256(Path) for file hash calculation
- Added FileMetadataReader contract for read-only file metadata access
- Added module boundary documentation for launcher-core dependency policy
- Added launcher-core module boundary architecture test scaffold
- Added VerifyFilesTask for executing file verification through VerificationService
- Added DownloadPlan and DownloadPlanBuilder for deriving downloads from verification results
- Added BuildDownloadPlanTask for building and storing DownloadPlan in LaunchContext
- Added BUILD_DOWNLOAD_PLAN operation for running download planning through OperationManager
- LauncherEngine now builds DownloadPlan when verification detects files to download
- Added DOWNLOAD_FILES operation for running download execution through OperationManager
- Added launcher-downloader DefaultDownloadService scaffold
- Application assembly now wires DownloadService into the operation factory
- Added downloader preparation with FileDownloader and DefaultDownloadService wiring
- LauncherEngine now runs DOWNLOAD_FILES after building DownloadPlan
- LauncherEngine now verifies files again after download execution
- Added specialized download progress events for DOWNLOAD_FILES execution
- Added launcher state transition tests for download execution flow
- Added DownloadException in the launcher-downloader for file download failures
- Added downloaded file size validation after each file download
- Added GameLaunchPlan for describing game launch input
- Added BUILD_GAME_LAUNCH_PLAN operation before game launch
- Added GameProcessLauncher abstraction for starting game processes
- Added ProcessBuilder-based game process launcher
- DefaultGameService now launches a process from GameLaunchPlan
- Manifest now validates a file list before verification and download planning
- FileEntry now validates path, hash, size and download URL metadata
- Manifest-related models now validate the required file, loader and launch metadata
- VerificationPlan and DownloadPlan now protect their file lists from null and external mutation

### Changed

- Migrated LauncherEngine to launch operations via OperationManager
- LauncherEngine no longer uses the old execution path via TaskPipeline
- Validated the new behavior with tests for LauncherEngine and the operation execution flow
- OperationManager is now the main entry point for long-running launcher operations
- OperationResult now stores failure messages instead of printing them as side effects
- OperationFailedEvent now includes a failure message from OperationResult
- LaunchOperation now converts finalizeOperation(...) exceptions into failed operation results
- DefaultFileVerifier now uses FileMetadataReader instead of mutable FileStorage
- Updated module dependency diagram to reflect launcher-app as the composition root
- Moved concrete application assembly and infrastructure factories from launcher-core to launcher-app
- Enabled launcher-core module boundary architecture test after moving concrete factories
- Moved remaining application assembly contracts and service bundles from launcher-core to launcher-app
- Moved verification result contract into launcher-core
- LauncherEngine now runs VERIFY_FILES after LOAD_MANIFEST during launch
- LaunchContext now includes a DownloadPlan
- Aligned download flow and download events documentation with the implemented downloader behavior
- Aligned module boundary documentation with the current launcher-core and launcher-app responsibilities
- Marked early ADRs as historical where implementation details changed after the operation model migration
- Aligned verification flow documentation with the current VerificationPlan terminology
- Marked outdated verification diagrams as historical architecture snapshots
- Aligned launcher lifecycle documentation with the prepare directories launch flow
- Aligned launcher lifecycle documentation with the launch game flow
- LauncherEngine now builds GameLaunchPlan after preparing directories and before launching the game
- GameService now receives GameLaunchPlan instead of launching without explicit launch input
- LaunchInfo now validates launch metadata before building GameLaunchPlan
- BuildGameLaunchPlanTask now fails when manifest launch info is missing

### Removed

- Removed the deprecated TaskPipeline, as it is no longer used in the code
- Removed obsolete launch planning abstractions from the old task pipeline model:
  LaunchPlan, DefaultLaunchPlan, TaskFactory, DefaultTaskFactory
- Removed obsolete HashVerifier abstraction

## v0.2.0

### Added

- Session lifecycle
- Verification report builder
- Application assembly
- Infrastructure factories

### Changed

- LaunchContext decomposition started
- Launcher bootstrap simplified

---

## Phase 1

### Added

- Architecture foundation
- RFC documentation
- ADR documentation
- Architecture diagrams
- Lifecycle documentation
- Sequence examples
- Architecture review process
- Documentation guidelines

### Result

Completed architectural foundation of the launcher project

---

## Phase 2

### Added

- Completed Phase 2 concurrency architecture
- Introduced concurrent execution model
- Added project glossary
- Added architecture snapshot
- Added future architecture roadmap
- Added MIT license

### Changed

- Refined operation lifecycle
- Defined execution strategy responsibilities
- Defined operation completion semantics
- Expanded architectural documentation

### Deferred/Proposed

Documentation future architectural extensions through ADR-0008-ADR-0011

---

## V1 Implementation

## Added

- Introduced Operation architecture
- Introduced OperationManager
- Introduced OperationFactory
- Introduced ExecutionStrategy
- Added Architecture Smoke Tests
- Added architecture testing documentation