# OperationManager migration

## Контекст

LauncherEngine ранее запускал задачи через старую модель TaskPipeline. После появления OperationManager,
LaunchOperation и ExecutionStrategy в проекте появились два конкурирующих пути выполнения длительных сценариев

## Что сделано

- Перевел LauncherEngine на запуск OperationType.LOAD_MANIFEST через OperationManager
- Сохранил создание LaunchContext внутри LauncherEngine для текущего единственного launch-сценария
- Добавил тестовое покрытие для нового поведения LauncherEngine
- Удалил прямую зависимость LauncherEngine от TaskPipeline
- Подготовил проект к удалению старой task-pipeline модели

## Что получилось хорошо

- Переход выполнен маленькими шагами
- Основной сценарий запуска стал соответствовать новой operation-модели
- Тесты подтвердили, что LauncherEngine теперь координирует операцию, а не исполняет задачи напрямую

## Что требует внимания

- Нужно решить судьбу LaunchPlan, DefaultLaunchPlan, TaskFactory, DefaultTaskFactory
- Нужно расширить жизненный цикл LaunchOperation
- Нужно добавить события уровня операции: start, completed, failed

## Следующие действия

1. Удалить или переосмыслить остатки старой launch-plan модели
2. Добавить базовый lifecycle в LaunchOperation
3. Добавить operation-level events
4. Расширить сценарий запуска после LOAD_MANIFEST
