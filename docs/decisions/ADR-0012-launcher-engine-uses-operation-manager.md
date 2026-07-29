# ADR-0012 LauncherEngine uses OperationManager

## Статус

Accepted

## Контекст

После введения LaunchOperation, OperationManager и ExecutionStrategy в проекте существовали два способа
выполнения launch-сценария

- ранний TaskPipeline
- operation-модель

Существование двух параллельных путей увеличивало риск расхождения поведения

## Решение

LauncherEngine должен запускать длительные сценарии через OperationManager

Актуальный путь выполнения:

LauncherEngine -> OperationManager -> LaunchOperation -> ExecutionStrategy -> LauncherTask

## Последствия

- LauncherEngine больше не знает о TaskPipeline
- Последовательное выполнение задач переносится в SequentialExecutionStrategy