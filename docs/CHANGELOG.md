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

---

## Unreleased

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

### Removed

- Removed the deprecated TaskPipeline, as it is no longer used in the code
- Removed obsolete launch planning abstractions from the old task pipeline model: 
  LaunchPlan, DefaultLaunchPlan, TaskFactory, DefaultTaskFactory
- Removed obsolete HashVerifier abstraction