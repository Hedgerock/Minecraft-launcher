[← Назад к списку решений](../README.md)

# ADR-0056: Определить границу завершения принятого запроса запуска

## Статус

Accepted

> Примечание: решение реализовано в итерациях
> `feat(app): add presentation launch completion contract`
> `feat(presentation): deliver launch completion outcomes`

---

## Контекст

На момент принятия решения `PresentationLaunchBoundary` принимает запрос запуска и выполняет launcher lifecycle в фоновом
контексте

Граница приема запроса и владение фоновым выполнением зафиксированы в [ADR-0052](ADR-0052-presentation-launch-request-boundary.md)

После принятия запроса presentation state переходит в `LAUNCHING` согласно [ADR-0053](ADR-0053-presentation-launch-state-boundary.md)

Когда `LauncherLifecycleRunner` возвращает `LaunchResult`, application boundary передает его обработчику, после чего
presentation layer переходит в `LAUNCHED` или `FAILED`

`LaunchResult` описывает outcome launcher lifecycle и финальное состояние `LauncherEngine` согласно [ADR-0046](ADR-0046-launcher-launch-result-boundary.md)

Создание launcher lifecycle или его выполнение может завершиться неожиданным исключением до появления `LaunchResult`

В этом случае состояние активного запроса в application boundary освобождается, но presentation layer не получает терминальный
исход и может остаться в `LAUNCHING`

Создание искусственного `LaunchResult` с придуманным финальным состоянием смешало бы сбой application boundary с результатом
`LauncherEngine`

Перед завершением presentation milestone необходимо определить отдельный исход принятого запроса, если launcher lifecycle
не смог вернуть `LaunchResult`

---

## Решение

`launcher-app` владеет завершением принятого presentation launch request

Для передачи терминального исхода вводится application-level модель `PresentationLaunchCompletion`

Модель различает два исхода

- Launcher lifecycle вернул `LaunchResult`
- Фоновое выполнение запроса завершилось неожиданным сбоем до получения `LaunchResult`

Полученный `LaunchResult` передается без изменения его смысла

Неожиданный сбой не преобразуется в `LaunchResult` и не получает искусственный `LauncherState`

Каждый принятый запрос формирует один терминальный исход и передает его через отдельную границу обработки `PresentationLaunchCompletion`

Отклоненный запрос не формирует терминальный исход фонового исполнения

`launcher-app` определяет только тип исхода и не формирует пользовательское сообщение

`launcher-ui` преобразует неожиданный сбой выполнения в безопасное общее представление ошибки и переводит presentation state
из `LAUNCHING` в `FAILED`

Исход с `LaunchResult` сохраняет существующие правила перехода в `LAUNCHED` или `FAILED` и преобразования `LaunchFailure`

Техническое сообщение неожиданного исключения не отображается пользователю напрямую

Состояние активного запроса в application boundary освобождается независимо от исхода выполнения и работы обработчика
завершения

Сбой обработчика завершения не считается повторным сбоем launcher lifecycle и не создает второй терминальный исход

Гарантия передачи терминального исхода предполагает работающую границу его доставки

Сбой доставки или обработки исхода не может быть устранен повторной отправкой через тот же обработчик без отдельной политики

Граница выглядит так

```text
presentation launch request
    -> background launcher lifecycle
        -> LaunchResult
        -> unexpected execution failure
    -> PresentationLaunchCompletion
        -> presentation adapter
            -> LAUNCHED или FAILED
```

`LauncherEngine` не получает ответственность за фоновые запросы, ошибки доставки или presentation state

Отдельная граница `PresentationLaunchCompletion` заменяет обработку одного лишь `LaunchResult` для presentation-запроса, но
не отменяет существующий `LauncherResultHandler` для других entrypoint

---

## Рассмотренные варианты

### Создавать LaunchResult при неожиданном исключении

Вариант отклонен

`LaunchResult.finalState` описывает состояние `LauncherEngine`

Если `LauncherEngine` не был создан или не вернул результат, application boundary не может достоверно указать его финальное
состояние

### Передавать исключение непосредственно в launcher-ui

Вариант отклонен

Presentation layer получил бы зависимость от деталей application assembly и исполнения launcher lifecycle

Это также создало бы риск прямого отображения технического сообщения пользователю

### Повторно отправлять исход при сбое обработчика

Вариант отложен

Повторный вызов того же обработчика может снова завершиться ошибкой или привести к дублирующей обработке

На момент принятия решения отсутствует отдельная подтвержденная политика восстановления доставки

---

## Последствия

Принятый запрос больше не остается без терминального исхода только потому, что launcher lifecycle выбросил
неожиданное исключение

`LaunchResult` сохраняет значение результата `LauncherEngine`

Application boundary получает отдельную модель завершения presentation request и границу ее доставки

Presentation layer получает явный сценарий перехода из `LAUNCHING` в `FAILED` без `LaunchResult`

Существующая безопасная обработка `LaunchFailure` для полученного `LaunchResult` сохраняется

Появляются дополнительные контракты и тесты для двух видов терминального исхода

Гарантия не означает, что application boundary может обеспечить успешную обработку исхода неисправным presentation adapter

Сбой доставки требует отдельной диагностики или политики восстановления, если такой сценарий будет подтвержден

---

## Не входит в решение

- Изменение семантики `LaunchResult`
- Искусственное финальное состояние `LauncherEngine`
- Новые состояния presentation layer
- Отображение текста неожиданного исключения пользователю
- Повторная отправка терминального исхода
- Recovery actions
- Retry policy
- Cancel behavior
- Progress presentation
- Логирование и мониторинг
- Обработка фатальных ошибок JVM
- Generic concurrent workflow framework

---

## Связанные решения

- [ADR-0046: Определить границу результата запуска Launcher](ADR-0046-launcher-launch-result-boundary.md)
- [ADR-0047: Определить границу обработки результата запуска Launcher](ADR-0047-launcher-result-handling-boundary.md)
- [ADR-0052: Определить границу запроса запуска из presentation layer](ADR-0052-presentation-launch-request-boundary.md)
- [ADR-0053: Определить границу состояния запуска в presentation layer](ADR-0053-presentation-launch-state-boundary.md)
- [ADR-0055: Определить границу отображения launch failure в presentation layer](ADR-0055-presentation-launch-failure-boundary.md)
