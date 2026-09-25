[← Назад к списку решений](../README.md)

# ADR-0058: Определить границу передачи этапов presentation launch request

## Статус

Accepted

> Примечание: решение реализовано в итерациях
> `feat(app): map launcher states to presentation launch phases`
> `feat(app): deliver presentation launch phase updates`
> `feat(ui): display presentation launch phases`

---

## Контекст

На момент принятия решения `PresentationLaunchBoundary` выполняет принятый запрос запуска в фоновом контексте согласно
[ADR-0052](ADR-0052-presentation-launch-request-boundary.md)

`launcher-ui` отображает состояние `LAUNCHING` до получения терминального `PresentationLaunchCompletion`

Launcher lifecycle при этом последовательно проходит загрузку манифеста, проверку ресурсов, возможную загрузку файлов и
подготовку запуска игры

Пользователь видит общее сообщение о запуске, но не видит текущий этап длительного выполнения

`LauncherStateMachine` уже публикует переходы состояния через `StateChangedEvent`

Прямая подписка `launcher-ui` на внутренний `EventBus` связала бы presentation layer с orchestration events и позволила бы
сбою UI-обработчика повлиять на синхронную публикацию события

Существующий `DownloadProgressChangedEvent` не передает промежуточный потоковый прогресс загрузки

Необходимо определить отдельную границу передачи текущего этапа без изменения терминального исхода запроса и без обещания
числового progress

---

## Решение

`launcher-app` владеет application-level границей передачи этапов принятого presentation launch request

Источником этапов являются переходы launcher lifecycle, наблюдаемые для конкретного принятого запроса

`launcher-app` преобразует внутреннее состояние launcher lifecycle в ограниченную модель этапа, предназначенную для внешнего
presentation adapter

`launcher-ui` не получает прямой доступ к `EventBus`, `StateChangedEvent` или внутренней модели переходов `LauncherStateMachine`

Этапы описывают выполняющуюся часть launcher lifecycle и не заменяют `PresentationLaunchState`

`READY`, `LAUNCHED` и `FAILED` остаются presentation states согласно [ADR-0053](ADR-0053-presentation-launch-state-boundary.md)

Терминальный исход по-прежнему передается только через `PresentationLaunchCompletion` согласно [ADR-0056](ADR-0056-presentation-launch-completion-boundary.md)

Отклоненный запрос не публикует этапы выполнения

Для одного принятого запроса вызовы обработчика этапов следуют порядку наблюдаемых переходов и предшествуют вызову
обработчика терминального исхода

Отсутствие отдельного обновления этапа не препятствует передаче терминального исхода

Обработчик этапа может вызываться из фонового контекста

`launcher-ui` самостоятельно переносит обновление controls в JavaFX Application Thread и определяет пользовательский текст
этапа

Синхронный сбой обработчика этапа не изменяет launcher lifecycle и не препятствует передаче `PresentationLaunchCompletion`

Такой сбой передается через существующую локальную диагностическую границу с отдельным источником

Передача этапов не содержит технических причин ошибок, пользовательских сообщений исключений или числовых показателей загрузки

---

## Рассмотренные варианты

### Подписать launcher-ui на внутренний EventBus

Вариант отклонен

UI получил бы зависимость от внутренних событий orchestration, а синхронный сбой подписчика мог бы повлиять на выполнение
launcher lifecycle

### Использовать LauncherState как presentation state

Вариант отклонен согласно [ADR-0053](ADR-0053-presentation-launch-state-boundary.md)

Текущий этап выполнения и состояния пользовательского взаимодействия решают разные задачи

### Сразу показать процент загрузки

Вариант отложен

Текущий контракт загрузки не предоставляет промежуточный потоковый progress

Числовое отображение на его основе создало бы ложное представление о фактическом ходе загрузки

---

## Последствия

Presentation layer получает ограниченную информацию о текущем этапе принятого запроса без доступа к внутреннему `EventBus`

`PresentationLaunchState` и `PresentationLaunchCompletion` сохраняют существующий смысл

`launcher-app` получает ответственность за наблюдение переходов конкретного launcher lifecycle,
преобразование этапов и изоляцию синхронных сбоев их обработчика

Отображение этапа остается best-effort: оно не заменяет терминальный исход и не гарантирует промежуточных
обновлений внутри одного длительного этапа

Появляется отдельная модель этапов, которую необходимо поддерживать при изменении launcher lifecycle

Перенос обновлений в UI thread остается ответственностью `launcher-ui`

---

## Не входит в решение

- Потоковый progress загрузки файлов
- Проценты, оценка оставшегося времени и скорость загрузки
- Изменение внутренней execution model `LauncherEngine`
- Новые состояния `PresentationLaunchState`
- Замена или расширение `PresentationLaunchCompletion` данными этапа
- Cancel behavior и retry policy
- Отслеживание жизненного цикла запущенной игры
- Асинхронные ошибки внутри JavaFX callback после возврата обработчика этапа
- Общая система подписок на события launcher lifecycle
- Concurrent execution model

---

## Связанные решения

- [ADR-0052: Определить границу запроса запуска из presentation layer](ADR-0052-presentation-launch-request-boundary.md)
- [ADR-0053: Определить границу состояния запуска в presentation layer](ADR-0053-presentation-launch-state-boundary.md)
- [ADR-0056: Определить границу завершения принятого запроса запуска](ADR-0056-presentation-launch-completion-boundary.md)
- [ADR-0057: Определить границу локальной диагностики неожиданных сбоев запроса запуска](ADR-0057-presentation-launch-diagnostics-boundary.md)
