[← Назад к списку решений](../README.md)

# ADR-0053: Определить границу состояния запуска в presentation layer

## Статус

Accepted

---

## Контекст

На момент принятия решения application boundary для приема launch request из presentation layer реализована
согласно [ADR-0052](ADR-0052-presentation-launch-request-boundary.md)

`PresentationLaunchBoundary` немедленно возвращает `LaunchRequestResult`, выполняет launcher lifecycle за пределами presentation
thread и передает итоговый `LaunchResult` через `LauncherResultHandler`

Минимальный JavaFX entrypoint использует эту границу для запуска launcher lifecycle

После принятия запроса кнопка запуска блокируется, а после получения итогового результата снова становится доступной

Это поведение пока распределено между обработчиком пользовательского действия и callback итогового результата

Явный source of truth для presentation state отсутствует

Дальнейшее добавление визуального результата запуска непосредственно через состояние JavaFX controls приведет к смешению

- Application outcomes
- Presentation state transitions
- JavaFX rendering
- Допустимости пользовательских действий

`LaunchRequestResult` и `LaunchResult` описывают разные границы

`LaunchRequestResult` сообщает, был ли запрос принят application boundary

`LaunchResult` сообщает итог всего launcher lifecycle

`LauncherState` описывает внутреннее состояние launcher orchestration и не является presentation state

Перед отображением результата запуска в JavaFX UI необходимо зафиксировать

- Владение presentation state
- Минимальный набор состояний
- Переходы после приема запроса
- Переходы после завершения launcher lifecycle
- Связь состояния с доступностью launch action

---

## Решение

`launcher-ui` владеет минимальной моделью состояния запуска presentation layer

`launcher-app` и `launcher-core` не получают ответственность за хранение или отображение presentation state

Presentation state не зависит от JavaFX controls и представляет наблюдаемое состояние взаимодействия запуска

Минимальный набор состояний

- `READY` — launch request может быть отправлен
- `LAUNCHING` — launch request принят, и launcher lifecycle выполняется
- `LAUNCHED` — launcher lifecycle успешно завершился
- `FAILED` — launcher lifecycle завершился неуспешно

Начальным состоянием является `READY`

Переходы состояния

```text
READY
    -> LAUNCHING
        -> LAUNCHED
        -> FAILED

LAUNCHED
    -> LAUNCHING

FAILED
    -> LAUNCHING
```

Принятый `LaunchRequestResult.ACCEPTED` переводит presentation state в `LAUNCHING`

`LaunchRequestResult.REJECTED_ALREADY_RUNNING` не изменяет presentation state

Успешный `LaunchResult` переводит presentation state из `LAUNCHING` в `LAUNCHED`

Неуспешный `LaunchResult` переводит presentation state из `LAUNCHING` в `FAILED`

Доступность launch action определяется presentation state

Launch action недоступен только в состоянии `LAUNCHING`

В состояниях `READY`, `LAUNCHED` и `FAILED` новый launch request может быть отправлен

Presentation layer использует только внешний outcome `LaunchResult` и не отображает внутренние переходы `LauncherStateMachine`
как собственные состояния

Состояние `LAUNCHED` означает успешное завершение launcher lifecycle и успешный старт game process

Оно не описывает жизненный цикл запущенного game process и не означает, что игра продолжает выполняться

JavaFX entrypoint отвечает за отображение presentation state через controls

Компонент, владеющий переходами состояния, не должен зависеть от конкретных JavaFX controls

---

## Рассмотренные варианты

### Хранить состояние непосредственно в JavaFX controls

Вариант отклонен

Состояние будет неявно распределено между `Button`, `Label`, event handlers и result callbacks

Такое поведение сложнее тестировать без запуска JavaFX runtime

### Хранить presentation state в launcher-app

Вариант отклонен

`launcher-app` владеет приемом launch request и фоновым выполнением launcher lifecycle, но не должен принимать presentation
decisions

Presentation state может отличаться между JavaFX, Electron или другими внешними clients

### Использовать LauncherState как presentation state

Вариант отклонен

`LauncherState` описывает внутренние этапы orchestration и содержит больше деталей, чем требуется минимальному UI

Прямая зависимость UI behavior от внутренних launcher states усилит связанность presentation и orchestration layers

### Сразу ввести полную UI state model

Вариант отложен

На момент принятия решения подтвержден только минимальный сценарий отображения приема и результата launch request

Progress presentation, cancel behavior, retry policy и structured user-facing errors пока не требуют полной UI state model

---

## Последствия

Presentation layer получает явный и проверяемый source of truth для состояния запуска

Доступность launch action больше не определяется отдельными несвязанными изменениями JavaFX controls

Переходы после `LaunchRequestResult` и `LaunchResult` могут тестироваться без запуска JavaFX runtime

`launcher-app` сохраняет ответственность за выполнение launch request, но не знает о presentation state

`launcher-core` сохраняет ответственность за launcher orchestration и не получает UI concerns

JavaFX entrypoint получает дополнительное отображение состояния, но не становится владельцем правил перехода

Появляется отдельная минимальная presentation model, которую необходимо поддерживать при добавлении новых presentation
outcomes

Состояния остаются намеренно крупными и не отражают progress отдельных launcher operations

`LAUNCHED` не является моделью lifecycle запущенного game process

Расширение модели новыми состояниями должно происходить только после появления подтвержденного пользовательского сценария

---

## Не входит в решение

- Progress presentation
- Отображение отдельных `LauncherState`
- Детализированные user-facing error messages
- Structured presentation error model
- Retry policy
- Cancel behavior
- Очередь launch requests
- Game process lifecycle tracking
- Локализация
- Полная UI state model
- Generic presentation framework
- Electron client
- Backend split
- Shared UX system

---

## Связанные решения

- [ADR-0037: Определить политику reserved modules](ADR-0037-reserved-modules-policy.md)
- [ADR-0046: Определить границу результата запуска Launcher](ADR-0046-launcher-launch-result-boundary.md)
- [ADR-0047: Определить границу обработки результата запуска Launcher](ADR-0047-launcher-result-handling-boundary.md)
- [ADR-0052: Определить границу запроса запуска из presentation layer](ADR-0052-presentation-launch-request-boundary.md)
