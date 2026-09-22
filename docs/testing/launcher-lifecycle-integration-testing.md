# Интеграционное тестирование launcher lifecycle

## Назначение

Документ фиксирует границу integration tests для launcher lifecycle

Эти тесты должны подтверждать, что собранный launcher способен пройти выбранный lifecycle scenario через production
wiring и вернуть наблюдаемый результат запуска

Основной observable result для таких тестов — `LaunchResult`

---

## Основная идея

Launcher lifecycle integration test проверяет не отдельную operation, а связку компонентов вокруг
`LauncherEngine.launch(...)`

Тест должен проверять поведение на внешней границе launcher lifecycle, а не внутренние детали `LauncherStateMachine`,
`OperationManager` или отдельных `LaunchOperation`

---

## Что проверяется

Integration test может проверять

- создание `LauncherEngine` через application assembly
- выполнение `LauncherEngine.launch(...)`
- успешный или неуспешный `LaunchResult`
- финальное состояние, возвращенное через `LaunchResult`
- корректную связку manifest loading, verification, download, native extraction, launch planning и game launch в рамках
  выбранного сценария
- production wiring между `launcher-app`, `launcher-core` и adapter modules
- generic failure context, возвращаемый через `LaunchResult` для неуспешного lifecycle scenario

---

## Что не проверяется

Integration test не должен превращаться в полный end-to-end тест реального Minecraft запуска

В область первого integration slice не входят

- реальная внешняя сеть
- реальный Minecraft client
- UI presentation
- retry policy
- recovery behavior
- structured operation diagnostics
- performance testing
- concurrency behavior
- Java installation discovery
- Java runtime fallback policy

---

## Граница test doubles

Для стабильности integration tests допускается использовать контролируемые внешние границы

Например

- локальный HTTP server вместо внешней сети
- временную launcher directory через `@TempDir`
- fake Java executable через `javaExecutableOverride`
- минимальные test resources вместо реальных game assets

Test doubles должны заменять только внешнюю среду

Они не должны подменять production lifecycle orchestration внутри `LauncherEngine`

---

## Ограничения process boundary

Launcher lifecycle integration test может доходить до запуска внешнего процесса

Такой тест должен учитывать, что successful launch означает успешный старт процесса, а не его завершение

Если тест использует fake executable, он должен явно разделять сценарии

- Java runtime version detection
- game process launch

Для Java runtime version detection fake executable должен поддерживать вызов с аргументом `-version` и возвращать output,
который может быть разобран `JavaRuntimeVersionDetector`

Для game process launch fake executable должен завершиться быстро и не удерживать рабочую директорию launcher

На Windows внешний процесс может блокировать удаление `@TempDir`, если его working directory находится внутри временной
директории теста

Поэтому fake executable для game process launch должен либо завершаться до удаления временной директории теста,
либо переходить в директорию за пределами launcher directory перед завершением

Если production launcher считает процесс успешно запущенным сразу после `ProcessBuilder.start()`, тест не должен ожидать
завершения процесса как часть `LaunchResult`

В таких сценариях допустимо использовать небольшой synchronization marker, чтобы убедиться, что fake process успел
выполнить тестовую ветку и освободить временную директорию

---

## Первый целевой сценарий

Первый integration slice должен быть минимальным

Given

- launcher configuration с локальным manifest URI
- временная launcher directory
- manifest с минимальными ресурсами запуска
- контролируемый Java executable override, который поддерживает `-version` и game launch branch

When

- `LauncherEngine` создается через application assembly
- вызывается `LauncherEngine.launch(...)`

Then

- возвращается `LaunchResult`
- результат отражает outcome всего launcher lifecycle
- тест не читает internal state machine напрямую
- fake game process успевает выполнить synchronization marker и не удерживает launcher directory

---

## Связанные документы

- [Жизненный цикл лаунчера](../architecture/launcher/launcher-lifecycle.md)
- [ADR-0046: Определить границу результата запуска Launcher](../decisions/records/ADR-0046-launcher-launch-result-boundary.md)
- [Правила написания тестов](../rules/test-guidelines.md)
