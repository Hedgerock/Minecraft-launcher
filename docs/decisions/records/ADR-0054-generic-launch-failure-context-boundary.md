[← Назад к списку решений](../README.md)

# ADR-0054: Определить границу generic launch failure context

## Статус

Accepted

> Примечание: решение реализовано в итерациях
> `feat(core): add generic launch failure context model`
> `feat(core): propagate generic launch failure context`

---

## Контекст

На момент принятия решения `LaunchResult` описывает минимальный outcome всего launcher lifecycle

Модель содержит

- Флаг успешности запуска
- Финальное состояние launcher

Эта граница была зафиксирована в
[ADR-0046](ADR-0046-launcher-launch-result-boundary.md)

Минимального результата достаточно для управления presentation state и определения успешности launcher lifecycle

Однако неуспешный `LaunchResult` не сохраняет контекст причины завершения

Operation lifecycle уже использует generic `OperationFailure`, который проходит через

```text
OperationFailure
    -> OperationResult
        -> OperationFailedEvent
```

Эта граница зафиксирована в
[ADR-0048](ADR-0048-generic-operation-failure-context-boundary.md)

`LauncherEngine` использует `OperationResult` для принятия lifecycle decision, но преобразует неуспешный результат операции
в минимальный `LaunchResult.failure(...)`

Generic operation failure context при этом не проходит через launcher result boundary

Кроме ошибок отдельных операций, launcher lifecycle содержит failure-сценарии координации, которые не принадлежат
конкретному `OperationResult`

Presentation layer уже способен отобразить состояние `FAILED`, но не должен получать внутренние operation events, domain-specific
exceptions или детали orchestration для определения причины ошибки

Перед дальнейшим развитием presentation error behavior необходимо определить generic failure context результата всего
launcher lifecycle

---

## Решение

`launcher-core` владеет generic launch failure context

Для представления неуспешного результата всего launcher lifecycle вводится launch-level модель `LaunchFailure`

`LaunchResult` остается внешним результатом `LauncherEngine.launch(...)` и source of truth для outcome всего launcher lifecycle

Неуспешный `LaunchResult` должен содержать `LaunchFailure`

Успешный `LaunchResult` не должен содержать failure context

`LaunchFailure` должна предоставлять readable message независимо от того, возникла ошибка внутри operation или на уровне
координации launcher lifecycle

Если launcher lifecycle завершается из-за failed operation, `LaunchFailure` сохраняет

- Тип завершившейся операции
- Generic `OperationFailure`

Если ошибка возникает на уровне координации launcher lifecycle, `LaunchFailure` представляет ее без создания искусственного
`OperationResult` или `OperationFailedEvent`

`LauncherEngine` отвечает за преобразование operation-level или lifecycle-level failure в `LaunchFailure`

`OperationResult` остается результатом отдельной operation

`OperationFailedEvent` остается внутренним событием operation lifecycle и не становится presentation boundary

`launcher-app` передает расширенный `LaunchResult` через существующий `LauncherResultHandler` без интерпретации failure context

Граница выглядит так

```text
OperationResult
    -> LauncherEngine
        -> LaunchFailure
            -> LaunchResult
                -> LauncherResultHandler
```

`LaunchFailure` не принимает presentation, retry или recovery decisions

---

## Рассмотренные варианты

### Оставить LaunchResult минимальным

Вариант отклонен

Presentation layer сможет определить только факт неуспешного запуска и будет вынужден получать причину ошибки через
другую границу

Это приведет к появлению нескольких источников результата одного launcher lifecycle

### Добавить OperationFailure непосредственно в LaunchResult

Вариант отклонен

`OperationFailure` описывает failure отдельной operation

Не все failure-сценарии `LauncherEngine` принадлежат конкретной operation

Прямое использование `OperationFailure` смешает operation-level и launcher-level semantics

### Передавать OperationFailedEvent в presentation layer

Вариант отклонен

`OperationFailedEvent` является частью внутреннего operation lifecycle

Прямая подписка presentation layer на внутренние события усилит связанность presentation и orchestration layers
и не покроет lifecycle-level failures

---

## Последствия

`LaunchResult` получает generic failure context для неуспешного launcher lifecycle

Operation-level failure context больше не теряется внутри `LauncherEngine`

Lifecycle-level failures могут быть представлены без создания искусственной operation

`OperationResult` сохраняет operation-level ответственность

`OperationFailedEvent` не становится внешним presentation contract

Существующая `LauncherResultHandler` boundary продолжает передавать единый результат launcher lifecycle

`launcher-core` получает дополнительную launch-level модель и ответственность за преобразование failure context

Фабрики `LaunchResult` и существующие тесты неуспешного запуска должны учитывать обязательный failure context

Presentation layer пока продолжает отображать минимальное состояние `FAILED`

Преобразование `LaunchFailure` в user-facing presentation требует отдельного решения

---

## Не входит в решение

- Structured presentation error model
- User-facing error messages
- Domain-specific failure codes в `LaunchResult`
- Retry policy
- Recovery behavior
- Cancel behavior
- Progress presentation
- Локализация
- Логирование и мониторинг
- Изменение `OperationFailedEvent`
- Прямая подписка presentation layer на внутренний `EventBus`

---

## Связанные решения

- [ADR-0040: Определить границу operation failure diagnostics](ADR-0040-operation-failure-diagnostics-boundary.md)
- [ADR-0046: Определить границу результата запуска Launcher](ADR-0046-launcher-launch-result-boundary.md)
- [ADR-0047: Определить границу обработки результата запуска Launcher](ADR-0047-launcher-result-handling-boundary.md)
- [ADR-0048: Определить границу generic operation failure context](ADR-0048-generic-operation-failure-context-boundary.md)
- [ADR-0052: Определить границу запроса запуска из presentation layer](ADR-0052-presentation-launch-request-boundary.md)
- [ADR-0053: Определить границу состояния запуска в presentation layer](ADR-0053-presentation-launch-state-boundary.md)
