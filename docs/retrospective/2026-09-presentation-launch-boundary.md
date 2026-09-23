# Ретроспектива: presentation launch boundary

## Контекст

Данный milestone развил presentation launch flow после `v0.7.0-manifest-runtime-flow`

До начала этапа launcher уже выполнял lifecycle и возвращал `LaunchResult`, но JavaFX entrypoint не имел устойчивой
границы для фонового запроса запуска, собственного состояния и безопасного отображения ошибок

Целью этапа было довести минимальный сценарий запуска до UI без переноса presentation concerns в `launcher-core` и без
изменения смысла `LaunchResult`

---

## Что было сделано

- Зафиксирована и реализована application boundary для фонового presentation launch request
- JavaFX entrypoint подключен к этой границе, а зависимость `launcher-ui` закреплена архитектурным тестом
- Добавлены presentation state и отображение состояний `READY`, `LAUNCHING`, `LAUNCHED` и `FAILED`
- Generic failure context проведен до `LaunchResult`
- Добавлены безопасная presentation failure model и преобразование launch failure для UI
- Введен `PresentationLaunchCompletion`, различающий полученный `LaunchResult` и неожиданный сбой до его появления
- Принятый запрос получает терминальный исход при работающей границе доставки
- Неожиданный сбой переводит presentation state в `FAILED` без искусственного `LaunchResult`
- Добавлены тесты application boundary, presentation state, JavaFX delivery и сценариев ошибок

---

## Что подтвердилось

`LaunchResult` должен описывать результат launcher lifecycle, а не любой сбой при обработке presentation request

Отдельный completion outcome позволяет завершить принятый запрос, даже если lifecycle не смог вернуть `LaunchResult`

Presentation state является подходящим источником состояния для доступности launch action и отображаемого статуса

Технические сообщения ошибок не должны попадать в JavaFX напрямую

---

## Что было улучшено архитектурно

Граница между выполнением launcher lifecycle и его представлением стала явной

```text
JavaFX entrypoint
    -> PresentationLaunchBoundary
        -> background launcher lifecycle
        -> PresentationLaunchCompletion
    -> JavaFX adapter
        -> presentation state
```

`launcher-app` владеет исполнением запроса и типом его исхода

`launcher-ui` владеет пользовательским состоянием и безопасным отображением результата

`launcher-core` не получил зависимость от фоновых запросов, JavaFX или presentation state

---

## Что осталось отложенным

- Progress presentation
- Cancel behavior и retry policy
- Recovery actions
- Game process lifecycle tracking
- Локализация
- Логирование и мониторинг
- Общая UX system и дополнительные presentation clients

---

## Технический долг

Ограничения текущей границы остаются явными

Передача терминального исхода предполагает работающий обработчик завершения

Сбой доставки не исправляется повторным вызовом того же обработчика без отдельной политики и не превращается в новый
сбой launcher lifecycle

Дальнейшая диагностика и восстановление доставки требуют подтвержденного сценария

---

## Главный вывод

Milestone подтвердил, что минимальный JavaFX launch flow можно построить поверх technology-neutral application boundary
без изменения launcher orchestration

Принятый запрос теперь различает результат launcher lifecycle и неожиданный сбой его выполнения, а UI при исправной доставке
исхода получает безопасный терминальный статус в обоих сценариях

Следующее направление следует выбрать после подготовки релиза и отдельной ревизии, не расширяя автоматически текущий
presentation milestone
