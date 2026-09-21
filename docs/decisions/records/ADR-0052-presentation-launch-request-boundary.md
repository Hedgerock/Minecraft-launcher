[← Назад к списку решений](../README.md)

# ADR-0052: Определить границу запроса запуска из presentation layer

## Статус

Accepted

> Примечание: решение частично реализовано в итерациях
> `feat(app): add presentation launch request contract`
> `feat(app): execute presentation launch requests in background`
> `feat(app): wire presentation launch boundary in bootstrap`
>
> До полного завершения остается подключение boundary к lifecycle JavaFX entrypoint

---

## Контекст

На момент принятия решения основной launcher lifecycle запускается синхронно через application entrypoint

`LauncherEngine.launch(...)` выполняет последовательный lifecycle flow и возвращает `LaunchResult`

Граница результата запуска зафиксирована в [ADR-0046](ADR-0046-launcher-launch-result-boundary.md)

Ответственность внешнего слоя за обработку результата зафиксирована в [ADR-0047](ADR-0047-launcher-result-handling-boundary.md)

Текущий синхронный запуск достаточен для минимального CLI entrypoint, но не подходит для будущего presentation layer

Прямой вызов `LauncherEngine.launch(...)` из UI thread заблокирует обработку пользовательского интерфейса до завершения
launcher lifecycle

Повторное пользовательское действие во время выполняющегося запуска может создать несколько пересекающихся lifecycle flow

`LauncherEngine` и связанные с ним stateful components описывают один launcher lifecycle и не должны повторно использоваться
для независимых запусков

При этом фоновое выполнение внешнего launch request не требует изменения внутренней execution model `LauncherEngine`

Последовательность launcher operations должна остаться синхронной и управляться существующей orchestration boundary

Перед развитием JavaFX UI необходимо зафиксировать

- Границу приема запроса запуска
- Владение фоновым выполнением
- Поведение повторного запроса
- Передачу итогового `LaunchResult`
- Направление зависимости между presentation и application layers

---

## Решение

`launcher-app` владеет application boundary для приема запроса запуска из presentation layer

Граница не зависит от JavaFX classes, UI controls или конкретной presentation technology

`launcher-ui` обращается к application boundary и не управляет `LauncherEngine`, `OperationManager` или application assembly
напрямую

Каждый принятый запрос должен запускать новый launcher lifecycle через новый экземпляр `LauncherEngine`

`LauncherEngine.launch(...)` остается синхронным

Application boundary выполняет синхронный launcher lifecycle через управляемое фоновое выполнение за пределами presentation
thread

Один экземпляр application boundary допускает не более одного активного launch request

Переход из свободного состояния в состояние выполняющегося запуска должен быть атомарным

Запрос возвращает немедленный результат приема

Минимальные исходы приема запроса

- Запрос принят
- Запрос отклонен, потому что launcher lifecycle уже выполняется

Результат приема запроса не заменяет `LaunchResult`

Повторный запрос во время активного lifecycle

- Не запускает второй `LauncherEngine`
- Не ставится в очередь
- Не прерывает текущий запуск
- Возвращает явный результат отклонения

После завершения launcher lifecycle итоговый `LaunchResult` передается через существующую external result handling boundary

Состояние активного запуска должно освобождаться после завершения фонового выполнения независимо от успешности
launcher lifecycle или обработки результата

После завершения одного lifecycle новый запрос может быть принят и должен получить новый экземпляр `LauncherEngine`

Application boundary владеет состоянием активного запуска и механизмом фонового выполнения

Presentation entrypoint отвечает за завершение application boundary вместе с завершением собственного lifecycle

Обработчик результата может быть вызван из background execution context

Будущий JavaFX adapter самостоятельно отвечает за перенос UI updates в JavaFX Application Thread

Направление зависимости выглядит так

```text
launcher-ui
    -> launcher-app presentation launch boundary
        -> application assembly
            -> new LauncherEngine
                -> synchronous launcher lifecycle
                -> LaunchResult
        -> external result handling
```

`LauncherEngine` не получает ответственность за

- Создание background threads
- Предотвращение повторных presentation requests
- Управление presentation lifecycle
- Обновление UI state
- Переключение на JavaFX Application Thread

---

## Рассмотренные варианты

### Вызывать LauncherEngine напрямую из presentation thread

Вариант отклонен

`LauncherEngine.launch(...)` выполняет полный launcher lifecycle синхронно

Прямой вызов заблокирует presentation thread и свяжет UI с деталями создания launcher lifecycle

### Сделать LauncherEngine асинхронным

Вариант отклонен

Фоновое выполнение является требованием внешнего presentation scenario, а не внутреннего launcher orchestration

Добавление thread ownership в `LauncherEngine` смешает lifecycle coordination с application и presentation concerns

### Управлять фоновым запуском непосредственно в UI controller

Вариант отклонен

Такой подход распределит правила предотвращения повторного запуска, обработки результата и управления execution lifecycle
между UI components

Эти правила должны оставаться независимыми от конкретной presentation technology

### Добавить очередь launch requests

Вариант отложен

На момент принятия решения отсутствует подтвержденный сценарий последовательной обработки нескольких пользовательских
запросов

Автоматический запуск устаревшего запроса после завершения предыдущего lifecycle может быть неожиданным для пользователя

### Ввести generic concurrent workflow framework

Вариант отложен

Текущий сценарий требует только фонового выполнения одного последовательного launcher lifecycle

Он не подтверждает необходимость parallel operations, concurrent downloads или общей workflow abstraction

---

## Последствия

Presentation layer получает неблокирующую границу запуска launcher lifecycle

Правило одного активного launch request становится явным и проверяемым

`LauncherEngine` сохраняет синхронную execution model и ответственность за orchestration

Для каждого принятого запроса создается независимый launcher lifecycle

Application layer получает дополнительное состояние и ответственность за lifecycle фонового выполнения

Появляется необходимость тестировать

- Прием первого запроса
- Отклонение повторного запроса во время выполнения
- Передачу `LaunchResult`
- Освобождение активного состояния и прием нового запроса после завершения предыдущего lifecycle
- Завершение фонового execution resource

Вызов result handler не получает автоматической гарантии выполнения на presentation thread

Конкретный UI adapter должен самостоятельно выполнять необходимое thread marshalling

Решение добавляет минимальную concurrency boundary, но не изменяет последовательное выполнение launcher operations

---

## Не входит в решение

- Реализация JavaFX controls и screens
- UI state model
- Progress presentation
- User-facing error messages
- Cancel behavior
- Retry policy
- Очередь launch requests
- Parallel downloads
- Concurrent execution strategy внутри `LauncherEngine`
- Generic workflow framework
- Расширение `LaunchResult`
- Изменение `OperationResult`
- JavaFX Application Thread adapter
- Electron client
- Backend split
- Shared UX system

---

## Связанные решения

- [ADR-0012: LauncherEngine uses OperationManager](ADR-0012-launcher-engine-uses-operation-manager.md)
- [ADR-0034: Определить классификацию границ launcher-core](ADR-0034-core-boundary-classification.md)
- [ADR-0037: Определить политику reserved modules](ADR-0037-reserved-modules-policy.md)
- [ADR-0046: Определить границу результата запуска Launcher](ADR-0046-launcher-launch-result-boundary.md)
- [ADR-0047: Определить границу обработки результата запуска Launcher](ADR-0047-launcher-result-handling-boundary.md)
