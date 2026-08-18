# Контекст запуска

## Назначение

Контекст одного запуска лаунчера

## Принципы

- Не хранит сервисы
- Не управляет жизненным циклом объектов
- Хранит только артефакты выполнения
- Создается для текущего сценария запуска
- Передается в Operation через OperationManager
- Используется LauncherTask только во время выполнения execute(...)

## Артефакты

LoadManifestTask -> Manifest
Manifest -> LaunchInfo -> GameLaunchPlan
VerifyFilesTask -> VerificationPlan
BuildDownloadPlanTask -> DownloadPlan
DownloadFilesTask -> uses DownloadPlan
BuildGameLaunchPlanTask -> GameLaunchPlan
LaunchGameTask -> uses GameLaunchPlan