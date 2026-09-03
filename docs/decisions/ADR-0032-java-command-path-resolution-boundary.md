[← Назад к списку решений](README.md)

# ADR-0032: Определить границу PATH resolution для Java command name

## Статус

Accepted

> Примечание: решение частично реализовано в итерациях
> `feat(core): add java command path resolver contract`
> `feat(core): resolve java command name from path environment`
> `feat(core): use java command path resolver in launch plan builder`

---

## Контекст

После ADR-0031 launcher различает две формы Java executable reference

- command name
- explicit filesystem path

`LaunchInfo.javaExecutable` остается manifest-provided metadata и не считается filesystem path автоматически

На текущем этапе `ManifestJavaExecutableReferenceResolver` преобразует manifest-provided `javaExecutable` в
`JavaExecutableReference.commandName(...)`

`GameLaunchPlanBuilder` получает выбранный `JavaExecutableReference`, выполняет readiness check и передает reference в
`GameLaunchCommandBuilder`

Application assembly пока использует `NoOpJavaExecutableReadinessChecker`, потому что command name `java` нельзя
корректно проверить через `Files.exists(...)`

Для включения реальной filesystem readiness проверки command name должен быть сначала разрешен в explicit
filesystem path

Если добавить PATH lookup в `JavaExecutableReferenceResolver`, resolver начнет смешивать преобразование
manifest metadata и обращение к runtime environment

Если добавить PATH lookup в `GameLaunchCommandBuilder` или `GameService`, command building и process launch
начнут зависеть от environment lookup

Для дальнейшего развития Java runtime flow нужно определить отдельную границу PATH resolution

---

## Решение

PATH resolution для Java command name должен быть отдельной ответственностью launch/runtime слоя

Для этого должен быть введен отдельный контракт `JavaCommandPathResolver`

`JavaCommandPathResolver` должен принимать `JavaExecutableReference` типа command name и возвращать
`JavaExecutableReference` типа explicit filesystem path

```text
JavaExecutableReference.commandName
    -> JavaCommandPathResolver
        -> JavaExecutableReference.explicitPath
```

`JavaExecutableReferenceResolver` не должен выполнять PATH lookup

Его ответственность — преобразовать raw manifest metadata в исходный `JavaExecutableReference`

`JavaRuntimeSelector` не должен выполнять PATH lookup

Его ответственность — выбрать Java executable reference для текущего запуска

`GameLaunchPlanBuilder` может вызывать `JavaCommandPathResolver` после выбора Java executable reference и до
readiness check

`DefaultJavaExecutableReadinessChecker` продолжает работать только с explicit filesystem path

`GameLaunchCommandBuilder` не должен выполнять PATH lookup

`GameService` не должен выполнять PATH lookup

Минимальный будущий flow

```text
LaunchInfo.javaExecutable
    -> JavaExecutableReferenceResolver
        -> JavaExecutableReference.commandName
            -> JavaRuntimeSelector
                -> JavaCommandPathResolver
                    -> JavaExecutableReference.explicitPath
                        -> JavaExecutableReadinessChecker
                            -> GameLaunchCommandBuilder
                                -> GameLaunchPlan
```

На текущем этапе подключение `JavaCommandPathResolver` в application assembly должно быть отдельной кодовой итерацией

До этого `NoOpJavaExecutableReadinessChecker` может оставаться безопасной policy для command name сценария

---

## Последствия

PATH lookup получает явную границу и не смешивается с manifest mapping, runtime selection, command building или process
launch

Появляется место для platform-specific правил поиска executable

Application assembly сможет заменить no-op readiness policy на реальную проверку после
появления PATH resolution

Ошибки отсутствующего command name смогут быть обнаружены до запуска process

`JavaExecutableReferenceResolver` остается простым преобразователем manifest metadata

`GameLaunchCommandBuilder` продолжает строить command из уже подготовленных данных

В будущем `JavaCommandPathResolver` сможет учитывать

- Значение environment variable `PATH`
- Platform-specific executable extensions
- Windows `PATHEXT`
- Absolute и relative command candidates
- Readable failure для command name, который не найден

---

## Не входит в решение

- Реализация `JavaCommandPathResolver`
- Подключение `JavaCommandPathResolver` в application assembly
- Чтение environment variable `PATH`
- Поддержка Windows `PATHEXT`
- Выбор Java version
- Поиск Java installations вне `PATH`
- Fallback на системную Java без manifest metadata
- Автоматическая установка Java
- Изменение manifest JSON contract
- Изменение `GameService`
- Изменение `GameLaunchCommandBuilder`

---

## Связанные решения

- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
- [ADR-0030: Определить границу проверки Java executable](ADR-0030-java-executable-readiness-boundary.md)
- [ADR-0031: Определить границу Java executable reference](ADR-0031-java-executable-reference-boundary.md)
