[← Назад к списку решений](../README.md)

# ADR-0055: Определить границу отображения launch failure в presentation layer

## Статус

Accepted

> Примечание: решение реализовано в итерациях
> `feat(ui): add presentation launch failure mapping`
> `feat(ui): present launch failure context`

---

## Контекст

На момент принятия решения минимальный JavaFX entrypoint отображает состояния `READY`, `LAUNCHING`, `LAUNCHED`, `FAILED`

Граница presentation state зафиксирована в [ADR-0053](ADR-0053-presentation-launch-state-boundary.md)

Неуспешный launcher lifecycle переводит presentation state в `FAILED`

Для этого состояния JavaFX entrypoint отображает общее сообщение `Launch failed`

После реализации [ADR-0054](ADR-0054-generic-launch-failure-context-boundary.md) неуспешный `LaunchResult` содержит обязательный `LaunchFailure`

Operation-level failure сохраняет `OperationType` и исходный `OperationFailure`

Lifecycle-level failure содержит readable message без искусственного operation context

`LauncherResultHandler` передает расширенный `LaunchResult` в presentation layer, но текущая `PresentationLaunchStateMachine`
использует только флаг успешности и не сохраняет failure context

Прямое отображение `LaunchFailure.message()` или `OperationFailure.details()` может раскрыть внутренние пути, URL, технические
сообщения и другие данные, которые не предназначены для пользователя

`launcher-app` не должен интерпретировать failure context, потому что выбор пользовательского представления принадлежит
presentation layer

Перед отображением причины неуспешного запуска необходимо определить

- Владение presentation failure model
- Границу преобразования `LaunchFailure`
- Правила безопасного отображения operation-level failure
- Поведение lifecycle-level failure
- Связь failure context с presentation state

---

## Решение

`launcher-ui` владеет преобразованием `LaunchFailure` в безопасное пользовательское представление

Для представления ошибки запуска вводится минимальная модель `PresentationLaunchFailure`

Минимальная `PresentationLaunchFailure` содержит controlled user-facing message

Компонент `PresentationLaunchFailureMapper` преобразует `LaunchFailure` в `PresentationLaunchFailure`

Для operation-level failure mapper использует `OperationType` как stable generic source для выбора controlled presentation
message

Несколько технических операций могут преобразоваться в одно пользовательское сообщение, если их различие не имеет
пользовательской ценности

Для lifecycle-level failure mapper возвращает общее безопасное сообщение

Raw `LaunchFailure.message()`, `OperationFailure.message()`, `OperationFailure.details()` и domain-specific exceptions не
отображаются пользователю напрямую

Presentation state хранит только `PresentationLaunchFailure`, а не исходный technical failure context

`PresentationLaunchFailure` присутствует только в состоянии `FAILED`

После принятия нового launch request предыдущий presentation failure context очищается

Успешное завершение launcher lifecycle не содержит presentation failure context

Отклонение повторного запроса во время `LAUNCHING` не изменяет presentation failure context

Существующее состояние `FAILED` сохраняется и не разделяется на operation-specific состояния

Граница выглядит так

```text
LaunchResult
    -> LaunchFailure
        -> PresentationLaunchFailureMapper
            -> PresentationLaunchFailure
                -> presentation state
                    -> JavaFX rendering
```

`launcher-app` продолжает передавать `LaunchResult` без интерпретации failure context

`launcher-core` не получает presentation messages или UI-specific error model

---

## Рассмотренные варианты

### Отображать LaunchFailure.message напрямую

Вариант отклонен

Readable technical message не является гарантированно безопасным пользовательским сообщением

Оно может содержать внутренние пути, URL, названия компонентов или adapter-specific details

### Преобразовать failure context в launcher-app

Вариант отклонен

`launcher-app` владеет application boundary и выполнением launch request, но не должен принимать presentation decisions

Разные presentation clients могут использовать разные тексты и способы отображения одной ошибки

### Сохранить только общее сообщение Launch failed

Вариант отклонен

Generic launch failure context уже позволяет безопасно определить этап, на котором завершился launcher lifecycle

Сохранение только общего сообщения не использует доступную информацию и не помогает пользователю понять характер ошибки

### Сразу ввести полную structured presentation error model

Вариант отложен

На момент принятия решения подтвержден только сценарий безопасного отображения причины неуспешного запуска

Error categories, recovery actions, retry policy и localization пока не имеют достаточного количества независимых сценариев

---

## Последствия

Presentation layer получает собственную минимальную failure model

Технический failure context преобразуется в controlled user-facing message до сохранения в presentation state

Raw operation messages и details не раскрываются JavaFX controls

`launcher-core` и `launcher-app` сохраняют существующие ответственности

`PresentationLaunchStateMachine` получает дополнительный optional failure context для состояния `FAILED`

Новый принятый launch request должен очищать предыдущую presentation failure

Operation-specific mapping требует обновления при появлении новых `OperationType`, имеющих пользовательскую ценность

Lifecycle-level failures пока отображаются общим сообщением и не раскрывают техническую причину

JavaFX rendering получает дополнительное presentation behavior, но не становится владельцем mapping rules

Решение добавляет минимальную presentation error model без recovery behavior и полной UI state model

---

## Не входит в решение

- Отображение raw `LaunchFailure.message()`
- Отображение `OperationFailure.details()`
- Domain-specific presentation error codes
- Recovery actions
- Retry policy
- Cancel behavior
- Progress presentation
- Локализация
- Error history
- Логирование и мониторинг
- Technical diagnostics screen
- Copy-to-clipboard diagnostics
- Изменение `LaunchFailure`
- Изменение `OperationFailure`
- Изменение `LauncherResultHandler`
- Новые `PresentationLaunchState`
- Electron client
- Backend split
- Shared UX system

---

## Связанные решения

- [ADR-0040: Определить границу operation failure diagnostics](ADR-0040-operation-failure-diagnostics-boundary.md)
- [ADR-0046: Определить границу результата запуска Launcher](ADR-0046-launcher-launch-result-boundary.md)
- [ADR-0047: Определить границу обработки результата запуска Launcher](ADR-0047-launcher-result-handling-boundary.md)
- [ADR-0048: Определить границу generic operation failure context](ADR-0048-generic-operation-failure-context-boundary.md)
- [ADR-0052: Определить границу запроса запуска из presentation layer](ADR-0052-presentation-launch-request-boundary.md)
- [ADR-0053: Определить границу состояния запуска в presentation layer](ADR-0053-presentation-launch-state-boundary.md)
- [ADR-0054: Определить границу generic launch failure context](ADR-0054-generic-launch-failure-context-boundary.md)
