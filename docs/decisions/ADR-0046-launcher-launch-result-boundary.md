[← Назад к списку решений](README.md)

# ADR-0046: Определить границу результата запуска Launcher

## Статус

Accepted

> Примечание: решение реализовано в итерации `feat(core): add launcher launch result`

---

## Контекст

После перехода на operation-модель `LauncherEngine` отвечает за координацию lifecycle flow и делегирует выполнение
конкретных операций через `OperationManager`

Актуальная ответственность `LauncherEngine` описана в [ADR-0012](ADR-0012-launcher-engine-uses-operation-manager.md)

Историческое решение [ADR-0003](ADR-0003-launcher-engine-responsibility.md) больше не является актуальным source of truth
для внутреннего выполнения задач

На момент принятия решения `LauncherEngine.launch(...)` выполняет lifecycle flow, переводит `LauncherStateMachine` между
состояниями и останавливается при failed operation

Однако метод не возвращает observable result наружу

Это достаточно для текущего CLI skeleton, но становится слабой границей для будущих сценариев

- UI должен понимать, завершился ли запуск успешно
- integration tests должны иметь stable observable result
- external launcher boundary не должна читать внутренний state machine напрямую
- будущая error presentation не должна требовать доступа к internal operation lifecycle

При этом в проекте уже зафиксирована отдельная граница operation failure diagnostics в [ADR-0040](ADR-0040-operation-failure-diagnostics-boundary.md)

Поэтому результат запуска не должен становиться replacement для `OperationResult`, structured operation diagnostics или
domain-specific error model

---

## Решение

`LauncherEngine.launch(...)` должен возвращать минимальный результат запуска

Результат запуска должен описывать outcome всего launcher lifecycle, а не детали отдельной operation

Минимальная граница результата запуска

- successful launch
- failed launch
- final launcher state

`LauncherEngine` остается coordinator lifecycle flow

`LauncherEngine` не должен

- Выполнять бизнес-логику операций
- Интерпретировать domain-specific failure reasons
- Возвращать Java-specific, download-specific или verification-specific error model
- Заменять `OperationResult`
- Управлять UI presentation

`OperationManager` и `LaunchOperation` продолжают возвращать `OperationResult`

`LauncherEngine` использует `OperationResult` только для принятия lifecycle decision и формирования минимального
launch outcome

Граница выглядит так

```text
Launcher / UI
    -> LauncherEngine.launch(...)
        -> OperationManager
        -> OperationResult
        -> LauncherStateMachine
    -> LaunchResult
```

---

## Рассмотренные варианты

### Оставить `launch(...)` без возвращаемого результата

Вариант отклонен

Такой подход сохраняет текущую простоту, но заставляет внешние слои косвенно определять результат запуска через
internal state или events

Это усложняет future UI и integration tests

### Вернуть `OperationResult` наружу

Вариант отклонен

`OperationResult` описывает результат отдельной operation, а не всего launcher lifecycle

Если вернуть его напрямую, внешний слой начнет зависеть от operation-level semantics

### Сразу добавить structured launch diagnostics

Вариант отложен

Structured diagnostics полезны для UI, recovery behavior и error presentation, но являются более широкой темой

На момент принятия решения достаточно минимального observable outcome

---

## Последствия

Внешний слой получает stable result запуска

Integration tests смогут проверять launcher lifecycle без доступа к internal state machine

`LauncherEngine` остается coordinator, а не executor бизнес-логики

Operation-level diagnostics остаются внутри operation boundary

Решение не вводит UI error model

В будущем `LaunchResult` может стать входной точкой для более богатого error presentation, но только после отдельного
решения

Решение не изменяет границу operation failure diagnostics, зафиксированную в [ADR-0040](ADR-0040-operation-failure-diagnostics-boundary.md)

---

## Не входит в решение

- Structured operation diagnostics
- Generic operation failure context
- UI presentation model
- Retry policy
- Recovery behavior
- Domain-specific failure reasons в `LaunchResult`
- Изменение `OperationResult`
- Изменение ответственности `OperationManager`
- Возврат к `TaskPipeline`

---

## Связанные решения

- [ADR-0002: Launcher lifecycle](ADR-0002-launcher-lifecycle.md)
- [ADR-0003: LauncherEngine Responsibility](ADR-0003-launcher-engine-responsibility.md)
- [ADR-0007: Context Ownership](ADR-0007-context-ownership.md)
- [ADR-0012: LauncherEngine uses OperationManager](ADR-0012-launcher-engine-uses-operation-manager.md)
- [ADR-0040: Определить границу operation failure diagnostics](ADR-0040-operation-failure-diagnostics-boundary.md)
