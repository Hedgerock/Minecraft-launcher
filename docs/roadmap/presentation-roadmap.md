[← Назад к общему пути](general-roadmap.md)

# Путь развития presentation layer

## Текущий план

- Провести итоговую ревизию presentation launch flow перед подготовкой milestone release
- Не добавлять recovery actions или локализацию без подтвержденного сценария
- Не добавлять progress presentation, cancel behavior и retry policy без отдельного архитектурного решения

---

## Milestone — Presentation launch boundary

Минимальная presentation launch boundary доведена до JavaFX entrypoint без переноса presentation concerns в `launcher-app`
или `launcher-core`

### Закрыто

- Реализована application boundary для фонового выполнения одного launcher lifecycle
- Минимальный JavaFX entrypoint подключен к presentation launch boundary и управляет ее lifecycle
- Dependency boundary `launcher-ui` закреплена архитектурным тестом
- Добавлена минимальная presentation launch state model
- JavaFX entrypoint использует presentation state как source of truth для доступности launch action
- Добавлено минимальное status presentation для состояний `READY`, `LAUNCHING`, `LAUNCHED` и `FAILED`
- Реализовано безопасное отображение launch failure через presentation model и mapper
- JavaFX entrypoint отображает сообщение из presentation state без раскрытия технического failure context
- Добавлен JavaFX adapter для передачи `PresentationLaunchCompletion` в JavaFX Application Thread
- Принятый launch request получает терминальный исход при наличии `LaunchResult` и при неожиданном сбое выполнения
- Неожиданный сбой выполнения отображается как безопасное состояние `FAILED` без искусственного `LaunchResult`

Архитектурные границы зафиксированы в

- [ADR-0052](../decisions/records/ADR-0052-presentation-launch-request-boundary.md)
- [ADR-0053](../decisions/records/ADR-0053-presentation-launch-state-boundary.md)
- [ADR-0055](../decisions/records/ADR-0055-presentation-launch-failure-boundary.md)
- [ADR-0056](../decisions/records/ADR-0056-presentation-launch-completion-boundary.md)

---

## Отложено

- Progress presentation до появления стабильной application boundary для передачи progress events
- Recovery actions до появления подтвержденного сценария пользовательского восстановления после ошибки
- Cancel behavior до появления cancellation boundary в launcher lifecycle
- Retry policy до определения допустимых сценариев повторного запуска
- Game process lifecycle tracking до появления требования отслеживать состояние запущенной игры
- Локализация до появления устойчивого набора пользовательских текстов
- Shared UX system до появления нескольких независимых presentation clients
- Полноценная navigation model до появления нескольких самостоятельных screens
- Electron client до стабилизации technology-neutral application boundaries
- Backend split до появления подтвержденного remote client scenario

---

## Возможные следующие направления

### Progress presentation

Если длительность verification, download или native extraction потребует пользовательской обратной связи, необходимо
определить application-level boundary для передачи progress events

`launcher-ui` не должен подписываться на внутренние события `launcher-core` напрямую

### Presentation error model

Минимальная граница преобразования `LaunchFailure` в безопасное пользовательское представление зафиксирована и реализована
согласно [ADR-0055](../decisions/records/ADR-0055-presentation-launch-failure-boundary.md)

Дальнейшее развитие error categories, recovery actions и localization откладывается до появления отдельных подтвержденных
сценариев

### UI composition

Новые screens, navigation и reusable presentation components стоит вводить после появления нескольких независимых
пользовательских сценариев

Минимальный launch screen пока не подтверждает необходимость общего UI framework

---

## Правило развития

Presentation layer развивается через небольшие подтвержденные пользовательские сценарии

`launcher-ui` использует technology-neutral boundaries из `launcher-app` и не управляет `LauncherEngine`, operation layer или
application assembly напрямую

Новые состояния и interaction policies должны добавляться через отдельные ADR, если они изменяют ответственность, lifecycle
или границы presentation layer
