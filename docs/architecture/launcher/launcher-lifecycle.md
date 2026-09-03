# Жизненный цикл лаунчера

## Цель

Описать жизненный цикл приложения `Launcher`, основные состояния системы и
взаимодействие между ключевыми компонентами

## Основная идея

`Launcher` представляет собой управляемую систему состояний

Каждый этап жизненного цикла приложения имеет четко определенную ответственность и не может
самостоятельно нарушать границы соседних этапов

## Таблица состояний

| Состояние                   | Ответственность                     |
|-----------------------------|-------------------------------------|
| `IDLE`                      | Launcher запущен и ожидает действия |
| `CHECKING_UPDATES`          | Проверка доступности новой версии   |
| `LOADING_MANIFEST`          | Получение описания сборки           |
| `VERIFYING_FILES`           | Проверка локального состояния       |
| `BUILDING_DOWNLOAD_PLAN`    | Построение плана загрузки           |
| `DOWNLOADING`               | Получение недостающих ресурсов      |
| `PREPARING_GAME`            | Подготовка окружения                |
| `EXTRACTING_NATIVES`        | Подготовка native artifacts         |
| `BUILDING_GAME_LAUNCH_PLAN` | Построение плана запуска игры       |
| `LAUNCHING`                 | Запуск игрового процесса            |
| `RUNNING`                   | Игра запущена                       |
| `FAILED`                    | Не удалось выполнять операцию       |

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

---

После успешной загрузки `manifest` `LauncherEngine` сохраняет в `LaunchContext` `Manifest` и `RuntimeLibrarySelection`,
затем переходит к `VERIFYING_FILES`

Если первичный `VerificationPlan` невалиден, `LauncherEngine` переходит к построению `DownloadPlan`

Если `VerificationPlan` содержит ресурсы, требующие загрузки, `LauncherEngine` запускает
`BUILD_DOWNLOAD_PLAN`, затем `DOWNLOAD_FILES`, затем повторно запускает `VERIFY_FILES`

Если построение `DownloadPlan`, загрузка ресурсов или повторная проверка завершается ошибкой, launcher
переходит в `FAILED`

Если повторная проверка успешна и новый `VerificationPlan` валиден, `LauncherEngine` запускает `PREPARE_DIRECTORIES`

Если `PREPARE_DIRECTORIES` завершается ошибкой, launcher переходит в `FAILED`

После успешной подготовки директорий `LauncherEngine` запускает `EXTRACT_NATIVES`

Если `EXTRACT_NATIVES` завершается ошибкой, launcher переходит в `FAILED`

После успешного `EXTRACT_NATIVES` `LauncherEngine` запускает `BUILD_GAME_LAUNCH_PLAN`

Во время построения `GameLaunchPlan` launcher создает `LaunchVariables` и применяет поддерживаемые
подстановки в `jvmArgs` и `gameArgs`

Также в процессе построения `GameLaunchPlan` строит `GameClasspath` из `RuntimeLibrarySelection.libraries`,
безопасно разрешает classpath entries относительно игровой директории, форматирует его и передает как значение
подстановки `${classpath}`

Если `RuntimeLibrarySelection.libraries` пустой, используется fallback `launchInfo.classpath`, который тоже проходит
через тот же механизм безопасного разрешения пути

Директория natives передается в `LaunchVariables` и может быть использована manifest metadata через
подстановку `${natives_directory}`

`GameLaunchPlanBuilder` выбирает Java executable через `JavaRuntimeSelector`

`JavaRuntimeSelector` использует `JavaExecutableReferenceResolver`, чтобы преобразовать
manifest-provided `javaExecutable` в `JavaExecutableReference`

`JavaRuntimeSelector` возвращает `JavaExecutableReference`, который сохраняет различие между command name и explicit
filesystem path

После выбора reference `GameLaunchPlanBuilder` выполняет readiness check через `JavaExecutableReadinessChecker` и
передает reference в `GameLaunchCommandBuilder`

`GameLaunchCommandBuilder` использует `JavaExecutableReference.value()` как первый элемент команды запуска

На текущем этапе application assembly использует `NoOpJavaExecutableReadinessChecker`, чтобы не ломать сценарий, где
manifest указывает command name `java`, а не filesystem path

Реальная filesystem-проверка доступна через `DefaultJavaExecutableReadinessChecker`, но ее подключение требует
отдельного решения о различии command name и explicit path

Если `Manifest` не содержит `LaunchInfo`, `BUILD_GAME_LAUNCH_PLAN` завершается ошибкой, launcher переходит в `FAILED`

Если `BUILD_GAME_LAUNCH_PLAN` завершается с ошибкой, launcher переходит в `FAILED`

После успешного `BUILD_GAME_LAUNCH_PLAN` `LauncherEngine` запускает `LAUNCH_GAME`

Во время `LAUNCH_GAME` `GameService` передает `GameLaunchPlan` в адаптер запуска процесса

Если `LAUNCH_GAME` завершается с ошибкой, launcher переходит в `FAILED`

После успешного `LAUNCH_GAME` launcher переходит в `RUNNING`

## Обоснование проекта

- `Bootstrap` отделен от `LauncherEngine` для изоляции процесса сборки зависимостей
- `LauncherEngine` не знает деталей конкретных `Operation`
- `LaunchContext` создается отдельно для каждой `Operation`
- `LauncherStateMachine` централизует изменение состояния приложения
- `OperationManager` управляет запуском `Operation`
- `ExecutionStrategy` определяет способ выполнения `LauncherTask` внутри `Operation`

Связанные документы

- [ADR-0003 Launcher Engine Responsibility](../../decisions/ADR-0003-launcher-engine-responsibility.md)
- [ADR-0005 Context Ownership (Deferred)](../../decisions/ADR-0005-immutable-domain-model.md)
- [RFC-0002 Session Lifecycle Management](../../rfc/RFC-0002-session-lifecycle-management.md)
- [RFC-0003 Task Cancellation](../../rfc/RFC-0003-task-lifecycle.md)

Диаграммы

- [launcher-lifecycle.puml](../../diagrams/launcher/launcher-lifecycle.puml)
- [launcher-user-journey.puml](../../diagrams/launcher/launcher-user-journey.puml)
- [launcher-startup.puml](../../diagrams/launcher/launcher-startup.puml)
- [launch-sequence.puml](../../diagrams/launcher/launch-sequence.puml)