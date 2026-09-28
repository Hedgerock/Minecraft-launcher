[← Назад к списку решений](../README.md)

# ADR-0061: Определить границу ошибки startup-конфигурации в presentation layer

## Статус

Accepted

> Примечание: решение реализовано в итерации `feat(ui): present startup configuration failure`

---

## Контекст

На момент принятия решения `LauncherApplication` разрешает исходную конфигурацию и создает `PresentationLaunchBoundary` до
показа JavaFX window

Если локальный `keystone.properties` отсутствует, не читается или содержит некорректный `manifest.uri`,
`LauncherConfigurationResolver` завершает разрешение конфигурации исключением

В этом случае окно не показывается, а пользователь не получает контролируемого объяснения причины недоступности запуска

[ADR-0059](ADR-0059-launcher-manifest-source-boundary.md) определяет источник manifest URI до launcher lifecycle

[ADR-0060](ADR-0060-launcher-user-directories-boundary.md) определяет расположение локальной конфигурации, но не устанавливает поведение UI при ошибке ее чтения

Состояния `READY`, `LAUNCHING`, `LAUNCHED` и `FAILED` согласно [ADR-0053](ADR-0053-presentation-launch-state-boundary.md) описывают взаимодействие с launch request

Ошибка исходной конфигурации возникает до приема такого запроса

Необходимо определить, как presentation layer отображает этот исход, не изменяя смысл `LaunchResult`,
`PresentationLaunchCompletion` и состояния `FAILED`

---

## Решение

`launcher-app` сохраняет ответственность за разрешение исходной конфигурации в `LauncherConfiguration`

`launcher-ui` владеет минимальной моделью доступности запуска после попытки подготовки presentation boundary

Модель различает два исхода startup

- Конфигурация разрешена, и presentation launch boundary доступна
- Локальная конфигурация manifest URI не может быть использована

При успешной подготовке сохраняется существующий presentation launch flow

При ошибке локальной конфигурации JavaFX window показывается, но launch action недоступен

Presentation layer отображает контролируемое пользовательское сообщение без исходного текста исключения, URI и
локальных путей

Launch request в этом случае не отправляется, `LauncherEngine` не запускается и терминальный `PresentationLaunchCompletion`
не создается

Startup-модель не заменяет `PresentationLaunchStateMachine`

После успешной подготовки доступность launch action по-прежнему определяется состоянием принятого запроса согласно
[ADR-0053](ADR-0053-presentation-launch-state-boundary.md)

До успешной подготовки startup boundary состояние `READY` само по себе не означает, что launch action доступен

Ошибка startup-конфигурации не преобразуется в `PresentationLaunchState.FAILED` или `PresentationLaunchFailure`, поскольку эти
модели описывают неуспешное завершение launch request

Явные аргументы запуска сохраняют существующий приоритет перед локальным источником manifest URI

Решение охватывает ожидаемые ошибки чтения и проверки локального manifest URI

Неожиданные сбои других частей startup не перехватываются как ошибка локальной конфигурации

---

## Рассмотренные варианты

### Использовать PresentationLaunchState.FAILED

Вариант отклонен

`FAILED` означает неуспешное завершение launch request

При ошибке исходной конфигурации запрос еще не был принят

### Не показывать окно при ошибке конфигурации

Вариант отклонен для ожидаемой ошибки локальной конфигурации

Пользователь не получает контролируемого объяснения, почему действие запуска недоступно

### Сразу добавить восстановление конфигурации через UI

Вариант отложен

На момент принятия решения отсутствует отдельный сценарий редактирования конфигурации, повторной подготовки boundary или
выбора источника manifest URI в UI

---

## Последствия

Presentation layer получает отдельное представление исхода startup до приема launch request

Окно может быть показано при ожидаемой ошибке локальной конфигурации без создания искусственного `LaunchResult` или
`PresentationLaunchCompletion`

Правило доступности launch action учитывает как успешную подготовку startup boundary, так и существующее состояние launch
request

Это уточняет область применения [ADR-0053](ADR-0053-presentation-launch-state-boundary.md), не меняя исторического содержания решения

Появляются отдельные тесты успешной подготовки и недоступности запуска при ошибке конфигурации

При отсутствии recovery action пользователь не может исправить конфигурацию внутри открытого окна и должен подготовить ее
вне UI перед следующим запуском приложения

Минимальная startup-модель добавляет отдельное состояние presentation layer, которое потребуется учитывать при будущем
развитии UI

---

## Не входит в решение

- Автоматическое создание `keystone.properties`
- Редактирование конфигурации в UI
- Повторная подготовка presentation boundary без перезапуска приложения
- Новые источники manifest URI
- Изменение поведения CLI entrypoint
- Преобразование любых неожиданных startup exceptions в ошибку локальной конфигурации
- Общая система startup diagnostics
- Изменение `LaunchResult` или `PresentationLaunchCompletion`
- Recovery actions, retry policy и локализация

---

## Связанные решения

- [ADR-0053: Определить границу состояния запуска в presentation layer](ADR-0053-presentation-launch-state-boundary.md)
- [ADR-0055: Определить границу отображения launch failure в presentation layer](ADR-0055-presentation-launch-failure-boundary.md)
- [ADR-0056: Определить границу завершения принятого запроса запуска](ADR-0056-presentation-launch-completion-boundary.md)
- [ADR-0059: Определить границу источника manifest URI для запуска из UI](ADR-0059-launcher-manifest-source-boundary.md)
- [ADR-0060: Определить пользовательские каталоги конфигурации и данных launcher](ADR-0060-launcher-user-directories-boundary.md)
