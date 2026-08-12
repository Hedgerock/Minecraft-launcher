# Launcher Lifecycle

## Цель 

Описать жизненный цикл приложения `Launcher`, основные состояния системы и
взаимодействие между ключевыми компонентами

## Основная идея

`Launcher` представляет собой управляемую систему состояний

Каждый этап жизненного цикла приложения имеет четко определенную ответственность и не может
самостоятельно нарушать границы соседних этапов

## Таблица состояний

| State                    | Responsibility                      |
|--------------------------|-------------------------------------|
| `IDLE`                   | Launcher запущен и ожидает действия |
| `CHECKING_UPDATES`       | Проверка доступности новой версии   |
| `LOADING_MANIFEST`       | Получение описания сборки           |
| `VERIFYING_FILES`        | Проверка локального состояния       |
| `BUILDING_DOWNLOAD_PLAN` | Построение плана загрузки           |
| `DOWNLOADING`            | Получение недостающих ресурсов      |
| `PREPARING_GAME`         | Подготовка окружения                |
| `LAUNCHING`              | Запуск игрового процесса            |
| `RUNNING`                | Игра запущена                       |
| `FAILED`                 | Не удалось выполнять операцию       |

## Инварианты

L-1

В один момент времени `Launcher` выполняет не более одной Operation для нового профиля

L-2

Каждая `Operation` использует собственный `LaunchContext`

L-3

`LauncherEngine` не содержит бизнес-логики отдельных `Operation`

L-4

Все переходы между состояниями происходят исключительно через `LauncherStateMachine`

L-5

Завершение `Operation` всегда приводит систему в согласованное состояние 
(`RUNNING`, `FAILED` или `IDLE` в зависимости от сценария)

После успешной загрузки `manifest` `LauncherEngine` переходит к `VERIFYING_FILES`

Если первичный `VerificationPlan` невалиден, `LauncherEngine` переходит к построению `DownloadPlan`

Если `VerificationPlan` содержит файлы, требующие загрузки, `LauncherEngine` запускает 
`BUILD_DOWNLOAD_PLAN`, затем `DOWNLOAD_FILES`, затем повторно запускает `VERIFY_FILES`

Если построение `DownloadPlan`, загрузка файлов или повторная проверка завершается ошибкой, launcher 
переходит в `FAILED`

Если повторная проверка успешна и новый `VerificationPlan` валиден, `LauncherEngine` запускает `PREPARE_DIRECTORIES`

Если `PREPARE_DIRECTORIES` завершается ошибкой, launcher переходит в `FAILED`

После успешной подготовки директорий `LauncherEngine` запускает `LAUNCH_GAME`

Если `LAUNCH_GAME` завершается с ошибкой, launcher переходит в `FAILED`

После успешного `LAUNCH_GAME` launcher переходит в `RUNNING`

## Обоснование Проекта

- `Bootstrap` отделен от `LauncherEngine` для изоляции процесса сборки зависимостей
- `LauncherEngine` не знает деталей конкретных `Operation`
- `LaunchContext` создается отдельно для каждой `Operation`
- `LauncherStateMachine` централизует изменение состояния приложения
- `OperationManager` управляет запуском `Operation`
- `ExecutionStrategy` определяет способ выполнения `LauncherTask` внутри `Operation`

Связанные документы

- ADR-0003 Launcher Engine Responsibility
- ADR-0005 Context Ownership (Deferred)
- RFC-0002 Session Lifecycle Management
- RFC-0003 Task Cancellation

Диаграммы

- launcher-lifecycle.puml
- launcher-user-journey.puml
- launcher-startup.puml
- launch-sequence.puml











