[← Назад к списку решений](../README.md)

# ADR-0057: Определить границу локальной диагностики неожиданных сбоев запроса запуска

## Статус

Accepted

> Примечание: решение реализовано в итерациях
> `feat(app): add presentation launch diagnostic reporter`
> `feat(app): report unexpected presentation launch failures`

---

## Контекст

На момент принятия решения `PresentationLaunchBoundary` выполняет принятый запрос запуска в фоновом контексте

Согласно [ADR-0056](ADR-0056-presentation-launch-completion-boundary.md) неожиданный сбой до получения `LaunchResult`
преобразуется в `PresentationLaunchCompletion.executionFailure()`

Такой исход позволяет presentation layer перейти из `LAUNCHING` в `FAILED` без искусственного результата `LauncherEngine`

Однако исходное исключение при преобразовании не сохраняется

Пользовательское сообщение должно оставаться безопасным, но отсутствие отдельной технической диагностики затрудняет
выяснение причины неожиданного сбоя

Синхронный вызов обработчика `PresentationLaunchCompletion` также может завершиться исключением

Этот сбой не является повторным сбоем launcher lifecycle и не должен создавать второй completion

На момент принятия решения проект не имеет общей политики структурированного логирования или хранения диагностических
данных

Необходимо определить узкую локальную границу диагностики без расширения `LaunchResult`, presentation state и общего failure
context

---

## Решение

`launcher-app` владеет диагностикой неожиданных сбоев принятого presentation launch request

Для этого вводится отдельная application-level граница передачи технической диагностики

Она различает два источника сбоя

- Выполнение launcher lifecycle завершилось исключением до получения `LaunchResult`
- Синхронный вызов обработчика `PresentationLaunchCompletion` завершился исключением

Диагностическая граница получает источник сбоя и исходную техническую причину

Техническая причина не добавляется в `PresentationLaunchCompletion`, `LaunchResult` или presentation state и не передается
в JavaFX как пользовательские данные

Локальный диагностический adapter преобразует причину в контролируемую техническую запись

Минимальная диагностическая запись содержит источник сбоя и тип исключения

Дополнительные сведения допускаются только с учетом защиты чувствительных данных

Raw exception message, credentials, tokens, URL и пользовательские пути не должны попадать в диагностический вывод без
отдельной политики их безопасной обработки

Ошибка самой диагностики не изменяет исход запроса, не препятствует освобождению состояния активного запроса и не маскирует
исходный сбой

После ошибки выполнения сохраняется передача одного `PresentationLaunchCompletion.executionFailure()`

Ошибка обработчика completion не создает второй терминальный исход и не преобразуется в `LaunchResult`

Обычный неуспешный `LaunchResult` не считается неожиданным сбоем этой границы: его failure context сохраняет существующий
путь согласно [ADR-0054](ADR-0054-generic-launch-failure-context-boundary.md)

Граница выглядит так

```text
accepted presentation launch request
    -> launcher lifecycle
        -> LaunchResult
        -> unexpected execution failure
            -> local technical diagnostics
            -> PresentationLaunchCompletion.executionFailure()
    -> completion handler
        -> synchronous handler failure
            -> local technical diagnostics
```

Диагностика не является каналом доставки пользовательского результата и не заменяет `PresentationLaunchCompletion`

---

## Рассмотренные варианты

### Добавить техническую причину в PresentationLaunchCompletion

Вариант отклонен

Completion описывает исход для presentation layer

Добавление исходного исключения создало бы риск передачи технических данных в UI и смешало бы пользовательский результат
с локальной диагностикой

### Создать LaunchResult при неожиданном сбое

Вариант отклонен согласно [ADR-0056](ADR-0056-presentation-launch-completion-boundary.md)

Если launcher lifecycle не вернул результат, application boundary не может достоверно указать финальное
состояние `LauncherEngine`

### Логировать исключение непосредственно в application boundary

Вариант отклонен

Прямая привязка к способу вывода затруднила бы проверку диагностического поведения и последующее изменение локального
adapter

### Сразу ввести общую систему логирования

Вариант отложен

Один подтвержденный сценарий неожиданного сбоя пока не определяет уровни логирования, хранение, ротацию и корреляцию всех
событий launcher lifecycle

---

## Последствия

Исходное исключение становится доступным отдельной диагностической границе, а не теряется при формировании безопасного
completion

Сбои выполнения и синхронного вызова обработчика различаются, но сохраняют существующую семантику завершения запроса

`launcher-app` получает дополнительную диагностическую границу и ее production adapter

Диагностика остается вспомогательным действием: ее собственный сбой не может нарушить доставку completion или освобождение
активного запроса

Presentation layer продолжает получать только безопасный исход и не становится владельцем технического исключения

Появляется необходимость проверять диагностические сценарии и защиту чувствительных данных отдельно от UI behavior

Локальная диагностика не гарантирует успешную запись при отказе самого diagnostic adapter

---

## Не входит в решение

- Изменение `LaunchResult` или `PresentationLaunchCompletion`
- Передача исходного исключения в `launcher-ui`
- Отображение technical diagnostics пользователю
- Повторная доставка completion при сбое обработчика
- Асинхронные ошибки внутри JavaFX callback после успешного возврата обработчика
- Диагностика отклоненного запроса, который не был принят к выполнению
- Общая logging policy для всех модулей
- Хранение и ротация файлов логов
- Корреляция всех событий launcher lifecycle
- Remote telemetry, monitoring и crash reporting
- Retry и recovery policy

---

## Связанные решения

- [ADR-0040: Определить границу operation failure diagnostics](ADR-0040-operation-failure-diagnostics-boundary.md)
- [ADR-0046: Определить границу результата запуска Launcher](ADR-0046-launcher-launch-result-boundary.md)
- [ADR-0052: Определить границу запроса запуска из presentation layer](ADR-0052-presentation-launch-request-boundary.md)
- [ADR-0054: Определить границу generic launch failure context](ADR-0054-generic-launch-failure-context-boundary.md)
- [ADR-0055: Определить границу отображения launch failure в presentation layer](ADR-0055-presentation-launch-failure-boundary.md)
- [ADR-0056: Определить границу завершения принятого запроса запуска](ADR-0056-presentation-launch-completion-boundary.md)
