[← Назад к списку решений](README.md)

# ADR-0030: Определить границу проверки Java executable

## Статус

Accepted

---

## Контекст

После ADR-0029 выбор Java runtime вынесен в отдельный контракт `JavaRuntimeSelector`

`GameLaunchPlanBuilder` больше не читает `LaunchInfo.javaExecutable` напрямую при построении команды запуска

Вместо этого он получает выбранный Java executable через `JavaRuntimeSelector` и передает его в
`GameLaunchCommandBuilder`

На текущем этапе минимальная реализация `ManifestJavaRuntimeSelector` использует значение из manifest metadata

Однако выбранный Java executable пока не проверяется перед построением `GameLaunchPlan`

Если путь к Java отсутствует, недоступен или указывает не на executable file, ошибка обнаружится только при запуске
process через `GameService`

Это смешивает две разные ответственности

- Подготовку launch plan
- Фактический запуск process

Для дальнейшего развития launch runtime нужно определить, где должна находиться проверка readiness выбранного
Java executable

---

## Решение

Проверка готовности Java executable должна быть отдельной ответственностью launch/runtime слоя

Для этого должен быть введен отдельный contract проверки readiness выбранного Java executable

Этот contract должен получать уже выбранный Java executable и возвращать результат проверки или ошибку readiness

`JavaRuntimeSelector` не должен проверять filesystem readiness

Его ответственность — выбрать Java executable для текущего запуска

`GameLaunchPlanBuilder` может вызывать readiness check после выбора Java executable и до построения command

`GameLaunchCommandBuilder` не должен проверять существование Java executable

Его ответственность — построить command из уже подготовленных данных

`GameService` не должен выбирать или валидировать Java runtime

Его ответственность — запустить уже подготовленный `GameLaunchPlan`

Минимальный будущий flow

```text
LaunchInfo.javaExecutable
    -> JavaRuntimeSelector
        -> selected Java executable
            -> Java executable readiness check
                -> GameLaunchCommandBuilder
                    -> GameLaunchPlan
                        -> GameService
```

---

## Последствия

Ошибка недоступного Java executable сможет быть обнаружена до запуска process

`GameService` останется тонким adapter-ом запуска процесса

`GameLaunchCommandBuilder` сохраняет ответственность только за построение command

`JavaRuntimeSelector` не будет смешивать выбор runtime и filesystem validation

Появится отдельное место для будущей диагностики ошибок Java runtime

В будущем readiness check сможет учитывать

- Существование файла
- Тип filesystem entry
- Executable permissions
- Readable failure message
- Platform-specific правила проверки executable


---

## Не входит в решение

- Реализация readiness checker
- Поиск Java installations
- Выбор Java version
- Fallback на системную Java
- Автоматическая установка Java
- Изменение manifest JSON contract
- Изменение `GameService`
- Проверка совместимости Java version с manifest requirements
- Platform-specific executable discovery

---

## Связанные решения

- [ADR-0013: Порты оркестрации принадлежат launcher-core](ADR-0013-core-owns-orchestration-ports.md)
- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
