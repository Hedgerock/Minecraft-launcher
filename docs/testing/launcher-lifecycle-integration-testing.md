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

## Первый целевой сценарий

Первый integration slice должен быть минимальным

Given

- launcher configuration с локальным manifest URI
- временная launcher directory
- manifest с минимальными ресурсами запуска
- контролируемый Java executable override

When

- `LauncherEngine` создается через application assembly
- вызывается `LauncherEngine.launch(...)`

Then

- возвращается `LaunchResult`
- результат отражает outcome всего launcher lifecycle
- тест не читает internal state machine напрямую

---

## Связанные документы

- [Жизненный цикл лаунчера](../architecture/launcher/launcher-lifecycle.md)
- [ADR-0046: Определить границу результата запуска Launcher](../decisions/ADR-0046-launcher-launch-result-boundary.md)
- [Правила написания тестов](../rules/test-guidelines.md)
