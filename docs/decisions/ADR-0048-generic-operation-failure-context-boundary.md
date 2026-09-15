[← Назад к списку решений](README.md)

# ADR-0048: Определить границу generic operation failure context

## Статус

Accepted

> Примечание: решение частично реализовано в итерациях
> `feat(core): add generic operation failure context`
> `feat(core): map operation exceptions to generic failure context`

---

## Контекст

На момент принятия решения operation lifecycle использует минимальную модель результата

`TaskResult.failure(...)` хранит readable message

`SequentialExecutionStrategy` преобразует failed task result в `OperationResult.failure(...)`

`LaunchOperation` преобразует exception в `OperationResult.failure(...)`

`OperationResult` содержит только

- success flag
- optional error message

`OperationFailedEvent` публикует `OperationType` и readable error message

Такой подход достаточен для lifecycle control

Однако проект уже получил несколько domain-specific failure моделей

- `JavaRuntimeFailureException` и `JavaRuntimeFailureReason`
- `DownloadException` и `DownloadExceptionReason`
- storage exceptions
- manifest mapping exceptions
- native extraction exceptions
- game process launch exceptions

При прохождении через operation lifecycle большая часть структурного контекста теряется

Например

```text
DownloadException(reason, url, path, targetPath)
    -> TaskResult.failure(message)
        -> OperationResult.failure(message)
            -> OperationFailedEvent(errorMessage)
```

Ранее [ADR-0040](ADR-0040-operation-failure-diagnostics-boundary.md) зафиксировал, что `OperationResult` не должен зависеть
от Java runtime-specific failure reason

Если structured diagnostics потребуется на уровне operation lifecycle, она должна появиться через generic operation failure
context

После появления `LaunchResult`, launcher lifecycle integration test и application-level `LauncherResultHandler` появился
подтвержденный сценарий для подготовки generic failure boundary

Future UI, CLI behavior, recovery policy и error presentation не должны строиться на domain-specific exceptions напрямую

---

## Решение

Operation lifecycle должен получить generic operation failure context

Generic failure context должен описывать operation-level failure без привязки к конкретному domain-specific enum

Минимальная модель generic failure context должна содержать

- readable message
- generic failure code или category
- optional failure source
- optional details

`OperationResult` должен оставаться operation-level результатом

`OperationResult` может содержать generic failure context, но не должен зависеть от

- `JavaRuntimeFailureReason`
- `DownloadExceptionReason`
- adapter-specific exception classes
- UI-specific presentation model

Domain-specific exceptions могут быть преобразованы в generic failure context на operation boundary

Граница выглядит так

```text
Domain boundary
    -> domain-specific exception
        -> operation failure mapper
            -> OperationFailure
                -> OperationResult
                    -> OperationFailedEvent
```

`OperationFailure` должен быть пригоден для

- operation lifecycle decisions
- event publishing
- future result handling
- future UI / CLI presentation
- future recovery analysis

Но `OperationFailure` не должен сам принимать presentation, retry или recovery decisions

На момент принятия решения достаточно зафиксировать границу

Конкретный набор generic failure codes, details model и mapping rules должен вводиться отдельными маленькими итерациями

---

## Рассмотренные варианты

### Оставить только readable error message

Вариант отклонен

Readable message достаточно для текущих тестов и минимального lifecycle, но недостаточно для future UI, CLI behavior,
diagnostics и recovery analysis

Такой подход продолжит терять domain-specific context на operation boundary

### Пробросить domain-specific reasons в `OperationResult`

Вариант отклонен

Если `OperationResult` начнет зависеть от `JavaRuntimeFailureReason`, `DownloadExceptionReason` или других domain-specific
enums, operation lifecycle станет зависеть от конкретных adapter/domain flows

Это нарушит границу, зафиксированную в [ADR-0040](ADR-0040-operation-failure-diagnostics-boundary.md)

### Расширить `LaunchResult`

Вариант отложен

`LaunchResult` описывает outcome всего launcher lifecycle

Он должен оставаться минимальным boundary object до отдельного решения

Generic operation failure context сначала должен появиться на operation boundary

### Сразу добавить UI error model

Вариант отложен

UI error model является presentation-level решением

Сначала нужно определить operation-level failure context, который future UI сможет использовать

---

## Последствия

Operation lifecycle получает путь к structured diagnostics без зависимости от конкретных domain-specific failure models

`OperationResult` может развиваться от readable message к generic failure context

Domain-specific errors сохраняют ценность внутри своих boundaries

Future UI и CLI смогут использовать operation-level failure context без знания Java runtime, downloader или storage
исключений

`LaunchResult` не расширяется в рамках этого решения

Существующие readable error messages должны оставаться доступными для совместимости тестов, events и текущего поведения

Ввод `OperationFailure`, generic failure codes и mapping rules должен выполняться постепенно

---

## Не входит в решение

- Немедленная реализация `OperationFailure`
- Полный список generic failure codes
- Маппинг всех domain-specific exceptions
- Изменение `LaunchResult`
- UI error model
- CLI output
- Exit codes
- Retry policy
- Recovery behavior
- Java runtime fallback policy
- Download retry policy
- Изменение всех event contracts за одну итерацию

---

## Связанные решения

- [ADR-0011: Failure Policy Extraction](ADR-0011-failure-policy-extraction.md)
- [ADR-0012: LauncherEngine uses OperationManager](ADR-0012-launcher-engine-uses-operation-manager.md)
- [ADR-0039: Определить модель ошибок Java runtime](ADR-0039-java-runtime-failure-model.md)
- [ADR-0040: Определить границу operation failure diagnostics](ADR-0040-operation-failure-diagnostics-boundary.md)
- [ADR-0046: Определить границу результата запуска Launcher](ADR-0046-launcher-launch-result-boundary.md)
- [ADR-0047: Определить границу обработки результата запуска Launcher](ADR-0047-launcher-result-handling-boundary.md)
