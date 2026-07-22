# LaunchContext

## Назначение

Контекст одного запуска лаунчера

## Принципы

- Не хранит сервисы
- Не управляет жизненным циклом объектов
- Хранит только артефакты выполнения
- Передается последовательно через TaskPipeline

## Артефакты

LoadManifestTask -> Manifest

VerificationTask -> VerificationReport

DownloadPlanningTask -> DownloadPlan