[← Назад к списку решений](../README.md)

# ADR-0047: Определить границу обработки результата запуска Launcher

## Статус

Accepted

> Примечание: решение реализовано в итерации `feat(app): handle launcher result at application boundary`

---

## Контекст

На момент принятия решения `LauncherEngine.launch(...)` возвращает `LaunchResult`

Граница результата запуска зафиксирована в [ADR-0046](ADR-0046-launcher-launch-result-boundary.md)

`LaunchResult` описывает минимальный outcome всего launcher lifecycle

- успешный запуск
- неуспешный запуск
- финальное состояние launcher

После появления `LaunchResult` внешний слой получил стабильную точку наблюдения за результатом запуска

Это позволило добавить launcher lifecycle integration test на уровне application assembly

Однако внешний entrypoint приложения пока не имеет отдельной политики обработки `LaunchResult`

На момент принятия решения `Launcher` создает `LauncherEngine`, вызывает `launch(...)` и не интерпретирует результат

Для текущего CLI skeleton это допустимо, но перед развитием future UI boundary нужно зафиксировать, где проходит граница
ответственности между launcher lifecycle и presentation layer

Важно не смешивать launcher lifecycle с пользовательским UI, structured diagnostics или future product UX

---

## Решение

`LauncherEngine` отвечает за выполнение launcher lifecycle flow и возвращает `LaunchResult`

Внешний слой приложения отвечает за обработку `LaunchResult`

К внешнему слою относятся

- `Launcher`
- future CLI adapter
- future UI adapter
- future presentation layer

`LaunchResult` является boundary object между launcher lifecycle и внешним слоем

`LauncherEngine` не должен

- печатать результат запуска в консоль
- формировать user-facing messages
- принимать presentation decisions
- завершать JVM process
- знать о CLI, JavaFX, Electron или web UI
- возвращать structured diagnostics без отдельного решения

Внешний слой может

- получить `LaunchResult`
- принять минимальное решение по success/failure
- передать результат дальше в presentation layer
- преобразовать результат в exit code, UI state или user-facing message после отдельного решения

На текущем этапе external result handling остается минимальным

Граница выглядит так

```text
Launcher / future presentation adapter
    -> LauncherEngine.launch(...)
    -> LaunchResult
    -> external result handling
```

---

## Рассмотренные варианты

### Оставить `LaunchResult` полностью неиспользованным

Вариант отклонен

Такой подход сохраняет текущее поведение, но делает `LaunchResult` формальной моделью без закрепленной внешней роли

Это ослабляет подготовку к future UI boundary и делает application entrypoint менее выразительным

### Обрабатывать результат внутри `LauncherEngine`

Вариант отклонен

`LauncherEngine` является coordinator launcher lifecycle, а не presentation layer

Если поместить обработку результата внутрь `LauncherEngine`, lifecycle layer начнет зависеть от решений внешнего слоя

### Сразу добавить CLI output или exit codes

Вариант отложен

CLI output и exit codes являются presentation-level decisions

Они полезны для будущего CLI behavior, но требуют отдельного решения, чтобы не смешивать launcher lifecycle boundary с
user-facing behavior

### Сразу добавить UI presentation model

Вариант отложен

UI presentation model относится к будущему UI milestone

На момент принятия решения проекту достаточно зафиксировать ownership обработки `LaunchResult`

---

## Последствия

`LaunchResult` становится устойчивой границей между launcher lifecycle и внешним слоем

`LauncherEngine` сохраняет ответственность coordinator-а и не получает presentation concerns

Future UI boundary получает подготовленную точку интеграции

CLI, JavaFX, Electron, web UI или другой внешний adapter смогут использовать `LaunchResult`, не меняя launcher lifecycle
flow

На текущем этапе решение не требует добавления полноценного UI или CLI behavior

Появляется следующий возможный маленький шаг: явно обработать `LaunchResult` в application entrypoint без добавления
user-facing presentation logic

---

## Не входит в решение

- CLI output
- Exit codes
- UI state model
- JavaFX client
- Electron client
- Web frontend
- Shared UX system
- User-facing error messages
- Structured operation diagnostics
- Retry policy
- Recovery behavior
- Microservice architecture
- Backend split
- Product-level UX strategy

---

## Связанные решения

- [ADR-0002: Launcher lifecycle](ADR-0002-launcher-lifecycle.md)
- [ADR-0003: LauncherEngine Responsibility](ADR-0003-launcher-engine-responsibility.md)
- [ADR-0012: LauncherEngine uses OperationManager](ADR-0012-launcher-engine-uses-operation-manager.md)
- [ADR-0040: Определить границу operation failure diagnostics](ADR-0040-operation-failure-diagnostics-boundary.md)
- [ADR-0046: Определить границу результата запуска Launcher](ADR-0046-launcher-launch-result-boundary.md)
