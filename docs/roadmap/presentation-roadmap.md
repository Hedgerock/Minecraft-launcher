[← Назад к общему пути](general-roadmap.md)

# Путь развития presentation layer

## Текущий план

- Реализовать минимальную presentation launch state model согласно [ADR-0053](../decisions/records/ADR-0053-presentation-launch-state-boundary.md)
- Добавить состояния `READY`, `LAUNCHING`, `LAUNCHED` и `FAILED`
- Централизовать переходы после `LaunchRequestResult` и `LaunchResult`
- Подключить presentation state к JavaFX entrypoint
- Подтвердить переходы состояния unit-тестами
- Не расширять минимальную модель до progress presentation, cancel behavior, retry policy и structured user-facing error
  model без подтвержденного сценария
- После завершения минимальной state model провести ревизию presentation flow перед выбором следующего поведения

---

## Milestone — Presentation launch boundary

Минимальная presentation launch boundary доведена до JavaFX entrypoint без переноса presentation concerns в `launcher-app`
или `launcher-core`

### Закрыто

- Реализована application boundary для фонового выполнения одного launcher lifecycle
- Добавлен JavaFX adapter для передачи `LaunchResult` в JavaFX Application Thread
- Минимальный JavaFX entrypoint подключен к presentation launch boundary и управляет ее lifecycle
- Dependency boundary `launcher-ui` закреплена архитектурным тестом

Архитектурная граница зафиксирована в [ADR-0052](../decisions/records/ADR-0052-presentation-launch-request-boundary.md)

---

## Отложено

- Progress presentation до появления стабильной application boundary для передачи progress events
- Structured user-facing error model до появления подтвержденного сценария отображения и восстановления после ошибок
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

### Result presentation

После реализации минимальной state model можно определить визуальное представление состояний `LAUNCHED` и `FAILED`

Этот шаг не должен вводить structured error model, recovery actions или локализацию без отдельного подтвержденного сценария

### Progress presentation

Если длительность verification, download или native extraction потребует пользовательской обратной связи, необходимо
определить application-level boundary для передачи progress events

`launcher-ui` не должен подписываться на внутренние события `launcher-core` напрямую

### Presentation error model

Перед добавлением детализированных сообщений и recovery actions необходимо определить преобразование launcher failure
context в безопасное пользовательское представление

Presentation error model не должна раскрывать внутренние exception details, credentials или технические данные без явной
необходимости

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
